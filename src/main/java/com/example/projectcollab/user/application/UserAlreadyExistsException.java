package com.example.projectcollab.user.application;

public final class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException() {
        super("user.already_exists");
    }
}
