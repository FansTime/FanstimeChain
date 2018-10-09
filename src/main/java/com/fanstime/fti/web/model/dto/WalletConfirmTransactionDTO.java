package com.fanstime.fti.web.model.dto;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.math.BigInteger;

/**
 * Created by Bynum Williams on 25.08.18.
 */
@Value
@AllArgsConstructor
public class WalletConfirmTransactionDTO {

    private final String hash;

    private final BigInteger amount;

    private final boolean sending;
}
