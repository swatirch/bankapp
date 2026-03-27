package com.banking.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Transaction {

    private final String transactionId;
    private final BigDecimal amount;
    private final TransactionType type;
    private final BigDecimal balanceAfter;
    private final String description;
    private final LocalDateTime timestamp;

    // ---- main constructor — used when creating a NEW transaction ----
    public Transaction(BigDecimal amount, TransactionType type,
                       BigDecimal balanceAfter, String description) {
        this.transactionId = UUID.randomUUID().toString();
        this.amount = amount;
        this.type = type;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.timestamp = LocalDateTime.now();
    }
    private Transaction(String transactionId, BigDecimal amount,
                        TransactionType type, BigDecimal balanceAfter,
                        String description, LocalDateTime timestamp) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.type = type;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.timestamp = timestamp;
    }

    public static Transaction reconstitute(
            String transactionId,
            BigDecimal amount,
            TransactionType type,
            BigDecimal balanceAfter,
            String description,
            LocalDateTime timestamp) {
        return new Transaction(
                transactionId,
                amount,
                type,
                balanceAfter,
                description,
                timestamp
        );
    }
    // ---- getters only — Transaction is immutable ----

    public String getTransactionId() { return transactionId; }
    public BigDecimal getAmount() { return amount; }
    public TransactionType getType() { return type; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public String getDescription() { return description; }
    public LocalDateTime getTimestamp() { return timestamp; }
}