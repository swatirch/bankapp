package com.banking.service;

import com.banking.domain.Account;
import com.banking.domain.AccountType;
import com.banking.exception.AccountNotFoundException;
import com.banking.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository){
        this.accountRepository=accountRepository;
    }

    public Account createAccount(String ownerName, AccountType accountType, BigDecimal initialDeposit){
        Account account = new Account(ownerName,accountType,initialDeposit);
        return accountRepository.save(account);
    }

    public Account deposit(String accountId,BigDecimal amount){
        Account account = findAccountById(accountId);
        account.deposit(amount);
        return accountRepository.save(account);    }

    public Account withdraw(String accountId,BigDecimal amount){
        Account account = findAccountById(accountId);
        account.withdraw(amount);
        return accountRepository.save(account);
    }

    private Account findAccountById(String accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found with id: " + accountId
                ));
    }

    public Account getAccount(String accountId){
        return findAccountById(accountId);
    }
}
