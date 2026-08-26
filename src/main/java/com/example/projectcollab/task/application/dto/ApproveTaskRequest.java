package com.example.projectcollab.task.application.dto;

public record ApproveTaskRequest(
        Long assigneeUserId,

        Boolean unassigned,

        Long revision
) {
}
