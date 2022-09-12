package com.custom.orm.exceptions;


public class CustomClassNotFoundException extends RuntimeException {

    public CustomClassNotFoundException() {
        super();
    }

    public CustomClassNotFoundException(String message) {
        super(message);
    }

    public CustomClassNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public CustomClassNotFoundException(Throwable cause) {
        super(cause);
    }

    protected CustomClassNotFoundException(String message, Throwable cause,
                                           boolean enableSuppression,
                                           boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
