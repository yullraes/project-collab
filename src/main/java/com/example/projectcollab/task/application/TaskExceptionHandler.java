package com.example.projectcollab.task.application;

import com.example.projectcollab.task.domain.TaskNotFoundException;
import com.example.projectcollab.task.domain.TaskPermissionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public final class TaskExceptionHandler {
    @ExceptionHandler(TaskPermissionException.class)
    public ResponseEntity<TaskErrorResponse> onPermission(final TaskPermissionException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new TaskErrorResponse("task.forbidden", ex.getMessage()));
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<TaskErrorResponse> onNotFound(final TaskNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new TaskErrorResponse("task.notFound", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<TaskErrorResponse> onIllegalArgument(final IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new TaskErrorResponse("task.illegal_argument", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<TaskErrorResponse> onIllegalState(final IllegalStateException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new TaskErrorResponse("task.illegal_state", ex.getMessage()));
    }
}
