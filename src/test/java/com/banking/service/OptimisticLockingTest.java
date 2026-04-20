package com.banking.service;

import com.banking.entity.AccountEntity;
import com.banking.domain.AccountType;
import com.banking.repository.AccountRepository;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class OptimisticLockingTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void version_shouldStartAtZero() {
        AccountEntity entity = createTestAccount();
        AccountEntity saved = accountRepository.save(entity);
        assertThat(saved.getVersion()).isEqualTo(0L);
    }

    @Test
    void version_shouldIncrementOnEachSave() {
        AccountEntity entity = createTestAccount();
        AccountEntity saved = accountRepository.saveAndFlush(entity);
        assertThat(saved.getVersion()).isEqualTo(0L);

        saved.setBalance(new BigDecimal("2000.00"));
        AccountEntity updated = accountRepository.saveAndFlush(saved);
        assertThat(updated.getVersion()).isEqualTo(1L);
    }

    @Test
    void concurrentModification_shouldThrowException() {
        AccountEntity entity = createTestAccount();
        accountRepository.saveAndFlush(entity);
        String id = entity.getAccountId();

        // Thread 1 reads
        AccountEntity thread1 = accountRepository.findById(id).get();

        // Detach thread1 from session — simulates separate HTTP request
        entityManager.detach(thread1);

        // Thread 2 reads same row
        AccountEntity thread2 = accountRepository.findById(id).get();

        // Thread 2 saves first — version becomes 1
        thread2.setBalance(new BigDecimal("500.00"));
        accountRepository.saveAndFlush(thread2);

        // Thread 1 tries to save with stale version = 0 → must fail
        thread1.setBalance(new BigDecimal("200.00"));
        assertThatThrownBy(() -> accountRepository.saveAndFlush(thread1))
                .isInstanceOf(Exception.class);
    }

    private AccountEntity createTestAccount() {
        AccountEntity entity = new AccountEntity();
        entity.setAccountId(java.util.UUID.randomUUID().toString());
        entity.setAccountNumber("4532901234561234");
        entity.setOwnerName("Test User");
        entity.setAccountType(AccountType.SAVINGS);
        entity.setBalance(new BigDecimal("1000.00"));
        entity.setAccountStatus(com.banking.domain.AccountStatus.ACTIVE);
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }
}