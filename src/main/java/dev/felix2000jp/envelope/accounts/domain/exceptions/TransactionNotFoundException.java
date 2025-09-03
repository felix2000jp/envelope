package dev.felix2000jp.envelope.accounts.domain.exceptions;

public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException() {
        super("Transaction could not be found");
    }

}
