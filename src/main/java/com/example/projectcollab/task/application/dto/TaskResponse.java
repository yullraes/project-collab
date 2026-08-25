package com.example.projectcollab.task.application.dto;

import com.example.projectcollab.task.domain.TaskResource;

import java.time.Instant;

public record TaskResponse(
        Long taskId,
        Long projectId,
        String creatorUserId,
        String assigneeUserId,
        String title,
        String description,
        String state,
        String rejectionReason,
        Instant createdAt,
        Instant updatedAt
) {

    public static TaskResponse from(final TaskResource taskResource) {
        return new TaskResponse(
                taskResource.taskId(),
                taskResource.projectId(),
                taskResource.creatorUserId(),
                taskResource.assigneeUserId(),
                taskResource.title(),
                taskResource.description(),
                taskResource.state(),
                taskResource.rejectionReason(),
                taskResource.createdAt(),
                taskResource.updatedAt()
        );
    }
}
