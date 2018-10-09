package com.fanstime.fti.web.model.dto;

import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Created by Bynum Williams on 10.08.18.
 */
@Value
@AllArgsConstructor
public class MinerDTO {

    private final String address;

    private final Integer count;
}
