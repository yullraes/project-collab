package com.example.projectcollab.task.application;

import com.example.projectcollab.task.application.dto.AssignTaskRequest;
import com.example.projectcollab.task.application.dto.CreateTaskRequest;
import com.example.projectcollab.task.application.dto.RejectTaskRequest;
import com.example.projectcollab.task.application.dto.ReviseTaskRequest;
import com.example.projectcollab.task.application.dto.TaskResponse;
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

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
public final class TaskController {
    private final TaskService taskService;

    public TaskController(final TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable final long projectId,
            @RequestParam(name = "userId") final String userId,
            @RequestBody final CreateTaskRequest request
    ) {
        TaskResponse response = taskService.createTask(projectId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> listTasks(
            @PathVariable final long projectId,
            @RequestParam(name = "userId") final String userId
    ) {
        return ResponseEntity.ok(taskService.listTasks(projectId, userId));
    }

    @GetMapping(path = "/{taskId}")
    public ResponseEntity<TaskResponse> getTaskDetail(
            @PathVariable final long projectId,
            @PathVariable final long taskId,
            @RequestParam(name = "userId") final String userId
    ) {
        return ResponseEntity.ok(taskService.getTaskDetail(projectId, taskId, userId));
    }

    @PutMapping(path = "/{taskId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskResponse> editTask(
            @PathVariable final long projectId,
            @PathVariable final long taskId,
            @RequestParam(name = "userId") final String userId,
            @RequestBody final ReviseTaskRequest request
    ) {
        return ResponseEntity.ok(taskService.editTask(projectId, taskId, request, userId));
    }

    @DeleteMapping(path = "/{taskId}")
    public ResponseEntity<Void> removeTask(
            @PathVariable final long projectId,
            @PathVariable final long taskId,
            @RequestParam(name = "userId") final String userId
    ) {
        taskService.removeTask(projectId, taskId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/{taskId}/assign", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskResponse> assign(
            @PathVariable final long projectId,
            @PathVariable final long taskId,
            @RequestParam(name = "userId") final String userId,
            @RequestBody final AssignTaskRequest request
    ) {
        return ResponseEntity.ok(taskService.assign(projectId, taskId, request, userId));
    }

    @DeleteMapping(path = "/{taskId}/assignee")
    public ResponseEntity<TaskResponse> unassign(
            @PathVariable final long projectId,
            @PathVariable final long taskId,
            @RequestParam(name = "userId") final String userId
    ) {
        return ResponseEntity.ok(taskService.unassign(projectId, taskId, userId));
    }

    @PostMapping(path = "/{taskId}/relinquish")
    public ResponseEntity<TaskResponse> releaseTask(
            @PathVariable final long projectId,
            @PathVariable final long taskId,
            @RequestParam(name = "userId") final String userId
    ) {
        return ResponseEntity.ok(taskService.releaseTask(projectId, taskId, userId));
    }

    @PostMapping(path = "/{taskId}/approve")
    public ResponseEntity<TaskResponse> approve(
            @PathVariable final long projectId,
            @PathVariable final long taskId,
            @RequestParam(name = "userId") final String userId
    ) {
        return ResponseEntity.ok(taskService.approve(projectId, taskId, userId));
    }

    @PostMapping(path = "/{taskId}/reject", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskResponse> reject(
            @PathVariable final long projectId,
            @PathVariable final long taskId,
            @RequestParam(name = "userId") final String userId,
            @RequestBody final RejectTaskRequest request
    ) {
        return ResponseEntity.ok(taskService.reject(projectId, taskId, request, userId));
    }

    @PostMapping(path = "/{taskId}/resubmit")
    public ResponseEntity<TaskResponse> resubmit(
            @PathVariable final long projectId,
            @PathVariable final long taskId,
            @RequestParam(name = "userId") final String userId
    ) {
        return ResponseEntity.ok(taskService.resubmit(projectId, taskId, userId));
    }

    @PostMapping(path = "/{taskId}/start")
    public ResponseEntity<TaskResponse> start(
            @PathVariable final long projectId,
            @PathVariable final long taskId,
            @RequestParam(name = "userId") final String userId
    ) {
        return ResponseEntity.ok(taskService.start(projectId, taskId, userId));
    }

    @PostMapping(path = "/{taskId}/request-review")
    public ResponseEntity<TaskResponse> requestReview(
            @PathVariable final long projectId,
            @PathVariable final long taskId,
            @RequestParam(name = "userId") final String userId
    ) {
        return ResponseEntity.ok(taskService.requestReview(projectId, taskId, userId));
    }

    @PostMapping(path = "/{taskId}/request-changes")
    public ResponseEntity<TaskResponse> requestChanges(
            @PathVariable final long projectId,
            @PathVariable final long taskId,
            @RequestParam(name = "userId") final String userId
    ) {
        return ResponseEntity.ok(taskService.requestChanges(projectId, taskId, userId));
    }

    @PostMapping(path = "/{taskId}/complete")
    public ResponseEntity<TaskResponse> complete(
            @PathVariable final long projectId,
            @PathVariable final long taskId,
            @RequestParam(name = "userId") final String userId
    ) {
        return ResponseEntity.ok(taskService.complete(projectId, taskId, userId));
    }
}
