package com.example.transaction_authorizer.services.strategies;

import com.example.transaction_authorizer.models.AccountModel;

import java.math.BigDecimal;

public interface CategoryStrategy {
    boolean debit(AccountModel accountModel, BigDecimal amount);
}
