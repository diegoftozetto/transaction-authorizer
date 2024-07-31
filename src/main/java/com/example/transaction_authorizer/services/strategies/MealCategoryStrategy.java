package com.example.transaction_authorizer.services.strategies;

import com.example.transaction_authorizer.models.AccountModel;
import com.example.transaction_authorizer.enumeration.CategoryEnum;
import com.example.transaction_authorizer.utils.BigDecimalUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class MealCategoryStrategy implements CategoryStrategy {

    @Override
    public boolean debit(AccountModel accountModel, BigDecimal amount) {
        var meal = accountModel.getBalanceByType(CategoryEnum.MEAL.name());
        var cash = accountModel.getBalanceByType(CategoryEnum.CASH.name());

        BigDecimal remainingFoodAmount = meal.getTotalAmount().subtract(amount);
        if (BigDecimalUtil.isNonNegative(remainingFoodAmount)) {
            meal.setTotalAmount(remainingFoodAmount);
            return true;
        }

        BigDecimal remainingCashAmount = cash.getTotalAmount().subtract(remainingFoodAmount.abs());
        if (BigDecimalUtil.isNonNegative(remainingCashAmount)) {
            meal.setTotalAmount(BigDecimal.ZERO);
            cash.setTotalAmount(remainingCashAmount);
            return true;
        }
        return false;
    }
}
