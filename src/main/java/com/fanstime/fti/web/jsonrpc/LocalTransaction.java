package com.fanstime.fti.web.jsonrpc;

import com.fanstime.fti.core.Transaction;

/**
 * Transaction for making constant calls without changing network state.
 *
 * Created by Bynum Williams on 22.02.18.
 */
public class LocalTransaction extends Transaction {

    public LocalTransaction(byte[] rawData) {
        super(rawData);
    }

    public void setSender(byte[] sendAddress) {
        this.sendAddress = sendAddress;
    }
}
