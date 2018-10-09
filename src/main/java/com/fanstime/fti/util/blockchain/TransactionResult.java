package com.fanstime.fti.util.blockchain;

import com.fanstime.fti.core.TransactionExecutionSummary;
import com.fanstime.fti.core.TransactionReceipt;

/**
 * Created by Jay Nicolas on 26.07.2018.
 */
public class TransactionResult {
    TransactionReceipt receipt;
    TransactionExecutionSummary executionSummary;

    public boolean isIncluded() {
        return receipt != null;
    }

    public TransactionReceipt getReceipt() {
        return receipt;
    }

    public TransactionExecutionSummary getExecutionSummary() {
        return executionSummary;
    }
}
