package com.fanstime.fti.web.model.dto;

import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Created by Bynum Williams on 09.08.18.
 */
@Value
@AllArgsConstructor
public class BlockInfo {

    private final long blockNumber;

    private final String blockHash;

    private final String parentHash;

    private final long difficulty;
}
