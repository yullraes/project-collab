package com.example.projectcollab.project.application;

public final class ProjectMemberAlreadyExistsException extends IllegalStateException {
    public ProjectMemberAlreadyExistsException() {
        super("project.member.already_exists");
    }
}
