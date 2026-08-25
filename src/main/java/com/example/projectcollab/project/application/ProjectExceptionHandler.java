package com.example.projectcollab.project.application;

import com.example.projectcollab.project.domain.ProjectNotFoundException;
import com.example.projectcollab.project.domain.ProjectPermissionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public final class ProjectExceptionHandler {
    @ExceptionHandler(ProjectPermissionException.class)
    public ResponseEntity<ProjectErrorResponse> onPermission(final ProjectPermissionException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ProjectErrorResponse("project.forbidden", ex.getMessage()));
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ProjectErrorResponse> onNotFound(final ProjectNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ProjectErrorResponse("project.notFound", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProjectErrorResponse> onIllegalState(final IllegalStateException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ProjectErrorResponse("project.illegal_state", ex.getMessage()));
    }
}
