package com.fanstime.fti.web.jsonrpc;

import com.fanstime.fti.core.Block;
import com.fanstime.fti.core.TransactionInfo;

import static com.fanstime.fti.web.jsonrpc.TypeConverter.toJsonHex;

/**
 * Created by Jay Nicolas on 05.08.2018.
 */
public class TransactionReceiptDTOExt extends TransactionReceiptDTO {

    public String returnData;
    public String error;

    public TransactionReceiptDTOExt(Block block, TransactionInfo txInfo) {
        super(block, txInfo);
        returnData = toJsonHex(txInfo.getReceipt().getExecutionResult());
        error = txInfo.getReceipt().getError();
    }
}
