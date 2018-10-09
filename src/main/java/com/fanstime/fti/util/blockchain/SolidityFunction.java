package com.fanstime.fti.util.blockchain;

import com.fanstime.fti.core.CallTransaction;

/**
 * Created by Jay Nicolas on 02.03.2018.
 */
public interface SolidityFunction {

    SolidityContract getContract();

    CallTransaction.Function getInterface();
}
