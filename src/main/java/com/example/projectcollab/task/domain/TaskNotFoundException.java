package com.example.projectcollab.task.domain;

public final class TaskNotFoundException extends IllegalStateException {
    private final long taskId;

    public TaskNotFoundException(final long taskId) {
        super("task.not.found");
        this.taskId = taskId;
    }

    public long taskId() {
        return taskId;
    }
}
