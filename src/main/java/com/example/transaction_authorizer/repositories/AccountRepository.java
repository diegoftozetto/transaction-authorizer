package com.example.transaction_authorizer.repositories;

import com.example.transaction_authorizer.models.AccountModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountModel, Long> {
    Optional<AccountModel> findByAccount(String account);
}
