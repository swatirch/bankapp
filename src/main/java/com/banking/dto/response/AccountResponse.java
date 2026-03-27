package com.banking.dto.response;

import com.banking.domain.Account;
import com.banking.domain.AccountStatus;
import com.banking.domain.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(
        String accountId,
        String accountNumber,
        String ownerName,
        AccountType accountType,
        BigDecimal balance,
        AccountStatus accountStatus,
        LocalDateTime createdAt) {

    public static AccountResponse from(Account account){
        return new AccountResponse(
                account.getAccountId(),
                account.getAccountNumber(),
                account.getOwnerName(),
                account.getAccountType(),
                account.getBalance(),
                account.getAccountStatus(),
                account.getCreatedAt()
        );
    }

}
