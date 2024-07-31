package com.example.transaction_authorizer.mocks;

import com.example.transaction_authorizer.models.MccModel;
import com.example.transaction_authorizer.models.MerchantModel;

public class MerchantModelTemplate {

    public static MerchantModel createMerchantModel(String name, MccModel mccModel) {
        MerchantModel merchantModel = new MerchantModel();
        merchantModel.setId(1L);
        merchantModel.setName(name);
        merchantModel.setMcc(mccModel);
        return merchantModel;
    }
}
