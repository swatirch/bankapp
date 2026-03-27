package com.banking.repository;

import com.banking.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
 interface SpringDataAccountRepository extends JpaRepository<AccountEntity,String>
{
    Optional<AccountEntity> findByAccountNumber(String accountNumber);
}
