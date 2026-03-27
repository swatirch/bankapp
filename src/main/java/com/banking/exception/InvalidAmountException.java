package com.banking.exception;

public class InvalidAmountException extends BankingException{

    public InvalidAmountException(String message){
        super(ErrorCode.INVALID_AMOUNT,message);
    }
}
