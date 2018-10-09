package com.fanstime.fti.vm.program;

import com.fanstime.fti.crypto.HashUtil;
import com.fanstime.fti.vm.*;
import org.spongycastle.util.encoders.Hex;

import com.fanstime.fti.bsh.Interpreter;
import com.fanstime.fti.bsh.TargetError;
import com.fanstime.fti.bsh.EvalError;

import java.math.BigInteger;
import java.util.*;

import static java.lang.StrictMath.min;
import static java.lang.String.format;
import static com.fanstime.fti.util.BIUtil.*;

public class ProgramObject {
    private final Program program;
    private final Interpreter bsh;
    
    public ProgramObject(Program p, Interpreter b) {
        this.program = p;
        this.bsh = b;
    }
    
    public static String toJsonHex(byte[] x) {
        return x == null || x.length == 0 ? null : "0x" + Hex.toHexString(x);
    }
    
    public byte[] sha3(byte[] data) throws Exception {
        program.spendGas(OpCode.SHA3.getTier().asInt(), OpCode.SHA3.name());
        return HashUtil.sha3(data);
    }
    
    public void print(String data) {
        print(data.getBytes());
    }
    
    public void print(byte[] data) {
        DataWord address = program.getOwnerAddress();
        List<DataWord> topics = new ArrayList<>();
        
        LogInfo logInfo = new LogInfo(address.getLast20Bytes(), topics, data);
        program.spendGas(OpCode.LOG0.getTier().asInt(), OpCode.LOG0.name());
        program.getResult().addLogInfo(logInfo);
    }
    
    public void print(String data, byte[] t0, byte[] t1, byte[] t2, byte[] t3) {
        print(data.getBytes(), t0, t1, t2, t3);
    }
    
    public void print(byte[] data, byte[] t0, byte[] t1, byte[] t2, byte[] t3) {
        DataWord address = program.getOwnerAddress();
        List<DataWord> topics = new ArrayList<>();
        if (t0 != null) topics.add(new DataWord(t0));
        if (t1 != null) topics.add(new DataWord(t1));
        if (t2 != null) topics.add(new DataWord(t2));
        if (t3 != null) topics.add(new DataWord(t3));
        
        LogInfo logInfo = new LogInfo(address.getLast20Bytes(), topics, data);
        program.spendGas(OpCode.LOG4.getTier().asInt(), OpCode.LOG4.name());
        program.getResult().addLogInfo(logInfo);
    }
    
    public void setHReturn(byte[] buff) {
        program.spendGas(OpCode.RETURN.getTier().asInt(), OpCode.RETURN.name());
        program.setHReturn(buff);
    }
    
    public void suicide(byte[] obtainerAddress) {
        GasCost gasCosts = program.getBlockchainConfig().getGasCost();
        program.spendGas(gasCosts.getSUICIDE(), OpCode.SUICIDE.name());
        
        DataWord keyWord = new DataWord(obtainerAddress);
        program.suicide(keyWord);
    }

    public void storageSave(DataWord word1, DataWord word2) {
        storageSave(word1.getData(), word2.getData());
    }
    
    public void storageSave(byte[] key, byte[] val) {
        GasCost gasCosts = program.getBlockchainConfig().getGasCost();
        program.spendGas(gasCosts.getSET_SSTORE(), OpCode.SSTORE.name());
        program.storageSave(key, val);
    }
    
    public byte[] storageLoad(byte[] key) {
        GasCost gasCosts = program.getBlockchainConfig().getGasCost();
        program.spendGas(gasCosts.getSLOAD(), OpCode.SLOAD.name());
        return program.storageLoad(key).getData();
    }
    
    public void runCode(byte[] code) {
        GasCost gasCosts = program.getBlockchainConfig().getGasCost();
        program.spendGas(gasCosts.getSET_SSTORE(), OpCode.CREATE.name());
        
        String scriptData = new String(code);
        try {
            this.bsh.eval(scriptData);
        } catch (TargetError e1) {
            print("BeanShell target error: " + e1);
        } catch (EvalError e2) {
            print("BeanShell eval error: " + e2);
        }
    }
    
    public byte[] getCodeAt(byte[] address) {
        DataWord keyWord = new DataWord(address);
        program.spendGas(OpCode.CALL.getTier().asInt(), OpCode.CALL.name());
        return program.getCodeAt(keyWord);
    }
    
    public byte[] getOwnerAddress() {
        program.spendGas(OpCode.ADDRESS.getTier().asInt(), OpCode.ADDRESS.name());
        return program.getOwnerAddress().getData();
    }
    
    public byte[] getBlockHash(int index) {
        program.spendGas(OpCode.BLOCKHASH.getTier().asInt(), OpCode.BLOCKHASH.name());
        return program.getBlockHash(index).getData();
    }
    
    public boolean transferBalance(byte[] srcAddr, byte[] toAddr, byte[] value) {
        BigInteger txGasLimit = new BigInteger(1, getGasLimit());
        BigInteger txGasCost = toBI(getGasPrice()).multiply(txGasLimit);
        program.spendGas(txGasCost.longValue(), OpCode.BALANCE.name());

        BigInteger endowment = toBI(value);
        BigInteger srcBalance = program.getStorage().getBalance(srcAddr);
        if (!isCovers(srcBalance, endowment)) return false;

        transfer(program.getStorage(), srcAddr, toAddr, endowment);
        return true;
    }
    
    public byte[] getBalance(byte[] address) {
        GasCost gasCosts = program.getBlockchainConfig().getGasCost();
        program.spendGas(gasCosts.getBALANCE(), OpCode.BALANCE.name());
        
        DataWord keyWord = new DataWord(address);
        return program.getBalance(keyWord).getData();
    }
    
    public byte[] getOriginAddress() {
        program.spendGas(OpCode.ORIGIN.getTier().asInt(), OpCode.ORIGIN.name());
        return program.getOriginAddress().getData();
    }
    
    public byte[] getCallerAddress() {
        program.spendGas(OpCode.CALLER.getTier().asInt(), OpCode.CALLER.name());
        return program.getCallerAddress().getData();
    }
    
    public byte[] getGasPrice() {
        program.spendGas(OpCode.GASPRICE.getTier().asInt(), OpCode.GASPRICE.name());
        return program.getGasPrice().getData();
    }
    
    public byte[] getGas() {
        program.spendGas(OpCode.GASPRICE.getTier().asInt(), OpCode.GASPRICE.name());
        return program.getGas().getData();
    }
    
    public byte[] getGasLimit() {
        program.spendGas(OpCode.GASLIMIT.getTier().asInt(), OpCode.GASLIMIT.name());
        return program.getGasLimit().getData();
    }
    
    public byte[] getCallValue() {
        program.spendGas(OpCode.CALLVALUE.getTier().asInt(), OpCode.CALLVALUE.name());
        return program.getCallValue().getData();
    }
    
    public byte[] getDifficulty() {
        program.spendGas(OpCode.DIFFICULTY.getTier().asInt(), OpCode.DIFFICULTY.name());
        return program.getDifficulty().getData();
    }
    
    public byte[] getNumber() {
        program.spendGas(OpCode.NUMBER.getTier().asInt(), OpCode.NUMBER.name());
        return program.getNumber().getData();
    }
    
    public byte[] getTimestamp() {
        program.spendGas(OpCode.TIMESTAMP.getTier().asInt(), OpCode.TIMESTAMP.name());
        return program.getTimestamp().getData();
    }
    
    public byte[] getCoinbase() {
        program.spendGas(OpCode.COINBASE.getTier().asInt(), OpCode.COINBASE.name());
        return program.getCoinbase().getData();
    }
}
