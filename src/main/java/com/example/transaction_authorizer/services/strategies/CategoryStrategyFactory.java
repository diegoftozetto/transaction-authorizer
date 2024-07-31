package com.example.transaction_authorizer.services.strategies;


import com.example.transaction_authorizer.enumeration.CategoryEnum;
import com.example.transaction_authorizer.repositories.MccRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CategoryStrategyFactory {

    private final Map<String, CategoryStrategy> strategies = new HashMap<>();

    @Autowired
    public CategoryStrategyFactory(FoodCategoryStrategy foodBalanceStrategy, MealCategoryStrategy mealBalanceStrategy, CashCategoryStrategy cashBalanceStrategy) {
        strategies.put(CategoryEnum.FOOD.name(), foodBalanceStrategy);
        strategies.put(CategoryEnum.MEAL.name(), mealBalanceStrategy);
        strategies.put(CategoryEnum.CASH.name(), cashBalanceStrategy);
    }

    public CategoryStrategy getStrategy(String mcc) {
        return strategies.getOrDefault(mcc, strategies.get(CategoryEnum.CASH.name()));
    }
}
