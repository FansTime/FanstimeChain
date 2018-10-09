package com.fanstime.fti.core;

import com.fanstime.fti.listener.FtiListener;
import org.apache.commons.lang3.tuple.Pair;
import com.fanstime.fti.config.BlockchainConfig;
import com.fanstime.fti.config.CommonConfig;
import com.fanstime.fti.config.SystemProperties;
import com.fanstime.fti.db.BlockStore;
import com.fanstime.fti.db.ContractDetails;
import com.fanstime.fti.listener.FtiListenerAdapter;
import com.fanstime.fti.util.ByteArraySet;
import com.fanstime.fti.vm.*;
import com.fanstime.fti.vm.program.Program;
import com.fanstime.fti.vm.program.ProgramObject;
import com.fanstime.fti.vm.program.ProgramResult;
import com.fanstime.fti.vm.program.invoke.ProgramInvoke;
import com.fanstime.fti.vm.program.invoke.ProgramInvokeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;

// TODO: create an independent VM for BeanShell?
import com.fanstime.fti.bsh.Interpreter;
import com.fanstime.fti.bsh.TargetError;
import com.fanstime.fti.bsh.EvalError;

import static org.apache.commons.lang3.ArrayUtils.getLength;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static com.fanstime.fti.util.BIUtil.*;
import static com.fanstime.fti.util.ByteUtil.EMPTY_BYTE_ARRAY;
import static com.fanstime.fti.util.ByteUtil.toHexString;
import static com.fanstime.fti.vm.VMUtils.saveProgramTraceFile;
import static com.fanstime.fti.vm.VMUtils.zipAndEncode;


public class TransactionExecutor {

    private static final Logger logger = LoggerFactory.getLogger("execute");
    private static final Logger stateLogger = LoggerFactory.getLogger("state");

    SystemProperties config;
    CommonConfig commonConfig;
    BlockchainConfig blockchainConfig;

    private Transaction tx;
    private Repository track;
    private Repository cacheTrack;
    private BlockStore blockStore;
    private final long gasUsedInTheBlock;
    private boolean readyToExecute = false;
    private String execError;

    private ProgramInvokeFactory programInvokeFactory;
    private byte[] coinbase;

    private TransactionReceipt receipt;
    private ProgramResult result = new ProgramResult();
    private Block currentBlock;

    private final FtiListener listener;

    private Interpreter bsh;  // For Java scripts
    private VM vm;
    private Program program;

    PrecompiledContracts.PrecompiledContract precompiledContract;

    BigInteger m_endGas = BigInteger.ZERO;
    long basicTxCost = 0;
    List<LogInfo> logs = null;

    private ByteArraySet touchedAccounts = new ByteArraySet();

    boolean localCall = false;

    public TransactionExecutor(Transaction tx, byte[] coinbase, Repository track, BlockStore blockStore,
                               ProgramInvokeFactory programInvokeFactory, Block currentBlock) {

        this(tx, coinbase, track, blockStore, programInvokeFactory, currentBlock, new FtiListenerAdapter(), 0);
    }

    public TransactionExecutor(Transaction tx, byte[] coinbase, Repository track, BlockStore blockStore,
                               ProgramInvokeFactory programInvokeFactory, Block currentBlock,
                               FtiListener listener, long gasUsedInTheBlock) {

        this.tx = tx;
        this.coinbase = coinbase;
        this.track = track;
        this.cacheTrack = track.startTracking();
        this.blockStore = blockStore;
        this.programInvokeFactory = programInvokeFactory;
        this.currentBlock = currentBlock;
        this.listener = listener;
        this.gasUsedInTheBlock = gasUsedInTheBlock;
        this.m_endGas = toBI(tx.getGasLimit());
        withCommonConfig(CommonConfig.getDefault());
    }

    public TransactionExecutor withCommonConfig(CommonConfig commonConfig) {
        this.commonConfig = commonConfig;
        this.config = commonConfig.systemProperties();
        this.blockchainConfig = config.getBlockchainConfig().getConfigForBlock(currentBlock.getNumber());
        return this;
    }

    private void execError(String err) {
        System.out.println("********** [TransactionExecutor::execError] " + err);
        logger.warn(err);
        execError = err;
    }

    /**
     * Do all the basic validation, if the executor
     * will be ready to run the transaction at the end
     * set readyToExecute = true
     */
    public void init() {
        basicTxCost = tx.transactionCost(config.getBlockchainConfig(), currentBlock);

        if (localCall) {
            readyToExecute = true;
            return;
        }

        BigInteger txGasLimit = new BigInteger(1, tx.getGasLimit());
        BigInteger curBlockGasLimit = new BigInteger(1, currentBlock.getGasLimit());

        boolean cumulativeGasReached = txGasLimit.add(BigInteger.valueOf(gasUsedInTheBlock)).compareTo(curBlockGasLimit) > 0;
        if (cumulativeGasReached) {

            execError(String.format("Too much gas used in this block: Require: %s Got: %s", new BigInteger(1, currentBlock.getGasLimit()).longValue() - toBI(tx.getGasLimit()).longValue(), toBI(tx.getGasLimit()).longValue()));

            return;
        }

        if (txGasLimit.compareTo(BigInteger.valueOf(basicTxCost)) < 0) {

            execError(String.format("Not enough gas for transaction execution: Require: %s Got: %s", basicTxCost, txGasLimit));

            return;
        }

        BigInteger reqNonce = track.getNonce(tx.getSender());
        BigInteger txNonce = toBI(tx.getNonce());
        if (isNotEqual(reqNonce, txNonce)) {
            execError(String.format("Invalid nonce: required: %s , tx.nonce: %s", reqNonce, txNonce));

            return;
        }

        BigInteger txGasCost = toBI(tx.getGasPrice()).multiply(txGasLimit);
        BigInteger totalCost = toBI(tx.getValue()).add(txGasCost);
        BigInteger senderBalance = track.getBalance(tx.getSender());

        if (!isCovers(senderBalance, totalCost)) {
            execError(String.format("Not enough cash: Require: %s, Sender cash: %s", totalCost, senderBalance));
            return;
        }

        if (!blockchainConfig.acceptTransactionSignature(tx)) {
            execError("Transaction signature not accepted: " + tx.getSignature());
            return;
        }

        readyToExecute = true;
    }

    public void execute() {

        if (!readyToExecute) return;

        if (!localCall) {
            track.increaseNonce(tx.getSender());

            BigInteger txGasLimit = toBI(tx.getGasLimit());
            BigInteger txGasCost = toBI(tx.getGasPrice()).multiply(txGasLimit);
            track.addBalance(tx.getSender(), txGasCost.negate());

            if (logger.isInfoEnabled())
                logger.info("Paying: txGasCost: [{}], gasPrice: [{}], gasLimit: [{}]", txGasCost, toBI(tx.getGasPrice()), txGasLimit);
        }

        if (tx.isContractCreation()) {
            create();
        } else {
            call();
        }
    }

    private void call() {
        if (!readyToExecute) return;

        byte[] targetAddress = tx.getReceiveAddress();
        precompiledContract = PrecompiledContracts.getContractForAddress(new DataWord(targetAddress), blockchainConfig);

        if (precompiledContract != null) {
            long requiredGas = precompiledContract.getGasForData(tx.getData());

            BigInteger spendingGas = BigInteger.valueOf(requiredGas).add(BigInteger.valueOf(basicTxCost));

            if (!localCall && m_endGas.compareTo(spendingGas) < 0) {
                // no refund
                // no endowment
                execError("Out of Gas calling precompiled contract 0x" + toHexString(targetAddress) +
                        ", required: " + spendingGas + ", left: " + m_endGas);
                m_endGas = BigInteger.ZERO;
                return;
            } else {

                m_endGas = m_endGas.subtract(spendingGas);

                // FIXME: save return for vm trace
                Pair<Boolean, byte[]> out = precompiledContract.execute(tx.getData());

                if (!out.getLeft()) {
                    execError("Error executing precompiled contract 0x" + toHexString(targetAddress));
                    m_endGas = BigInteger.ZERO;
                    return;
                }
            }

        } else {
            if (tx.getLanguage() == 1) {
                String scriptData = new String(track.getCode(targetAddress));
                System.out.println("********** [TransactionExecutor::call] review Java code of contract " +
                                   toHexString(targetAddress) + ": " + scriptData);
                
                ProgramInvoke programInvoke =
                        programInvokeFactory.createProgramInvoke(tx, currentBlock, cacheTrack, blockStore);
                this.program = new Program(scriptData.getBytes(), programInvoke, tx, config).withCommonConfig(commonConfig);
                
                this.bsh = new Interpreter();
                try {
                    this.bsh.set("program", new ProgramObject(this.program, this.bsh));
                    this.bsh.eval(scriptData);
                    this.bsh.eval(new String(tx.getData()));  // Run user-custom interface
                } catch (TargetError e1) {
                    execError("BeanShell target error: " + e1);
                } catch (EvalError e2) {
                    execError("BeanShell eval error: " + e2);
                }
                System.out.println("********** [TransactionExecutor::call] new JavaVM for calling: " + toHexString(targetAddress));
            }
            else {
                byte[] code = track.getCode(targetAddress);
                System.out.println("********** [TransactionExecutor::call] review code for calling: " + toHexString(code));
                
                if (isEmpty(code)) {
                    m_endGas = m_endGas.subtract(BigInteger.valueOf(basicTxCost));
                    result.spendGas(basicTxCost);
                } else {
                    ProgramInvoke programInvoke =
                            programInvokeFactory.createProgramInvoke(tx, currentBlock, cacheTrack, blockStore);
                    this.program = new Program(track.getCodeHash(targetAddress), code, programInvoke, tx, config)
                                              .withCommonConfig(commonConfig);
                    this.vm = new VM(config);
                    System.out.println("********** [TransactionExecutor::call] new EVM for calling: " + toHexString(targetAddress));
                }
            }
        }

        BigInteger endowment = toBI(tx.getValue());
        transfer(cacheTrack, tx.getSender(), targetAddress, endowment);
        touchedAccounts.add(targetAddress);
    }

    private void create() {
        byte[] newContractAddress = tx.getContractAddress();

        AccountState existingAddr = cacheTrack.getAccountState(newContractAddress);
        if (existingAddr != null && existingAddr.isContractExist(blockchainConfig)) {
            execError("Trying to create a contract with existing contract address: 0x" + toHexString(newContractAddress));
            m_endGas = BigInteger.ZERO;
            return;
        }

        //In case of hashing collisions (for TCK tests only), check for any balance before createAccount()
        BigInteger oldBalance = track.getBalance(newContractAddress);
        cacheTrack.createAccount(tx.getContractAddress());
        cacheTrack.addBalance(newContractAddress, oldBalance);
        if (blockchainConfig.eip161()) {
            cacheTrack.increaseNonce(newContractAddress);
        }

        if (tx.getLanguage() == 1) {
            System.out.println("********** [TransactionExecutor::create] review Java code: " + new String(tx.getData())
                             + " of contract " + toHexString(newContractAddress));
        }
        
        if (isEmpty(tx.getData())) {
            m_endGas = m_endGas.subtract(BigInteger.valueOf(basicTxCost));
            result.spendGas(basicTxCost);
        } else {
            ProgramInvoke programInvoke = programInvokeFactory.createProgramInvoke(tx, currentBlock, cacheTrack, blockStore);
            this.program = new Program(tx.getData(), programInvoke, tx, config).withCommonConfig(commonConfig);
            
            if (tx.getLanguage() == 1) {
                String scriptData = new String(tx.getData());  // TODO: compressed?
                this.bsh = new Interpreter();
                try {
                    this.bsh.set("program", new ProgramObject(this.program, this.bsh));
                    this.bsh.eval(scriptData);
                    this.bsh.eval("init();");  // Run init() function in user script
                    cacheTrack.saveCode(tx.getContractAddress(), scriptData.getBytes());
                } catch (TargetError e1) {
                    execError("BeanShell target error: " + e1);
                } catch (EvalError e2) {
                    execError("BeanShell eval error: " + e2);
                }
                System.out.println("********** [TransactionExecutor::create] new JavaVM for: " + toHexString(newContractAddress));
            }
            else {
                this.vm = new VM(config);
                System.out.println("********** [TransactionExecutor::create] new EVM for: " + toHexString(newContractAddress));
            }
            
            // reset storage if the contract with the same address already exists
            // TCK test case only - normally this is near-impossible situation in the real network
            // TODO make via Trie.clear() without keyset
//            ContractDetails contractDetails = program.getStorage().getContractDetails(newContractAddress);
//            for (DataWord key : contractDetails.getStorageKeys()) {
//                program.storageSave(key, DataWord.ZERO);
//            }
        }

        BigInteger endowment = toBI(tx.getValue());
        transfer(cacheTrack, tx.getSender(), newContractAddress, endowment);
        touchedAccounts.add(newContractAddress);
    }

    public void go() {
        if (!readyToExecute) return;

        try {
            if (vm != null) {
                // Charge basic cost of the transaction
                program.spendGas(tx.transactionCost(config.getBlockchainConfig(), currentBlock), "TRANSACTION COST");

                if (config.playVM())
                    vm.play(program);
                System.out.println("********** [TransactionExecutor::go] parse and play VM for: " + toBI(tx.getSender()));

                result = program.getResult();
                m_endGas = toBI(tx.getGasLimit()).subtract(toBI(program.getResult().getGasUsed()));

                if (tx.isContractCreation() && !result.isRevert()) {
                    int returnDataGasValue = getLength(program.getResult().getHReturn()) *
                            blockchainConfig.getGasCost().getCREATE_DATA();
                    if (m_endGas.compareTo(BigInteger.valueOf(returnDataGasValue)) < 0) {
                        // Not enough gas to return contract code
                        if (!blockchainConfig.getConstants().createEmptyContractOnOOG()) {
                            program.setRuntimeFailure(Program.Exception.notEnoughSpendingGas("No gas to return just created contract",
                                    returnDataGasValue, program));
                            result = program.getResult();
                        }
                        result.setHReturn(EMPTY_BYTE_ARRAY);
                    } else if (getLength(result.getHReturn()) > blockchainConfig.getConstants().getMAX_CONTRACT_SZIE()) {
                        // Contract size too large
                        program.setRuntimeFailure(Program.Exception.notEnoughSpendingGas("Contract size too large: " + getLength(result.getHReturn()),
                                returnDataGasValue, program));
                        result = program.getResult();
                        result.setHReturn(EMPTY_BYTE_ARRAY);
                    } else {
                        // Contract successfully created
                        m_endGas = m_endGas.subtract(BigInteger.valueOf(returnDataGasValue));
                        cacheTrack.saveCode(tx.getContractAddress(), result.getHReturn());
                    }
                }

                String err = config.getBlockchainConfig().getConfigForBlock(currentBlock.getNumber()).
                        validateTransactionChanges(blockStore, currentBlock, tx, null);
                if (err != null) {
                    System.out.println("********** [TransactionExecutor::go] transaction changes validation failed: " + err);
                    program.setRuntimeFailure(new RuntimeException("Transaction changes validation failed: " + err));
                }


                if (result.getException() != null || result.isRevert()) {
                    result.getDeleteAccounts().clear();
                    result.getLogInfoList().clear();
                    result.resetFutureRefund();
                    rollback();

                    if (result.getException() != null) {
                        throw result.getException();
                    } else {
                        execError("REVERT opcode executed");
                    }
                } else {
                    touchedAccounts.addAll(result.getTouchedAccounts());
                    cacheTrack.commit();
                }

            } else if (tx.getLanguage() == 1) {
                program.spendGas(tx.transactionCost(config.getBlockchainConfig(), currentBlock), "TRANSACTION COST");
                result = program.getResult();
                m_endGas = toBI(tx.getGasLimit()).subtract(toBI(program.getResult().getGasUsed()));
                
                if (tx.isContractCreation()) {
                    int returnDataGasValue = getLength(program.getResult().getHReturn()) *
                                             blockchainConfig.getGasCost().getCREATE_DATA();
                    
                    // Contract successfully created
                    m_endGas = m_endGas.subtract(BigInteger.valueOf(returnDataGasValue));
                    cacheTrack.saveCode(tx.getContractAddress(), tx.getData());
                }
                
                touchedAccounts.addAll(result.getTouchedAccounts());
                cacheTrack.commit();
            }
            else {
                cacheTrack.commit();
            }

        } catch (Throwable e) {

            // TODO: catch whatever they will throw on you !!!
            rollback();
            m_endGas = BigInteger.ZERO;
            execError(e.getMessage());
        }
    }

    private void rollback() {

        cacheTrack.rollback();

        // remove touched account
        touchedAccounts.remove(
                tx.isContractCreation() ? tx.getContractAddress() : tx.getReceiveAddress());
    }

    public TransactionExecutionSummary finalization() {
        if (!readyToExecute) return null;

        TransactionExecutionSummary.Builder summaryBuilder = TransactionExecutionSummary.builderFor(tx)
                .gasLeftover(m_endGas)
                .logs(result.getLogInfoList())
                .result(result.getHReturn());

        if (result != null) {
            // Accumulate refunds for suicides
            result.addFutureRefund(result.getDeleteAccounts().size() * config.getBlockchainConfig().
                    getConfigForBlock(currentBlock.getNumber()).getGasCost().getSUICIDE_REFUND());
            long gasRefund = Math.min(result.getFutureRefund(), getGasUsed() / 2);
            byte[] addr = tx.isContractCreation() ? tx.getContractAddress() : tx.getReceiveAddress();
            m_endGas = m_endGas.add(BigInteger.valueOf(gasRefund));

            summaryBuilder
                    .gasUsed(toBI(result.getGasUsed()))
                    .gasRefund(toBI(gasRefund))
                    .deletedAccounts(result.getDeleteAccounts())
                    .internalTransactions(result.getInternalTransactions());

            ContractDetails contractDetails = track.getContractDetails(addr);
            if (contractDetails != null) {
                // TODO
//                summaryBuilder.storageDiff(track.getContractDetails(addr).getStorage());
//
//                if (program != null) {
//                    summaryBuilder.touchedStorage(contractDetails.getStorage(), program.getStorageDiff());
//                }
            }

            if (result.getException() != null) {
                summaryBuilder.markAsFailed();
            }
        }

        TransactionExecutionSummary summary = summaryBuilder.build();

        // Refund for gas leftover
        track.addBalance(tx.getSender(), summary.getLeftover().add(summary.getRefund()));
        logger.info("Pay total refund to sender: [{}], refund val: [{}]", toHexString(tx.getSender()), summary.getRefund());

        // Transfer fees to miner
        track.addBalance(coinbase, summary.getFee());
        touchedAccounts.add(coinbase);
        logger.info("Pay fees to miner: [{}], feesEarned: [{}]", toHexString(coinbase), summary.getFee());

        if (result != null) {
            logs = result.getLogInfoList();
            // Traverse list of suicides
            for (DataWord address : result.getDeleteAccounts()) {
                track.delete(address.getLast20Bytes());
            }
        }

        if (blockchainConfig.eip161()) {
            for (byte[] acctAddr : touchedAccounts) {
                AccountState state = track.getAccountState(acctAddr);
                if (state != null && state.isEmpty()) {
                    track.delete(acctAddr);
                }
            }
        }


        listener.onTransactionExecuted(summary);

        if (config.vmTrace() && program != null && result != null) {
            String trace = program.getTrace()
                    .result(result.getHReturn())
                    .error(result.getException())
                    .toString();


            if (config.vmTraceCompressed()) {
                trace = zipAndEncode(trace);
            }

            String txHash = toHexString(tx.getHash());
            saveProgramTraceFile(config, txHash, trace);
            listener.onVMTraceCreated(txHash, trace);
        }
        return summary;
    }

    public TransactionExecutor setLocalCall(boolean localCall) {
        this.localCall = localCall;
        return this;
    }


    public TransactionReceipt getReceipt() {
        if (receipt == null) {
            receipt = new TransactionReceipt();
            long totalGasUsed = gasUsedInTheBlock + getGasUsed();
            receipt.setCumulativeGas(totalGasUsed);
            receipt.setTransaction(tx);
            receipt.setLogInfoList(getVMLogs());
            receipt.setGasUsed(getGasUsed());
            receipt.setExecutionResult(getResult().getHReturn());
            receipt.setError(execError);
//          receipt.setPostTxState(track.getRoot()); // TODO later when RepositoryTrack.getRoot() is implemented
        }
        return receipt;
    }

    public List<LogInfo> getVMLogs() {
        return logs;
    }

    public ProgramResult getResult() {
        return result;
    }

    public long getGasUsed() {
        return toBI(tx.getGasLimit()).subtract(m_endGas).longValue();
    }

}
