package com.fanstime.fti.config.net;

import com.fanstime.fti.config.blockchain.*;

/**
 * Created by Jay Nicolas on 25.02.2018.
 */
public class RopstenNetConfig extends BaseNetConfig {

    public RopstenNetConfig() {
        add(0, new HomesteadConfig());
        add(10, new RopstenConfig(new HomesteadConfig()));
        add(1_700_000, new RopstenConfig(new ByzantiumConfig(new DaoHFConfig())));
    }
}
