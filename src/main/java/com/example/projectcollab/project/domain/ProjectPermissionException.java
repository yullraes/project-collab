package com.example.projectcollab.project.domain;

public final class ProjectPermissionException extends IllegalStateException {
    private final String actorUserId;

    public ProjectPermissionException(final String actorUserId, final String code) {
        super(code);
        this.actorUserId = actorUserId;
    }

    public String actorUserId() {
        return actorUserId;
    }
}
