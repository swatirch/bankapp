package com.banking.service;

import com.banking.domain.Account;
import com.banking.domain.AccountStatus;
import com.banking.domain.AccountType;
import com.banking.entity.AccountEntity;
import com.banking.kafka.TransactionEventProducer;
import com.banking.repository.AccountRepository;
import com.banking.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.Executor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

        @Mock
        private AccountRepository accountRepository;

        @Mock
        private TransactionRepository transactionRepository;

        @Mock
        private NotificationService notificationService;

        @Mock
        private TransactionEventProducer eventProducer;

        @Mock
        private Executor queryExecutor; // ← ADD THIS

        @InjectMocks
        private AccountService accountService;

        @Test
        void deposit_shouldTriggerDepositNotification() {
                // ARRANGE
                String accountId = "acc-123";
                BigDecimal amount = new BigDecimal("500.00");

                AccountEntity entity = stubAccountEntity(accountId);
                when(accountRepository.findById(accountId))
                                .thenReturn(Optional.of(entity));
                when(accountRepository.save(any()))
                                .thenReturn(entity);

                // ACT
                accountService.deposit(accountId, amount);

                // ASSERT
                verify(notificationService, times(1))
                                .sendDepositNotification(accountId, amount);
        }

        @Test
        void withdraw_shouldTriggerWithdrawalNotification() {
                // ARRANGE
                String accountId = "acc-123";
                BigDecimal amount = new BigDecimal("100.00");

                AccountEntity entity = stubAccountEntity(accountId);
                when(accountRepository.findById(accountId))
                                .thenReturn(Optional.of(entity));
                when(accountRepository.save(any()))
                                .thenReturn(entity);

                // ACT
                accountService.withdraw(accountId, amount);

                // ASSERT
                verify(notificationService, times(1))
                                .sendWithdrawalNotification(accountId, amount);
        }

        @Test
        void deposit_whenNotificationFails_shouldStillCompleteDeposit() {
                // ARRANGE — notification throws exception
                String accountId = "acc-123";
                BigDecimal amount = new BigDecimal("500.00");

                AccountEntity entity = stubAccountEntity(accountId);
                when(accountRepository.findById(accountId))
                                .thenReturn(Optional.of(entity));
                when(accountRepository.save(any()))
                                .thenReturn(entity);

                // notification service throws — simulates SMS gateway down
                doThrow(new RuntimeException("SMS gateway down"))
                                .when(notificationService)
                                .sendDepositNotification(any(), any());

                // ACT + ASSERT — deposit must NOT throw
                // notification failure should not affect deposit
                org.assertj.core.api.Assertions.assertThatCode(() -> accountService.deposit(accountId, amount))
                                .doesNotThrowAnyException();
        }

        // ── helper ───────────────────────────────────────────────────────────────

        private AccountEntity stubAccountEntity(String accountId) {
                AccountEntity entity = new AccountEntity();
                entity.setAccountId(accountId);
                entity.setAccountNumber("4532901234561234");
                entity.setOwnerName("Test User");
                entity.setAccountType(AccountType.SAVINGS);
                entity.setBalance(new BigDecimal("1000.00"));
                entity.setAccountStatus(AccountStatus.ACTIVE);
                entity.setCreatedAt(LocalDateTime.now());
                entity.setOwnerId("user-123");
                entity.setTransactions(new ArrayList<>());
                return entity;
        }
}