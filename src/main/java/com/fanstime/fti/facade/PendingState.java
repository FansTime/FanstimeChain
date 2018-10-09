package com.fanstime.fti.facade;

import com.fanstime.fti.core.*;

import java.util.List;

public interface PendingState {

    /**
     * @return pending state repository
     */
    com.fanstime.fti.core.Repository getRepository();

    /**
     * @return list of pending transactions
     */
    List<Transaction> getPendingTransactions();
}
