package com.banking.repository;

import com.banking.domain.Account;
import com.banking.entity.AccountEntity;
import com.banking.mapper.AccountMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AccountRepositoryImpl implements AccountRepository {

    private final SpringDataAccountRepository jpaRepository;

    public AccountRepositoryImpl(SpringDataAccountRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Account save(Account account) {
        AccountEntity entity = AccountMapper.toEntity(account);
        AccountEntity saved = jpaRepository.save(entity);
        return AccountMapper.toDomain(saved);
    }

    @Override
    public Optional<Account> findById(String accountId) {
        return jpaRepository.findById(accountId)
                .map(AccountMapper::toDomain);
    }

    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) {
        return jpaRepository.findByAccountNumber(accountNumber)
                .map(AccountMapper::toDomain);
    }
}