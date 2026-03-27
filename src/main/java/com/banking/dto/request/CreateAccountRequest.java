package com.banking.dto.request;

import com.banking.domain.AccountType;

import java.math.BigDecimal;

public record CreateAccountRequest(String ownerName,
                            AccountType accountType,
                            BigDecimal initialDeposit) {}