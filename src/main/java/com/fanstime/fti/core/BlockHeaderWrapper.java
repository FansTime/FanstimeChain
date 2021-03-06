package com.fanstime.fti.core;

import com.fanstime.fti.util.RLP;
import com.fanstime.fti.util.RLPElement;
import com.fanstime.fti.util.RLPList;
import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;
import java.util.List;
import static com.fanstime.fti.util.ByteUtil.toHexString;

/**
 * <p>Wraps {@link BlockHeader}</p>
 * Adds some additional data
 *
 */
public class BlockHeaderWrapper {

    private BlockHeader header;
    private byte[] nodeId;

    public BlockHeaderWrapper(BlockHeader header, byte[] nodeId) {
        this.header = header;
        this.nodeId = nodeId;
    }

    public BlockHeaderWrapper(byte[] bytes) {
        parse(bytes);
    }

    public byte[] getBytes() {
        byte[] headerBytes = header.getEncoded();
        byte[] nodeIdBytes = RLP.encodeElement(nodeId);
        return RLP.encodeList(headerBytes, nodeIdBytes);
    }

    private void parse(byte[] bytes) {
        List<RLPElement> params = RLP.decode2(bytes);
        List<RLPElement> wrapper = (RLPList) params.get(0);

        byte[] headerBytes = wrapper.get(0).getRLPData();

        this.header= new BlockHeader(headerBytes);
        this.nodeId = wrapper.get(1).getRLPData();
    }

    public byte[] getNodeId() {
        return nodeId;
    }

    public byte[] getHash() {
        return header.getHash();
    }

    public long getNumber() {
        return header.getNumber();
    }

    public BlockHeader getHeader() {
        return header;
    }

    public String getHexStrShort() {
        return Hex.toHexString(header.getHash()).substring(0, 6);
    }

    public boolean sentBy(byte[] nodeId) {
        return Arrays.equals(this.nodeId, nodeId);
    }

    @Override
    public String toString() {
        return "BlockHeaderWrapper {" +
                "header=" + header +
                ", nodeId=" + toHexString(nodeId) +
                '}';
    }
}
