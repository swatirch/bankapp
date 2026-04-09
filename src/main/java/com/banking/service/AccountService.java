package com.banking.service;

import com.banking.domain.Account;
import com.banking.domain.AccountType;
import com.banking.entity.AccountEntity;
import com.banking.exception.AccountNotFoundException;
import com.banking.exception.SameAccountTransferException;
import com.banking.mapper.AccountMapper;
import com.banking.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Account createAccount(String ownerName, AccountType accountType, BigDecimal initialDeposit) {
        Account account = new Account(ownerName, accountType, initialDeposit);
        AccountEntity saved = accountRepository.save(AccountMapper.toEntity(account));
        return AccountMapper.toDomain(saved);
    }

    @Transactional
    public Account deposit(String accountId, BigDecimal amount) {
        Account account = findAccountById(accountId);
        account.deposit(amount);
        AccountEntity saved = accountRepository.save(AccountMapper.toEntity(account));
        return AccountMapper.toDomain(saved);
    }

    @Transactional
    public Account withdraw(String accountId, BigDecimal amount) {
        Account account = findAccountById(accountId);
        account.withdraw(amount);
        AccountEntity saved = accountRepository.save(AccountMapper.toEntity(account));
        return AccountMapper.toDomain(saved);
    }

    @Transactional
    public List<Account> transfer(String fromAccountId, String toAccountId, BigDecimal amount) {
        if (fromAccountId.equals(toAccountId)) {
            throw new SameAccountTransferException(
                    "Cannot transfer to the same account"
            );
        }
        Account fromAccount = findAccountById(fromAccountId);
        Account toAccount = findAccountById(toAccountId);

        fromAccount.transferOut(amount);
        toAccount.transferIn(amount);

        AccountEntity savedFrom = accountRepository.save(AccountMapper.toEntity(fromAccount));
        AccountEntity savedTo = accountRepository.save(AccountMapper.toEntity(toAccount));

        return List.of(AccountMapper.toDomain(savedFrom), AccountMapper.toDomain(savedTo));
    }

    @Transactional(readOnly = true)
    public Account getAccount(String accountId) {
        return findAccountById(accountId);
    }

    private Account findAccountById(String accountId) {
        return accountRepository.findById(accountId)
                .map(AccountMapper::toDomain)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found with id: " + accountId
                ));
    }
}