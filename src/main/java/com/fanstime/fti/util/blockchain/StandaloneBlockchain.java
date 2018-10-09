package com.fanstime.fti.util.blockchain;

import com.fanstime.fti.config.BlockchainNetConfig;
import com.fanstime.fti.config.SystemProperties;
import com.fanstime.fti.config.blockchain.FrontierConfig;
import com.fanstime.fti.core.*;
import com.fanstime.fti.core.genesis.GenesisLoader;
import com.fanstime.fti.crypto.ECKey;
import com.fanstime.fti.datasource.JournalSource;
import com.fanstime.fti.datasource.Source;
import com.fanstime.fti.datasource.inmem.HashMapDB;
import com.fanstime.fti.db.ByteArrayWrapper;
import com.fanstime.fti.db.IndexedBlockStore;
import com.fanstime.fti.db.PruneManager;
import com.fanstime.fti.db.RepositoryRoot;
import com.fanstime.fti.listener.CompositeFtiListener;
import com.fanstime.fti.listener.FtiListener;
import com.fanstime.fti.listener.FtiListenerAdapter;
import com.fanstime.fti.mine.Ethash;
import com.fanstime.fti.solidity.compiler.CompilationResult;
import com.fanstime.fti.solidity.compiler.CompilationResult.ContractMetadata;
import com.fanstime.fti.solidity.compiler.SolidityCompiler;
import com.fanstime.fti.util.ByteUtil;
import com.fanstime.fti.util.FastByteComparisons;
import com.fanstime.fti.validator.DependentBlockHeaderRuleAdapter;
import com.fanstime.fti.vm.DataWord;
import com.fanstime.fti.vm.LogInfo;
import com.fanstime.fti.vm.program.invoke.ProgramInvokeFactoryImpl;
import org.iq80.leveldb.DBException;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

import static com.fanstime.fti.util.ByteUtil.wrap;

/**
 * Created by Jay Nicolas on 23.03.2018.
 */
public class StandaloneBlockchain implements LocalBlockchain {

    Genesis genesis;
    byte[] coinbase;
    BlockchainImpl blockchain;
    PendingStateImpl pendingState;
    CompositeFtiListener listener;
    ECKey txSender;
    long gasPrice;
    long gasLimit;
    boolean autoBlock;
    long dbDelay = 0;
    long totalDbHits = 0;
    BlockchainNetConfig netConfig;

    int blockGasIncreasePercent = 0;

    long time = 0;
    long timeIncrement = 13;

    private HashMapDB<byte[]> stateDS;
    JournalSource<byte[]> pruningStateDS;
    PruneManager pruneManager;

    private BlockSummary lastSummary;

    class PendingTx {
        ECKey sender;
        byte[] toAddress;
        BigInteger value;
        byte[] data;

        SolidityContractImpl createdContract;
        SolidityContractImpl targetContract;

        Transaction customTx;

        TransactionResult txResult = new TransactionResult();

        public PendingTx(byte[] toAddress, BigInteger value, byte[] data) {
            this.sender = txSender;
            this.toAddress = toAddress;
            this.value = value;
            this.data = data;
        }

        public PendingTx(byte[] toAddress, BigInteger value, byte[] data,
                         SolidityContractImpl createdContract, SolidityContractImpl targetContract, TransactionResult res) {
            this.sender = txSender;
            this.toAddress = toAddress;
            this.value = value;
            this.data = data;
            this.createdContract = createdContract;
            this.targetContract = targetContract;
            this.txResult = res;
        }

        public PendingTx(Transaction customTx) {
            this.customTx = customTx;
        }
    }

    List<PendingTx> submittedTxes = new CopyOnWriteArrayList<>();

    public StandaloneBlockchain() {
        Genesis genesis = GenesisLoader.loadGenesis(
                getClass().getResourceAsStream("/genesis/genesis-light-sb.json"));

        withGenesis(genesis);
        withGasPrice(50_000_000_000L);
        withGasLimit(5_000_000L);
        withMinerCoinbase(Hex.decode("ffffffffffffffffffffffffffffffffffffffff"));
        setSender(ECKey.fromPrivate(Hex.decode("3ec771c31cac8c0dba77a69e503765701d3c2bb62435888d4ffa38fed60c445c")));
//        withAccountBalance(txSender.getAddress(), new BigInteger("100000000000000000000000000"));
    }

    public StandaloneBlockchain withGenesis(Genesis genesis) {
        this.genesis = genesis;
        return this;
    }

    public StandaloneBlockchain withMinerCoinbase(byte[] coinbase) {
        this.coinbase = coinbase;
        return this;
    }

    public StandaloneBlockchain withNetConfig(BlockchainNetConfig netConfig) {
        this.netConfig = netConfig;
        return this;
    }

    public StandaloneBlockchain withAccountBalance(byte[] address, BigInteger weis) {
        AccountState state = new AccountState(BigInteger.ZERO, weis);
        genesis.addPremine(wrap(address), state);
        genesis.setStateRoot(GenesisLoader.generateRootHash(genesis.getPremine()));

        return this;
    }


    public StandaloneBlockchain withGasPrice(long gasPrice) {
        this.gasPrice = gasPrice;
        return this;
    }

    public StandaloneBlockchain withGasLimit(long gasLimit) {
        this.gasLimit = gasLimit;
        return this;
    }

    public StandaloneBlockchain withAutoblock(boolean autoblock) {
        this.autoBlock = autoblock;
        return this;
    }

    public StandaloneBlockchain withCurrentTime(Date date) {
        this.time = date.getTime() / 1000;
        return this;
    }

    /**
     * [-100, 100]
     * 0 - the same block gas limit as parent
     * 100 - max available increase from parent gas limit
     * -100 - max available decrease from parent gas limit
     */
    public StandaloneBlockchain withBlockGasIncrease(int blockGasIncreasePercent) {
        this.blockGasIncreasePercent = blockGasIncreasePercent;
        return this;
    }

    public StandaloneBlockchain withDbDelay(long dbDelay) {
        this.dbDelay = dbDelay;
        return this;
    }

    private Map<PendingTx, Transaction> createTransactions(Block parent) {
        Map<PendingTx, Transaction> txes = new LinkedHashMap<>();
        Map<ByteArrayWrapper, Long> nonces = new HashMap<>();
        Repository repoSnapshot = getBlockchain().getRepository().getSnapshotTo(parent.getStateRoot());
        for (PendingTx tx : submittedTxes) {
            Transaction transaction;
            if (tx.customTx == null) {
                ByteArrayWrapper senderW = new ByteArrayWrapper(tx.sender.getAddress());
                Long nonce = nonces.get(senderW);
                if (nonce == null) {
                    BigInteger bcNonce = repoSnapshot.getNonce(tx.sender.getAddress());
                    nonce = bcNonce.longValue();
                }
                nonces.put(senderW, nonce + 1);

                byte[] toAddress = tx.targetContract != null ? tx.targetContract.getAddress() : tx.toAddress;

                transaction = createTransaction(tx.sender, nonce, toAddress, tx.value, tx.data);

                if (tx.createdContract != null) {
                    tx.createdContract.setAddress(transaction.getContractAddress());
                }
            } else {
                transaction = tx.customTx;
            }

            txes.put(tx, transaction);
        }
        return txes;
    }

    public PendingStateImpl getPendingState() {
        return pendingState;
    }

    public void generatePendingTransactions() {
        pendingState.addPendingTransactions(new ArrayList<>(createTransactions(getBlockchain().getBestBlock()).values()));
    }

    @Override
    public Block createBlock() {
        return createForkBlock(getBlockchain().getBestBlock());
    }

    @Override
    public Block createForkBlock(Block parent) {
        try {
            Map<PendingTx, Transaction> txes = createTransactions(parent);

            time += timeIncrement;
            Block b = getBlockchain().createNewBlock(parent, new ArrayList<>(txes.values()), Collections.EMPTY_LIST, time);

            int GAS_LIMIT_BOUND_DIVISOR = SystemProperties.getDefault().getBlockchainConfig().
                    getCommonConstants().getGAS_LIMIT_BOUND_DIVISOR();
            BigInteger newGas = ByteUtil.bytesToBigInteger(parent.getGasLimit())
                    .multiply(BigInteger.valueOf(GAS_LIMIT_BOUND_DIVISOR * 100 + blockGasIncreasePercent))
                    .divide(BigInteger.valueOf(GAS_LIMIT_BOUND_DIVISOR * 100));
            b.getHeader().setGasLimit(ByteUtil.bigIntegerToBytes(newGas));

            Ethash.getForBlock(SystemProperties.getDefault(), b.getNumber()).mineLight(b).get();
            ImportResult importResult = getBlockchain().tryToConnect(b);
            if (importResult != ImportResult.IMPORTED_BEST && importResult != ImportResult.IMPORTED_NOT_BEST) {
                throw new RuntimeException("Invalid block import result " + importResult + " for block " + b);
            }

            List<PendingTx> pendingTxes = new ArrayList<>(txes.keySet());
            for (int i = 0; i < lastSummary.getReceipts().size(); i++) {
                pendingTxes.get(i).txResult.receipt = lastSummary.getReceipts().get(i);
                pendingTxes.get(i).txResult.executionSummary = getTxSummary(lastSummary, i);
            }

            submittedTxes.clear();
            return b;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private TransactionExecutionSummary getTxSummary(BlockSummary bs, int idx) {
        TransactionReceipt txReceipt = bs.getReceipts().get(idx);
        for (TransactionExecutionSummary summary : bs.getSummaries()) {
            if (FastByteComparisons.equal(txReceipt.getTransaction().getHash(), summary.getTransaction().getHash())) {
                return summary;
            }
        }
        return null;
    }

    public Transaction createTransaction(long nonce, byte[] toAddress, long value, byte[] data) {
        return createTransaction(getSender(), nonce, toAddress, BigInteger.valueOf(value), data);
    }

    public Transaction createTransaction(ECKey sender, long nonce, byte[] toAddress, BigInteger value, byte[] data) {
        Transaction transaction = new Transaction(ByteUtil.longToBytesNoLeadZeroes(nonce),
                ByteUtil.longToBytesNoLeadZeroes(gasPrice),
                ByteUtil.longToBytesNoLeadZeroes(gasLimit),
                toAddress, ByteUtil.bigIntegerToBytes(value),
                data,
                null);
        transaction.sign(sender);
        return transaction;
    }

    public void resetSubmittedTransactions() {
        submittedTxes.clear();
    }

    @Override
    public void setSender(ECKey senderPrivateKey) {
        txSender = senderPrivateKey;
//        if (!getBlockchain().getRepository().isExist(senderPrivateKey.getAddress())) {
//            Repository repository = getBlockchain().getRepository();
//            Repository track = repository.startTracking();
//            track.createAccount(senderPrivateKey.getAddress());
//            track.commit();
//        }
    }

    public ECKey getSender() {
        return txSender;
    }

    @Override
    public void sendFti(byte[] toAddress, BigInteger weis) {
        submitNewTx(new PendingTx(toAddress, weis, new byte[0]));
    }

    public void submitTransaction(Transaction tx) {
        submitNewTx(new PendingTx(tx));
    }

    @Override
    public SolidityContract submitNewContract(String soliditySrc, Object... constructorArgs) {
        return submitNewContract(soliditySrc, null, constructorArgs);
    }

    @Override
    public SolidityContract submitNewContract(String soliditySrc, String contractName, Object... constructorArgs) {
        SolidityContractImpl contract = createContract(soliditySrc, contractName);
        return submitNewContract(contract, constructorArgs);
    }

    @Override
    public SolidityContract submitNewContractFromJson(String json, Object... constructorArgs) {
        return submitNewContractFromJson(json, null, constructorArgs);
    }

    @Override
    public SolidityContract submitNewContractFromJson(String json, String contractName, Object... constructorArgs) {
        SolidityContractImpl contract;
        try {
            contract = createContractFromJson(contractName, json);
            return submitNewContract(contract, constructorArgs);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SolidityContract submitNewContract(ContractMetadata contractMetaData, Object... constructorArgs) {
        SolidityContractImpl contract = new SolidityContractImpl(contractMetaData);
        return submitNewContract(contract, constructorArgs);
    }

    private SolidityContract submitNewContract(SolidityContractImpl contract, Object... constructorArgs) {
        CallTransaction.Function constructor = contract.contract.getConstructor();
        if (constructor == null && constructorArgs.length > 0) {
            throw new RuntimeException("No constructor with params found");
        }
        byte[] argsEncoded = constructor == null ? new byte[0] : constructor.encodeArguments(constructorArgs);
        submitNewTx(new PendingTx(new byte[0], BigInteger.ZERO,
                ByteUtil.merge(Hex.decode(contract.getBinary()), argsEncoded), contract, null,
                new TransactionResult()));
        return contract;
    }

    private SolidityContractImpl createContract(String soliditySrc, String contractName) {
        try {
            SolidityCompiler.Result compileRes = SolidityCompiler.compile(soliditySrc.getBytes(), true, SolidityCompiler.Options.ABI, SolidityCompiler.Options.BIN);
            if (compileRes.isFailed()) throw new RuntimeException("Compile result: " + compileRes.errors);
            return createContractFromJson(contractName, compileRes.output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private SolidityContractImpl createContractFromJson(String contractName, String json) throws IOException {
        CompilationResult result = CompilationResult.parse(json);
        if (contractName == null) {
            contractName = result.getContractName();
        }

        return createContract(contractName, result);
    }

    /**
     * @param contractName
     * @param result
     * @return
     */
    private SolidityContractImpl createContract(String contractName, CompilationResult result) {
        ContractMetadata cMetaData = result.getContract(contractName);
        SolidityContractImpl contract = createContract(cMetaData);

        for (CompilationResult.ContractMetadata metadata : result.getContracts()) {
            contract.addRelatedContract(metadata.abi);
        }
        return contract;
    }

    private SolidityContractImpl createContract(ContractMetadata contractData) {
        SolidityContractImpl contract = new SolidityContractImpl(contractData);
        return contract;
    }

    @Override
    public SolidityContract createExistingContractFromSrc(String soliditySrc, String contractName, byte[] contractAddress) {
        SolidityContractImpl contract = createContract(soliditySrc, contractName);
        contract.setAddress(contractAddress);
        return contract;
    }

    @Override
    public SolidityContract createExistingContractFromSrc(String soliditySrc, byte[] contractAddress) {
        return createExistingContractFromSrc(soliditySrc, null, contractAddress);
    }

    @Override
    public SolidityContract createExistingContractFromABI(String ABI, byte[] contractAddress) {
        SolidityContractImpl contract = new SolidityContractImpl(ABI);
        contract.setAddress(contractAddress);
        return contract;
    }

    @Override
    public BlockchainImpl getBlockchain() {
        if (blockchain == null) {
            blockchain = createBlockchain(genesis);
            blockchain.setMinerCoinbase(coinbase);
            addFtiListener(new FtiListenerAdapter() {
                @Override
                public void onBlock(BlockSummary blockSummary) {
                    lastSummary = blockSummary;
                }
            });
        }
        return blockchain;
    }

    public void addFtiListener(FtiListener listener) {
        getBlockchain();
        this.listener.addListener(listener);
    }

    private void submitNewTx(PendingTx tx) {
        getBlockchain();
        submittedTxes.add(tx);
        if (autoBlock) {
            createBlock();
        }
    }

    public HashMapDB<byte[]> getStateDS() {
        return stateDS;
    }

    public Source<byte[], byte[]> getPruningStateDS() {
        return pruningStateDS;
    }

    public long getTotalDbHits() {
        return totalDbHits;
    }

    private BlockchainImpl createBlockchain(Genesis genesis) {
        SystemProperties.getDefault().setBlockchainConfig(netConfig != null ? netConfig : getEasyMiningConfig());

        IndexedBlockStore blockStore = new IndexedBlockStore();
        blockStore.init(new HashMapDB<byte[]>(), new HashMapDB<byte[]>());

        stateDS = new HashMapDB<>();
        pruningStateDS = new JournalSource<>(stateDS);
        pruneManager = new PruneManager(blockStore, pruningStateDS,
                stateDS, SystemProperties.getDefault().databasePruneDepth());

        final RepositoryRoot repository = new RepositoryRoot(pruningStateDS);

        ProgramInvokeFactoryImpl programInvokeFactory = new ProgramInvokeFactoryImpl();
        listener = new CompositeFtiListener();

        BlockchainImpl blockchain = new BlockchainImpl(blockStore, repository)
                .withFtiListener(listener);
        blockchain.setParentHeaderValidator(new DependentBlockHeaderRuleAdapter());
        blockchain.setProgramInvokeFactory(programInvokeFactory);
        blockchain.setPruneManager(pruneManager);

        blockchain.byTest = true;

        pendingState = new PendingStateImpl(listener);

        pendingState.setBlockchain(blockchain);
        blockchain.setPendingState(pendingState);

        Genesis.populateRepository(repository, genesis);

        repository.commit();

        blockStore.saveBlock(genesis, genesis.getDifficultyBI(), true);

        blockchain.setBestBlock(genesis);
        blockchain.setTotalDifficulty(genesis.getDifficultyBI());

        pruneManager.blockCommitted(genesis.getHeader());

        return blockchain;
    }

    public class SolidityFunctionImpl implements SolidityFunction {
        SolidityContractImpl contract;
        CallTransaction.Function abi;

        public SolidityFunctionImpl(SolidityContractImpl contract, CallTransaction.Function abi) {
            this.contract = contract;
            this.abi = abi;
        }

        @Override
        public SolidityContract getContract() {
            return contract;
        }

        @Override
        public CallTransaction.Function getInterface() {
            return abi;
        }
    }

    public class SolidityContractImpl implements SolidityContract {
        byte[] address;
        public CompilationResult.ContractMetadata compiled;
        public CallTransaction.Contract contract;
        public List<CallTransaction.Contract> relatedContracts = new ArrayList<>();

        public SolidityContractImpl(String abi) {
            contract = new CallTransaction.Contract(abi);
        }

        public SolidityContractImpl(CompilationResult.ContractMetadata result) {
            this(result.abi);
            compiled = result;
        }

        public void addRelatedContract(String abi) {
            CallTransaction.Contract c = new CallTransaction.Contract(abi);
            relatedContracts.add(c);
        }

        void setAddress(byte[] address) {
            this.address = address;
        }

        @Override
        public byte[] getAddress() {
            if (address == null) {
                throw new RuntimeException("Contract address will be assigned only after block inclusion. Call createBlock() first.");
            }
            return address;
        }

        @Override
        public SolidityCallResult callFunction(String functionName, Object... args) {
            return callFunction(0, functionName, args);
        }

        @Override
        public SolidityCallResult callFunction(long value, String functionName, Object... args) {
            CallTransaction.Function function = contract.getByName(functionName);
            byte[] data = function.encode(convertArgs(args));
            SolidityCallResult res = new SolidityCallResultImpl(this, function);
            submitNewTx(new PendingTx(null, BigInteger.valueOf(value), data, null, this, res));
            return res;
        }

        @Override
        public Object[] callConstFunction(String functionName, Object... args) {
            return callConstFunction(getBlockchain().getBestBlock(), functionName, args);
        }

        @Override
        public Object[] callConstFunction(Block callBlock, String functionName, Object... args) {

            CallTransaction.Function func = contract.getByName(functionName);
            if (func == null) throw new RuntimeException("No function with name '" + functionName + "'");
            Transaction tx = CallTransaction.createCallTransaction(0, 0, 100000000000000L,
                    Hex.toHexString(getAddress()), 0, func, convertArgs(args));
            tx.sign(ECKey.DUMMY);

            Repository repository = getBlockchain().getRepository().getSnapshotTo(callBlock.getStateRoot()).startTracking();

            try {
                com.fanstime.fti.core.TransactionExecutor executor = new com.fanstime.fti.core.TransactionExecutor
                        (tx, callBlock.getCoinbase(), repository, getBlockchain().getBlockStore(),
                                getBlockchain().getProgramInvokeFactory(), callBlock)
                        .setLocalCall(true);

                executor.init();
                executor.execute();
                executor.go();
                executor.finalization();

                return func.decodeResult(executor.getResult().getHReturn());
            } finally {
                repository.rollback();
            }
        }

        private Object[] convertArgs(Object[] args) {
            Object[] ret = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof SolidityFunction) {
                    SolidityFunction f = (SolidityFunction) args[i];
                    ret[i] = ByteUtil.merge(f.getContract().getAddress(), f.getInterface().encodeSignature());
                } else {
                    ret[i] = args[i];
                }
            }
            return ret;
        }

        @Override
        public SolidityStorage getStorage() {
            return new SolidityStorageImpl(getAddress());
        }

        @Override
        public String getABI() {
            return compiled.abi;
        }

        @Override
        public String getBinary() {
            return compiled.bin;
        }

        @Override
        public void call(byte[] callData) {
            // for this we need cleaner separation of EasyBlockchain to
            // Abstract and Solidity specific
            throw new UnsupportedOperationException();
        }

        @Override
        public SolidityFunction getFunction(String name) {
            return new SolidityFunctionImpl(this, contract.getByName(name));
        }
    }

    public class SolidityCallResultImpl extends SolidityCallResult {
        SolidityContractImpl contract;
        CallTransaction.Function function;

        SolidityCallResultImpl(SolidityContractImpl contract, CallTransaction.Function function) {
            this.contract = contract;
            this.function = function;
        }

        @Override
        public CallTransaction.Function getFunction() {
            return function;
        }

        public List<CallTransaction.Invocation> getEvents() {
            List<CallTransaction.Invocation> ret = new ArrayList<>();
            for (LogInfo logInfo : getReceipt().getLogInfoList()) {
                for (CallTransaction.Contract c : contract.relatedContracts) {
                    CallTransaction.Invocation event = c.parseEvent(logInfo);
                    if (event != null) ret.add(event);
                }
            }
            return ret;
        }

        @Override
        public String toString() {
            String ret = "SolidityCallResult{" +
                    function + ": " +
                    (isIncluded() ? "EXECUTED" : "PENDING") + ", ";
            if (isIncluded()) {
                ret += isSuccessful() ? "SUCCESS" : ("ERR (" + getReceipt().getError() + ")");
                ret += ", ";
                if (isSuccessful()) {
                    ret += "Ret: " + Arrays.toString(getReturnValues()) + ", ";
                    ret += "Events: " + getEvents() + ", ";
                }
            }
            return ret + "}";
        }
    }


    class SolidityStorageImpl implements SolidityStorage {
        byte[] contractAddr;

        public SolidityStorageImpl(byte[] contractAddr) {
            this.contractAddr = contractAddr;
        }

        @Override
        public byte[] getStorageSlot(long slot) {
            return getStorageSlot(new DataWord(slot).getData());
        }

        @Override
        public byte[] getStorageSlot(byte[] slot) {
            DataWord ret = getBlockchain().getRepository().getContractDetails(contractAddr).get(new DataWord(slot));
            return ret.getData();
        }
    }

    class SlowHashMapDB extends HashMapDB<byte[]> {
        private void sleep(int cnt) {
            totalDbHits += cnt;
            if (dbDelay == 0) return;
            try {
                Thread.sleep(dbDelay * cnt);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public synchronized void delete(byte[] arg0) throws DBException {
            super.delete(arg0);
            sleep(1);
        }

        @Override
        public synchronized byte[] get(byte[] arg0) throws DBException {
            sleep(1);
            return super.get(arg0);
        }

        @Override
        public synchronized void put(byte[] key, byte[] value) throws DBException {
            sleep(1);
            super.put(key, value);
        }

        @Override
        public synchronized void updateBatch(Map<byte[], byte[]> rows) {
            sleep(rows.size() / 2);
            super.updateBatch(rows);
        }
    }

    // Override blockchain net config for fast mining
    public static FrontierConfig getEasyMiningConfig() {
        return new FrontierConfig(new FrontierConfig.FrontierConstants() {
            @Override
            public BigInteger getMINIMUM_DIFFICULTY() {
                return BigInteger.ONE;
            }
        });
    }
}
