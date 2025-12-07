package com.fsk.transaction.rollback.exception;

public class NoRollbackException extends RuntimeException {
    public NoRollbackException(String message) {
        super(message);
    }
}


