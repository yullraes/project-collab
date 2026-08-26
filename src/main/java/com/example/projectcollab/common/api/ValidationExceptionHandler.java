package com.example.projectcollab.common.api;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@RestControllerAdvice
public final class ValidationExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> onInvalidRequest(final MethodArgumentNotValidException ex) {
        return invalid(fieldViolations(ex));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiErrorResponse> onBindingFailure(final BindException ex) {
        return invalid(fieldViolations(ex));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> onUnreadableBody(final HttpMessageNotReadableException ex) {
        return invalid(List.of());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> onMissingParameter(final MissingServletRequestParameterException ex) {
        return invalid(List.of(new FieldViolation(ex.getParameterName(), "필수 요청 파라미터입니다.")));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> onTypeMismatch(final MethodArgumentTypeMismatchException ex) {
        return invalid(List.of(new FieldViolation(ex.getName(), "올바른 형식이 아닙니다.")));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> onConstraintViolation(final ConstraintViolationException ex) {
        List<FieldViolation> violations = ex.getConstraintViolations().stream()
                .map(violation -> new FieldViolation(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()
                ))
                .toList();
        return invalid(violations);
    }

    private List<FieldViolation> fieldViolations(final BindException ex) {
        return ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldViolation(error.getField(), error.getDefaultMessage()))
                .toList();
    }

    private ResponseEntity<ApiErrorResponse> invalid(final List<FieldViolation> violations) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse("request.invalid", "request.invalid", violations));
    }
}
