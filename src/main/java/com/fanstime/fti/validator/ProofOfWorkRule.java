package com.fanstime.fti.validator;

import com.fanstime.fti.core.BlockHeader;
import com.fanstime.fti.util.FastByteComparisons;

/**
 * Checks proof value against its boundary for the block header
 *
 */
public class ProofOfWorkRule extends BlockHeaderRule {

    @Override
    public ValidationResult validate(BlockHeader header) {
        byte[] proof = header.calcPowValue();
        byte[] boundary = header.getPowBoundary();

        if (!header.isGenesis() && FastByteComparisons.compareTo(proof, 0, 32, boundary, 0, 32) > 0) {
            return fault(String.format("#%d: proofValue > header.getPowBoundary()", header.getNumber()));
        }

        return Success;
    }
}
