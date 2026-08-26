package com.example.projectcollab.user.api;

import com.example.projectcollab.user.application.UserAlreadyExistsException;
import com.example.projectcollab.user.application.UserNotFoundException;
import com.example.projectcollab.user.application.UserValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public final class UserExceptionHandler {
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<UserErrorResponse> onAlreadyExists(final UserAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new UserErrorResponse("user.already_exists", ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<UserErrorResponse> onNotFound(final UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new UserErrorResponse("user.not_found", ex.getMessage()));
    }

    @ExceptionHandler(UserValidationException.class)
    public ResponseEntity<UserErrorResponse> onValidation(final UserValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new UserErrorResponse("user.input.invalid", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<UserErrorResponse> onInvalidRequest(final MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new UserErrorResponse("user.input.invalid", "user.input.invalid"));
    }
}
