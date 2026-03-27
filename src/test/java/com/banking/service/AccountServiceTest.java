package com.banking.service;

import com.banking.domain.Account;
import com.banking.domain.AccountType;
import com.banking.exception.AccountNotFoundException;
import com.banking.exception.InsufficientBalanceException;
import com.banking.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
 class AccountServiceTest {

    @Mock
    AccountRepository accountRepository;

    @InjectMocks
    AccountService accountService;

    @Test
    void shouldCreateAccountAndSaveIt(){
        Account account = new Account("John", AccountType.SAVINGS,new BigDecimal("1000.00"));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account created = accountService.createAccount("John",AccountType.SAVINGS,new BigDecimal("1000.00"));
        assertNotNull(created);
        assertEquals("John",created.getOwnerName());
        verify(accountRepository,times(1)).save(any(Account.class));
    }

    @Test
    void shouldDepositMoneyInAccount() {
        Account account = new Account("John", AccountType.SAVINGS,
                new BigDecimal("1000.00"));
        when(accountRepository.findById(account.getAccountId()))
                .thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class)))
                .thenReturn(account);

        Account result = accountService.deposit(account.getAccountId(),
                new BigDecimal("500.00"));

        assertEquals(0, result.getBalance().compareTo(new BigDecimal("1500.00")));
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void shouldWithdrawMoneyFromAccount() {
        Account account = new Account("John", AccountType.SAVINGS,
                new BigDecimal("1000.00"));
        when(accountRepository.findById(account.getAccountId()))
                .thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class)))
                .thenReturn(account);

        Account result = accountService.withdraw(account.getAccountId(),
                new BigDecimal("200.00"));

        assertEquals(0, result.getBalance().compareTo(new BigDecimal("800.00")));
        verify(accountRepository, times(1)).save(any(Account.class));
    }


    @Test
    void shouldReturnAccountById(){
        Account account = new Account("John", AccountType.SAVINGS, new BigDecimal("1000.00"));
        when(accountRepository.findById(account.getAccountId())).thenReturn(Optional.of(account));
        Account found = accountService.getAccount(account.getAccountId());
        assertEquals("John",found.getOwnerName());
        verify(accountRepository,times(1)).findById(account.getAccountId());
    }

    @Test
    void shouldThrowExceptionWhenDepositingToNonExistentAccount(){
        when(accountRepository.findById("bad-id")).thenReturn(Optional.empty());
        BigDecimal amount = new BigDecimal("500.00");
        assertThrows(AccountNotFoundException.class,()->accountService.deposit("bad-id",amount));
    }

    @Test
    void shouldThrowExceptionWhenWithdrawingMoreThanBalance(){
        Account account = new Account("John", AccountType.SAVINGS, new BigDecimal("1000.00"));
        String accountId = account.getAccountId();  // ← extract ID outside lambda
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        BigDecimal amount =  new BigDecimal("1500.00");
        assertThrows(InsufficientBalanceException.class,()->accountService.withdraw(accountId,amount));
    }
}
