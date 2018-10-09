package com.fanstime.fti.web.service;

import com.fanstime.fti.facade.Fti;
import com.fanstime.fti.web.model.dto.MinerStatusDTO;
import com.fanstime.fti.util.BlockUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import com.fanstime.fti.config.SystemProperties;
import com.fanstime.fti.core.Block;
import com.fanstime.fti.core.Repository;
import com.fanstime.fti.mine.EthashListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Queue;

/**
 * Created by Bynum Williams on 19.08.16.
 */
@Slf4j(topic = "fanstime")
@Service
public class PrivateMinerService {

    public final static String MINER_TOPIC = "/topic/mineInfo";

    @Autowired
    Environment env;

    @Autowired
    Fti fti;

    @Autowired
    SystemProperties config;

    @Autowired
    public Repository repository;

    @Autowired
    private ClientMessageService clientMessageService;

    private final static int AVG_METRICS_BASE = 100;
    Queue<Block> latestBlocks = new CircularFifoQueue<>(AVG_METRICS_BASE);

    private MineStatus status = MineStatus.DISABLED;

    @PostConstruct
    public void init() throws IOException, InterruptedException {
        final boolean isPrivateNetwork = env.getProperty("networkProfile", "").equalsIgnoreCase("private");
        log.info("isPrivateNetwork: " + isPrivateNetwork);
        fti.getBlockMiner().addListener(new EthashListener() {
            @Override
            public void miningStarted() {
                status = MineStatus.MINING;
                pushStatus(status);
                log.info("miningStarted");
            }

            @Override
            public void miningStopped() {
                status = MineStatus.DISABLED;
                pushStatus(status);
                latestBlocks.clear();
                log.info("miningStopped");
            }

            @Override
            public void blockMiningStarted(Block block) {
                log.info("new block mining started");
            }

            @Override
            public void blockMined(Block block) {
                if (status != MineStatus.MINING) {
                    status = MineStatus.MINING;
                }
                latestBlocks.add(block);
                log.info("blockMined");
            }

            @Override
            public void blockMiningCanceled(Block block) {
                log.info("blockMiningCanceled");
            }

            @Override
            public void onDatasetUpdate(DatasetStatus datasetStatus) {
                switch (datasetStatus) {
                    case LIGHT_DATASET_GENERATE_START:
                        status = MineStatus.LIGHT_DAG_GENERATE;
                        break;
                    case FULL_DATASET_GENERATE_START:
                        status = MineStatus.FULL_DAG_GENERATE;
                        pushStatus(status);
                        break;
                    case FULL_DATASET_GENERATED:
                        status = MineStatus.DAG_GENERATED;
                        pushStatus(status);
                        break;
                    case DATASET_READY:
                        status = MineStatus.DAG_GENERATED;
                        break;
                }
                log.info("Dataset status updated: {}", datasetStatus);
            }
        });
        // WOW, how is stinks!
        // Overriding mine.start which was reset in {@link com.blockchain.fanstime.Application}
        SystemProperties.resetToDefault();
        config.overrideParams("mine.start", new Boolean(SystemProperties.getDefault().minerStart()).toString());
        if (config.minerStart()) {
            if (!config.isSyncEnabled()) {
                fti.getBlockMiner().startMining();
            } else {
                this.status = MineStatus.AWAITING;
            }
        }
    }

    /**
     * @return average hash rate/second for our own mined blocks
     */
    public BigInteger calcAvgHashRate() {
        return BlockUtils.calculateHashRate(new ArrayList<>(latestBlocks));
    }

    /**
     * Pushes status change immediately to client application
     */
    private void pushStatus(MineStatus status) {
        clientMessageService.sendToTopic(MINER_TOPIC, new MinerStatusDTO(status.toString()));
    }

    public MineStatus getStatus() {
        return status;
    }

    public enum MineStatus {
        DISABLED,
        LIGHT_DAG_GENERATE,
        FULL_DAG_GENERATE,
        DAG_GENERATED,
        MINING,
        AWAITING // Mining is on, but we are on long sync, waiting for short sync
    }
}
