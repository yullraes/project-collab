package com.example.projectcollab.task.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record RejectTaskRequest(
        @Schema(description = "제안을 반려하는 이유", example = "완료 조건을 더 구체적으로 작성해 주세요.")
        @NotBlank
        String rejectionReason,

        @Schema(description = "조회 응답에서 받은 현재 작업 리비전", example = "0")
        @NotNull
        @PositiveOrZero
        Long revision
) {
}
