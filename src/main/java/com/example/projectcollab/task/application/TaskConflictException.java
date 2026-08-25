package com.example.projectcollab.task.application;

public final class TaskConflictException extends RuntimeException {
    public TaskConflictException() {
        super("task.revision.conflict");
    }
}
