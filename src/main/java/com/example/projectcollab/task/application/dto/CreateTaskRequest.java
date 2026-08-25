package com.example.projectcollab.task.application.dto;

public record CreateTaskRequest(
        String title,

        String description,

        String assigneeUserId,

        Boolean unassigned
) {
}
