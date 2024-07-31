package com.example.transaction_authorizer.mocks;

import com.example.transaction_authorizer.models.CategoryModel;
import com.example.transaction_authorizer.models.MccModel;

public class MccModelTemplate {

    public static MccModel createMccModel(String code, CategoryModel categoryModel) {
        MccModel mccModel = new MccModel();
        mccModel.setId(1L);
        mccModel.setCode(code);
        mccModel.setCategory(categoryModel);
        return mccModel;
    }
}
