package com.example.projectcollab.task.application.dto;

public record AssignTaskRequest(
        Long assigneeUserId,

        Long revision
) {
}
