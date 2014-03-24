package com.redhat.mongotx;

public class InvalidTransactionException extends Exception {

    public InvalidTransactionException(String txId) {
        super(txId);
    }
}
