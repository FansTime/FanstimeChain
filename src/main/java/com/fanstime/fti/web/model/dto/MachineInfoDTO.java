package com.fanstime.fti.web.model.dto;

import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Created by Bynum Williams on 11.07.18.
 */
@Value
@AllArgsConstructor
public class MachineInfoDTO {

    /**
     * Percentage 0..100
     */
    private final Integer cpuUsage;

    /**
     * In bytes.
     */
    private final Long memoryFree;

    /**
     * In bytes.
     */
    private final Long memoryTotal;

    /**
     * In bytes.
     */
    private final  Long dbSize;

    /**
     * In bytes.
     */
    private final  Long freeSpace;

}
