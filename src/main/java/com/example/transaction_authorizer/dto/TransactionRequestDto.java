package com.example.transaction_authorizer.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class TransactionRequestDto {

    @NotBlank
    private String account;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotBlank
    @Length(min = 4, max = 4)
    private String mcc;

    @NotBlank
    private String merchant;
}
