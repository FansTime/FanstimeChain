package com.fanstime.fti.web.jsonrpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Value;
import lombok.experimental.NonFinal;
import com.fanstime.fti.core.Block;
import com.fanstime.fti.core.TransactionInfo;
import com.fanstime.fti.core.TransactionReceipt;
import com.fanstime.fti.vm.LogInfo;

import static com.fanstime.fti.web.jsonrpc.TypeConverter.toJsonHex;

/**
 * Created by Angelina on 5/1/2018.
 */
@Value
@NonFinal
public class TransactionReceiptDTO {

    public String transactionHash;          // hash of the transaction.
    public String transactionIndex;         // integer of the transactions index position in the block.
    public String blockHash;                // hash of the block where this transaction was in.
    public String blockNumber;              // block number where this transaction was in.
    public String from;                     // 20 Bytes - address of the sender.
    public String to;                       // 20 Bytes - address of the receiver. null when its a contract creation transaction.
    public String cumulativeGasUsed;        // The total amount of gas used when this transaction was executed in the block.
    public String gasUsed;                  // The amount of gas used by this specific transaction alone.
    public String contractAddress;          // The contract address created, if the transaction was a contract creation, otherwise  null .
    public JsonRpc.LogFilterElement[] logs;         // Array of log objects, which this transaction generated.
    public String logsBloom;                       // 256 Bytes - Bloom filter for light clients to quickly retrieve related logs.
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String root;  // 32 bytes of post-transaction stateroot (pre Byzantium)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String status;  //  either 1 (success) or 0 (failure) (post Byzantium)

    public TransactionReceiptDTO(Block block, TransactionInfo txInfo){
        TransactionReceipt receipt = txInfo.getReceipt();

        transactionHash = toJsonHex(receipt.getTransaction().getHash());
        transactionIndex = toJsonHex(new Integer(txInfo.getIndex()).longValue());
        cumulativeGasUsed = toJsonHex(receipt.getCumulativeGas());
        gasUsed = toJsonHex(receipt.getGasUsed());
        contractAddress = toJsonHex(receipt.getTransaction().getContractAddress());
        from = toJsonHex(receipt.getTransaction().getSender());
        to = toJsonHex(receipt.getTransaction().getReceiveAddress());
        logs = new JsonRpc.LogFilterElement[receipt.getLogInfoList().size()];
        if (block != null) {
            blockNumber = toJsonHex(block.getNumber());
            blockHash = toJsonHex(txInfo.getBlockHash());
        } else {
            blockNumber = null;
            blockHash = null;
        }

        for (int i = 0; i < logs.length; i++) {
            LogInfo logInfo = receipt.getLogInfoList().get(i);
            logs[i] = new JsonRpc.LogFilterElement(logInfo, block, txInfo.getIndex(),
                    txInfo.getReceipt().getTransaction(), i);
        }
        logsBloom = toJsonHex(receipt.getBloomFilter().getData());

        if (receipt.hasTxStatus()) { // post Byzantium
            root = null;
            status = receipt.isTxStatusOK() ? "0x1" : "0x0";
        } else { // pre Byzantium
            root = toJsonHex(receipt.getPostTxState());
            status = null;
        }
    }
}
