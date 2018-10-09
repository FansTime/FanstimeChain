package com.fanstime.fti.config.net;

import com.fanstime.fti.config.blockchain.Eip150HFConfig;
import com.fanstime.fti.config.blockchain.Eip160HFConfig;
import com.fanstime.fti.config.blockchain.MordenConfig;

/**
 * Created by Jay Nicolas on 25.02.2018.
 */
public class MordenNetConfig extends BaseNetConfig {

    public MordenNetConfig() {
        add(0, new MordenConfig.Frontier());
        add(494_000, new MordenConfig.Homestead());
        add(1_783_000, new Eip150HFConfig(new MordenConfig.Homestead()));
        add(1_885_000, new Eip160HFConfig(new MordenConfig.Homestead()));

    }
}
