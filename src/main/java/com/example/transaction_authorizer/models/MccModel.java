package com.example.transaction_authorizer.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "tb_mcc")
public class MccModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 4)
    private String code;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private CategoryModel category;
}
