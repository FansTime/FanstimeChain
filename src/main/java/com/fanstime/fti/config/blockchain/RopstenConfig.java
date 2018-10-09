package com.fanstime.fti.config.blockchain;

import org.apache.commons.lang3.tuple.Pair;
import com.fanstime.fti.config.BlockchainConfig;
import com.fanstime.fti.validator.BlockCustomHashRule;
import com.fanstime.fti.validator.BlockHeaderValidator;
import org.spongycastle.util.encoders.Hex;

/**
 * Created by Jay Nicolas on 21.04.2018.
 */
public class RopstenConfig extends Eip160HFConfig {

    // Check for 1 known block to exclude fake peers
    private static final long CHECK_BLOCK_NUMBER = 10;
    private static final byte[] CHECK_BLOCK_HASH = Hex.decode("b3074f936815a0425e674890d7db7b5e94f3a06dca5b22d291b55dcd02dde93e");

    public RopstenConfig(BlockchainConfig parent) {
        super(parent);
        headerValidators().add(Pair.of(CHECK_BLOCK_NUMBER, new BlockHeaderValidator(new BlockCustomHashRule(CHECK_BLOCK_HASH))));
    }

    @Override
    public Integer getChainId() {
        return 3;
    }
}
