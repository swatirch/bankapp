package com.banking.exception;


public final class ErrorCode {

    // Account errors
    public static final String INVALID_ACCOUNT = "INVALID_ACCOUNT";
    public static final String ACCOUNT_NOT_FOUND = "ACCOUNT_NOT_FOUND";
    public static final String INVALID_ACCOUNT_STATUS = "INVALID_ACCOUNT_STATUS";

    // Transaction errors
    public static final String INSUFFICIENT_BALANCE = "INSUFFICIENT_BALANCE";
    public static final String INVALID_AMOUNT = "INVALID_AMOUNT";
    public static final String TRANSACTION_NOT_FOUND = "TRANSACTION_NOT_FOUND";

    // Transfer errors
    public static final String TRANSFER_FAILED = "TRANSFER_FAILED";
    public static final String SAME_ACCOUNT_TRANSFER = "SAME_ACCOUNT_TRANSFER";

    // Private constructor — nobody should instantiate this class
    private ErrorCode() {}
}