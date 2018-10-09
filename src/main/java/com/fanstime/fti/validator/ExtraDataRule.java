package com.fanstime.fti.validator;

import com.fanstime.fti.config.Constants;
import com.fanstime.fti.config.SystemProperties;
import com.fanstime.fti.core.BlockHeader;

/**
 * Checks {@link BlockHeader#extraData} size against {@link Constants#getMAXIMUM_EXTRA_DATA_SIZE}
 *
 */
public class ExtraDataRule extends BlockHeaderRule {

    private final int MAXIMUM_EXTRA_DATA_SIZE;

    public ExtraDataRule(SystemProperties config) {
        MAXIMUM_EXTRA_DATA_SIZE = config.getBlockchainConfig().
                getCommonConstants().getMAXIMUM_EXTRA_DATA_SIZE();
    }

    @Override
    public ValidationResult validate(BlockHeader header) {
        if (header.getExtraData() != null && header.getExtraData().length > MAXIMUM_EXTRA_DATA_SIZE) {
            return fault(String.format(
                    "#%d: header.getExtraData().length > MAXIMUM_EXTRA_DATA_SIZE",
                    header.getNumber()
            ));
        }

        return Success;
    }
}
