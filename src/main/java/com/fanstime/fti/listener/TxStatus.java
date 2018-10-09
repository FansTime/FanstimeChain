package com.fanstime.fti.listener;

/**
 * Created by Jay Nicolas on 15.07.2018.
 */
public class TxStatus {

    public static final TxStatus REJECTED = new TxStatus(0);
    public static final TxStatus PENDING = new TxStatus(0);
    public static TxStatus getConfirmed(int blocks) {
        return new TxStatus(blocks);
    }

    public final int confirmed;

    private TxStatus(int confirmed) {
        this.confirmed = confirmed;
    }

    @Override
    public String toString() {
        if (this == REJECTED) return "REJECTED";
        if (this == PENDING) return "PENDING";
        return "CONFIRMED_" + confirmed;
    }
}
