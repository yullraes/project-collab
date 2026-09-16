package com.example.projectcollab.user.application;

public final class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("user.not_found");
    }
}
