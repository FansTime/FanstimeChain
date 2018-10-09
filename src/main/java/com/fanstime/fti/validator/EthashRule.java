package com.fanstime.fti.validator;

import org.apache.commons.lang3.tuple.Pair;
import com.fanstime.fti.config.SystemProperties;
import com.fanstime.fti.core.BlockHeader;
import com.fanstime.fti.core.BlockSummary;
import com.fanstime.fti.listener.CompositeFtiListener;
import com.fanstime.fti.listener.FtiListenerAdapter;
import com.fanstime.fti.mine.EthashValidationHelper;
import com.fanstime.fti.util.FastByteComparisons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

import static com.fanstime.fti.validator.EthashRule.ChainType.main;
import static com.fanstime.fti.validator.EthashRule.ChainType.reverse;
import static com.fanstime.fti.validator.EthashRule.Mode.fake;
import static com.fanstime.fti.validator.EthashRule.Mode.mixed;

/**
 * Runs block header validation against Ethash dataset.
 *
 * <p>
 *     Configurable to work in several modes:
 *     <ul>
 *         <li> fake - partial checks without verification against Ethash dataset
 *         <li> strict - full check for each block
 *         <li> mixed  - run full check for each block if main import flow during short sync,
 *                       run full check in random fashion (<code>1/{@link #MIX_DENOMINATOR}</code> blocks are checked)
 *                                during long sync, fast sync headers and blocks downloading
 *
 *
 */
public class EthashRule extends BlockHeaderRule {

    private static final Logger logger = LoggerFactory.getLogger("blockchain");

    EthashValidationHelper ethashHelper;
    ProofOfWorkRule powRule = new ProofOfWorkRule();

    public enum Mode {
        strict,
        mixed,
        fake;

        static Mode parse(String name, Mode defaultMode) {
            for (Mode mode : values()) {
                if (mode.name().equals(name.toLowerCase()))
                    return mode;
            }
            return defaultMode;
        }
    }

    public enum ChainType {
        main,       /** main chain, cache updates are stick to best block events, requires listener */
        direct,     /** side chain, cache is triggered each validation attempt, no listener required */
        reverse;    /** side chain with reverted validation order */

        public boolean isSide() {
            return this == reverse || this == direct;
        }
    }

    private static final int MIX_DENOMINATOR = 5;
    private Mode mode = mixed;
    private ChainType chain = main;
    private boolean syncDone = false;
    private Random rnd = new Random();

    // two most common settings
    public static EthashRule createRegular(SystemProperties systemProperties, CompositeFtiListener listener) {
        return new EthashRule(Mode.parse(systemProperties.getEthashMode(), mixed), main, listener);
    }

    public static EthashRule createReverse(SystemProperties systemProperties) {
        return new EthashRule(Mode.parse(systemProperties.getEthashMode(), mixed), reverse, null);
    }

    public EthashRule(Mode mode, ChainType chain, CompositeFtiListener listener) {
        this.mode = mode;
        this.chain = chain;

        if (this.mode != fake) {
            this.ethashHelper = new EthashValidationHelper(
                    chain == reverse ? EthashValidationHelper.CacheOrder.reverse : EthashValidationHelper.CacheOrder.direct);

            if (this.chain == main && listener != null) {
                listener.addListener(new FtiListenerAdapter() {
                    @Override
                    public void onSyncDone(SyncState state) {
                        EthashRule.this.syncDone = true;
                    }

                    @Override
                    public void onBlock(BlockSummary blockSummary, boolean best) {
                        if (best) ethashHelper.preCache(blockSummary.getBlock().getNumber());
                    }
                });
            }
        }
    }

    @Override
    public ValidationResult validate(BlockHeader header) {

        if (header.isGenesis())
            return Success;

        if (ethashHelper == null)
            return powRule.validate(header);

        // trigger cache for side chains before mixed mode condition
        if (chain.isSide())
            ethashHelper.preCache(header.getNumber());

        // mixed mode payload
        if (mode == mixed && !syncDone && rnd.nextInt(100) % MIX_DENOMINATOR > 0)
            return powRule.validate(header);

        try {
            Pair<byte[], byte[]> res = ethashHelper.ethashWorkFor(header, header.getNonce(), true);
            // no cache for the epoch? fallback into fake rule
            if (res == null) {
                return powRule.validate(header);
            }

            if (!FastByteComparisons.equal(res.getLeft(), header.getMixHash())) {
                return fault(String.format("#%d: mixHash doesn't match", header.getNumber()));
            }

            if (FastByteComparisons.compareTo(res.getRight(), 0, 32, header.getPowBoundary(), 0, 32) > 0) {
                return fault(String.format("#%d: proofValue > header.getPowBoundary()", header.getNumber()));
            }

            return Success;
        } catch (Exception e) {
            logger.error("Failed to verify ethash work for block {}", header.getShortDescr(), e);
            return fault("Failed to verify ethash work for block " + header.getShortDescr());
        }
    }
}
