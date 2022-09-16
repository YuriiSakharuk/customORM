package com.custom.orm.exceptions;

public class ForeignKeyNotFoundException extends RuntimeException{

    public ForeignKeyNotFoundException() {
    }

    public ForeignKeyNotFoundException(String message) {
        super(message);
    }

    public ForeignKeyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ForeignKeyNotFoundException(Throwable cause) {
        super(cause);
    }

    public ForeignKeyNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
