package com.fanstime.fti.core.connect.submit;

import com.fanstime.fti.core.Transaction;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TransactionExecutor {

    static {
        instance = new TransactionExecutor();
    }

    public static TransactionExecutor instance;
    private ExecutorService executor = Executors.newFixedThreadPool(1);

    public Future<List<Transaction>> submitTransaction(TransactionTask task) {
        return executor.submit(task);
    }
}
