package com.example.transaction_authorizer.services.strategies;

import com.example.transaction_authorizer.models.AccountModel;
import com.example.transaction_authorizer.enumeration.CategoryEnum;
import com.example.transaction_authorizer.utils.BigDecimalUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class FoodCategoryStrategy implements CategoryStrategy {

    @Override
    public boolean debit(AccountModel accountModel, BigDecimal amount) {
        var food = accountModel.getBalanceByType(CategoryEnum.FOOD.name());
        var cash = accountModel.getBalanceByType(CategoryEnum.CASH.name());

        BigDecimal remainingFoodAmount = food.getTotalAmount().subtract(amount);
        if (BigDecimalUtil.isNonNegative(remainingFoodAmount)) {
            food.setTotalAmount(remainingFoodAmount);
            return true;
        }

        BigDecimal remainingCashAmount = cash.getTotalAmount().subtract(remainingFoodAmount.abs());
        if (BigDecimalUtil.isNonNegative(remainingCashAmount)) {
            food.setTotalAmount(BigDecimal.ZERO);
            cash.setTotalAmount(remainingCashAmount);
            return true;
        }
        return false;
    }
}
