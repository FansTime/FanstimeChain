package com.fanstime.fti.web.model.dto;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bynum Williams on 24.08.18.
 */
@Value
@AllArgsConstructor
public class WalletInfoDTO {

    private final BigInteger totalAmount;

    private final List<WalletAddressDTO> addresses = new ArrayList();
}
