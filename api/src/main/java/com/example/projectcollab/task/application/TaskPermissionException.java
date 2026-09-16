package com.example.projectcollab.task.application;

public final class TaskPermissionException extends RuntimeException {
    public TaskPermissionException(final String code) {
        super(code);
    }
}
