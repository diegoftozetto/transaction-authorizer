package com.example.transaction_authorizer.services.strategies;

import com.example.transaction_authorizer.models.AccountModel;
import com.example.transaction_authorizer.enumeration.CategoryEnum;
import com.example.transaction_authorizer.utils.BigDecimalUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CashCategoryStrategy implements CategoryStrategy {

    @Override
    public boolean debit(AccountModel accountModel, BigDecimal amount) {
        var cash = accountModel.getBalanceByType(CategoryEnum.CASH.name());

        BigDecimal remainingCashAmount = cash.getTotalAmount().subtract(amount);
        if (BigDecimalUtil.isNonNegative(remainingCashAmount)) {
            cash.setTotalAmount(remainingCashAmount);
            return true;
        }
        return false;
    }
}
