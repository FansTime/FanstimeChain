package com.fanstime.fti.mine;

/**
 * {@link MinerListener} designed for use with {@link EthashMiner}
 */
public interface EthashListener extends MinerListener {

    enum DatasetStatus {
        /**
         * Dataset requested and will be prepared
         */
        DATASET_PREPARE,
        /**
         * Indicates start of light DAG generation
         * If full dataset is requested, its event
         * {@link #FULL_DATASET_GENERATE_START} fires before this one
         */
        LIGHT_DATASET_GENERATE_START,
        /**
         * Indicates that light dataset is already generated
         * and will be loaded from disk though it could be outdated
         * and therefore {@link #LIGHT_DATASET_LOADED} will not be fired
         */
        LIGHT_DATASET_LOAD_START,
        /**
         * Indicates end of loading light dataset from disk
         */
        LIGHT_DATASET_LOADED,
        /**
         * Indicates finish of light dataset generation
         */
        LIGHT_DATASET_GENERATED,
        /**
         * Indicates start of full DAG generation
         * Full DAG generation is a heavy procedure
         * which could take a lot of time.
         * Also full dataset requires light dataset
         * so it will be either generated or loaded from
         * disk as part of this job
         */
        FULL_DATASET_GENERATE_START,
        /**
         * Indicates that full dataset is already generated
         * and will be loaded from disk though it could be outdated
         * and therefore {@link #FULL_DATASET_LOADED} will not be fired
         */
        FULL_DATASET_LOAD_START,
        /**
         * Indicates end of full dataset loading from disk
         */
        FULL_DATASET_LOADED,
        /**
         * Indicates finish of full dataset generation
         */
        FULL_DATASET_GENERATED,
        /**
         * Requested dataset is complete and ready for use
         */
        DATASET_READY,
    }

    void onDatasetUpdate(DatasetStatus datasetStatus);
}
