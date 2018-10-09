package com.fanstime.fti.core.connect.rlpx.discover;

import com.fanstime.fti.core.connect.rlpx.*;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

/**
 * The instance of this class responsible for discovery messages exchange with the specified Node
 * It also manages itself regarding inclusion/eviction from Kademlia table
 *
 * Created by Tony Hunt on 14.07.2018.
 */
public class NodeHandler {
    static final org.slf4j.Logger logger = LoggerFactory.getLogger("discover");


    Node node;
    private NodeStatistics nodeStatistics;

    public NodeHandler(Node node) {
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    public NodeStatistics getNodeStatistics() {
        if (nodeStatistics == null) {
            nodeStatistics = new NodeStatistics(node);
        }
        return nodeStatistics;
    }



    @Override
    public String toString() {
        return "NodeHandler[state: "  + ", node: " + node.getHost() + ":" + node.getPort() + ", id="
                + (node.getId().length > 0 ? Hex.toHexString(node.getId(), 0, 4) : "empty") + "]";
    }


}
