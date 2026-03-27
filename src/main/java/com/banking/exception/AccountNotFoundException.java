package com.banking.exception;

public class AccountNotFoundException extends BankingException {
    public AccountNotFoundException(String message) {
        super(ErrorCode.ACCOUNT_NOT_FOUND, message);
    }
}