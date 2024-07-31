package com.example.transaction_authorizer.models;

import com.example.transaction_authorizer.exceptions.TransactionProcessingException;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "tb_account")
public class AccountModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String account;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<BalanceModel> balances;

    public BalanceModel getBalanceByType(String type) {
        return this.balances.stream()
                .filter(balance -> balance.getCategory().getName().equals(type))
                .findFirst().orElseThrow(TransactionProcessingException::new);
    }

    @Override
    public String toString() {
        return "AccountEntity{" +
                "id=" + id +
                ", account='" + account + '\'' +
                '}';
    }
}
