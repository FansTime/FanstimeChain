package com.fanstime.fti.web.model.dto;

import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Status of Miner
 */
@Value
@AllArgsConstructor
public class MinerStatusDTO {

    private final String status;
}
