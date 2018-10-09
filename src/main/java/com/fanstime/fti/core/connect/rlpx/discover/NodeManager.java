package com.fanstime.fti.core.connect.rlpx.discover;

import com.fanstime.fti.config.SystemProperties;
import com.fanstime.fti.crypto.ECKey;
import com.fanstime.fti.db.PeerSource;
import com.fanstime.fti.listener.FtiListener;
import com.fanstime.fti.core.connect.rlpx.Node;
import com.fanstime.fti.util.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Predicate;

/**
 * The central class for Peer Discovery machinery.
 * <p>
 * The NodeManager manages info on all the Nodes discovered by the peer discovery
 * protocol, routes protocol messages to the corresponding NodeHandlers and
 * supplies the info about discovered Nodes and their usage statistics
 * <p>
 * Created by Jay Nicolas on 16.07.2018.
 */
@Component
public class NodeManager {
    static final org.slf4j.Logger logger = LoggerFactory.getLogger("discover");

    private final boolean PERSIST;

    static final int MAX_NODES = 2000;
    static final int NODES_TRIM_THRESHOLD = 3000;

    PeerSource peerSource;
    FtiListener ftiListener;
    SystemProperties config = SystemProperties.getDefault();

    private Map<String, NodeHandler> nodeHandlerMap = new HashMap<>();
    final ECKey key;
    final Node homeNode;

    private Timer logStatsTimer = new Timer();
    private Timer nodeManagerTasksTimer = new Timer("NodeManagerTasks");
    ;
    private ScheduledExecutorService pongTimer;

    @Autowired
    public NodeManager(SystemProperties config, FtiListener ftiListener,
                       ApplicationContext ctx) {
        this.config = config;
        this.ftiListener = ftiListener;

        PERSIST = config.peerDiscoveryPersist();
        if (PERSIST) peerSource = ctx.getBean(PeerSource.class);

        key = config.getMyKey();
        homeNode = new Node(config.nodeId(), config.externalIp(), config.listenPort());

        logStatsTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                logger.trace("Statistics:\n {}", dumpAllStatistics());
            }
        }, 1 * 1000, 60 * 1000);

        this.pongTimer = Executors.newSingleThreadScheduledExecutor();
        for (Node node : config.peerActive()) {
            getNodeHandler(node).getNodeStatistics().setPredefined(true);
        }
    }

    private void dbWrite() {
        List<Pair<Node, Integer>> batch = new ArrayList<>();
        synchronized (this) {
            for (NodeHandler handler : nodeHandlerMap.values()) {
                batch.add(Pair.of(handler.getNode(), handler.getNodeStatistics().getPersistedReputation()));
            }
        }
        peerSource.clear();
        for (Pair<Node, Integer> nodeElement : batch) {
            peerSource.getNodes().add(nodeElement);
        }
        peerSource.getNodes().flush();
        logger.info("Write Node statistics to DB: " + peerSource.getNodes().size() + " nodes.");
    }

    private String getKey(Node n) {
        return getKey(new InetSocketAddress(n.getHost(), n.getPort()));
    }

    private String getKey(InetSocketAddress address) {
        InetAddress addr = address.getAddress();
        // addr == null if the hostname can't be resolved
        return (addr == null ? address.getHostString() : addr.getHostAddress()) + ":" + address.getPort();
    }

    public synchronized NodeHandler getNodeHandler(Node n) {
        String key = getKey(n);
        NodeHandler ret = nodeHandlerMap.get(key);
        if (ret == null) {
            trimTable();
            ret = new NodeHandler(n);
            nodeHandlerMap.put(key, ret);
        } else if (ret.getNode().isDiscoveryNode() && !n.isDiscoveryNode()) {
            // we found discovery node with same host:port,
            // replace node with correct nodeId
            ret.node = n;
            logger.debug(" +++ Found real nodeId for discovery endpoint {}", n);
        }

        return ret;
    }

    private void trimTable() {
        if (nodeHandlerMap.size() > NODES_TRIM_THRESHOLD) {

            List<NodeHandler> sorted = new ArrayList<>(nodeHandlerMap.values());
            // reverse sort by reputation
            sorted.sort((o1, o2) -> o1.getNodeStatistics().getReputation() - o2.getNodeStatistics().getReputation());

            for (NodeHandler handler : sorted) {
                nodeHandlerMap.remove(getKey(handler.getNode()));
                if (nodeHandlerMap.size() <= MAX_NODES) break;
            }
        }
    }


    public NodeStatistics getNodeStatistics(Node n) {
        return getNodeHandler(n).getNodeStatistics();
    }

    /**
     * Checks whether peers with such InetSocketAddress has penalize disconnect record
     *
     * @param addr Peer address
     * @return true if penalized, false if not or no records
     */
    public boolean isReputationPenalized(InetSocketAddress addr) {
        return getNodeStatistics(new Node(new byte[0], addr.getHostString(),
                addr.getPort())).isReputationPenalized();
    }

    public synchronized List<NodeHandler> getNodes(int minReputation) {
        List<NodeHandler> ret = new ArrayList<>();
        for (NodeHandler nodeHandler : nodeHandlerMap.values()) {
            if (nodeHandler.getNodeStatistics().getReputation() >= minReputation) {
                ret.add(nodeHandler);
            }
        }
        return ret;
    }

    /**
     * Returns limited list of nodes matching {@code predicate} criteria<br>
     * The nodes are sorted then by their totalDifficulties
     *
     * @param predicate only those nodes which are satisfied to its condition are included in results
     * @param limit     max size of returning list
     * @return list of nodes matching criteria
     */
    public List<NodeHandler> getNodes(
            Predicate<NodeHandler> predicate,
            int limit) {
        ArrayList<NodeHandler> filtered = new ArrayList<>();
        synchronized (this) {
            for (NodeHandler handler : nodeHandlerMap.values()) {
                if (predicate.test(handler)) {
                    filtered.add(handler);
                }
            }
        }
        filtered.sort((o1, o2) -> o2.getNodeStatistics().getEthTotalDifficulty().compareTo(
                o1.getNodeStatistics().getEthTotalDifficulty()));
        return CollectionUtils.truncate(filtered, limit);
    }


    public synchronized String dumpAllStatistics() {
        List<NodeHandler> l = new ArrayList<>(nodeHandlerMap.values());
        l.sort((o1, o2) -> -(o1.getNodeStatistics().getReputation() - o2.getNodeStatistics().getReputation()));

        StringBuilder sb = new StringBuilder();
        int zeroReputCount = 0;
        for (NodeHandler nodeHandler : l) {
            if (nodeHandler.getNodeStatistics().getReputation() > 0) {
                sb.append(nodeHandler).append("\t").append(nodeHandler.getNodeStatistics()).append("\n");
            } else {
                zeroReputCount++;
            }
        }
        sb.append("0 reputation: ").append(zeroReputCount).append(" nodes.\n");
        return sb.toString();
    }


    public void close() {
        try {
            nodeManagerTasksTimer.cancel();
            if (PERSIST) {
                try {
                    dbWrite();
                } catch (Throwable e) {     // IllegalAccessError is expected
                    // NOTE: logback stops context right after shutdown initiated. It is problematic to see log output
                    // System out could help
                    logger.warn("Problem during NodeManager persist in close: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.warn("Problems canceling nodeManagerTasksTimer", e);
        }
        try {
            logger.info("Cancelling pongTimer");
            pongTimer.shutdownNow();
        } catch (Exception e) {
            logger.warn("Problems cancelling pongTimer", e);
        }
        try {
            logStatsTimer.cancel();
        } catch (Exception e) {
            logger.warn("Problems canceling logStatsTimer", e);
        }
    }


}
