package com.example.projectcollab.task.application;

import com.example.projectcollab.project.domain.ProjectNotFoundException;
import com.example.projectcollab.project.domain.ProjectPermissionException;
import com.example.projectcollab.project.domain.ProjectRepository;
import com.example.projectcollab.project.domain.ProjectResource;
import com.example.projectcollab.task.application.dto.AssignTaskRequest;
import com.example.projectcollab.task.application.dto.CreateTaskRequest;
import com.example.projectcollab.task.application.dto.RejectTaskRequest;
import com.example.projectcollab.task.application.dto.ReviseTaskRequest;
import com.example.projectcollab.task.application.dto.TaskResponse;
import com.example.projectcollab.task.domain.Assignee;
import com.example.projectcollab.task.domain.Creator;
import com.example.projectcollab.task.domain.Task;
import com.example.projectcollab.task.domain.TaskAssignment;
import com.example.projectcollab.task.domain.TaskContent;
import com.example.projectcollab.task.domain.TaskMapper;
import com.example.projectcollab.task.domain.TaskNotFoundException;
import com.example.projectcollab.task.domain.TaskPermissionException;
import com.example.projectcollab.task.domain.TaskRepository;
import com.example.projectcollab.task.domain.TaskResource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public final class TaskService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    public TaskService(final TaskRepository taskRepository, final ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
    }

    public TaskResponse createTask(
            final long projectId,
            final CreateTaskRequest request,
            final String userId
    ) {
        ensureModifiableProject(projectId, userId, "task.create.forbidden");

        String title = normalize(request.title());
        String description = normalize(request.description());
        TaskContent content = TaskContent.of(title, description);

        Task task = Boolean.TRUE.equals(request.acceptImmediately())
                ? Task.register(projectId, new Creator(userId), content)
                : Task.propose(projectId, new Creator(userId), content);

        TaskResource created = TaskMapper.toResource(task);
        return TaskResponse.from(taskRepository.save(created));
    }

    public List<TaskResponse> listTasks(final long projectId, final String userId) {
        ensureReadableProject(projectId, userId, "task.read.forbidden");
        return taskRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());
    }

    public TaskResponse getTaskDetail(final long projectId, final long taskId, final String userId) {
        TaskResource current = loadReadableTask(projectId, taskId, userId);
        return TaskResponse.from(current);
    }

    public TaskResponse editTask(
            final long projectId,
            final long taskId,
            final ReviseTaskRequest request,
            final String userId
    ) {
        TaskResource current = loadWritableTask(projectId, taskId, userId);
        Task domainTask = TaskMapper.toDomain(current);
        TaskContent content = TaskContent.of(request.title(), request.description());
        domainTask.revise(content);
        TaskMapper.apply(current, domainTask);
        return TaskResponse.from(taskRepository.save(current));
    }

    public TaskResponse assign(
            final long projectId,
            final long taskId,
            final AssignTaskRequest request,
            final String userId
    ) {
        TaskResource current = loadWritableTask(projectId, taskId, userId);
        Task domainTask = TaskMapper.toDomain(current);
        domainTask.assign(new Assignee(request.assigneeUserId()));
        TaskMapper.apply(current, domainTask);
        return TaskResponse.from(taskRepository.save(current));
    }

    public TaskResponse unassign(final long projectId, final long taskId, final String userId) {
        TaskResource current = loadWritableTask(projectId, taskId, userId);
        Task domainTask = TaskMapper.toDomain(current);
        domainTask.unassign();
        TaskMapper.apply(current, domainTask);
        return TaskResponse.from(taskRepository.save(current));
    }

    public TaskResponse releaseTask(final long projectId, final long taskId, final String userId) {
        TaskResource current = loadWritableTask(projectId, taskId, userId);
        Task domainTask = TaskMapper.toDomain(current);
        requireCurrentAssignee(domainTask, userId);
        domainTask.relinquish();
        TaskMapper.apply(current, domainTask);
        return TaskResponse.from(taskRepository.save(current));
    }

    public TaskResponse approve(final long projectId, final long taskId, final String userId) {
        TaskResource current = loadWritableTask(projectId, taskId, userId);
        Task domainTask = TaskMapper.toDomain(current);
        domainTask.approve();
        TaskMapper.apply(current, domainTask);
        return TaskResponse.from(taskRepository.save(current));
    }

    public TaskResponse reject(
            final long projectId,
            final long taskId,
            final RejectTaskRequest request,
            final String userId
    ) {
        TaskResource current = loadWritableTask(projectId, taskId, userId);
        Task domainTask = TaskMapper.toDomain(current);
        domainTask.reject(request.rejectionReason());
        TaskMapper.apply(current, domainTask);
        return TaskResponse.from(taskRepository.save(current));
    }

    public TaskResponse resubmit(final long projectId, final long taskId, final String userId) {
        TaskResource current = loadWritableTask(projectId, taskId, userId);
        Task domainTask = TaskMapper.toDomain(current);
        domainTask.resubmit();
        TaskMapper.apply(current, domainTask);
        return TaskResponse.from(taskRepository.save(current));
    }

    public TaskResponse start(final long projectId, final long taskId, final String userId) {
        TaskResource current = loadWritableTask(projectId, taskId, userId);
        Task domainTask = TaskMapper.toDomain(current);
        requireCurrentAssignee(domainTask, userId);
        domainTask.start();
        TaskMapper.apply(current, domainTask);
        return TaskResponse.from(taskRepository.save(current));
    }

    public TaskResponse requestReview(final long projectId, final long taskId, final String userId) {
        TaskResource current = loadWritableTask(projectId, taskId, userId);
        Task domainTask = TaskMapper.toDomain(current);
        requireCurrentAssignee(domainTask, userId);
        domainTask.requestReview();
        TaskMapper.apply(current, domainTask);
        return TaskResponse.from(taskRepository.save(current));
    }

    public TaskResponse requestChanges(final long projectId, final long taskId, final String userId) {
        TaskResource current = loadWritableTask(projectId, taskId, userId);
        Task domainTask = TaskMapper.toDomain(current);
        requireCurrentAssignee(domainTask, userId);
        domainTask.requestChanges();
        TaskMapper.apply(current, domainTask);
        return TaskResponse.from(taskRepository.save(current));
    }

    public TaskResponse complete(final long projectId, final long taskId, final String userId) {
        TaskResource current = loadWritableTask(projectId, taskId, userId);
        Task domainTask = TaskMapper.toDomain(current);
        requireCurrentAssignee(domainTask, userId);
        domainTask.complete();
        TaskMapper.apply(current, domainTask);
        return TaskResponse.from(taskRepository.save(current));
    }

    public void removeTask(final long projectId, final long taskId, final String userId) {
        TaskResource current = loadWritableTask(projectId, taskId, userId);
        if (!userId.equals(current.creatorUserId())) {
            throw new TaskPermissionException(userId, "task.withdraw.forbidden");
        }

        Task domainTask = TaskMapper.toDomain(current);
        domainTask.validateWithdrawal();
        taskRepository.delete(current);
    }

    private void requireCurrentAssignee(final Task task, final String userId) {
        if (!(task.assignment() instanceof TaskAssignment.Assigned assigned)) {
            throw new TaskPermissionException(userId, "task.assignee.required");
        }

        if (!assigned.assignee().username().equals(userId)) {
            throw new TaskPermissionException(userId, "task.assignee.only");
        }
    }

    private TaskResource loadReadableTask(final long projectId, final long taskId, final String userId) {
        ensureReadableProject(projectId, userId, "task.read.forbidden");
        TaskResource task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        ensureSameProject(task, projectId, userId);
        return task;
    }

    private TaskResource loadWritableTask(final long projectId, final long taskId, final String userId) {
        ensureModifiableProject(projectId, userId, "task.modify.forbidden");
        TaskResource task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        ensureSameProject(task, projectId, userId);
        return task;
    }

    private void ensureSameProject(final TaskResource task, final long projectId, final String userId) {
        if (!task.projectId().equals(projectId)) {
            throw new TaskPermissionException(userId, "task.project.mismatch");
        }
    }

    private void ensureReadableProject(final long projectId, final String userId, final String code) {
        ProjectResource project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        if (!canAccessProject(project, userId)) {
            throw new ProjectPermissionException(userId, code);
        }
    }

    private void ensureModifiableProject(final long projectId, final String userId, final String code) {
        ProjectResource project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        if (!canAccessProject(project, userId)) {
            throw new ProjectPermissionException(userId, code);
        }
    }

    private boolean canAccessProject(final ProjectResource project, final String userId) {
        return project.isOwner(userId) || project.isAdmin(userId);
    }

    private String normalize(final String value) {
        return value == null ? null : value.trim();
    }
}
