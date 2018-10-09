package com.fanstime.fti.core.connect.handler;

import com.fanstime.fti.core.*;
import com.fanstime.fti.sync.SyncStatistics;
import com.google.common.util.concurrent.ListenableFuture;

import java.math.BigInteger;
import java.util.List;

/**
 * It's quite annoying to always check {@code if (fti != null)} before accessing it. <br>
 * <p>
 * This adapter helps to avoid such checks. It provides meaningful answers to Fti client
 * assuming that Fti hasn't been initialized yet. <br>
 * <p>
 * Check {@link com.fanstime.fti.core.connect.server.Channel} for example.
 */
public class FtiAdapter implements Fti {

    private final SyncStatistics syncStats = new SyncStatistics();

    @Override
    public boolean hasStatusPassed() {
        return false;
    }

    @Override
    public boolean hasStatusSucceeded() {
        return false;
    }

    @Override
    public void onShutdown() {
    }

    @Override
    public String getSyncStats() {
        return "";
    }

    @Override
    public boolean isHashRetrievingDone() {
        return false;
    }

    @Override
    public boolean isHashRetrieving() {
        return false;
    }

    @Override
    public boolean isIdle() {
        return true;
    }

    @Override
    public SyncStatistics getStats() {
        return syncStats;
    }

    @Override
    public void disableTransactions() {
    }

    @Override
    public void enableTransactions() {
    }

    @Override
    public void sendTransaction(List<Transaction> tx) {
    }

    @Override
    public ListenableFuture<List<BlockHeader>> sendGetBlockHeaders(long blockNumber, int maxBlocksAsk, boolean reverse) {
        return null;
    }

    @Override
    public ListenableFuture<List<BlockHeader>> sendGetBlockHeaders(byte[] blockHash, int maxBlocksAsk, int skip, boolean reverse) {
        return null;
    }

    @Override
    public ListenableFuture<List<Block>> sendGetBlockBodies(List<BlockHeaderWrapper> headers) {
        return null;
    }

    @Override
    public void sendNewBlock(Block newBlock) {
    }

    @Override
    public void sendNewBlockHashes(Block block) {

    }

    @Override
    public void onSyncDone(boolean done) {
    }

    @Override
    public void sendStatus() {
    }

    @Override
    public void dropConnection() {
    }

    @Override
    public void fetchBodies(List<BlockHeaderWrapper> headers) {
    }

    @Override
    public BlockIdentifier getBestKnownBlock() {
        return null;
    }

    @Override
    public BigInteger getTotalDifficulty() {
        return BigInteger.ZERO;
    }

}
