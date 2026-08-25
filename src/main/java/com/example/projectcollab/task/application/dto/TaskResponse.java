package com.example.projectcollab.task.application.dto;

import com.example.projectcollab.task.domain.Task;
import com.example.projectcollab.task.domain.TaskAssignment;

import java.time.Instant;

public record TaskResponse(
        Long taskId,
        Long projectId,
        Long revision,
        String creatorUserId,
        String assigneeUserId,
        String title,
        String description,
        String state,
        String rejectionReason,
        Instant createdAt,
        Instant updatedAt
) {

    public static TaskResponse from(final Task task) {
        String assigneeUserId = task.assignment() instanceof TaskAssignment.Assigned assigned
                ? assigned.assignee().username()
                : null;
        return new TaskResponse(
                task.taskId(),
                task.projectId(),
                task.revision(),
                task.creator().username(),
                assigneeUserId,
                task.content().title(),
                task.content().description(),
                task.state().name(),
                task.rejectionReason(),
                task.createdAt(),
                task.updatedAt()
        );
    }
}
