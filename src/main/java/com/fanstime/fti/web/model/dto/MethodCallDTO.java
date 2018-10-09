package com.fanstime.fti.web.model.dto;

import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Created by Bynum Williams on 22.07.18.
 */
@Value
@AllArgsConstructor
public class MethodCallDTO {

    private final String methodName;

    private final Long count;

    private final Long lastTime;

    private final String lastResult;

    private final String curl;
}
