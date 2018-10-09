package com.fanstime.fti.mine;

import com.google.common.util.concurrent.ListenableFuture;
import com.fanstime.fti.config.SystemProperties;
import com.fanstime.fti.core.Block;
import com.fanstime.fti.core.BlockHeader;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * The adapter of Ethash for MinerIfc
 *
 * Created by Jay Nicolas on 26.02.2018.
 */
public class EthashMiner implements MinerIfc {

    SystemProperties config;

    private int cpuThreads;
    private boolean fullMining = true;
    private Set<EthashListener> listeners = new CopyOnWriteArraySet<>();

    public EthashMiner(SystemProperties config) {
        this.config = config;
        cpuThreads = config.getMineCpuThreads();
        fullMining = config.isMineFullDataset();
    }

    @Override
    public ListenableFuture<MiningResult> mine(Block block) {
        return fullMining ?
                Ethash.getForBlock(config, block.getNumber(), listeners).mine(block, cpuThreads) :
                Ethash.getForBlock(config, block.getNumber(), listeners).mineLight(block, cpuThreads);
    }

    @Override
    public boolean validate(BlockHeader blockHeader) {
        return Ethash.getForBlock(config, blockHeader.getNumber(), listeners).validate(blockHeader);
    }

    /**
     * Listeners changes affects only future {@link #mine(Block)} and
     * {@link #validate(BlockHeader)} calls
     * Only instances of {@link EthashListener} are used, because EthashMiner
     * produces only events compatible with it
     */
    @Override
    public void setListeners(Collection<MinerListener> listeners) {
        this.listeners.clear();
        listeners.stream()
                .filter(listener -> listener instanceof EthashListener)
                .map(listener -> (EthashListener) listener)
                .forEach(this.listeners::add);
    }
}
