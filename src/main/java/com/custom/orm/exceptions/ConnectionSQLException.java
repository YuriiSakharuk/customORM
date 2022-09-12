package com.custom.orm.exceptions;


public class ConnectionSQLException extends RuntimeException {

    public ConnectionSQLException() {
        super();
    }

    public ConnectionSQLException(String message) {
        super(message);
    }

    public ConnectionSQLException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectionSQLException(Throwable cause) {
        super(cause);
    }

    protected ConnectionSQLException(String message, Throwable cause,
                                     boolean enableSuppression,
                                     boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
