package com.example.transaction_authorizer.repositories;

import com.example.transaction_authorizer.models.MerchantModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MerchantRepository extends JpaRepository<MerchantModel, Long> {
    Optional<MerchantModel> findByName(String name);
}
