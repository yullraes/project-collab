package com.example.projectcollab.project.application;

import com.example.projectcollab.common.api.ApiErrorResponse;
import com.example.projectcollab.project.domain.ProjectNotFoundException;
import com.example.projectcollab.project.domain.ProjectPermissionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = ProjectController.class)
public final class ProjectExceptionHandler {
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiErrorResponse> onOptimisticLock(final ObjectOptimisticLockingFailureException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of("project.revision.conflict", "project.revision.conflict"));
    }

    @ExceptionHandler(ProjectMemberAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> onMemberAlreadyExists(final ProjectMemberAlreadyExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(ex.getMessage(), ex.getMessage()));
    }

    @ExceptionHandler(ProjectMemberNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> onMemberNotFound(final ProjectMemberNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(ex.getMessage(), ex.getMessage()));
    }

    @ExceptionHandler(ProjectUserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> onUserNotFound(final ProjectUserNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(ex.getMessage(), ex.getMessage()));
    }

    @ExceptionHandler(ProjectValidationException.class)
    public ResponseEntity<ApiErrorResponse> onValidation(final ProjectValidationException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(ex.getMessage(), ex.getMessage()));
    }

    @ExceptionHandler(ProjectPermissionException.class)
    public ResponseEntity<ApiErrorResponse> onPermission(final ProjectPermissionException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiErrorResponse.of(ex.getMessage(), ex.getMessage()));
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> onNotFound(final ProjectNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of("project.not_found", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> onIllegalState(final IllegalStateException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of("project.illegal_state", ex.getMessage()));
    }
}
