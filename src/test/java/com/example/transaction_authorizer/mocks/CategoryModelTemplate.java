package com.example.transaction_authorizer.mocks;

import com.example.transaction_authorizer.enumeration.CategoryEnum;
import com.example.transaction_authorizer.models.CategoryModel;

public class CategoryModelTemplate {

    public static CategoryModel createCategoryModel(CategoryEnum categoryEnum) {
        CategoryModel categoryModel = new CategoryModel();
        categoryModel.setId(1L);
        categoryModel.setName(categoryEnum.name());
        return categoryModel;
    }
}
