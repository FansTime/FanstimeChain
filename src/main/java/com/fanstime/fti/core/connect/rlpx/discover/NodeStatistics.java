package com.fanstime.fti.core.connect.rlpx.discover;

import com.fanstime.fti.core.connect.message.ReasonCode;
import com.fanstime.fti.core.connect.rlpx.Node;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.fanstime.fti.core.connect.server.ChannelManager.INBOUND_CONNECTION_BAN_TIMEOUT;
import static java.lang.Math.min;

/**
 * Handles all possible statistics related to a Node
 * The primary aim of this is collecting info about a Node
 * for maintaining its reputation.
 * <p>
 * Created by Tony Hunt on 16.07.2018.
 */
public class NodeStatistics {
    public final static int REPUTATION_PREDEFINED = 1000500;

    public class StatHandler {
        AtomicLong count = new AtomicLong(0);

        public void add() {
            count.incrementAndGet();
        }

        public void add(long delta) {
            count.addAndGet(delta);
        }

        public long get() {
            return count.get();
        }

        public String toString() {
            return count.toString();
        }
    }

    private final Node node;

    private boolean isPredefined = false;

    private int persistedReputation = 0;

    // discovery stat
    public final StatHandler discoverOutPing = new StatHandler();
    public final StatHandler discoverInPong = new StatHandler();
    public final StatHandler discoverOutPong = new StatHandler();
    public final StatHandler discoverInPing = new StatHandler();
    public final StatHandler discoverInFind = new StatHandler();
    public final StatHandler discoverOutFind = new StatHandler();
    public final StatHandler discoverInNeighbours = new StatHandler();
    public final StatHandler discoverOutNeighbours = new StatHandler();

    // rlpx stat
    public final StatHandler rlpxConnectionAttempts = new StatHandler();
    public final StatHandler rlpxAuthMessagesSent = new StatHandler();
    public final StatHandler rlpxHandshake = new StatHandler();
    public final StatHandler rlpxOutMessages = new StatHandler();
    public final StatHandler rlpxInMessages = new StatHandler();
    // Not the fork we are working on
    // Set only after specific block hashes received
    public boolean wrongFork;

    private String clientId = "";

    private ReasonCode rlpxLastRemoteDisconnectReason = null;
    private ReasonCode rlpxLastLocalDisconnectReason = null;
    private long lastDisconnectedTime = 0;

    // Fti stat
    public final StatHandler ethHandshake = new StatHandler();
    public final StatHandler ethInbound = new StatHandler();
    public final StatHandler ethOutbound = new StatHandler();
    private BigInteger ethTotalDifficulty = BigInteger.ZERO;

    public NodeStatistics(Node node) {
        this.node = node;
    }

    private int getSessionReputation() {
        return getSessionFairReputation() + (isPredefined ? REPUTATION_PREDEFINED : 0);
    }

    private int getSessionFairReputation() {
        int discoverReput = 0;

        discoverReput += min(discoverInPong.get(), 10) * (discoverOutPing.get() == discoverInPong.get() ? 2 : 1);
        discoverReput += min(discoverInNeighbours.get(), 10) * 2;
//        discoverReput += 20 / (min((int)discoverMessageLatency.getAvrg(), 1) / 100);

        int rlpxReput = 0;
        rlpxReput += rlpxAuthMessagesSent.get() > 0 ? 10 : 0;
        rlpxReput += rlpxHandshake.get() > 0 ? 20 : 0;
        rlpxReput += min(rlpxInMessages.get(), 10) * 3;

        if (wasDisconnected()) {
            if (rlpxLastLocalDisconnectReason == null && rlpxLastRemoteDisconnectReason == null) {
                // means connection was dropped without reporting any reason - bad
                rlpxReput *= 0.3;
            } else if (rlpxLastLocalDisconnectReason != ReasonCode.REQUESTED) {
                // the disconnect was not initiated by discover mode
                if (rlpxLastRemoteDisconnectReason == ReasonCode.TOO_MANY_PEERS) {
                    // The peer is popular, but we were unlucky
                    rlpxReput *= 0.3;
                } else if (rlpxLastRemoteDisconnectReason != ReasonCode.REQUESTED) {
                    // other disconnect reasons
                    rlpxReput *= 0.2;
                }
            }
        }

        return discoverReput + 100 * rlpxReput;
    }

    public int getReputation() {
        return isReputationPenalized() ? 0 : persistedReputation / 2 + getSessionReputation();
    }

    public boolean isReputationPenalized() {
        if (wrongFork) return true;
        if (wasDisconnected() && rlpxLastRemoteDisconnectReason == ReasonCode.TOO_MANY_PEERS &&
                System.currentTimeMillis() - lastDisconnectedTime < INBOUND_CONNECTION_BAN_TIMEOUT) {
            return true;
        }
        if (wasDisconnected() && rlpxLastRemoteDisconnectReason == ReasonCode.DUPLICATE_PEER &&
                System.currentTimeMillis() - lastDisconnectedTime < INBOUND_CONNECTION_BAN_TIMEOUT) {
            return true;
        }
        return rlpxLastLocalDisconnectReason == ReasonCode.NULL_IDENTITY ||
                rlpxLastRemoteDisconnectReason == ReasonCode.NULL_IDENTITY ||
                rlpxLastLocalDisconnectReason == ReasonCode.INCOMPATIBLE_PROTOCOL ||
                rlpxLastRemoteDisconnectReason == ReasonCode.INCOMPATIBLE_PROTOCOL ||
                rlpxLastLocalDisconnectReason == ReasonCode.USELESS_PEER ||
                rlpxLastRemoteDisconnectReason == ReasonCode.USELESS_PEER ||
                rlpxLastLocalDisconnectReason == ReasonCode.BAD_PROTOCOL ||
                rlpxLastRemoteDisconnectReason == ReasonCode.BAD_PROTOCOL;
    }

    public void nodeDisconnectedLocal(ReasonCode reason) {
        lastDisconnectedTime = System.currentTimeMillis();
        rlpxLastLocalDisconnectReason = reason;
    }

    public boolean wasDisconnected() {
        return lastDisconnectedTime > 0;
    }


    public BigInteger getEthTotalDifficulty() {
        return ethTotalDifficulty;
    }

    public String getClientId() {
        return clientId;
    }

    public void setPredefined(boolean isPredefined) {
        this.isPredefined = isPredefined;
    }

    public boolean isPredefined() {
        return isPredefined;
    }


    public int getPersistedReputation() {
        return isReputationPenalized() ? 0 : (persistedReputation + getSessionFairReputation()) / 2;
    }

    @Override
    public String toString() {
        return "NodeStat[reput: " + getReputation() + "(" + persistedReputation + "), discover: " +
                discoverInPong + "/" + discoverOutPing + " " +
                discoverOutPong + "/" + discoverInPing + " " +
                discoverInNeighbours + "/" + discoverOutFind + " " +
                discoverOutNeighbours + "/" + discoverInFind + " " +
                ", rlpx: " + rlpxHandshake + "/" + rlpxAuthMessagesSent + "/" + rlpxConnectionAttempts + " " +
                rlpxInMessages + "/" + rlpxOutMessages +
                ", fti: " + ethHandshake + "/" + ethInbound + "/" + ethOutbound + " " +
                (wasDisconnected() ? "X " : "") +
                (rlpxLastLocalDisconnectReason != null ? ("<=" + rlpxLastLocalDisconnectReason) : " ") +
                (rlpxLastRemoteDisconnectReason != null ? ("=>" + rlpxLastRemoteDisconnectReason) : " ") +
                "[" + clientId + "]";
    }


}
