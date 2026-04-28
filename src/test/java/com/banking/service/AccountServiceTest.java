package com.banking.service;

import com.banking.domain.Account;
import com.banking.domain.AccountStatus;
import com.banking.domain.AccountType;
import com.banking.entity.AccountEntity;
import com.banking.exception.AccountNotFoundException;
import com.banking.exception.InsufficientBalanceException;
import com.banking.exception.SameAccountTransferException;
import com.banking.repository.AccountRepository;
import com.banking.repository.TransactionRepository;
import com.banking.kafka.TransactionEventProducer;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionEventProducer eventProducer;

    @Mock
    private java.util.concurrent.Executor queryExecutor;

    @InjectMocks
    private AccountService accountService;

    // builds a realistic AccountEntity the way the DB would return one
    private AccountEntity buildEntity(String owner, BigDecimal balance) {
        AccountEntity entity = new AccountEntity();
        entity.setAccountId(UUID.randomUUID().toString());
        entity.setAccountNumber("ACC" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        entity.setOwnerName(owner);
        entity.setAccountType(AccountType.SAVINGS);
        entity.setBalance(balance);
        entity.setAccountStatus(AccountStatus.ACTIVE);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setTransactions(new ArrayList<>());
        return entity;
    }

    @Test
    void shouldCreateAccountAndSaveIt() {
        AccountEntity entity = buildEntity("John", new BigDecimal("1000.00"));
        when(accountRepository.save(any(AccountEntity.class))).thenReturn(entity);

        Account created = accountService.createAccount("John", AccountType.SAVINGS, new BigDecimal("1000.00"),
                "user-123");

        assertNotNull(created);
        assertEquals("John", created.getOwnerName());
        verify(accountRepository, times(1)).save(any(AccountEntity.class));
    }

    @Test
    void shouldDepositMoneyInAccount() {
        AccountEntity entity = buildEntity("John", new BigDecimal("1000.00"));
        when(accountRepository.findById(entity.getAccountId())).thenReturn(Optional.of(entity));
        when(accountRepository.save(any(AccountEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Account result = accountService.deposit(entity.getAccountId(), new BigDecimal("500.00"));

        assertEquals(0, result.getBalance().compareTo(new BigDecimal("1500.00")));
        verify(accountRepository, times(1)).save(any(AccountEntity.class));
    }

    @Test
    void shouldWithdrawMoneyFromAccount() {
        AccountEntity entity = buildEntity("John", new BigDecimal("1000.00"));
        when(accountRepository.findById(entity.getAccountId())).thenReturn(Optional.of(entity));
        when(accountRepository.save(any(AccountEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Account result = accountService.withdraw(entity.getAccountId(), new BigDecimal("200.00"));

        assertEquals(0, result.getBalance().compareTo(new BigDecimal("800.00")));
        verify(accountRepository, times(1)).save(any(AccountEntity.class));
    }

    @Test
    void shouldReturnAccountById() {
        AccountEntity entity = buildEntity("John", new BigDecimal("1000.00"));
        when(accountRepository.findById(entity.getAccountId())).thenReturn(Optional.of(entity));

        Account found = accountService.getAccount(entity.getAccountId());

        assertEquals("John", found.getOwnerName());
        verify(accountRepository, times(1)).findById(entity.getAccountId());
    }

    @Test
    void shouldThrowExceptionWhenDepositingToNonExistentAccount() {
        when(accountRepository.findById("bad-id")).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> accountService.deposit("bad-id", new BigDecimal("500.00")));
    }

    @Test
    void shouldThrowExceptionWhenWithdrawingMoreThanBalance() {
        AccountEntity entity = buildEntity("John", new BigDecimal("1000.00"));
        when(accountRepository.findById(entity.getAccountId())).thenReturn(Optional.of(entity));

        assertThrows(InsufficientBalanceException.class,
                () -> accountService.withdraw(entity.getAccountId(), new BigDecimal("1500.00")));
    }

    @Nested
    class TransferTest {

        private AccountEntity buildEntity(String owner, BigDecimal balance) {
            AccountEntity e = new AccountEntity();
            e.setAccountId(UUID.randomUUID().toString());
            e.setAccountNumber("ACC" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            e.setOwnerName(owner);
            e.setAccountType(AccountType.SAVINGS);
            e.setBalance(balance);
            e.setAccountStatus(AccountStatus.ACTIVE);
            e.setCreatedAt(LocalDateTime.now());
            e.setTransactions(new ArrayList<>());
            return e;
        }

        @Test
        void shouldTransferBetweenTwoAccounts() {
            AccountEntity from = buildEntity("Alice", new BigDecimal("1000.00"));
            AccountEntity to = buildEntity("Bob", new BigDecimal("500.00"));

            when(accountRepository.findById(from.getAccountId())).thenReturn(Optional.of(from));
            when(accountRepository.findById(to.getAccountId())).thenReturn(Optional.of(to));
            when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            var result = accountService.transfer(
                    from.getAccountId(), to.getAccountId(), new BigDecimal("200.00"));

            assertEquals(2, result.size());
            assertEquals(0, new BigDecimal("800.00").compareTo(result.get(0).getBalance()));
            assertEquals(0, new BigDecimal("700.00").compareTo(result.get(1).getBalance()));
            verify(accountRepository, times(2)).save(any());
        }

        @Test
        void shouldThrowWhenTransferringToSameAccount() {
            assertThrows(SameAccountTransferException.class,
                    () -> accountService.transfer("same-id", "same-id", new BigDecimal("100.00")));
        }

        @Test
        void shouldThrowWhenFromAccountNotFound() {
            when(accountRepository.findById("bad-id")).thenReturn(Optional.empty());
            assertThrows(AccountNotFoundException.class,
                    () -> accountService.transfer("bad-id", "other-id", new BigDecimal("100.00")));
        }

        @Test
        void shouldThrowWhenToAccountNotFound() {
            AccountEntity from = buildEntity("Alice", new BigDecimal("1000.00"));
            when(accountRepository.findById(from.getAccountId())).thenReturn(Optional.of(from));
            when(accountRepository.findById("bad-id")).thenReturn(Optional.empty());

            assertThrows(AccountNotFoundException.class,
                    () -> accountService.transfer(from.getAccountId(), "bad-id", new BigDecimal("100.00")));
        }
    }
}