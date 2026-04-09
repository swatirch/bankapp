package com.banking.dto.response;

public record TransferResponse(
        AccountResponse fromAccount,
        AccountResponse toAccount
) {
    public static TransferResponse of(AccountResponse from, AccountResponse to) {
        return new TransferResponse(from, to);
    }
}