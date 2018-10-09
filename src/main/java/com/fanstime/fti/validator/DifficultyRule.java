package com.fanstime.fti.validator;

import com.fanstime.fti.config.SystemProperties;
import com.fanstime.fti.core.BlockHeader;

import java.math.BigInteger;

import static com.fanstime.fti.util.BIUtil.isEqual;

/**
 * Checks block's difficulty against calculated difficulty value
 *
 */
public class DifficultyRule extends DependentBlockHeaderRule {

    private final SystemProperties config;

    public DifficultyRule(SystemProperties config) {
        this.config = config;
    }

    @Override
    public boolean validate(BlockHeader header, BlockHeader parent) {

        errors.clear();

        BigInteger calcDifficulty = header.calcDifficulty(config.getBlockchainConfig(), parent);
        BigInteger difficulty = header.getDifficultyBI();

        if (!isEqual(difficulty, calcDifficulty)) {

            errors.add(String.format("#%d: difficulty != calcDifficulty", header.getNumber()));
            return false;
        }

        return true;
    }
}
