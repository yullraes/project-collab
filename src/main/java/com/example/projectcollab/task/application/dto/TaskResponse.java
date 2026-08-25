package com.example.projectcollab.task.application.dto;

import com.example.projectcollab.task.persistence.TaskEntity;

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

    public static TaskResponse from(final TaskEntity taskEntity) {
        return new TaskResponse(
                taskEntity.taskId(),
                taskEntity.projectId(),
                taskEntity.creatorUserId(),
                taskEntity.assigneeUserId(),
                taskEntity.title(),
                taskEntity.description(),
                taskEntity.state(),
                taskEntity.rejectionReason(),
                taskEntity.createdAt(),
                taskEntity.updatedAt()
        );
    }
}
