package com.fanstime.fti.core;

import com.fanstime.fti.util.RLP;
import com.fanstime.fti.util.RLPList;

import java.math.BigInteger;

import static com.fanstime.fti.util.ByteUtil.byteArrayToLong;
import static com.fanstime.fti.util.ByteUtil.toHexString;


/**
 * Block identifier holds block hash and number <br>
 * This tuple is used in some places of the core,
 * like by {@link FtiMessageCodes#NEW_BLOCK_HASHES} message wrapper
 *
 */
public class BlockIdentifier {

    /**
     * Block hash
     */
    private byte[] hash;

    /**
     * Block number
     */
    private long number;

    public BlockIdentifier(RLPList rlp) {
        this.hash = rlp.get(0).getRLPData();
        this.number = byteArrayToLong(rlp.get(1).getRLPData());
    }

    public BlockIdentifier(byte[] hash, long number) {
        this.hash = hash;
        this.number = number;
    }

    public byte[] getHash() {
        return hash;
    }

    public long getNumber() {
        return number;
    }

    public byte[] getEncoded() {
        byte[] hash = RLP.encodeElement(this.hash);
        byte[] number = RLP.encodeBigInteger(BigInteger.valueOf(this.number));

        return RLP.encodeList(hash, number);
    }

    @Override
    public String toString() {
        return "BlockIdentifier {" +
                "hash=" + toHexString(hash) +
                ", number=" + number +
                '}';
    }
}
