package dev.felix2000jp.springboottemplate.appusers.domain.exceptions;

public class AppuserNotFoundException extends RuntimeException {

    public AppuserNotFoundException() {
        super("Appuser could not be found");
    }

}
