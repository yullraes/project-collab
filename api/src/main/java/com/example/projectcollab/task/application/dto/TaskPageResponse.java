package com.example.projectcollab.task.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

public record TaskPageResponse(
        @Schema(description = "현재 페이지의 작업 목록")
        List<TaskResponse> content,

        @Schema(description = "0부터 시작하는 현재 페이지 번호", example = "0")
        int page,

        @Schema(description = "페이지 크기", example = "20")
        int size,

        @Schema(description = "조건에 맞는 전체 작업 수", example = "6")
        long totalElements,

        @Schema(description = "전체 페이지 수", example = "1")
        int totalPages,

        @Schema(description = "다음 페이지 존재 여부", example = "false")
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
