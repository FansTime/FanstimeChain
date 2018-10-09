package com.fanstime.fti.validator;

import com.fanstime.fti.core.BlockHeader;

import java.util.Arrays;
import java.util.List;

/**
 * Composite {@link BlockHeader} validator
 * aggregating list of simple validation rules
 *
 */
public class BlockHeaderValidator extends BlockHeaderRule {

    private List<BlockHeaderRule> rules;

    public BlockHeaderValidator(List<BlockHeaderRule> rules) {
        this.rules = rules;
    }

    public BlockHeaderValidator(BlockHeaderRule ...rules) {
        this.rules = Arrays.asList(rules);
    }

    @Override
    public ValidationResult validate(BlockHeader header) {
        for (BlockHeaderRule rule : rules) {
            ValidationResult result = rule.validate(header);
            if (!result.success) {
                return result;
            }
        }
        return Success;
    }
}
