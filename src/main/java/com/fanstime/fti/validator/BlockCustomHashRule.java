package com.fanstime.fti.validator;

import com.fanstime.fti.core.BlockHeader;
import com.fanstime.fti.util.FastByteComparisons;
import org.spongycastle.util.encoders.Hex;

/**
 * Created by Bynum Williams on 26.12.16.
 */
public class BlockCustomHashRule extends BlockHeaderRule {

    public final byte[] blockHash;

    public BlockCustomHashRule(byte[] blockHash) {
        this.blockHash = blockHash;
    }

    @Override
    public ValidationResult validate(BlockHeader header) {
        if (!FastByteComparisons.equal(header.getHash(), blockHash)) {
            return fault("Block " + header.getNumber() + " hash constraint violated. Expected:" +
                    Hex.toHexString(blockHash) + ", got: " + Hex.toHexString(header.getHash()));
        }
        return Success;
    }
}
