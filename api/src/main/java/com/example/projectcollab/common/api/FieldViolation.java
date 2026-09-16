package com.example.projectcollab.common.api;

import io.swagger.v3.oas.annotations.media.Schema;

public record FieldViolation(
        @Schema(description = "오류가 발생한 요청 필드", example = "revision")
        String field,

        @Schema(description = "필드 검증 실패 이유", example = "0 이상이어야 합니다")
        String reason
) {
}
