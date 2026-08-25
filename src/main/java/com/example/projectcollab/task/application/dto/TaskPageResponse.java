package com.example.projectcollab.task.application.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record TaskPageResponse(
        List<TaskResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static TaskPageResponse from(final Page<TaskResponse> tasks) {
        return new TaskPageResponse(
                tasks.getContent(),
                tasks.getNumber(),
                tasks.getSize(),
                tasks.getTotalElements(),
                tasks.getTotalPages(),
                tasks.hasNext()
        );
    }
}
