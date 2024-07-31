package com.example.transaction_authorizer.repositories;

import com.example.transaction_authorizer.models.CategoryModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryModel, Long> {
    Optional<CategoryModel> findByName(String name);
}
