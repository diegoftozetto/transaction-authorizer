package com.example.transaction_authorizer.repositories;

import com.example.transaction_authorizer.models.MccModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MccRepository extends JpaRepository<MccModel, Long> {
    Optional<MccModel> findByCode(String code);
}
