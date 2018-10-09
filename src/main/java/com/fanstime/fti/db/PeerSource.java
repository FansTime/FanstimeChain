package com.fanstime.fti.db;

import org.apache.commons.lang3.tuple.Pair;
import com.fanstime.fti.datasource.DataSourceArray;
import com.fanstime.fti.datasource.DbSource;
import com.fanstime.fti.datasource.ObjectDataSource;
import com.fanstime.fti.datasource.Serializer;
import com.fanstime.fti.datasource.Source;
import com.fanstime.fti.core.connect.rlpx.Node;
import com.fanstime.fti.util.ByteUtil;
import com.fanstime.fti.util.RLP;
import com.fanstime.fti.util.RLPList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigInteger;

/**
 * Source for {@link com.fanstime.fti.core.connect.rlpx.Node} also known as Peers
 */
public class PeerSource {
    private static final Logger logger = LoggerFactory.getLogger("db");

    // for debug purposes
    public static PeerSource INST;

    private Source<byte[], byte[]> src;

    DataSourceArray<Pair<Node, Integer>> nodes;

    public static final Serializer<Pair<Node, Integer>, byte[]> NODE_SERIALIZER = new Serializer<Pair<Node, Integer>, byte[]>(){

        @Override
        public byte[] serialize(Pair<Node, Integer> value) {
            byte[] nodeRlp = value.getLeft().getRLP();
            byte[] nodeIsDiscovery = RLP.encodeByte(value.getLeft().isDiscoveryNode() ? (byte) 1 : 0);
            byte[] savedReputation = RLP.encodeBigInteger(BigInteger.valueOf(value.getRight()));

            return RLP.encodeList(nodeRlp, nodeIsDiscovery, savedReputation);
        }

        @Override
        public Pair<Node, Integer> deserialize(byte[] bytes) {
            if (bytes == null) return null;

            RLPList nodeElement = (RLPList) RLP.decode2(bytes).get(0);
            byte[] nodeRlp = nodeElement.get(0).getRLPData();
            byte[] nodeIsDiscovery = nodeElement.get(1).getRLPData();
            byte[] savedReputation = nodeElement.get(2).getRLPData();
            Node node = new Node(nodeRlp);
            node.setDiscoveryNode(nodeIsDiscovery != null);

            return Pair.of(node, ByteUtil.byteArrayToInt(savedReputation));
        }
    };

    public PeerSource(Source<byte[], byte[]> src) {
        this.src = src;
        INST = this;
        this.nodes = new DataSourceArray<>(
                new ObjectDataSource<>(src, NODE_SERIALIZER, 512));
    }

    public DataSourceArray<Pair<Node, Integer>> getNodes() {
        return nodes;
    }

    public void clear() {
        if (src instanceof DbSource) {
            ((DbSource) src).reset();
            this.nodes = new DataSourceArray<>(
                    new ObjectDataSource<>(src, NODE_SERIALIZER, 512));
        } else {
            throw new RuntimeException("Not supported");
        }
    }
}
