package com.example.transaction_authorizer.controllers;

import com.example.transaction_authorizer.dto.TransactionRequestDto;
import com.example.transaction_authorizer.dto.TransactionResponseDto;
import com.example.transaction_authorizer.enumeration.ResponseEnum;
import com.example.transaction_authorizer.services.TransactionAuthorizerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api")
@RestController
public class TransactionAuthorizerController {

    @Autowired
    private TransactionAuthorizerService transactionAuthorizerService;

    @PostMapping("/transactions")
    public ResponseEntity<TransactionResponseDto> processTransaction(@Valid @RequestBody TransactionRequestDto transactionRequestDto) {
        transactionAuthorizerService.processTransaction(transactionRequestDto);

        return new ResponseEntity<>(
                new TransactionResponseDto(ResponseEnum.SUCCESS.getCode(), ResponseEnum.SUCCESS.getMessage()),
                HttpStatus.OK
        );
    }
}
