package com.banking.repository;

import com.banking.domain.AccountStatus;
import com.banking.domain.AccountType;
import com.banking.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<AccountEntity, String> {
    Optional<AccountEntity> findByAccountNumber(String accountNumber);

    List<AccountEntity> findByAccountTypeAndAccountStatus(
            AccountType accountType, AccountStatus status);
}