package com.example.transaction_authorizer.services;

import com.example.transaction_authorizer.dto.TransactionRequestDto;
import com.example.transaction_authorizer.enumeration.CategoryEnum;
import com.example.transaction_authorizer.exceptions.TransactionProcessingException;
import com.example.transaction_authorizer.exceptions.TransactionRejectedException;
import com.example.transaction_authorizer.mocks.AccountModelTemplate;
import com.example.transaction_authorizer.mocks.CategoryModelTemplate;
import com.example.transaction_authorizer.mocks.MccModelTemplate;
import com.example.transaction_authorizer.mocks.MerchantModelTemplate;
import com.example.transaction_authorizer.models.*;
import com.example.transaction_authorizer.repositories.*;
import com.example.transaction_authorizer.services.strategies.CashCategoryStrategy;
import com.example.transaction_authorizer.services.strategies.CategoryStrategyFactory;
import com.example.transaction_authorizer.services.strategies.FoodCategoryStrategy;
import com.example.transaction_authorizer.services.strategies.MealCategoryStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TransactionAuthorizerServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private MerchantRepository merchantRepository;

    @Mock
    private MccRepository mccRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private FoodCategoryStrategy foodCategoryStrategy;

    @Mock
    private MealCategoryStrategy mealCategoryStrategy;

    @Mock
    private CashCategoryStrategy cashCategoryStrategy;

    private CategoryStrategyFactory categoryStrategyFactory;
    private TransactionAuthorizerService transactionAuthorizerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        categoryStrategyFactory = new CategoryStrategyFactory(foodCategoryStrategy, mealCategoryStrategy, cashCategoryStrategy);
        transactionAuthorizerService = new TransactionAuthorizerService(
                accountRepository, transactionRepository, merchantRepository, mccRepository, categoryRepository, categoryStrategyFactory
        );
    }

    @Test
    public void shouldReturnTransactionProcessingExceptionWhenAccountNumberProvidedIsNotFound() {
        TransactionRequestDto transactionRequestDto = new TransactionRequestDto("23432432543", BigDecimal.TEN, "1234", "Test Merchant");

        when(accountRepository.findByAccount(transactionRequestDto.getAccount())).thenReturn(Optional.empty());

        assertThrows(TransactionProcessingException.class, () -> {
            transactionAuthorizerService.processTransaction(transactionRequestDto);
        });
    }

    @Test
    public void shouldUseFoodBalanceWhenInvalidMerchantAndValidMccForFoodCategoryWithFullBalanceAvailableAreProvided() {
        TransactionRequestDto transactionRequestDto = new TransactionRequestDto("23432445435", BigDecimal.TWO, "1234", "Test Merchant");

        CategoryModel categoryFoodModel = CategoryModelTemplate.createCategoryModel(CategoryEnum.FOOD);
        CategoryModel categoryMealModel = CategoryModelTemplate.createCategoryModel(CategoryEnum.MEAL);
        CategoryModel categoryCashModel = CategoryModelTemplate.createCategoryModel(CategoryEnum.CASH);

        MccModel mccModel = MccModelTemplate.createMccModel(transactionRequestDto.getMcc(), categoryFoodModel);
        AccountModel accountModel = AccountModelTemplate.createAccountModel(transactionRequestDto.getAccount());
        accountModel.setBalances(List.of(
                new BalanceModel(1L, BigDecimal.TEN, accountModel, categoryFoodModel),
                new BalanceModel(2L, BigDecimal.ZERO, accountModel, categoryMealModel),
                new BalanceModel(3L, BigDecimal.ZERO, accountModel, categoryCashModel)
        ));

        when(accountRepository.findByAccount(transactionRequestDto.getAccount())).thenReturn(Optional.of(accountModel));
        when(merchantRepository.findByName(transactionRequestDto.getMerchant())).thenReturn(Optional.empty());
        when(mccRepository.findByCode(transactionRequestDto.getMcc())).thenReturn(Optional.of(mccModel));
        when(transactionRepository.save(any(TransactionModel.class))).thenReturn(null);
        when(foodCategoryStrategy.debit(any(AccountModel.class), any(BigDecimal.class))).thenCallRealMethod();

        transactionAuthorizerService.processTransaction(transactionRequestDto);

        verify(mealCategoryStrategy, times(0)).debit(accountModel, transactionRequestDto.getAmount());
        verify(cashCategoryStrategy, times(0)).debit(accountModel, transactionRequestDto.getAmount());
        verify(foodCategoryStrategy).debit(accountModel, transactionRequestDto.getAmount());
    }

    @Test
    public void shouldUseFoodAndCashBalanceWhenInvalidMerchantAndValidMccForFoodCategoryWithInsufficientFoodBalanceAndAvailableCashBalanceAreProvided() {
        TransactionRequestDto transactionRequestDto = new TransactionRequestDto("324324424332", BigDecimal.TEN, "1234", "Test Merchant");

        CategoryModel categoryFoodModel = CategoryModelTemplate.createCategoryModel(CategoryEnum.FOOD);
        CategoryModel categoryMealModel = CategoryModelTemplate.createCategoryModel(CategoryEnum.MEAL);
        CategoryModel categoryCashModel = CategoryModelTemplate.createCategoryModel(CategoryEnum.CASH);

        MccModel mccModel = MccModelTemplate.createMccModel(transactionRequestDto.getMcc(), categoryFoodModel);
        AccountModel accountModel = AccountModelTemplate.createAccountModel(transactionRequestDto.getAccount());
        accountModel.setBalances(List.of(
                new BalanceModel(1L, BigDecimal.TWO, accountModel, categoryFoodModel),
                new BalanceModel(2L, BigDecimal.ZERO, accountModel, categoryMealModel),
                new BalanceModel(3L, BigDecimal.TEN, accountModel, categoryCashModel)
        ));

        when(accountRepository.findByAccount(transactionRequestDto.getAccount())).thenReturn(Optional.of(accountModel));
        when(merchantRepository.findByName(transactionRequestDto.getMerchant())).thenReturn(Optional.empty());
        when(mccRepository.findByCode(transactionRequestDto.getMcc())).thenReturn(Optional.of(mccModel));
        when(transactionRepository.save(any(TransactionModel.class))).thenReturn(null);
        when(foodCategoryStrategy.debit(any(AccountModel.class), any(BigDecimal.class))).thenCallRealMethod();

        transactionAuthorizerService.processTransaction(transactionRequestDto);

        verify(mealCategoryStrategy, times(0)).debit(accountModel, transactionRequestDto.getAmount());
        verify(cashCategoryStrategy, times(0)).debit(accountModel, transactionRequestDto.getAmount());
        verify(foodCategoryStrategy).debit(accountModel, transactionRequestDto.getAmount());
        assertEquals(BigDecimal.ZERO, accountModel.getBalanceByType(CategoryEnum.FOOD.name()).getTotalAmount());
        assertEquals(BigDecimal.ZERO, accountModel.getBalanceByType(CategoryEnum.MEAL.name()).getTotalAmount());
        assertEquals(BigDecimal.TWO, accountModel.getBalanceByType(CategoryEnum.CASH.name()).getTotalAmount());
    }

    @Test
    public void shouldReturnTransactionRejectedExceptionWhenInvalidMerchantAndValidMccForFoodCategoryWithInsufficientFoodBalanceAndInsufficientCashBalanceAreProvided() {
        TransactionRequestDto transactionRequestDto = new TransactionRequestDto("13324234234", BigDecimal.valueOf(50L), "1234", "Test Merchant");

        CategoryModel categoryFoodModel = CategoryModelTemplate.createCategoryModel(CategoryEnum.FOOD);
        CategoryModel categoryMealModel = CategoryModelTemplate.createCategoryModel(CategoryEnum.MEAL);
        CategoryModel categoryCashModel = CategoryModelTemplate.createCategoryModel(CategoryEnum.CASH);

        MccModel mccModel = MccModelTemplate.createMccModel(transactionRequestDto.getMcc(), categoryFoodModel);
        AccountModel accountModel = AccountModelTemplate.createAccountModel(transactionRequestDto.getAccount());
        accountModel.setBalances(List.of(
                new BalanceModel(1L, BigDecimal.TEN, accountModel, categoryFoodModel),
                new BalanceModel(2L, BigDecimal.TWO, accountModel, categoryMealModel),
                new BalanceModel(3L, BigDecimal.TEN, accountModel, categoryCashModel)
        ));

        when(accountRepository.findByAccount(transactionRequestDto.getAccount())).thenReturn(Optional.of(accountModel));
        when(merchantRepository.findByName(transactionRequestDto.getMerchant())).thenReturn(Optional.empty());
        when(mccRepository.findByCode(transactionRequestDto.getMcc())).thenReturn(Optional.of(mccModel));
        when(transactionRepository.save(any(TransactionModel.class))).thenReturn(null);
        when(foodCategoryStrategy.debit(any(AccountModel.class), any(BigDecimal.class))).thenCallRealMethod();

        assertThrows(TransactionRejectedException.class, () -> {
            transactionAuthorizerService.processTransaction(transactionRequestDto);
        });
    }

    @Test
    public void shouldUseMealBalanceWhenInvalidMerchantAndValidMccForMealCategoryWithFullBalanceAvailableAreProvided() {
        TransactionRequestDto transactionRequestDto = new TransactionRequestDto("1312324324", BigDecimal.TWO, "2020", "Merchant Test");

        CategoryModel categoryFoodModel = CategoryModelTemplate.createCategoryModel(CategoryEnum.FOOD);
        CategoryModel categoryMealModel = CategoryModelTemplate.createCategoryModel(CategoryEnum.MEAL);
        CategoryModel categoryCashModel = CategoryModelTemplate.createCategoryModel(CategoryEnum.CASH);

        MccModel mccModel = MccModelTemplate.createMccModel(transactionRequestDto.getMcc(), categoryMealModel);
        AccountModel accountModel = AccountModelTemplate.createAccountModel(transactionRequestDto.getAccount());
        accountModel.setBalances(List.of(
                new BalanceModel(1L, BigDecimal.ZERO, accountModel, categoryFoodModel),
                new BalanceModel(2L, BigDecimal.TEN, accountModel, categoryMealModel),
                new BalanceModel(3L, BigDecimal.ZERO, accountModel, categoryCashModel)
        ));

        when(accountRepository.findByAccount(transactionRequestDto.getAccount())).thenReturn(Optional.of(accountModel));
        when(merchantRepository.findByName(transactionRequestDto.getMerchant())).thenReturn(Optional.empty());
        when(mccRepository.findByCode(transactionRequestDto.getMcc())).thenReturn(Optional.of(mccModel));
        when(transactionRepository.save(any(TransactionModel.class))).thenReturn(null);
        when(mealCategoryStrategy.debit(any(AccountModel.class), any(BigDecimal.class))).thenCallRealMethod();

        transactionAuthorizerService.processTransaction(transactionRequestDto);

        verify(foodCategoryStrategy, times(0)).debit(accountModel, transactionRequestDto.getAmount());
        verify(cashCategoryStrategy, times(0)).debit(accountModel, transactionRequestDto.getAmount());
        verify(mealCategoryStrategy).debit(accountModel, transactionRequestDto.getAmount());
        assertEquals(BigDecimal.ZERO, accountModel.getBalanceByType(CategoryEnum.FOOD.name()).getTotalAmount());
        assertEquals(BigDecimal.valueOf(8L), accountModel.getBalanceByType(CategoryEnum.MEAL.name()).getTotalAmount());
        assertEquals(BigDecimal.ZERO, accountModel.getBalanceByType(CategoryEnum.CASH.name()).getTotalAmount());
    }

    @Test
    public void shouldUseMealAndCashBalanceWhenInvalidMerchantAndValidMccForMealCategoryWithInsufficientMealBalanceAndAvailableCashBalanceAreProvided() {
        TransactionRequestDto transactionRequestDto = new TransactionRequestDto("21331243423", BigDecimal.valueOf(20L), "2020", "Merchant Test");

        CategoryModel categoryFoodModel = CategoryModelTemplate.createCategoryModel(CategoryEnum.FOOD);
        CategoryModel categoryMealModel = CategoryModelTemplate.createCategoryModel(CategoryEnum.MEAL);
        CategoryModel categoryCashModel = CategoryModelTemplate.createCategoryModel(CategoryEnum.CASH);

        MccModel mccModel = MccModelTemplate.createMccModel(transactionRequestDto.getMcc(), categoryMealModel);
        AccountModel accountModel = AccountModelTemplate.createAccountModel(transactionRequestDto.getAccount());
        accountModel.setBalances(List.of(
                new BalanceModel(1L, BigDecimal.ZERO, accountModel, categoryFoodModel),
                new BalanceModel(2L, BigDecimal.TEN, accountModel, categoryMealModel),
                new BalanceModel(3L, BigDecimal.valueOf(15L), accountModel, categoryCashModel)
        ));

        when(accountRepository.findByAccount(transactionRequestDto.getAccount())).thenReturn(Optional.of(accountModel));
        when(merchantRepository.findByName(transactionRequestDto.getMerchant())).thenReturn(Optional.empty());
        when(mccRepository.findByCode(transactionRequestDto.getMcc())).thenReturn(Optional.of(mccModel));
        when(transactionRepository.save(any(TransactionModel.class))).thenReturn(null);
        when(mealCategoryStrategy.debit(any(AccountModel.class), any(BigDecimal.class))).thenCallRealMethod();

        transactionAuthorizerService.processTransaction(transactionRequestDto);

        verify(foodCategoryStrategy, times(0)).debit(accountModel, transactionRequestDto.getAmount());
        verify(cashCategoryStrategy, times(0)).debit(accountModel, transactionRequestDto.getAmount());
        verify(mealCategoryStrategy).debit(accountModel, transactionRequestDto.getAmount());
        assertEquals(BigDecimal.ZERO, accountModel.getBalanceByType(CategoryEnum.FOOD.name()).getTotalAmount());
        assertEquals(BigDecimal.ZERO, accountModel.getBalanceByType(CategoryEnum.MEAL.name()).getTotalAmount());
        assertEquals(BigDecimal.valueOf(5L), accountModel.getBalanceByType(CategoryEnum.CASH.name()).getTotalAmount());
    }

    @Test
    public void shouldReturnTransactionRejectedExceptionWhenInvalidMerchantAndValidMccForMealCategoryWithInsufficientMealBalanceAndInsufficientCashBalanceAreProvided() {
        TransactionRequestDto transactionRequestDto = new TransactionRequestDto("13223423424", BigDecimal.valueOf(50L), "2020", "Merchant Test");

        CategoryModel categoryFoodModel = CategoryModelTemplate.createCategoryModel(CategoryEnum.FOOD);
        CategoryModel categoryMealModel = CategoryModelTemplate.createCategoryModel(CategoryEnum.MEAL);
        CategoryModel categoryCashModel = CategoryModelTemplate.createCategoryModel(CategoryEnum.CASH);

        MccModel mccModel = MccModelTemplate.createMccModel(transactionRequestDto.getMcc(), categoryMealModel);
        AccountModel accountModel = AccountModelTemplate.createAccountModel(transactionRequestDto.getAccount());
        accountModel.setBalances(List.of(
                new BalanceModel(1L, BigDecimal.ZERO, accountModel, categoryFoodModel),
                new BalanceModel(2L, BigDecimal.TEN, accountModel, categoryMealModel),
                new BalanceModel(3L, BigDecimal.valueOf(30L), accountModel, categoryCashModel)
        ));

        when(accountRepository.findByAccount(transactionRequestDto.getAccount())).thenReturn(Optional.of(accountModel));
        when(merchantRepository.findByName(transactionRequestDto.getMerchant())).thenReturn(Optional.empty());
        when(mccRepository.findByCode(transactionRequestDto.getMcc())).thenReturn(Optional.of(mccModel));
        when(transactionRepository.save(any(TransactionModel.class))).thenReturn(null);
        when(mealCategoryStrategy.debit(any(AccountModel.class), any(BigDecimal.class))).thenCallRealMethod();

        assertThrows(TransactionRejectedException.class, () -> {
            transactionAuthorizerService.processTransaction(transactionRequestDto);
        });
    }

    @Test
    public void shouldUseCashBalanceWhenInvalidMerchantAndInvalidMccWithFullCashBalanceAvailableAreProvided() {
        TransactionRequestDto transactionRequestDto = new TransactionRequestDto("13213143234", BigDecimal.TWO, "0000", "Merchant Test");

        CategoryModel categoryFoodModel = CategoryModelTemplate.createCategoryModel(CategoryEnum.FOOD);
        CategoryModel categoryMealModel = CategoryModelTemplate.createCategoryModel(CategoryEnum.MEAL);
        CategoryModel categoryCashModel = CategoryModelTemplate.createCategoryModel(CategoryEnum.CASH);

        AccountModel accountModel = AccountModelTemplate.createAccountModel(transactionRequestDto.getAccount());
        accountModel.setBalances(List.of(
                new BalanceModel(1L, BigDecimal.TWO, accountModel, categoryFoodModel),
                new BalanceModel(2L, BigDecimal.TEN, accountModel, categoryMealModel),
                new BalanceModel(3L, BigDecimal.TEN, accountModel, categoryCashModel)
        ));

        when(accountRepository.findByAccount(transactionRequestDto.getAccount())).thenReturn(Optional.of(accountModel));
        when(merchantRepository.findByName(transactionRequestDto.getMerchant())).thenReturn(Optional.empty());
        when(mccRepository.findByCode(transactionRequestDto.getMcc())).thenReturn(Optional.empty());
        when(categoryRepository.findByName(CategoryEnum.CASH.name())).thenReturn(Optional.of(categoryCashModel));
        when(transactionRepository.save(any(TransactionModel.class))).thenReturn(null);
        when(cashCategoryStrategy.debit(any(AccountModel.class), any(BigDecimal.class))).thenCallRealMethod();

        transactionAuthorizerService.processTransaction(transactionRequestDto);

        verify(foodCategoryStrategy, times(0)).debit(accountModel, transactionRequestDto.getAmount());
        verify(mealCategoryStrategy, times(0)).debit(accountModel, transactionRequestDto.getAmount());
        verify(cashCategoryStrategy).debit(accountModel, transactionRequestDto.getAmount());
        assertEquals(BigDecimal.TWO, accountModel.getBalanceByType(CategoryEnum.FOOD.name()).getTotalAmount());
        assertEquals(BigDecimal.TEN, accountModel.getBalanceByType(CategoryEnum.MEAL.name()).getTotalAmount());
        assertEquals(BigDecimal.valueOf(8L), accountModel.getBalanceByType(CategoryEnum.CASH.name()).getTotalAmount());
    }

    @Test
    public void shouldReturnTransactionRejectedExceptionWhenInvalidMerchantAndInvalidMccWithInsufficientCashBalanceAreProvided() {
        TransactionRequestDto transactionRequestDto = new TransactionRequestDto("1233244324", BigDecimal.valueOf(11L), "0000", "Merchant Test");

        CategoryModel categoryFoodModel = CategoryModelTemplate.createCategoryModel(CategoryEnum.FOOD);
        CategoryModel categoryMealModel = CategoryModelTemplate.createCategoryModel(CategoryEnum.MEAL);
        CategoryModel categoryCashModel = CategoryModelTemplate.createCategoryModel(CategoryEnum.CASH);

        AccountModel accountModel = AccountModelTemplate.createAccountModel(transactionRequestDto.getAccount());
        accountModel.setBalances(List.of(
                new BalanceModel(1L, BigDecimal.TWO, accountModel, categoryFoodModel),
                new BalanceModel(2L, BigDecimal.TEN, accountModel, categoryMealModel),
                new BalanceModel(3L, BigDecimal.TEN, accountModel, categoryCashModel)
        ));

        when(accountRepository.findByAccount(transactionRequestDto.getAccount())).thenReturn(Optional.of(accountModel));
        when(merchantRepository.findByName(transactionRequestDto.getMerchant())).thenReturn(Optional.empty());
        when(mccRepository.findByCode(transactionRequestDto.getMcc())).thenReturn(Optional.empty());
        when(categoryRepository.findByName(CategoryEnum.CASH.name())).thenReturn(Optional.of(categoryCashModel));
        when(transactionRepository.save(any(TransactionModel.class))).thenReturn(null);
        when(cashCategoryStrategy.debit(any(AccountModel.class), any(BigDecimal.class))).thenCallRealMethod();

        assertThrows(TransactionRejectedException.class, () -> {
            transactionAuthorizerService.processTransaction(transactionRequestDto);
        });
    }

    @Test
    public void shouldUseMccBalanceAssociatedWithMerchantWhenMerchantIsValidAndBalanceIsAvailable() {
        TransactionRequestDto transactionRequestDto = new TransactionRequestDto("12314324", BigDecimal.valueOf(15L), "0000", "PADARIA DO ZE               SAO PAULO BR");

        CategoryModel categoryFoodModel = CategoryModelTemplate.createCategoryModel(CategoryEnum.FOOD);
        CategoryModel categoryMealModel = CategoryModelTemplate.createCategoryModel(CategoryEnum.MEAL);
        CategoryModel categoryCashModel = CategoryModelTemplate.createCategoryModel(CategoryEnum.CASH);

        AccountModel accountModel = AccountModelTemplate.createAccountModel(transactionRequestDto.getAccount());
        accountModel.setBalances(List.of(
                new BalanceModel(1L, BigDecimal.valueOf(30L), accountModel, categoryFoodModel),
                new BalanceModel(2L, BigDecimal.TWO, accountModel, categoryMealModel),
                new BalanceModel(3L, BigDecimal.TEN, accountModel, categoryCashModel)
        ));

        MccModel mccModel = MccModelTemplate.createMccModel("1234", categoryFoodModel);
        MerchantModel merchantModel = MerchantModelTemplate.createMerchantModel(transactionRequestDto.getMerchant(), mccModel);

        when(accountRepository.findByAccount(anyString())).thenReturn(Optional.of(accountModel));
        when(merchantRepository.findByName(anyString())).thenReturn(Optional.of(merchantModel));
        when(transactionRepository.save(any(TransactionModel.class))).thenReturn(null);
        when(foodCategoryStrategy.debit(any(AccountModel.class), any(BigDecimal.class))).thenCallRealMethod();

        transactionAuthorizerService.processTransaction(transactionRequestDto);

        verify(mealCategoryStrategy, times(0)).debit(accountModel, transactionRequestDto.getAmount());
        verify(cashCategoryStrategy, times(0)).debit(accountModel, transactionRequestDto.getAmount());
        verify(foodCategoryStrategy).debit(accountModel, transactionRequestDto.getAmount());
        assertEquals(BigDecimal.valueOf(15L), accountModel.getBalanceByType(CategoryEnum.FOOD.name()).getTotalAmount());
        assertEquals(BigDecimal.TWO, accountModel.getBalanceByType(CategoryEnum.MEAL.name()).getTotalAmount());
        assertEquals(BigDecimal.TEN, accountModel.getBalanceByType(CategoryEnum.CASH.name()).getTotalAmount());
    }
}
