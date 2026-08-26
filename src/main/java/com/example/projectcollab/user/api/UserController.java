package com.example.projectcollab.user.api;

import com.example.projectcollab.user.application.UserService;
import com.example.projectcollab.user.application.dto.CreateUserRequest;
import com.example.projectcollab.user.application.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/users")
public final class UserController {
    private final UserService userService;

    public UserController(final UserService userService) {
        this.userService = userService;
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody final CreateUserRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(request));
    }

    @GetMapping(path = "/{userId}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> getUser(
            @PathVariable final long userId
    ) {
        return ResponseEntity.ok(userService.getUser(userId));
    }
}
