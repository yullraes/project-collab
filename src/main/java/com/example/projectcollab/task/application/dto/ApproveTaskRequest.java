package com.example.projectcollab.task.application.dto;

public record ApproveTaskRequest(
        String assigneeUserId,

        Boolean unassigned,

        Long revision
) {
}
