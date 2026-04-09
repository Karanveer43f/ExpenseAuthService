package org.karanveer43f.expenseTracker.exceptions;

public class RefreshTokenException extends Exception {

    public RefreshTokenException(String message) {
        super(message);
    }

    public RefreshTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}