package com.banking.exception;

public class BankingException extends RuntimeException{
    private final String errorCode;

    public BankingException(String errorCode, String message){
        super(message);
        this.errorCode=errorCode;
    }
    public String getErrorCode() {
        return errorCode;
    }
}
