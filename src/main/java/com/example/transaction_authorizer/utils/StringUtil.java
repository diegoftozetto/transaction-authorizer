package com.example.transaction_authorizer.utils;

public class StringUtil {

    public static String removeWhitespace(String value) {
        return value.replaceAll("\\s+", " ").trim();
    }
}
