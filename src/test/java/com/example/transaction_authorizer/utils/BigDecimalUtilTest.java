package com.example.transaction_authorizer.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class BigDecimalUtilTest {

    @ParameterizedTest
    @MethodSource("dataNumbers")
    public void shouldReturnTrueOrFalseWhenNumberIsPositiveOrNegative(BigDecimal actual, boolean expected) {
        assertEquals(expected, BigDecimalUtil.isNonNegative(actual));
    }

    private static Stream<Arguments> dataNumbers() {
        return Stream.of(
                arguments(BigDecimal.valueOf(-11L), false),
                arguments(BigDecimal.TWO, true),
                arguments(BigDecimal.TEN, true)
        );
    }
}
