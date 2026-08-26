package com.example.projectcollab.task.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record AssignTaskRequest(
        @Schema(description = "새 담당자 사용자 ID. 현재 프로젝트 멤버여야 합니다.", example = "3")
        @NotNull
        @Positive
        Long assigneeUserId,

        @Schema(description = "조회 응답에서 받은 현재 작업 리비전", example = "0")
        @NotNull
        @PositiveOrZero
        Long revision
) {
}
