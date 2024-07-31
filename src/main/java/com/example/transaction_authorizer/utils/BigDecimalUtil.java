package com.example.transaction_authorizer.utils;

import java.math.BigDecimal;

public class BigDecimalUtil {

    public static boolean isNonNegative(BigDecimal value) {
        return value.compareTo(BigDecimal.ZERO) >= 0;
    }
}
