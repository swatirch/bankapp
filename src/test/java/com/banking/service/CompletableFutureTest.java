package com.banking.service;

import com.banking.domain.AccountStatus;
import com.banking.domain.AccountType;
import com.banking.dto.response.AccountSummaryResponse;
import com.banking.entity.AccountEntity;
import com.banking.repository.AccountRepository;
import com.banking.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompletableFutureTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AccountService accountService;

    @Test
    void getAllAccountsWithStats_shouldReturnAllAccountsWithTransactionCount()
            throws Exception {
        // inject executor manually
        ReflectionTestUtils.setField(accountService, "queryExecutor",
                Executors.newFixedThreadPool(5));

        // ARRANGE
        AccountEntity account1 = stubAccount("acc-1");
        AccountEntity account2 = stubAccount("acc-2");

        when(accountRepository.findAll())
                .thenReturn(List.of(account1, account2));
        when(transactionRepository
                .countByAccount_AccountId("acc-1")).thenReturn(3L);
        when(transactionRepository
                .countByAccount_AccountId("acc-2")).thenReturn(7L);

        // ACT
        List<AccountSummaryResponse> results = accountService.getAllAccountsWithStats();

        // ASSERT
        assertThat(results).hasSize(2);
        assertThat(results)
                .extracting(AccountSummaryResponse::transactionCount)
                .containsExactlyInAnyOrder(3L, 7L);
    }

    @Test
    void getAllAccountsWithStats_whenOneFails_shouldReturnOthersWithZeroCount() {
        // inject executor
        ReflectionTestUtils.setField(accountService, "queryExecutor",
                Executors.newFixedThreadPool(5));

        // ARRANGE
        AccountEntity account1 = stubAccount("acc-1");
        AccountEntity account2 = stubAccount("acc-2");

        when(accountRepository.findAll())
                .thenReturn(List.of(account1, account2));
        when(transactionRepository
                .countByAccount_AccountId("acc-1")).thenReturn(5L);
        when(transactionRepository
                .countByAccount_AccountId("acc-2"))
                .thenThrow(new RuntimeException("DB timeout"));

        // ACT
        List<AccountSummaryResponse> results = accountService.getAllAccountsWithStats();

        // ASSERT — both returned, failed one has count 0
        assertThat(results).hasSize(2);
        assertThat(results)
                .extracting(AccountSummaryResponse::transactionCount)
                .containsExactlyInAnyOrder(5L, 0L);
    }

    private AccountEntity stubAccount(String accountId) {
        AccountEntity entity = new AccountEntity();
        entity.setAccountId(accountId);
        entity.setAccountNumber("4532901234561234");
        entity.setOwnerName("Test User");
        entity.setAccountType(AccountType.SAVINGS);
        entity.setBalance(new BigDecimal("1000.00"));
        entity.setAccountStatus(AccountStatus.ACTIVE);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setOwnerId("user-123");
        return entity;
    }
}