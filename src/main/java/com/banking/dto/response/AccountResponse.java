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

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getAccountId(),
                mask(account.getAccountNumber()), // ← masked
                account.getOwnerName(),
                account.getAccountType(),
                account.getBalance(),
                account.getAccountStatus(),
                account.getCreatedAt());
    }

    private static String mask(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 16) {
            return accountNumber; // return as-is if too short
        }
        return accountNumber.substring(0, 4)
                + "********"
                + accountNumber.substring(12);
    }
}