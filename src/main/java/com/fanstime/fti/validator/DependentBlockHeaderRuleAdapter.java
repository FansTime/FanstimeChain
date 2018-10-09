package com.fanstime.fti.validator;

import com.fanstime.fti.core.BlockHeader;

public class DependentBlockHeaderRuleAdapter extends DependentBlockHeaderRule {

    @Override
    public boolean validate(BlockHeader header, BlockHeader dependency) {
        return true;
    }
}
