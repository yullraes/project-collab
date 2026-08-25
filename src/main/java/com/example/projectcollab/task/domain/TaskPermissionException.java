package com.example.projectcollab.task.domain;

public final class TaskPermissionException extends IllegalStateException {
    private final String actorUserId;

    public TaskPermissionException(final String actorUserId, final String code) {
        super(code);
        this.actorUserId = actorUserId;
    }

    public String actorUserId() {
        return actorUserId;
    }
}
