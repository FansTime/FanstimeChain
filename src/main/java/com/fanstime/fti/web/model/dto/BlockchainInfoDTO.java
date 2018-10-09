package com.fanstime.fti.web.model.dto;

import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Created by Bynum Williams on 12.07.18.
 */
@Value
@AllArgsConstructor
public class BlockchainInfoDTO {

    private final Long highestBlockNumber;

    private final Long lastBlockNumber;

    /**
     * UTC time in seconds
     */
    private final Long lastBlockTime;

    private final Integer lastBlockTransactions;

    private final Long difficulty;

    // Not used now
    private final Long lastReforkTime;

    private final Long networkHashRate;

    private final Long gasPrice;

    private final NetworkInfoDTO.SyncStatusDTO syncStatus;
}
