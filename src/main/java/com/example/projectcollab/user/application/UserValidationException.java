package com.example.projectcollab.user.application;

public final class UserValidationException extends RuntimeException {
    public UserValidationException(final String message) {
        super(message);
    }
}
