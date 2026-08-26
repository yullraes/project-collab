package com.example.projectcollab.common.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record ApiErrorResponse(
        @Schema(description = "클라이언트가 분기할 수 있는 오류 코드", example = "task.revision.conflict")
        String code,

        @Schema(description = "오류 설명", example = "작업이 다른 요청에 의해 변경되었습니다.")
        String message,

        @Schema(description = "입력 필드별 검증 오류. 필드 오류가 아니면 빈 배열입니다.")
        List<FieldViolation> violations
) {
    public ApiErrorResponse {
        violations = List.copyOf(violations);
    }

    public static ApiErrorResponse of(final String code, final String message) {
        return new ApiErrorResponse(code, message, List.of());
    }
}
