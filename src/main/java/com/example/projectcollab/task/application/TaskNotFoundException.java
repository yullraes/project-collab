package com.example.projectcollab.task.application;

public final class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException() {
        super("task.not.found");
    }
}
