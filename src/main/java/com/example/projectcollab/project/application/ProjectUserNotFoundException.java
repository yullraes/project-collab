package com.example.projectcollab.project.application;

public final class ProjectUserNotFoundException extends IllegalStateException {
    public ProjectUserNotFoundException() {
        super("user.not_found");
    }
}
