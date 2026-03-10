package dev.felix2000jp.envelope.accounts.application.exceptions;

public class InvalidTransactionQueryException extends RuntimeException {

    public InvalidTransactionQueryException(String message) {
        super(message);
    }
}
