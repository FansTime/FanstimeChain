package com.fanstime.fti.validator;

import com.fanstime.fti.core.BlockHeader;
import com.fanstime.fti.util.ByteUtil;
import com.fanstime.fti.util.FastByteComparisons;
import org.spongycastle.util.encoders.Hex;

/**
 * Created by Bynum Williams on 26.12.16.
 */
public class ExtraDataPresenceRule extends BlockHeaderRule {

    public final byte[] data;

    public final boolean required;

    public ExtraDataPresenceRule(byte[] data, boolean required) {
        this.data = data;
        this.required = required;
    }

    @Override
    public ValidationResult validate(BlockHeader header) {
        final byte[] extraData = header.getExtraData() != null ? header.getExtraData() : ByteUtil.EMPTY_BYTE_ARRAY;
        final boolean extraDataMatches = FastByteComparisons.equal(extraData, data);

        if (required && !extraDataMatches) {
            return fault("Block " + header.getNumber() + " is no-fork. Expected presence of: " +
                    Hex.toHexString(data) + ", in extra data: " + Hex.toHexString(extraData));
        } else if (!required && extraDataMatches) {
            return fault("Block " + header.getNumber() + " is pro-fork. Expected no: " +
                    Hex.toHexString(data) + ", in extra data: " + Hex.toHexString(extraData));
        }
        return Success;
    }
}
