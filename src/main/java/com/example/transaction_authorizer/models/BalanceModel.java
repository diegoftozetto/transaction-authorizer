package com.example.transaction_authorizer.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_balance", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"account_id", "category_id"})
})
public class BalanceModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private AccountModel account;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private CategoryModel category;
}
