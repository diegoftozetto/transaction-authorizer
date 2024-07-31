package com.example.transaction_authorizer.mocks;

import com.example.transaction_authorizer.models.AccountModel;

public class AccountModelTemplate {


    public static AccountModel createAccountModel(String accountNumber) {
        AccountModel accountModel = new AccountModel();
        accountModel.setId(1L);
        accountModel.setAccount(accountNumber);
        return accountModel;
    }
}
