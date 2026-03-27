package com.banking.exception;

public class AccountStatusException extends BankingException{
    public AccountStatusException(String message){
        super(ErrorCode.INVALID_ACCOUNT_STATUS,message);
    }
}
