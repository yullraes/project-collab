package com.example.projectcollab.project.application;

import com.example.projectcollab.common.api.ApiErrorResponse;
import com.example.projectcollab.project.application.dto.CreateProjectRequest;
import com.example.projectcollab.project.application.dto.AddProjectMemberRequest;
import com.example.projectcollab.project.application.dto.ChangeProjectRoleRequest;
import com.example.projectcollab.project.application.dto.ProjectMemberResponse;
import com.example.projectcollab.project.application.dto.ProjectResponse;
import com.example.projectcollab.project.application.dto.UpdateProjectRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "프로젝트", description = "프로젝트와 OWNER·ADMIN·MEMBER 멤버십을 관리합니다.")
@ApiResponses({
        @ApiResponse(
                responseCode = "400",
                description = "입력 형식, 검증 또는 상태 오류",
                content = @Content(
                        mediaType = APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ApiErrorResponse.class),
                        examples = @ExampleObject(value = """
                                {"code":"request.invalid","message":"request.invalid","violations":[{"field":"name","reason":"공백일 수 없습니다"}]}
                                """)
                )
        ),
        @ApiResponse(
                responseCode = "403",
                description = "프로젝트 권한 부족",
                content = @Content(
                        mediaType = APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ApiErrorResponse.class),
                        examples = @ExampleObject(value = """
                                {"code":"project.access.forbidden","message":"project.access.forbidden","violations":[]}
                                """)
                )
        ),
        @ApiResponse(
                responseCode = "404",
                description = "사용자, 프로젝트 또는 멤버를 찾을 수 없음",
                content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))
        ),
        @ApiResponse(
                responseCode = "409",
                description = "멤버 중복 또는 동시에 변경된 프로젝트",
                content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))
        )
})
public final class ProjectController {
    private final ProjectService projectService;

    public ProjectController(final ProjectService projectService) {
        this.projectService = projectService;
    }

    @Operation(
            summary = "프로젝트 생성",
            description = "등록된 사용자가 프로젝트를 만들며 생성자는 OWNER가 됩니다."
    )
    @ApiResponse(
            responseCode = "201",
            description = "프로젝트 생성 성공",
            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProjectResponse.class))
    )
    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ProjectResponse> createProject(
            @Parameter(
                    description = "요청자 사용자 ID. 기본 실행의 OWNER 예상 ID는 1이며 시작 로그가 최종 기준입니다.",
                    example = "1"
            )
            @RequestParam(name = "userId") final long userId,
            @Valid @RequestBody final CreateProjectRequest request
    ) {
        ProjectResponse response = projectService.createProject(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "프로젝트 수정", description = "현재 프로젝트의 OWNER 또는 ADMIN만 수정할 수 있습니다.")
    @ApiResponse(
            responseCode = "200",
            description = "프로젝트 수정 성공",
            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProjectResponse.class))
    )
    @PutMapping(path = "/{projectId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ProjectResponse> updateProject(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable final long projectId,
            @Parameter(description = "요청자 사용자 ID. OWNER 또는 ADMIN이어야 합니다.", example = "1")
            @RequestParam(name = "userId") final long userId,
            @Valid @RequestBody final UpdateProjectRequest request
    ) {
        ProjectResponse response = projectService.updateProject(projectId, request, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내 프로젝트 목록 조회", description = "요청자가 현재 멤버인 프로젝트만 반환합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "프로젝트 목록 조회 성공",
            content = @Content(mediaType = APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ProjectResponse.class)))
    )
    @GetMapping
    public ResponseEntity<java.util.List<ProjectResponse>> getMyProjects(
            @Parameter(description = "요청자 사용자 ID", example = "3")
            @RequestParam(name = "userId") final long userId
    ) {
        return ResponseEntity.ok(projectService.getMyProjects(userId));
    }

    @Operation(summary = "프로젝트 상세 조회", description = "현재 프로젝트 멤버만 조회할 수 있습니다.")
    @ApiResponse(
            responseCode = "200",
            description = "프로젝트 상세 조회 성공",
            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProjectResponse.class))
    )
    @GetMapping(path = "/{projectId}")
    public ResponseEntity<ProjectResponse> getProject(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable final long projectId,
            @Parameter(description = "요청자 사용자 ID", example = "3")
            @RequestParam(name = "userId") final long userId
    ) {
        return ResponseEntity.ok(projectService.getProject(projectId, userId));
    }

    @Operation(summary = "프로젝트 삭제", description = "OWNER만 삭제할 수 있으며 소속 작업과 멤버십도 함께 삭제됩니다.")
    @ApiResponse(responseCode = "204", description = "프로젝트 삭제 성공")
    @DeleteMapping(path = "/{projectId}")
    public ResponseEntity<Void> deleteProject(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable final long projectId,
            @Parameter(description = "요청자 사용자 ID. OWNER여야 합니다.", example = "1")
            @RequestParam(name = "userId") final long userId
    ) {
        projectService.deleteProject(projectId, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "프로젝트 멤버 추가", description = "OWNER 또는 ADMIN이 등록된 사용자를 MEMBER로 추가합니다.")
    @ApiResponse(
            responseCode = "201",
            description = "프로젝트 멤버 추가 성공",
            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProjectMemberResponse.class))
    )
    @PostMapping(path = "/{projectId}/members", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ProjectMemberResponse> addMember(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable final long projectId,
            @Parameter(description = "요청자 사용자 ID. OWNER 또는 ADMIN이어야 합니다.", example = "1")
            @RequestParam(name = "userId") final long userId,
            @Valid @RequestBody final AddProjectMemberRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.addMember(projectId, userId, request));
    }

    @Operation(summary = "프로젝트 멤버 목록 조회", description = "현재 프로젝트 멤버만 조회할 수 있습니다.")
    @ApiResponse(
            responseCode = "200",
            description = "프로젝트 멤버 목록 조회 성공",
            content = @Content(mediaType = APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ProjectMemberResponse.class)))
    )
    @GetMapping(path = "/{projectId}/members", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProjectMemberResponse>> getMembers(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable final long projectId,
            @Parameter(description = "요청자 사용자 ID", example = "3")
            @RequestParam(name = "userId") final long userId
    ) {
        return ResponseEntity.ok(projectService.getMembers(projectId, userId));
    }

    @Operation(
            summary = "프로젝트 멤버 역할 변경",
            description = "OWNER 또는 ADMIN이 역할을 변경합니다. 프로젝트에는 항상 한 명 이상의 OWNER가 남아야 합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "프로젝트 멤버 역할 변경 성공",
            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProjectMemberResponse.class))
    )
    @PutMapping(path = "/{projectId}/members/{targetUserId}/role", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ProjectMemberResponse> changeMemberRole(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable final long projectId,
            @Parameter(description = "역할을 변경할 사용자 ID", example = "3")
            @PathVariable final long targetUserId,
            @Parameter(description = "요청자 사용자 ID. OWNER 또는 ADMIN이어야 합니다.", example = "1")
            @RequestParam(name = "userId") final long userId,
            @Valid @RequestBody final ChangeProjectRoleRequest request
    ) {
        return ResponseEntity.ok(projectService.changeMemberRole(projectId, userId, targetUserId, request));
    }

    @Operation(
            summary = "프로젝트 멤버 제거",
            description = "OWNER 또는 ADMIN이 멤버를 제거합니다. 담당 작업은 미할당으로 정규화되며 마지막 OWNER는 제거할 수 없습니다."
    )
    @ApiResponse(responseCode = "204", description = "프로젝트 멤버 제거 성공")
    @DeleteMapping(path = "/{projectId}/members/{targetUserId}")
    public ResponseEntity<Void> removeMember(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable final long projectId,
            @Parameter(description = "제거할 사용자 ID", example = "3")
            @PathVariable final long targetUserId,
            @Parameter(description = "요청자 사용자 ID. OWNER 또는 ADMIN이어야 합니다.", example = "1")
            @RequestParam(name = "userId") final long userId
    ) {
        projectService.removeMember(projectId, userId, targetUserId);
        return ResponseEntity.noContent().build();
    }
}
