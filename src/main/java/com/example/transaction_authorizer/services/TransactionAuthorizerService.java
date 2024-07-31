package com.example.transaction_authorizer.services;

import com.example.transaction_authorizer.dto.TransactionRequestDto;
import com.example.transaction_authorizer.models.CategoryModel;
import com.example.transaction_authorizer.models.MccModel;
import com.example.transaction_authorizer.models.TransactionModel;
import com.example.transaction_authorizer.enumeration.CategoryEnum;
import com.example.transaction_authorizer.exceptions.TransactionProcessingException;
import com.example.transaction_authorizer.exceptions.TransactionRejectedException;
import com.example.transaction_authorizer.repositories.*;
import com.example.transaction_authorizer.services.strategies.CategoryStrategy;
import com.example.transaction_authorizer.services.strategies.CategoryStrategyFactory;
import com.example.transaction_authorizer.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
public class TransactionAuthorizerService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final MerchantRepository merchantRepository;
    private final MccRepository mccRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryStrategyFactory categoryStrategyFactory;

    TransactionAuthorizerService(AccountRepository accountRepository, TransactionRepository transactionRepository, MerchantRepository merchantRepository, MccRepository mccRepository, CategoryRepository categoryRepository, CategoryStrategyFactory categoryStrategyFactory) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.merchantRepository = merchantRepository;
        this.mccRepository = mccRepository;
        this.categoryRepository = categoryRepository;
        this.categoryStrategyFactory = categoryStrategyFactory;
    }

    private static final Logger logger = LoggerFactory.getLogger(TransactionAuthorizerService.class);

    public void processTransaction(TransactionRequestDto transactionRequestDto) {
        logger.info("Initializing transaction authorization {}", transactionRequestDto);
        var account = accountRepository.findByAccount(transactionRequestDto.getAccount())
                .orElseThrow(TransactionProcessingException::new);

        logger.info("Searching for merchant: {}", transactionRequestDto.getMerchant());
        var merchant = merchantRepository.findByName(StringUtil.removeWhitespace(transactionRequestDto.getMerchant()))
                .orElse(null);
        var mcc = !ObjectUtils.isEmpty(merchant) ? merchant.getMcc() : getMccByCode(transactionRequestDto.getMcc());
        var category = !ObjectUtils.isEmpty(mcc) ? mcc.getCategory() : getCategoryCash();

        logger.info("Debiting account: {}", account);
        CategoryStrategy strategy = categoryStrategyFactory.getStrategy(category.getName());
        if (!strategy.debit(account, transactionRequestDto.getAmount())) {
            throw new TransactionRejectedException();
        }

        var transaction = TransactionModel.builder()
                .account(account)
                .mcc(mcc)
                .merchant(merchant)
                .amount(transactionRequestDto.getAmount())
                .category(category)
                .build();

        logger.info("Saving transaction: {}", transaction);
        transactionRepository.save(transaction);
    }

    private MccModel getMccByCode(String mcc) {
        return mccRepository.findByCode(mcc)
                .orElse(null);
    }

    private CategoryModel getCategoryCash() {
        return categoryRepository.findByName(CategoryEnum.CASH.name())
                .orElseThrow(TransactionProcessingException::new);
    }
}
