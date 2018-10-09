package com.fanstime.fti.util.blockchain;

import java.math.BigInteger;

/**
 * Created by Jay Nicolas on 22.06.2018.
 */
public class FtiUtil {
    public enum Unit {
        WEI(BigInteger.valueOf(1)),
        GWEI(BigInteger.valueOf(1_000_000_000)),
        SZABO(BigInteger.valueOf(1_000_000_000_000L)),
        FINNEY(BigInteger.valueOf(1_000_000_000_000_000L)),
        FTI(BigInteger.valueOf(1_000_000_000_000_000_000L));

        BigInteger i;
        Unit(BigInteger i) {
            this.i = i;
        }
    }

    public static BigInteger convert(long amount, Unit unit) {
        return BigInteger.valueOf(amount).multiply(unit.i);
    }
}
