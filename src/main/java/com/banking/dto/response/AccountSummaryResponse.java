package com.banking.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.banking.domain.AccountStatus;
import com.banking.domain.AccountType;

public record AccountSummaryResponse(
        String accountId,
        String accountNumber,
        String ownerName,
        AccountType accountType,
        BigDecimal balance,
        AccountStatus accountStatus,
        LocalDateTime createdAt,
        long transactionCount) {
    public static AccountSummaryResponse from(
            AccountResponse account, long transactionCount) {
        return new AccountSummaryResponse(
                account.accountId(),
                account.accountNumber(),
                account.ownerName(),
                account.accountType(),
                account.balance(),
                account.accountStatus(),
                account.createdAt(),
                transactionCount);
    }
}