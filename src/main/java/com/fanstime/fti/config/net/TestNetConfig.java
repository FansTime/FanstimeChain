package com.fanstime.fti.config.net;

import com.fanstime.fti.config.blockchain.FrontierConfig;
import com.fanstime.fti.config.blockchain.HomesteadConfig;

/**
 * Created by Jay Nicolas on 25.02.2018.
 */
public class TestNetConfig extends BaseNetConfig {
    public TestNetConfig() {
        add(0, new FrontierConfig());
        add(1_150_000, new HomesteadConfig());
    }
}
