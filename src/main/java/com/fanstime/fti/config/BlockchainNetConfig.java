package com.fanstime.fti.config;

/**
 * Describes a set of configs for a specific blockchain depending on the block number
 * E.g. the main Fti net has at least FrontierConfig and HomesteadConfig depending on the block
 *
 * Created by Jay Nicolas on 25.02.2018.
 */
public interface BlockchainNetConfig {

    /**
     * Get the config for the specific block
     */
    BlockchainConfig getConfigForBlock(long blockNumber);

    /**
     * Returns the constants common for all the blocks in this blockchain
     */
    Constants getCommonConstants();
}
