package com.banking.repository;

import com.banking.domain.Account;

import java.util.Optional;

public interface AccountRepository {
    Account save(Account account);
    Optional<Account> findById(String accountId);
    Optional<Account> findByAccountNumber(String accountNumber);
}
