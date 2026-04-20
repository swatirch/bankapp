package com.banking.dto.response;

import com.banking.domain.Transaction;
import com.banking.domain.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        String transactionId,
        BigDecimal amount,
        TransactionType type,
        BigDecimal balanceAfter,
        String description,
        LocalDateTime timestamp) {
    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(
                t.getTransactionId(),
                t.getAmount(),
                t.getType(),
                t.getBalanceAfter(),
                t.getDescription(),
                t.getTimestamp());
    }
}