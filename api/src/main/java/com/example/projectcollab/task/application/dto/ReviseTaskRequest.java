package com.example.projectcollab.task.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ReviseTaskRequest(
        @Schema(description = "변경할 작업 제목", example = "결제 오류 재현 및 수정")
        @NotBlank
        String title,

        @Schema(description = "변경할 작업 내용과 완료 조건", example = "재현 결과와 수정 범위를 함께 기록합니다.")
        @NotBlank
        String description,

        @Schema(description = "조회 응답에서 받은 현재 작업 리비전", example = "0")
        @NotNull
        @PositiveOrZero
        Long revision
) {
}
