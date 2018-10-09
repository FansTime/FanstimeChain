package com.fanstime.fti.mine;

import com.fanstime.fti.core.Block;

/**
 * Created by Jay Nicolas on 10.18.2018.
 */
public interface MinerListener {
    void miningStarted();
    void miningStopped();
    void blockMiningStarted(Block block);
    void blockMined(Block block);
    void blockMiningCanceled(Block block);
}
