package com.example.transaction_authorizer.enumeration;

import lombok.Getter;

@Getter
public enum ResponseEnum {
    SUCCESS("00", "Transação realizada com sucesso."),
    REJECTED("51", "Transação rejeitada, saldo insuficiente."),
    ERROR("07", "Transação não pode ser processada.");

    private final String code;
    private final String message;

    ResponseEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
