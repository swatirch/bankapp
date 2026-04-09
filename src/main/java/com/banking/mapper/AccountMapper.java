package com.banking.mapper;


import com.banking.domain.Account;
import com.banking.domain.Transaction;
import com.banking.entity.AccountEntity;
import com.banking.entity.TransactionEntity;

import java.util.List;

public class AccountMapper {

    // Domain → Entity (before saving to DB)
    public static AccountEntity toEntity(Account account) {
        AccountEntity entity = new AccountEntity();
        entity.setAccountId(account.getAccountId());
        entity.setAccountNumber(account.getAccountNumber());
        entity.setOwnerName(account.getOwnerName());
        entity.setAccountType(account.getAccountType());
        entity.setBalance(account.getBalance());
        entity.setAccountStatus(account.getAccountStatus());
        entity.setCreatedAt(account.getCreatedAt());
    
        List<TransactionEntity> transactionEntities = account.getTransactions()
                .stream()
                .map(TransactionMapper::toEntity)
                .toList();
    
        // tell each transaction which account it belongs to
        transactionEntities.forEach(t -> t.setAccount(entity));
    
        entity.setTransactions(transactionEntities);
    
        return entity;
    }

    // Entity → Domain (after loading from DB)
    public static Account toDomain(AccountEntity entity) {
        List<Transaction> transactions = entity.getTransactions()
                .stream()
                .map(TransactionMapper::toDomain)
                .toList();

        return Account.reconstitute(
                entity.getAccountId(),
                entity.getAccountNumber(),
                entity.getOwnerName(),
                entity.getAccountType(),
                entity.getBalance(),
                entity.getAccountStatus(),
                entity.getCreatedAt(),
                transactions
        );
    }

    // Nobody should instantiate this class
    private AccountMapper() {}
}
