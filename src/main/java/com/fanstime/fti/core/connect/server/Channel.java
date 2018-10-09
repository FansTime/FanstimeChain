package com.fanstime.fti.core.connect.server;

import com.fanstime.fti.config.SystemProperties;
import com.fanstime.fti.core.Block;
import com.fanstime.fti.core.Transaction;
import com.fanstime.fti.db.ByteArrayWrapper;
import com.fanstime.fti.core.connect.MessageQueue;
import com.fanstime.fti.core.connect.handler.Fti;
import com.fanstime.fti.core.connect.handler.FtiAdapter;
import com.fanstime.fti.core.connect.message.ReasonCode;
import com.fanstime.fti.core.connect.rlpx.Node;
import com.fanstime.fti.core.connect.rlpx.discover.NodeStatistics;
import com.fanstime.fti.util.CollectionUtils;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Scope("prototype")
public class Channel {

    private final static Logger logger = LoggerFactory.getLogger("net");

    @Autowired
    SystemProperties config;

    @Autowired
    private MessageQueue msgQueue;

    @Autowired
    private WireTrafficStats stats;

    private ChannelManager channelManager;

    private Fti fti = new FtiAdapter();

    private InetSocketAddress inetSocketAddress;

    private Node node;
    private NodeStatistics nodeStatistics;

    private boolean discoveryMode;
    private boolean isActive;
    private boolean isDisconnected;

    private String remoteId;

    public static final int MAX_SAFE_TXS = 192;

    public void init(ChannelPipeline pipeline, String remoteId, boolean discoveryMode, ChannelManager channelManager) {
        this.channelManager = channelManager;
        this.remoteId = remoteId;

        isActive = remoteId != null && !remoteId.isEmpty();

        pipeline.addLast("readTimeoutHandler",
                new ReadTimeoutHandler(config.peerChannelReadTimeout(), TimeUnit.SECONDS));
        pipeline.addLast(stats.tcp);

        this.discoveryMode = discoveryMode;

        if (discoveryMode) {
            // temporary key/nodeId to not accidentally smear our reputation with
            // unexpected disconnect
//            handshakeHandler.generateTempKey();
        }

        msgQueue.setChannel(this);


    }


    public void setInetSocketAddress(InetSocketAddress inetSocketAddress) {
        this.inetSocketAddress = inetSocketAddress;
    }

    public NodeStatistics getNodeStatistics() {
        return nodeStatistics;
    }


    public Node getNode() {
        return node;
    }


    public boolean isProtocolsInitialized() {
        return fti.hasStatusPassed();
    }

    public void onDisconnect() {
        isDisconnected = true;
    }

    public boolean isDisconnected() {
        return isDisconnected;
    }

    public void onSyncDone(boolean done) {

        if (done) {
            fti.enableTransactions();
        } else {
            fti.disableTransactions();
        }

        fti.onSyncDone(done);
    }

    public boolean isDiscoveryMode() {
        return discoveryMode;
    }

    public String getPeerId() {
        return node == null ? "<null>" : node.getHexId();
    }

    public String getPeerIdShort() {
        return node == null ? (remoteId != null && remoteId.length() >= 8 ? remoteId.substring(0, 8) : remoteId)
                : node.getHexIdShort();
    }

    public byte[] getNodeId() {
        return node == null ? null : node.getId();
    }

    /**
     * Indicates whether this connection was initiated by our peer
     */
    public boolean isActive() {
        return isActive;
    }

    public ByteArrayWrapper getNodeIdWrapper() {
        return node == null ? null : new ByteArrayWrapper(node.getId());
    }

    public void disconnect(ReasonCode reason) {
        getNodeStatistics().nodeDisconnectedLocal(reason);
        msgQueue.disconnect(reason);
    }

    public InetSocketAddress getInetSocketAddress() {
        return inetSocketAddress;
    }


    // ETH sub protocol

    public Fti getFtiHandler() {
        return fti;
    }

    public boolean hasEthStatusSucceeded() {
        return fti.hasStatusSucceeded();
    }

    public String logSyncStats() {
        return fti.getSyncStats();
    }

    public BigInteger getTotalDifficulty() {
        return getFtiHandler().getTotalDifficulty();
    }


    public boolean isMaster() {
        return fti.isHashRetrieving() || fti.isHashRetrievingDone();
    }

    public boolean isIdle() {
        return fti.isIdle();
    }


    /**
     * Sames as {@link #sendTransactions(List)} but input list is randomly sliced to
     * contain not more than {@link #MAX_SAFE_TXS} if needed
     *
     * @param txs List of txs to send
     */
    public void sendTransactionsCapped(List<Transaction> txs) {
        List<Transaction> slicedTxs;
        if (txs.size() <= MAX_SAFE_TXS) {
            slicedTxs = txs;
        } else {
            slicedTxs = CollectionUtils.truncateRand(txs, MAX_SAFE_TXS);
        }
        fti.sendTransaction(slicedTxs);
    }

    public void sendNewBlock(Block block) {
        fti.sendNewBlock(block);
    }

    public void sendNewBlockHashes(Block block) {
        fti.sendNewBlockHashes(block);
    }

    public void dropConnection() {
        fti.dropConnection();
    }

    public ChannelManager getChannelManager() {
        return channelManager;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Channel channel = (Channel) o;


        if (inetSocketAddress != null ? !inetSocketAddress.equals(channel.inetSocketAddress) : channel.inetSocketAddress != null)
            return false;
        if (node != null ? !node.equals(channel.node) : channel.node != null) return false;
        return false;
    }

    @Override
    public int hashCode() {
        int result = inetSocketAddress != null ? inetSocketAddress.hashCode() : 0;
        result = 31 * result + (node != null ? node.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s | %s", getPeerIdShort(), inetSocketAddress);
    }
}
