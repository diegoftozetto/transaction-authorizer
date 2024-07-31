package com.example.transaction_authorizer.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringUtilTest {

    @Test
    public void deveRemoverEspacosEmBrancoQuandoInformadoValorComEspacos() {
        String input = "  Espaço   Em     Branco    ";
        String expected = "Espaço Em Branco";
        String actual = StringUtil.removeWhitespace(input);
        assertEquals(expected, actual);
    }
}
