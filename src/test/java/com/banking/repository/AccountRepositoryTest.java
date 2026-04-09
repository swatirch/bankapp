package com.banking.repository;

import com.banking.entity.AccountEntity;
import com.banking.domain.AccountStatus;
import com.banking.domain.AccountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    private AccountEntity buildEntity(String owner) {
        AccountEntity e = new AccountEntity();
        e.setAccountId(UUID.randomUUID().toString());
        e.setAccountNumber("ACC" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        e.setOwnerName(owner);
        e.setAccountType(AccountType.SAVINGS);
        e.setBalance(new BigDecimal("1000.00"));
        e.setAccountStatus(AccountStatus.ACTIVE);
        e.setCreatedAt(LocalDateTime.now());
        return e;
    }

    @Nested
    class SaveAndRetrieve {

        @Test
        void shouldSaveAndFindById() {
            AccountEntity entity = buildEntity("John");
            accountRepository.save(entity);

            Optional<AccountEntity> found = accountRepository.findById(entity.getAccountId());
            assertTrue(found.isPresent());
            assertEquals("John", found.get().getOwnerName());
        }

        @Test
        void shouldSaveAndFindByAccountNumber() {
            AccountEntity entity = buildEntity("Jane");
            accountRepository.save(entity);

            Optional<AccountEntity> found = accountRepository.findByAccountNumber(entity.getAccountNumber());
            assertTrue(found.isPresent());
            assertEquals(entity.getAccountNumber(), found.get().getAccountNumber());
        }

        @Test
        void shouldReturnEmptyWhenIdDoesNotExist() {
            Optional<AccountEntity> found = accountRepository.findById("non-existing-id");
            assertFalse(found.isPresent());
        }

        @Test
        void shouldReturnEmptyWhenAccountNumberDoesNotExist() {
            Optional<AccountEntity> found = accountRepository.findByAccountNumber("ACCXXXXXXXX");
            assertFalse(found.isPresent());
        }
    }

    @Nested
    class Persistence {

        @Test
        void shouldPersistBalanceCorrectly() {
            AccountEntity entity = buildEntity("Alice");
            entity.setBalance(new BigDecimal("9999.9999"));
            accountRepository.save(entity);

            AccountEntity found = accountRepository.findById(entity.getAccountId()).orElseThrow();
            assertEquals(0, new BigDecimal("9999.9999").compareTo(found.getBalance()));
        }

        @Test
        void shouldPersistAccountStatusCorrectly() {
            AccountEntity entity = buildEntity("Bob");
            entity.setAccountStatus(AccountStatus.BLOCKED);
            accountRepository.save(entity);

            AccountEntity found = accountRepository.findById(entity.getAccountId()).orElseThrow();
            assertEquals(AccountStatus.BLOCKED, found.getAccountStatus());
        }
    }
}