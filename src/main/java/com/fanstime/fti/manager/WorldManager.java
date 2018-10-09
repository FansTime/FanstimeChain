package com.fanstime.fti.manager;

import com.fanstime.fti.config.SystemProperties;
import com.fanstime.fti.core.*;
import com.fanstime.fti.db.BlockStore;
import com.fanstime.fti.db.DbFlushManager;
import com.fanstime.fti.db.HeaderStore;
import com.fanstime.fti.db.migrate.MigrateHeaderSourceTotalDiff;
import com.fanstime.fti.listener.CompositeFtiListener;
import com.fanstime.fti.listener.FtiListener;
import com.fanstime.fti.core.connect.client.PeerClient;
import com.fanstime.fti.sync.FastSyncManager;
import com.fanstime.fti.sync.SyncManager;
import com.fanstime.fti.core.connect.rlpx.discover.NodeManager;
import com.fanstime.fti.core.connect.server.ChannelManager;
import com.fanstime.fti.sync.SyncPool;
import com.fanstime.fti.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static com.fanstime.fti.crypto.HashUtil.EMPTY_TRIE_HASH;
import static com.fanstime.fti.util.ByteUtil.toHexString;

/**
 * WorldManager is a singleton containing references to different parts of the system.
 *
 */
@Component
public class WorldManager {

    private static final Logger logger = LoggerFactory.getLogger("general");

    @Autowired
    private PeerClient activePeer;

    @Autowired
    private ChannelManager channelManager;

    @Autowired
    private AdminInfo adminInfo;

    @Autowired
    private NodeManager nodeManager;

    @Autowired
    private SyncManager syncManager;

    @Autowired
    private PendingState pendingState;

    @Autowired
    private EventDispatchThread eventDispatchThread;

    @Autowired
    private DbFlushManager dbFlushManager;

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    private SyncPool pool;

    private SystemProperties config;

    private FtiListener listener;

    private Blockchain blockchain;

    private Repository repository;

    private BlockStore blockStore;

    @Autowired
    public WorldManager(final SystemProperties config, final Repository repository,
                        final FtiListener listener, final Blockchain blockchain,
                        final BlockStore blockStore) {
        this.listener = listener;
        this.blockchain = blockchain;
        this.repository = repository;
        this.blockStore = blockStore;
        this.config = config;
        loadBlockchain();
    }

    @PostConstruct
    private void init() {
        fastSyncDbJobs();
        syncManager.init(channelManager, pool);
    }

    public void addListener(FtiListener listener) {
        logger.info("Fti listener added");
        ((CompositeFtiListener) this.listener).addListener(listener);
    }

    public void startPeerDiscovery() {
    }

    public void stopPeerDiscovery() {
        nodeManager.close();
    }

    public void initSyncing() {
        config.setSyncEnabled(true);
        syncManager.init(channelManager, pool);
    }

    public FtiListener getListener() {
        return listener;
    }

    public com.fanstime.fti.facade.Repository getRepository() {
        return (com.fanstime.fti.facade.Repository)repository;
    }

    public Blockchain getBlockchain() {
        return blockchain;
    }

    public PeerClient getActivePeer() {
        return activePeer;
    }

    public BlockStore getBlockStore() {
        return blockStore;
    }

    public PendingState getPendingState() {
        return pendingState;
    }

    public void loadBlockchain() {

        if (!config.databaseReset() || config.databaseResetBlock() != 0)
            blockStore.load();

        if (blockStore.getBestBlock() == null) {
            logger.info("DB is empty - adding Genesis");

            Genesis genesis = Genesis.getInstance(config);
            Genesis.populateRepository(repository, genesis);

//            repository.commitBlock(genesis.getHeader());
            repository.commit();

            blockStore.saveBlock(Genesis.getInstance(config), Genesis.getInstance(config).getDifficultyBI(), true);

            blockchain.setBestBlock(Genesis.getInstance(config));
            blockchain.setTotalDifficulty(Genesis.getInstance(config).getDifficultyBI());

            listener.onBlock(new BlockSummary(Genesis.getInstance(config), new HashMap<byte[], BigInteger>(), new ArrayList<TransactionReceipt>(), new ArrayList<TransactionExecutionSummary>()), true);
//            repository.dumpState(Genesis.getInstance(config), 0, 0, null);

            logger.info("Genesis block loaded");
        } else {

            if (!config.databaseReset() &&
                    !Arrays.equals(blockchain.getBlockByNumber(0).getHash(), config.getGenesis().getHash())) {
                // fatal exit
                Utils.showErrorAndExit("*** DB is incorrect, 0 block in DB doesn't match genesis");
            }

            Block bestBlock = blockStore.getBestBlock();
            if (config.databaseReset() && config.databaseResetBlock() > 0) {
                if (config.databaseResetBlock() > bestBlock.getNumber()) {
                    logger.error("*** Can't reset to block [{}] since block store is at block [{}].", config.databaseResetBlock(), bestBlock);
                    throw new RuntimeException("Reset block ahead of block store.");
                }
                bestBlock = blockStore.getChainBlockByNumber(config.databaseResetBlock());

                Repository snapshot = repository.getSnapshotTo(bestBlock.getStateRoot());
                if (false) { // TODO: some way to tell if the snapshot hasn't been pruned
                    logger.error("*** Could not reset database to block [{}] with stateRoot [{}], since state information is " +
                            "unavailable.  It might have been pruned from the database.");
                    throw new RuntimeException("State unavailable for reset block.");
                }
            }

            blockchain.setBestBlock(bestBlock);

            BigInteger totalDifficulty = blockStore.getTotalDifficultyForHash(bestBlock.getHash());
            blockchain.setTotalDifficulty(totalDifficulty);

            logger.info("*** Loaded up to block [{}] totalDifficulty [{}] with stateRoot [{}]",
                    blockchain.getBestBlock().getNumber(),
                    blockchain.getTotalDifficulty().toString(),
                    toHexString(blockchain.getBestBlock().getStateRoot()));
        }

        if (config.rootHashStart() != null) {

            // update world state by dummy hash
            byte[] rootHash = Hex.decode(config.rootHashStart());
            logger.info("Loading root hash from property file: [{}]", config.rootHashStart());
            this.repository.syncToRoot(rootHash);

        } else {

            // Update world state to latest loaded block from db
            // if state is not generated from empty premine list
            // todo this is just a workaround, move EMPTY_TRIE_HASH logic to Trie implementation
            if (!Arrays.equals(blockchain.getBestBlock().getStateRoot(), EMPTY_TRIE_HASH)) {
                this.repository.syncToRoot(blockchain.getBestBlock().getStateRoot());
            }
        }

/* todo: return it when there is no state conflicts on the chain
        boolean dbValid = this.repository.getWorldState().validate() || bestBlock.isGenesis();
        if (!dbValid){
            logger.error("The DB is not valid for that blockchain");
            System.exit(-1); //  todo: reset the repository and blockchain
        }
*/
    }

    /**
     * After introducing skipHistory in FastSync this method
     * adds additional header storage to Blockchain
     * as Blockstore is incomplete in this mode
     */
    private void fastSyncDbJobs() {
        // checking if fast sync ran sometime ago with "skipHistory flag"
        if (blockStore.getBestBlock().getNumber() > 0 &&
                blockStore.getChainBlockByNumber(1) == null) {
            FastSyncManager fastSyncManager = ctx.getBean(FastSyncManager.class);
            if (fastSyncManager.isInProgress()) {
                return;
            }
            logger.info("DB is filled using Fast Sync with skipHistory, adopting headerStore");
            ((BlockchainImpl) blockchain).setHeaderStore(ctx.getBean(HeaderStore.class));
        }
        MigrateHeaderSourceTotalDiff tempMigration = new MigrateHeaderSourceTotalDiff(ctx, blockStore, blockchain, config);
        tempMigration.run();
    }

    public void close() {
        logger.info("close: stopping peer discovery ...");
        stopPeerDiscovery();
        logger.info("close: stopping ChannelManager ...");
        channelManager.close();
        logger.info("close: stopping PeerClient ...");
        activePeer.close();
        logger.info("close: shutting down event dispatch thread used by EventBus ...");
        eventDispatchThread.shutdown();
        logger.info("close: closing Blockchain instance ...");
        blockchain.close();
        logger.info("close: closing main repository ...");
        repository.close();
        logger.info("close: database flush manager ...");
        dbFlushManager.close();
    }

}
