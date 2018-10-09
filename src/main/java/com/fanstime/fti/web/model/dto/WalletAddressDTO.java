package com.fanstime.fti.web.model.dto;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.math.BigInteger;

/**
 * Created by Bynum Williams on 24.08.18.
 */
@Value
@AllArgsConstructor
public class WalletAddressDTO {

    private final String name;

    private final String publicAddress;

    private final BigInteger amount;

    private final BigInteger pendingAmount;

    private final boolean hasKeystoreKey;
}
