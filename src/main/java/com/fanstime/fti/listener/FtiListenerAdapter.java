package com.fanstime.fti.listener;

import com.fanstime.fti.core.*;
import com.fanstime.fti.core.connect.message.Message;
import com.fanstime.fti.core.connect.server.Channel;

import java.util.List;

public class FtiListenerAdapter implements FtiListener {

    @Override
    public void trace(String output) {
    }

    public void onBlock(Block block, List<TransactionReceipt> receipts) {
    }

    @Override
    public void onBlock(BlockSummary blockSummary) {
        onBlock(blockSummary.getBlock(), blockSummary.getReceipts());
    }

    @Override
    public void onRecvMessage(Channel channel, Message message) {
    }

    @Override
    public void onSendMessage(Channel channel, Message message) {
    }

    @Override
    public void onPeerDisconnect(String host, long port) {
    }

    @Override
    public void onPendingTransactionsReceived(List<Transaction> transactions) {
    }

    @Override
    public void onPendingStateChanged(PendingState pendingState) {
    }

    @Override
    public void onSyncDone(SyncState state) {

    }

    @Override
    public void onNoConnections() {

    }


    @Override
    public void onVMTraceCreated(String transactionHash, String trace) {

    }


    @Override
    public void onTransactionExecuted(TransactionExecutionSummary summary) {

    }

    @Override
    public void onPeerAddedToSyncPool(Channel peer) {

    }

    @Override
    public void onPendingTransactionUpdate(TransactionReceipt txReceipt, PendingTransactionState state, Block block) {

    }
}
