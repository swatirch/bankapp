package com.banking.repository;

import com.banking.entity.TransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<TransactionEntity, String> {
    Page<TransactionEntity> findByAccount_AccountId(String accountId, Pageable pageable);
}