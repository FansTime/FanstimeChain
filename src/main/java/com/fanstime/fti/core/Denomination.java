package com.fanstime.fti.core;

import java.math.BigInteger;

public enum Denomination {

    WEI(newBigInt(0)),
    SZABO(newBigInt(12)),
    FINNEY(newBigInt(15)),
    FTI(newBigInt(18));

    private BigInteger amount;

    private Denomination(BigInteger value) {
        this.amount = value;
    }

    public BigInteger value() {
        return amount;
    }

    public long longValue() {
        return value().longValue();
    }

    private static BigInteger newBigInt(int value) {
        return BigInteger.valueOf(10).pow(value);
    }

    public static String toFriendlyString(BigInteger value) {
        if (value.compareTo(FTI.value()) == 1 || value.compareTo(FTI.value()) == 0) {
            return Float.toString(value.divide(FTI.value()).floatValue()) +  " FTI";
        }
        else if(value.compareTo(FINNEY.value()) == 1 || value.compareTo(FINNEY.value()) == 0) {
            return Float.toString(value.divide(FINNEY.value()).floatValue()) +  " FINNEY";
        }
        else if(value.compareTo(SZABO.value()) == 1 || value.compareTo(SZABO.value()) == 0) {
            return Float.toString(value.divide(SZABO.value()).floatValue()) +  " SZABO";
        }
        else
            return Float.toString(value.divide(WEI.value()).floatValue()) +  " WEI";
    }
}
