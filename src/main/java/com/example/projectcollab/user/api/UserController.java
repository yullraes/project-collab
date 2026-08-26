package com.example.projectcollab.user.api;

import com.example.projectcollab.common.api.ApiErrorResponse;
import com.example.projectcollab.user.application.UserService;
import com.example.projectcollab.user.application.dto.CreateUserRequest;
import com.example.projectcollab.user.application.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "사용자", description = "프로젝트에 참여할 사용자를 등록하고 조회합니다.")
@ApiResponses({
        @ApiResponse(
                responseCode = "400",
                description = "입력 형식 또는 검증 오류",
                content = @Content(
                        mediaType = APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ApiErrorResponse.class),
                        examples = @ExampleObject(value = """
                                {"code":"request.invalid","message":"request.invalid","violations":[{"field":"email","reason":"올바른 형식의 이메일 주소여야 합니다"}]}
                                """)
                )
        ),
        @ApiResponse(
                responseCode = "404",
                description = "사용자를 찾을 수 없음",
                content = @Content(
                        mediaType = APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ApiErrorResponse.class),
                        examples = @ExampleObject(value = """
                                {"code":"user.not_found","message":"user.not_found","violations":[]}
                                """)
                )
        ),
        @ApiResponse(
                responseCode = "409",
                description = "이메일 중복",
                content = @Content(
                        mediaType = APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ApiErrorResponse.class),
                        examples = @ExampleObject(value = """
                                {"code":"user.already_exists","message":"user.already_exists","violations":[]}
                                """)
                )
        )
})
public final class UserController {
    private final UserService userService;

    public UserController(final UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "사용자 생성", description = "이메일은 소문자로 정규화되며 중복 이메일은 거부됩니다.")
    @ApiResponse(
            responseCode = "201",
            description = "사용자 생성 성공",
            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserResponse.class))
    )
    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody final CreateUserRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(request));
    }

    @Operation(summary = "사용자 조회")
    @ApiResponse(
            responseCode = "200",
            description = "사용자 조회 성공",
            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserResponse.class))
    )
    @GetMapping(path = "/{userId}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> getUser(
            @Parameter(description = "조회할 사용자 ID", example = "1")
            @PathVariable final long userId
    ) {
        return ResponseEntity.ok(userService.getUser(userId));
    }
}
