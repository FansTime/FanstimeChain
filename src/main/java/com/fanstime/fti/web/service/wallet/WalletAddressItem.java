package com.fanstime.fti.web.service.wallet;

import lombok.NoArgsConstructor;

/**
 * Created by Bynum Williams on 26.08.16.
 */
@NoArgsConstructor
public class WalletAddressItem {

    public String address;

    public String name;

    public WalletAddressItem(String address, String name) {
        this.address = address;
        this.name = name;
    }
}
