package com.banking.repository;

import com.banking.domain.Account;
import com.banking.domain.AccountType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(AccountRepositoryImpl.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class AccountRepositoryImplTest {

    private final AccountRepository repository; // inject the impl

    AccountRepositoryImplTest(AccountRepositoryImpl repository) {
        this.repository = repository;
    }

    @Test
    void shouldSaveAndRetrieveAccountById() {
        Account account = new Account("John", AccountType.SAVINGS, new BigDecimal("1000.00"));
        repository.save(account);

        Optional<Account> found = repository.findById(account.getAccountId());
        assertTrue(found.isPresent());
        assertEquals("John", found.get().getOwnerName());
    }

    @Test
    void shouldFindAccountByAccountNumber() {
        Account account = new Account("John", AccountType.SAVINGS, new BigDecimal("1000.00"));
        repository.save(account);

        Optional<Account> found = repository.findByAccountNumber(account.getAccountNumber());
        assertTrue(found.isPresent());
        assertEquals(account.getAccountNumber(), found.get().getAccountNumber());
    }

    @Test
    void shouldReturnEmptyWhenAccountNotFound() {
        Optional<Account> found = repository.findById("non-existing-id");
        assertFalse(found.isPresent());
    }
}
