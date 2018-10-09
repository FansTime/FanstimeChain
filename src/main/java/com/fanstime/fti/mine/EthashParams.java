package com.fanstime.fti.mine;

/**
 * Created by Jay Nicolas on 27.07.2018.
 */
public class EthashParams {
    // bytes in word
    private final int WORD_BYTES = 4;

    // bytes in dataset at genesis
    private final long DATASET_BYTES_INIT = 1L << 30;

    // dataset growth per epoch
    private final long DATASET_BYTES_GROWTH = 1L << 23;

    //  bytes in dataset at genesis
    private final long CACHE_BYTES_INIT = 1L << 24;

    // cache growth per epoch
    private final long CACHE_BYTES_GROWTH = 1L << 17;

    //  Size of the DAG relative to the cache
    private final long CACHE_MULTIPLIER = 1024;

    //  blocks per epoch
    private final long EPOCH_LENGTH = 30000;

    // width of mix
    private final int MIX_BYTES = 128;

    //  hash length in bytes
    private final int HASH_BYTES = 64;

    // number of parents of each dataset element
    private final long DATASET_PARENTS = 256;

    // number of rounds in cache production
    private final long CACHE_ROUNDS = 3;

    //  number of accesses in hashimoto loop
    private final long ACCESSES = 64;

    /**
     * The parameters for Ethash's cache and dataset depend on the block number.
     * The cache size and dataset size both grow linearly; however, we always take the highest
     * prime below the linearly growing threshold in order to reduce the risk of accidental
     * regularities leading to cyclic behavior.
     */
    public long getCacheSize(long blockNumber) {
        long sz = CACHE_BYTES_INIT + CACHE_BYTES_GROWTH * (blockNumber / EPOCH_LENGTH);
        sz -= HASH_BYTES;
        while (!isPrime(sz / HASH_BYTES)) {
            sz -= 2 * HASH_BYTES;
        }
        return sz;
    }

    public long getFullSize(long blockNumber) {
        long sz = DATASET_BYTES_INIT + DATASET_BYTES_GROWTH * (blockNumber / EPOCH_LENGTH);
        sz -= MIX_BYTES;
        while (!isPrime(sz / MIX_BYTES)) {
            sz -= 2 * MIX_BYTES;
        }
        return sz;
    }

    private static boolean isPrime(long num) {
        if (num == 2) return true;
        if (num % 2 == 0) return false;
        for (int i = 3; i * i < num; i += 2)
            if (num % i == 0) return false;
        return true;
    }

    public int getWORD_BYTES() {
        return WORD_BYTES;
    }

    public long getDATASET_BYTES_INIT() {
        return DATASET_BYTES_INIT;
    }

    public long getDATASET_BYTES_GROWTH() {
        return DATASET_BYTES_GROWTH;
    }

    public long getCACHE_BYTES_INIT() {
        return CACHE_BYTES_INIT;
    }

    public long getCACHE_BYTES_GROWTH() {
        return CACHE_BYTES_GROWTH;
    }

    public long getCACHE_MULTIPLIER() {
        return CACHE_MULTIPLIER;
    }

    public long getEPOCH_LENGTH() {
        return EPOCH_LENGTH;
    }

    public int getMIX_BYTES() {
        return MIX_BYTES;
    }

    public int getHASH_BYTES() {
        return HASH_BYTES;
    }

    public long getDATASET_PARENTS() {
        return DATASET_PARENTS;
    }

    public long getCACHE_ROUNDS() {
        return CACHE_ROUNDS;
    }

    public long getACCESSES() {
        return ACCESSES;
    }
}

