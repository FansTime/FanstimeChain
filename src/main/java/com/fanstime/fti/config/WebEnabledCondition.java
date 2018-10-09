package com.fanstime.fti.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class WebEnabledCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context,
                           AnnotatedTypeMetadata metadata) {

        return WebEnabledCondition.matches();
    }

    public static boolean matches() {
        return ChainProperties.DEFAULT.isWebEnabled();
    }
}
