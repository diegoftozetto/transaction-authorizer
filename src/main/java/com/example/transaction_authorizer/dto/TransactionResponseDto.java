package com.example.transaction_authorizer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class TransactionResponseDto {

    private String code;
    private String message;
}
