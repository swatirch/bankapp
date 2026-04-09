package com.banking.dto.request;

import com.banking.domain.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateAccountRequest(

        @NotBlank(message = "Owner name must not be blank")
        String ownerName,

        @NotNull(message = "Account type must not be null")
        AccountType accountType,

        @NotNull(message = "Initial deposit must not be null")
        @DecimalMin(value = "0.01", message = "Initial deposit must be greater than zero")
        BigDecimal initialDeposit

) {}