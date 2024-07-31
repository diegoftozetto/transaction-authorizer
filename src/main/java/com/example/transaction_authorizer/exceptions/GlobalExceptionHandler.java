package com.example.transaction_authorizer.exceptions;

import com.example.transaction_authorizer.dto.TransactionResponseDto;
import com.example.transaction_authorizer.enumeration.ResponseEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<TransactionResponseDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        logger.error(e.getMessage(), e);
        return new ResponseEntity<>(
                new TransactionResponseDto(ResponseEnum.ERROR.getCode(), ResponseEnum.ERROR.getMessage()),
                HttpStatus.OK
        );
    }

    @ExceptionHandler(TransactionRejectedException.class)
    public ResponseEntity<TransactionResponseDto> handleTransactionRejectedException(TransactionRejectedException e) {
        logger.error(e.getMessage(), e);
        return new ResponseEntity<>(
                new TransactionResponseDto(ResponseEnum.REJECTED.getCode(), ResponseEnum.REJECTED.getMessage()),
                HttpStatus.OK
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<TransactionResponseDto> handleException(Exception e) {
        logger.error(e.getMessage(), e);
        return new ResponseEntity<>(
                new TransactionResponseDto(ResponseEnum.ERROR.getCode(), ResponseEnum.ERROR.getMessage()),
                HttpStatus.OK
        );
    }
}