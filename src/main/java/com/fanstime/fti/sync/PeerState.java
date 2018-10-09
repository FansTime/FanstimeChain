package com.fanstime.fti.sync;

public enum PeerState {

    // Common
    IDLE,
    HEADER_RETRIEVING,
    BLOCK_RETRIEVING,
    NODE_RETRIEVING,
    RECEIPT_RETRIEVING,

    // Peer
    DONE_HASH_RETRIEVING
}
