package com.example.projectcollab.task.api;

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

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable final long projectId,
            @RequestParam(name = "userId") final String userId,
            @RequestBody final CreateTaskRequest request
    ) {
        TaskResponse response = taskWriteService.createTask(projectId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<TaskPageResponse> listTasks(
            @PathVariable final long projectId,
            @RequestParam(name = "userId") final String userId,
            @RequestParam(name = "keyword", required = false) final String keyword,
            @RequestParam(name = "state", required = false) final Task.TaskState state,
            @RequestParam(name = "page", defaultValue = "0") final int page,
            @RequestParam(name = "size", defaultValue = "20") final int size
    ) {
        return ResponseEntity.ok(taskReadService.listTasks(projectId, userId, keyword, state, page, size));
    }

    @GetMapping(path = "/{taskId}")
    public ResponseEntity<TaskResponse> getTaskDetail(
            @PathVariable final long projectId,
            @PathVariable final long taskId,
            @RequestParam(name = "userId") final String userId
    ) {
        return ResponseEntity.ok(taskReadService.getTaskDetail(projectId, taskId, userId));
    }

    @PutMapping(path = "/{taskId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskResponse> editTask(
            @PathVariable final long projectId,
            @PathVariable final long taskId,
            @RequestParam(name = "userId") final String userId,
            @RequestBody final ReviseTaskRequest request
    ) {
        return ResponseEntity.ok(taskWriteService.editTask(projectId, taskId, request, userId));
    }

    @DeleteMapping(path = "/{taskId}")
    public ResponseEntity<Void> removeTask(
            @PathVariable final long projectId,
            @PathVariable final long taskId,
            @RequestParam(name = "userId") final String userId,
            @RequestParam(name = "revision") final long revision
    ) {
        taskWriteService.removeTask(projectId, taskId, revision, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/{taskId}/assign", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskResponse> assign(
            @PathVariable final long projectId,
            @PathVariable final long taskId,
            @RequestParam(name = "userId") final String userId,
            @RequestBody final AssignTaskRequest request
    ) {
        return ResponseEntity.ok(taskWriteService.assign(projectId, taskId, request, userId));
    }

    @DeleteMapping(path = "/{taskId}/assignee")
    public ResponseEntity<TaskResponse> unassign(
            @PathVariable final long projectId,
            @PathVariable final long taskId,
            @RequestParam(name = "userId") final String userId,
            @RequestParam(name = "revision") final long revision
    ) {
        return ResponseEntity.ok(taskWriteService.unassign(projectId, taskId, revision, userId));
    }

    @PostMapping(path = "/{taskId}/relinquish")
    public ResponseEntity<TaskResponse> releaseTask(
            @PathVariable final long projectId,
            @PathVariable final long taskId,
            @RequestParam(name = "userId") final String userId,
            @RequestParam(name = "revision") final long revision
    ) {
        return ResponseEntity.ok(taskWriteService.releaseTask(projectId, taskId, revision, userId));
    }

    @PostMapping(path = "/{taskId}/approve", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskResponse> approve(
            @PathVariable final long projectId,
            @PathVariable final long taskId,
            @RequestParam(name = "userId") final String userId,
            @RequestBody final ApproveTaskRequest request
    ) {
        return ResponseEntity.ok(taskWriteService.approve(projectId, taskId, request, userId));
    }

    @PostMapping(path = "/{taskId}/reject", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskResponse> reject(
            @PathVariable final long projectId,
            @PathVariable final long taskId,
            @RequestParam(name = "userId") final String userId,
            @RequestBody final RejectTaskRequest request
    ) {
        return ResponseEntity.ok(taskWriteService.reject(projectId, taskId, request, userId));
    }

    @PostMapping(path = "/{taskId}/resubmit")
    public ResponseEntity<TaskResponse> resubmit(
            @PathVariable final long projectId,
            @PathVariable final long taskId,
            @RequestParam(name = "userId") final String userId,
            @RequestParam(name = "revision") final long revision
    ) {
        return ResponseEntity.ok(taskWriteService.resubmit(projectId, taskId, revision, userId));
    }

    @PostMapping(path = "/{taskId}/start")
    public ResponseEntity<TaskResponse> start(
            @PathVariable final long projectId,
            @PathVariable final long taskId,
            @RequestParam(name = "userId") final String userId,
            @RequestParam(name = "revision") final long revision
    ) {
        return ResponseEntity.ok(taskWriteService.start(projectId, taskId, revision, userId));
    }

    @PostMapping(path = "/{taskId}/request-review")
    public ResponseEntity<TaskResponse> requestReview(
            @PathVariable final long projectId,
            @PathVariable final long taskId,
            @RequestParam(name = "userId") final String userId,
            @RequestParam(name = "revision") final long revision
    ) {
        return ResponseEntity.ok(taskWriteService.requestReview(projectId, taskId, revision, userId));
    }

    @PostMapping(path = "/{taskId}/request-changes")
    public ResponseEntity<TaskResponse> requestChanges(
            @PathVariable final long projectId,
            @PathVariable final long taskId,
            @RequestParam(name = "userId") final String userId,
            @RequestParam(name = "revision") final long revision
    ) {
        return ResponseEntity.ok(taskWriteService.requestChanges(projectId, taskId, revision, userId));
    }

    @PostMapping(path = "/{taskId}/complete")
    public ResponseEntity<TaskResponse> complete(
            @PathVariable final long projectId,
            @PathVariable final long taskId,
            @RequestParam(name = "userId") final String userId,
            @RequestParam(name = "revision") final long revision
    ) {
        return ResponseEntity.ok(taskWriteService.complete(projectId, taskId, revision, userId));
    }
}
