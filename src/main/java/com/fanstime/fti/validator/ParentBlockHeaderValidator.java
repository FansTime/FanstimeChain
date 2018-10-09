package com.fanstime.fti.validator;

import com.fanstime.fti.core.BlockHeader;

import java.util.List;

/**
 * Composite {@link BlockHeader} validator
 * aggregating list of simple validation rules depending on parent's block header
 *
 */
public class ParentBlockHeaderValidator extends DependentBlockHeaderRule {

    private List<DependentBlockHeaderRule> rules;

    public ParentBlockHeaderValidator(List<DependentBlockHeaderRule> rules) {
        this.rules = rules;
    }

    @Override
    public boolean validate(BlockHeader header, BlockHeader parent) {
        errors.clear();

        for (DependentBlockHeaderRule rule : rules) {
            if (!rule.validate(header, parent)) {
                errors.addAll(rule.getErrors());
                return false;
            }
        }

        return true;
    }
}
