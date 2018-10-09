package com.fanstime.fti.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


/**
 * Override default umJ config to apply custom configuration.
 * This is entry point for starting umJ core beans.
 *
 * Created by Bynum Williams on 08.09.18.
 */
@Configuration
@ComponentScan(
        basePackages = "com.fanstime.fti",
        excludeFilters = @ComponentScan.Filter(NoAutoscan.class))
public class FtiChainConfig extends CommonConfig {
}
