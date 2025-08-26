package dev.felix2000jp.springboottemplate.appusers.domain.exceptions;

public class AppuserAlreadyExistsException extends RuntimeException {

    public AppuserAlreadyExistsException() {
        super("SecurityUser already exists");
    }

}
