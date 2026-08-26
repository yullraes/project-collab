package com.example.projectcollab.task.api;

import com.example.projectcollab.common.api.ApiErrorResponse;
import com.example.projectcollab.task.application.TaskReadService;
import com.example.projectcollab.task.application.TaskWriteService;
import com.example.projectcollab.task.application.dto.ApproveTaskRequest;
import com.example.projectcollab.task.application.dto.AssignTaskRequest;
import com.example.projectcollab.task.application.dto.CreateTaskRequest;
import com.example.projectcollab.task.application.dto.RejectTaskRequest;
import com.example.projectcollab.task.application.dto.ReviseTaskRequest;
import com.example.projectcollab.task.application.dto.TaskPageResponse;
import com.example.projectcollab.task.application.dto.TaskResponse;
import com.example.projectcollab.task.domain.Task;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
@Tag(name = "작업", description = "작업 제안, 승인, 담당자 배정, 수행, 검토와 완료 상태를 관리합니다.")
@ApiResponses({
        @ApiResponse(
                responseCode = "400",
                description = "입력 형식, 검증 또는 허용되지 않은 상태 전이",
                content = @Content(
                        mediaType = APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ApiErrorResponse.class),
                        examples = @ExampleObject(value = """
                                {"code":"request.invalid","message":"request.invalid","violations":[{"field":"revision","reason":"0 이상이어야 합니다"}]}
                                """)
                )
        ),
        @ApiResponse(
                responseCode = "403",
                description = "프로젝트 또는 작업 권한 부족",
                content = @Content(
                        mediaType = APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ApiErrorResponse.class),
                        examples = @ExampleObject(value = """
                                {"code":"task.forbidden","message":"task.approve.forbidden","violations":[]}
                                """)
                )
        ),
        @ApiResponse(
                responseCode = "404",
                description = "프로젝트 또는 작업을 찾을 수 없음",
                content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))
        ),
        @ApiResponse(
                responseCode = "409",
                description = "오래된 작업 리비전",
                content = @Content(
                        mediaType = APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ApiErrorResponse.class),
                        examples = @ExampleObject(value = """
                                {"code":"task.revision.conflict","message":"작업이 다른 요청에 의해 변경되었습니다.","violations":[]}
                                """)
                )
        )
})
public final class TaskController {
    private final TaskWriteService taskWriteService;
    private final TaskReadService taskReadService;

    public TaskController(
            final TaskWriteService taskWriteService,
            final TaskReadService taskReadService
    ) {
        this.taskWriteService = taskWriteService;
        this.taskReadService = taskReadService;
    }

    @Operation(
            summary = "작업 생성",
            description = "MEMBER가 만들면 PENDING 제안이 되고, OWNER·ADMIN이 만들면 ACCEPTED 작업이 됩니다. "
                    + "담당자 선택과 미할당 등록은 OWNER·ADMIN만 할 수 있습니다."
    )
    @ApiResponse(
            responseCode = "201",
            description = "작업 생성 성공",
            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = TaskResponse.class))
    )
    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskResponse> createTask(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable final long projectId,
            @Parameter(
                    description = "요청자 사용자 ID. 기본 실행의 MEMBER 예상 ID는 3이며 시작 로그가 최종 기준입니다.",
                    example = "3"
            )
            @RequestParam(name = "userId") final long userId,
            @Valid @RequestBody final CreateTaskRequest request
    ) {
        TaskResponse response = taskWriteService.createTask(projectId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "작업 목록 조회", description = "현재 프로젝트 멤버만 검색·상태 필터·페이징 조회를 할 수 있습니다.")
    @ApiResponse(
            responseCode = "200",
            description = "작업 목록 조회 성공",
            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = TaskPageResponse.class))
    )
    @GetMapping
    public ResponseEntity<TaskPageResponse> listTasks(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable final long projectId,
            @Parameter(description = "요청자 사용자 ID", example = "3")
            @RequestParam(name = "userId") final long userId,
            @Parameter(description = "제목 또는 설명에서 찾을 검색어", example = "예제")
            @RequestParam(name = "keyword", required = false) final String keyword,
            @Parameter(description = "작업 상태 필터", example = "IN_PROGRESS")
            @RequestParam(name = "state", required = false) final Task.TaskState state,
            @Parameter(description = "0부터 시작하는 페이지 번호", example = "0")
            @RequestParam(name = "page", defaultValue = "0") final int page,
            @Parameter(description = "페이지 크기. 1 이상 100 이하여야 합니다.", example = "20")
            @RequestParam(name = "size", defaultValue = "20") final int size
    ) {
        return ResponseEntity.ok(taskReadService.listTasks(projectId, userId, keyword, state, page, size));
    }

    @Operation(summary = "작업 상세 조회", description = "현재 프로젝트 멤버만 조회할 수 있습니다.")
    @ApiResponse(
            responseCode = "200",
            description = "작업 상세 조회 성공",
            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = TaskResponse.class))
    )
    @GetMapping(path = "/{taskId}")
    public ResponseEntity<TaskResponse> getTaskDetail(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable final long projectId,
            @Parameter(description = "작업 ID", example = "1")
            @PathVariable final long taskId,
            @Parameter(description = "요청자 사용자 ID", example = "3")
            @RequestParam(name = "userId") final long userId
    ) {
        return ResponseEntity.ok(taskReadService.getTaskDetail(projectId, taskId, userId));
    }

    @Operation(
            summary = "작업 내용 수정",
            description = "관리자는 DONE 이전 작업을 상태 변경 없이 수정합니다. 일반 멤버는 자신이 만든 PENDING·REJECTED 제안 또는 "
                    + "자신이 담당하는 ACCEPTED·IN_PROGRESS·IN_REVIEW 작업만 수정할 수 있으며, 승인된 작업 수정 시 PENDING으로 돌아갑니다. "
                    + "DONE 작업의 내용은 수정할 수 없습니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "작업 내용 수정 성공",
            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = TaskResponse.class))
    )
    @PutMapping(path = "/{taskId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskResponse> editTask(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable final long projectId,
            @Parameter(description = "작업 ID", example = "1")
            @PathVariable final long taskId,
            @Parameter(description = "요청자 사용자 ID", example = "3")
            @RequestParam(name = "userId") final long userId,
            @Valid @RequestBody final ReviseTaskRequest request
    ) {
        return ResponseEntity.ok(taskWriteService.editTask(projectId, taskId, request, userId));
    }

    @Operation(
            summary = "작업 삭제",
            description = "OWNER·ADMIN은 모든 상태에서 삭제할 수 있습니다. 일반 멤버는 자신이 담당한 PENDING·REJECTED 제안만 철회할 수 있습니다."
    )
    @ApiResponse(responseCode = "204", description = "작업 삭제 성공")
    @DeleteMapping(path = "/{taskId}")
    public ResponseEntity<Void> removeTask(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable final long projectId,
            @Parameter(description = "작업 ID", example = "1")
            @PathVariable final long taskId,
            @Parameter(description = "요청자 사용자 ID", example = "2")
            @RequestParam(name = "userId") final long userId,
            @Parameter(description = "조회 응답에서 받은 현재 작업 리비전", example = "0")
            @RequestParam(name = "revision") final long revision
    ) {
        taskWriteService.removeTask(projectId, taskId, revision, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "담당자 지정 또는 변경",
            description = "OWNER·ADMIN이 ACCEPTED·IN_PROGRESS·IN_REVIEW 작업의 담당자를 현재 프로젝트 멤버로 지정합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "담당자 지정 또는 변경 성공",
            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = TaskResponse.class))
    )
    @PostMapping(path = "/{taskId}/assign", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskResponse> assign(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable final long projectId,
            @Parameter(description = "작업 ID", example = "3")
            @PathVariable final long taskId,
            @Parameter(description = "요청자 사용자 ID. OWNER 또는 ADMIN이어야 합니다.", example = "2")
            @RequestParam(name = "userId") final long userId,
            @Valid @RequestBody final AssignTaskRequest request
    ) {
        return ResponseEntity.ok(taskWriteService.assign(projectId, taskId, request, userId));
    }

    @Operation(
            summary = "담당자 해제",
            description = "OWNER·ADMIN이 ACCEPTED·IN_PROGRESS·IN_REVIEW 작업의 담당자를 해제합니다. "
                    + "IN_PROGRESS·IN_REVIEW 작업은 ACCEPTED로 돌아가며 DONE 작업의 담당자는 변경할 수 없습니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "담당자 해제 성공",
            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = TaskResponse.class))
    )
    @DeleteMapping(path = "/{taskId}/assignee")
    public ResponseEntity<TaskResponse> unassign(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable final long projectId,
            @Parameter(description = "작업 ID", example = "4")
            @PathVariable final long taskId,
            @Parameter(description = "요청자 사용자 ID. OWNER 또는 ADMIN이어야 합니다.", example = "2")
            @RequestParam(name = "userId") final long userId,
            @Parameter(description = "조회 응답에서 받은 현재 작업 리비전", example = "1")
            @RequestParam(name = "revision") final long revision
    ) {
        return ResponseEntity.ok(taskWriteService.unassign(projectId, taskId, revision, userId));
    }

    @Operation(
            summary = "담당 포기",
            description = "현재 담당자가 ACCEPTED·IN_PROGRESS·IN_REVIEW 작업의 담당을 포기합니다. "
                    + "IN_PROGRESS·IN_REVIEW 작업은 ACCEPTED로 돌아가며 DONE 작업의 담당자는 변경할 수 없습니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "담당 포기 성공",
            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = TaskResponse.class))
    )
    @PostMapping(path = "/{taskId}/relinquish")
    public ResponseEntity<TaskResponse> releaseTask(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable final long projectId,
            @Parameter(description = "작업 ID", example = "4")
            @PathVariable final long taskId,
            @Parameter(description = "현재 담당자 사용자 ID", example = "3")
            @RequestParam(name = "userId") final long userId,
            @Parameter(description = "조회 응답에서 받은 현재 작업 리비전", example = "1")
            @RequestParam(name = "revision") final long revision
    ) {
        return ResponseEntity.ok(taskWriteService.releaseTask(projectId, taskId, revision, userId));
    }

    @Operation(
            summary = "작업 제안 승인",
            description = "OWNER·ADMIN이 PENDING 작업을 ACCEPTED로 바꿉니다. 제안 생성자는 자신의 제안을 승인할 수 없습니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "작업 제안 승인 성공",
            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = TaskResponse.class))
    )
    @PostMapping(path = "/{taskId}/approve", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskResponse> approve(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable final long projectId,
            @Parameter(description = "PENDING 작업 ID. 기본 실행 예상값은 1이며 시작 로그가 최종 기준입니다.", example = "1")
            @PathVariable final long taskId,
            @Parameter(description = "요청자 사용자 ID. OWNER 또는 ADMIN이어야 합니다.", example = "2")
            @RequestParam(name = "userId") final long userId,
            @Valid @RequestBody final ApproveTaskRequest request
    ) {
        return ResponseEntity.ok(taskWriteService.approve(projectId, taskId, request, userId));
    }

    @Operation(
            summary = "작업 제안 반려",
            description = "OWNER·ADMIN이 PENDING 작업을 REJECTED로 바꾸고 반려 사유를 기록합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "작업 제안 반려 성공",
            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = TaskResponse.class))
    )
    @PostMapping(path = "/{taskId}/reject", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskResponse> reject(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable final long projectId,
            @Parameter(description = "PENDING 작업 ID", example = "1")
            @PathVariable final long taskId,
            @Parameter(description = "요청자 사용자 ID. OWNER 또는 ADMIN이어야 합니다.", example = "2")
            @RequestParam(name = "userId") final long userId,
            @Valid @RequestBody final RejectTaskRequest request
    ) {
        return ResponseEntity.ok(taskWriteService.reject(projectId, taskId, request, userId));
    }

    @Operation(
            summary = "반려 제안 재요청",
            description = "제안 생성자가 REJECTED 작업을 다시 PENDING으로 바꾸며 기존 반려 사유는 제거됩니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "반려 제안 재요청 성공",
            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = TaskResponse.class))
    )
    @PostMapping(path = "/{taskId}/resubmit")
    public ResponseEntity<TaskResponse> resubmit(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable final long projectId,
            @Parameter(description = "REJECTED 작업 ID. 기본 실행 예상값은 2이며 시작 로그가 최종 기준입니다.", example = "2")
            @PathVariable final long taskId,
            @Parameter(description = "제안 생성자 사용자 ID", example = "3")
            @RequestParam(name = "userId") final long userId,
            @Parameter(description = "조회 응답에서 받은 현재 작업 리비전", example = "1")
            @RequestParam(name = "revision") final long revision
    ) {
        return ResponseEntity.ok(taskWriteService.resubmit(projectId, taskId, revision, userId));
    }

    @Operation(
            summary = "작업 시작",
            description = "현재 담당자가 ACCEPTED 작업을 IN_PROGRESS로 바꿉니다. 미할당 작업은 시작할 수 없습니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "작업 시작 성공",
            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = TaskResponse.class))
    )
    @PostMapping(path = "/{taskId}/start")
    public ResponseEntity<TaskResponse> start(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable final long projectId,
            @Parameter(description = "ACCEPTED 작업 ID. 기본 실행 예상값은 3이며 시작 로그가 최종 기준입니다.", example = "3")
            @PathVariable final long taskId,
            @Parameter(description = "현재 담당자 사용자 ID", example = "3")
            @RequestParam(name = "userId") final long userId,
            @Parameter(description = "조회 응답에서 받은 현재 작업 리비전", example = "0")
            @RequestParam(name = "revision") final long revision
    ) {
        return ResponseEntity.ok(taskWriteService.start(projectId, taskId, revision, userId));
    }

    @Operation(
            summary = "완료 검토 요청",
            description = "현재 담당자가 IN_PROGRESS 작업을 IN_REVIEW로 바꿉니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "완료 검토 요청 성공",
            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = TaskResponse.class))
    )
    @PostMapping(path = "/{taskId}/request-review")
    public ResponseEntity<TaskResponse> requestReview(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable final long projectId,
            @Parameter(description = "IN_PROGRESS 작업 ID. 기본 실행 예상값은 4이며 시작 로그가 최종 기준입니다.", example = "4")
            @PathVariable final long taskId,
            @Parameter(description = "현재 담당자 사용자 ID", example = "3")
            @RequestParam(name = "userId") final long userId,
            @Parameter(description = "조회 응답에서 받은 현재 작업 리비전", example = "1")
            @RequestParam(name = "revision") final long revision
    ) {
        return ResponseEntity.ok(taskWriteService.requestReview(projectId, taskId, revision, userId));
    }

    @Operation(
            summary = "작업 보완 요청",
            description = "OWNER·ADMIN이 IN_REVIEW 작업을 IN_PROGRESS로 되돌립니다. 현재 담당자가 있어야 합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "작업 보완 요청 성공",
            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = TaskResponse.class))
    )
    @PostMapping(path = "/{taskId}/request-changes")
    public ResponseEntity<TaskResponse> requestChanges(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable final long projectId,
            @Parameter(description = "IN_REVIEW 작업 ID. 기본 실행 예상값은 5이며 시작 로그가 최종 기준입니다.", example = "5")
            @PathVariable final long taskId,
            @Parameter(description = "요청자 사용자 ID. OWNER 또는 ADMIN이어야 합니다.", example = "2")
            @RequestParam(name = "userId") final long userId,
            @Parameter(description = "조회 응답에서 받은 현재 작업 리비전", example = "2")
            @RequestParam(name = "revision") final long revision
    ) {
        return ResponseEntity.ok(taskWriteService.requestChanges(projectId, taskId, revision, userId));
    }

    @Operation(
            summary = "작업 완료 승인",
            description = "OWNER·ADMIN이 IN_REVIEW 작업을 DONE으로 바꿉니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "작업 완료 승인 성공",
            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = TaskResponse.class))
    )
    @PostMapping(path = "/{taskId}/complete")
    public ResponseEntity<TaskResponse> complete(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable final long projectId,
            @Parameter(description = "IN_REVIEW 작업 ID. 기본 실행 예상값은 5이며 시작 로그가 최종 기준입니다.", example = "5")
            @PathVariable final long taskId,
            @Parameter(description = "요청자 사용자 ID. OWNER 또는 ADMIN이어야 합니다.", example = "2")
            @RequestParam(name = "userId") final long userId,
            @Parameter(description = "조회 응답에서 받은 현재 작업 리비전", example = "2")
            @RequestParam(name = "revision") final long revision
    ) {
        return ResponseEntity.ok(taskWriteService.complete(projectId, taskId, revision, userId));
    }
}
