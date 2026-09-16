package com.example.projectcollab.user.api;

import com.example.projectcollab.common.api.ApiErrorResponse;
import com.example.projectcollab.user.application.UserAlreadyExistsException;
import com.example.projectcollab.user.application.UserNotFoundException;
import com.example.projectcollab.user.application.UserValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = UserController.class)
public final class UserExceptionHandler {
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> onAlreadyExists(final UserAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of("user.already_exists", ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> onNotFound(final UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of("user.not_found", ex.getMessage()));
    }

    @ExceptionHandler(UserValidationException.class)
    public ResponseEntity<ApiErrorResponse> onValidation(final UserValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of("user.input.invalid", ex.getMessage()));
    }

}
