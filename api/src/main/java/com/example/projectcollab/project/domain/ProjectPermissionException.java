package com.example.projectcollab.project.domain;

public final class ProjectPermissionException extends IllegalStateException {
    public ProjectPermissionException(final String code) {
        super(code);
    }
}
