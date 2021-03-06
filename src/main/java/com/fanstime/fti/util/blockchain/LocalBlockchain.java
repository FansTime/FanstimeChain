package com.fanstime.fti.util.blockchain;

import com.fanstime.fti.core.Block;

/**
 * This interface is implemented by the locally created blockchain
 * where block issuance can be controlled.
 *
 * All the pending transactions submitted via EasyBlockchain are
 * buffered and become part of the blockchain as soon as
 * a new block is generated
 *
 * Created by Jay Nicolas on 24.03.2018.
 */
public interface LocalBlockchain extends EasyBlockchain {

    /**
     * Creates a new block which includes all the transactions
     * created via EasyBlockchain since the last created block
     * The pending transaction list is cleared.
     * The current best block on the chain becomes a parent of the
     * created block
     */
    Block createBlock();

    /**
     * The same as previous but the block parent is specified explicitly
     * This is handy for test/experiments with the chain fork branches
     */
    Block createForkBlock(Block parent);
}
