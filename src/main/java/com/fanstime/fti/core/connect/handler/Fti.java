package com.fanstime.fti.core.connect.handler;

import com.fanstime.fti.core.*;
import com.fanstime.fti.sync.SyncStatistics;
import com.google.common.util.concurrent.ListenableFuture;

import java.math.BigInteger;
import java.util.List;

/**
 * Describes interface required by Fti peer clients
 *
 * @see com.fanstime.fti.core.connect.server.Channel
 */
public interface Fti {

    /**
     * @return true if StatusMessage was processed, false otherwise
     */
    boolean hasStatusPassed();

    /**
     * @return true if Status has succeeded
     */
    boolean hasStatusSucceeded();

    /**
     * Executes cleanups required to be done
     * during shutdown, e.g. disconnect
     */
    void onShutdown();

    /**
     * Puts sync statistics to log output
     */
    String getSyncStats();

    BlockIdentifier getBestKnownBlock();

    BigInteger getTotalDifficulty();

    /**
     * @return true if syncState is DONE_HASH_RETRIEVING, false otherwise
     */
    boolean isHashRetrievingDone();

    /**
     * @return true if syncState is HEADER_RETRIEVING, false otherwise
     */
    boolean isHashRetrieving();

    /**
     * @return true if syncState is IDLE, false otherwise
     */
    boolean isIdle();

    /**
     * @return sync statistics
     */
    SyncStatistics getStats();

    /**
     * Disables pending transaction processing
     */
    void disableTransactions();

    /**
     * Enables pending transaction processing
     */
    void enableTransactions();

    /**
     * Sends transaction to the wire
     *
     * @param tx sending transaction
     */
    void sendTransaction(List<Transaction> tx);

    /**
     * Send GET_BLOCK_HEADERS message to the peer
     */
    ListenableFuture<List<BlockHeader>> sendGetBlockHeaders(long blockNumber, int maxBlocksAsk, boolean reverse);

    ListenableFuture<List<BlockHeader>> sendGetBlockHeaders(byte[] blockHash, int maxBlocksAsk, int skip, boolean reverse);

    /**
     * Send GET_BLOCK_BODIES message to the peer
     */
    ListenableFuture<List<Block>> sendGetBlockBodies(List<BlockHeaderWrapper> headers);

    /**
     * Sends new block to the wire
     */
    void sendNewBlock(Block newBlock);

    /**
     * Sends new block hashes message to the wire
     */
    void sendNewBlockHashes(Block block);

    /**
     * Fires inner logic related to long sync done or undone event
     *
     * @param done true notifies that long sync is finished,
     *             false notifies that it's enabled again
     */
    void onSyncDone(boolean done);

    /**
     * Sends {@link FtiMessageCodes#STATUS} message
     */
    void sendStatus();

    /**
     * Drops connection with remote peer.
     * It should be called when peer don't behave
     */
    void dropConnection();

    /**
     * Force peer to fetch block bodies
     *
     * @param headers related headers
     */
    void fetchBodies(List<BlockHeaderWrapper> headers);

}
