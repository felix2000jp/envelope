package dev.felix2000jp.envelope.accounts.domain.exceptions;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException() {
        super("Account could not be found");
    }

}
