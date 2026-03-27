package com.banking.exception;

public class InsufficientBalanceException extends BankingException{
    public InsufficientBalanceException(String message){
        super(ErrorCode.INSUFFICIENT_BALANCE,message);
    }

}
