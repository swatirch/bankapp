package com.banking.mapper;

import com.banking.domain.Transaction;
import com.banking.entity.TransactionEntity;

public class TransactionMapper {

    public static TransactionEntity toEntity(Transaction transaction){
        TransactionEntity entity = new TransactionEntity();
        entity.setTransactionId(transaction.getTransactionId());
        entity.setAmount(transaction.getAmount());
        entity.setType(transaction.getType());
        entity.setBalanceAfter(transaction.getBalanceAfter());
        entity.setDescription(transaction.getDescription());
        entity.setTimestamp(transaction.getTimestamp());
        return entity;
    }

    public static Transaction toDomain(TransactionEntity entity) {
        return Transaction.reconstitute(
                entity.getTransactionId(),
                entity.getAmount(),
                entity.getType(),
                entity.getBalanceAfter(),
                entity.getDescription(),
                entity.getTimestamp()
        );
    }
            private TransactionMapper() {}

    }
