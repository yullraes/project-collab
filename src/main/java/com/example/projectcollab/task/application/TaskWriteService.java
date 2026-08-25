package com.example.projectcollab.task.application;

import com.example.projectcollab.project.domain.ProjectNotFoundException;
import com.example.projectcollab.project.domain.ProjectPermissionException;
import com.example.projectcollab.project.domain.ProjectRepository;
import com.example.projectcollab.project.domain.ProjectResource;
import com.example.projectcollab.task.application.dto.ApproveTaskRequest;
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
import com.example.projectcollab.task.domain.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskWriteService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    public TaskWriteService(
            final TaskRepository taskRepository,
            final ProjectRepository projectRepository
    ) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional
    public TaskResponse createTask(
            final long projectId,
            final CreateTaskRequest request,
            final String userId
    ) {
        ProjectResource project = loadProjectForUpdate(projectId, userId, "task.create.forbidden");
        TaskContent content = new TaskContent(normalize(request.title()), normalize(request.description()));
        Creator creator = new Creator(userId);

        Task task;
        if (project.isManager(userId)) {
            TaskAssignment assignment = resolveCreationAssignment(project, userId, request);
            task = Task.register(projectId, creator, content, assignment);
        } else {
            if (Boolean.TRUE.equals(request.unassigned())
                    || normalizeOptionalUserId(request.assigneeUserId()) != null) {
                throw new TaskPermissionException("task.member.assignee_selection.forbidden");
            }
            task = Task.propose(projectId, creator, content);
        }

        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse editTask(
            final long projectId,
            final long taskId,
            final ReviseTaskRequest request,
            final String userId
    ) {
        ProjectResource project = loadProjectForUpdate(projectId, userId, "task.modify.forbidden");
        Task task = loadTask(projectId, taskId);
        verifyRevision(task, request.revision());
        TaskContent content = new TaskContent(normalize(request.title()), normalize(request.description()));
        if (project.isManager(userId)) {
            task.edit(content);
        } else {
            if (!task.creator().username().equals(userId)) {
                throw new TaskPermissionException("task.creator.only");
            }
            task.revise(content);
        }
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse assign(
            final long projectId,
            final long taskId,
            final AssignTaskRequest request,
            final String userId
    ) {
        ProjectResource project = loadProjectForUpdate(projectId, userId, "task.assign.forbidden");
        requireManager(project, userId, "task.assign.forbidden");
        Task task = loadTask(projectId, taskId);
        verifyRevision(task, request.revision());

        String assigneeUserId = normalizeOptionalUserId(request.assigneeUserId());
        if (assigneeUserId == null) {
            throw new IllegalArgumentException("task.assignee.required");
        }
        requireProjectMember(project, assigneeUserId, "task.assignee.not_member");
        task.assign(new Assignee(assigneeUserId));
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse unassign(
            final long projectId,
            final long taskId,
            final long revision,
            final String userId
    ) {
        ProjectResource project = loadProjectForUpdate(projectId, userId, "task.unassign.forbidden");
        requireManager(project, userId, "task.unassign.forbidden");
        Task task = loadTask(projectId, taskId);
        verifyRevision(task, revision);
        task.unassign();
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse releaseTask(
            final long projectId,
            final long taskId,
            final long revision,
            final String userId
    ) {
        loadProjectForUpdate(projectId, userId, "task.relinquish.forbidden");
        Task task = loadTask(projectId, taskId);
        verifyRevision(task, revision);
        requireCurrentAssignee(task, userId);
        task.relinquish();
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse approve(
            final long projectId,
            final long taskId,
            final ApproveTaskRequest request,
            final String userId
    ) {
        ProjectResource project = loadProjectForUpdate(projectId, userId, "task.approve.forbidden");
        requireManager(project, userId, "task.approve.forbidden");
        Task task = loadTask(projectId, taskId);
        verifyRevision(task, request.revision());
        if (task.creator().username().equals(userId)) {
            throw new TaskPermissionException("task.self_approval.forbidden");
        }

        task.approve();
        applyApprovalAssignment(project, task, request);
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse reject(
            final long projectId,
            final long taskId,
            final RejectTaskRequest request,
            final String userId
    ) {
        ProjectResource project = loadProjectForUpdate(projectId, userId, "task.reject.forbidden");
        requireManager(project, userId, "task.reject.forbidden");
        Task task = loadTask(projectId, taskId);
        verifyRevision(task, request.revision());
        task.reject(normalize(request.rejectionReason()));
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse resubmit(
            final long projectId,
            final long taskId,
            final long revision,
            final String userId
    ) {
        loadProjectForUpdate(projectId, userId, "task.resubmit.forbidden");
        Task task = loadTask(projectId, taskId);
        verifyRevision(task, revision);
        if (!task.creator().username().equals(userId)) {
            throw new TaskPermissionException("task.creator.only");
        }
        task.resubmit();
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse start(
            final long projectId,
            final long taskId,
            final long revision,
            final String userId
    ) {
        loadProjectForUpdate(projectId, userId, "task.start.forbidden");
        Task task = loadTask(projectId, taskId);
        verifyRevision(task, revision);
        requireCurrentAssignee(task, userId);
        task.start();
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse requestReview(
            final long projectId,
            final long taskId,
            final long revision,
            final String userId
    ) {
        loadProjectForUpdate(projectId, userId, "task.review.forbidden");
        Task task = loadTask(projectId, taskId);
        verifyRevision(task, revision);
        requireCurrentAssignee(task, userId);
        task.requestReview();
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse requestChanges(
            final long projectId,
            final long taskId,
            final long revision,
            final String userId
    ) {
        ProjectResource project = loadProjectForUpdate(projectId, userId, "task.request_changes.forbidden");
        requireManager(project, userId, "task.request_changes.forbidden");
        Task task = loadTask(projectId, taskId);
        verifyRevision(task, revision);
        task.requestChanges();
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse complete(
            final long projectId,
            final long taskId,
            final long revision,
            final String userId
    ) {
        ProjectResource project = loadProjectForUpdate(projectId, userId, "task.complete.forbidden");
        requireManager(project, userId, "task.complete.forbidden");
        Task task = loadTask(projectId, taskId);
        verifyRevision(task, revision);
        task.complete();
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public void removeTask(
            final long projectId,
            final long taskId,
            final long revision,
            final String userId
    ) {
        ProjectResource project = loadProjectForUpdate(projectId, userId, "task.delete.forbidden");
        Task task = loadTask(projectId, taskId);
        verifyRevision(task, revision);
        if (!project.isManager(userId)) {
            requireCurrentAssignee(task, userId);
            task.validateWithdrawal();
        }
        taskRepository.delete(task);
    }

    private TaskAssignment resolveCreationAssignment(
            final ProjectResource project,
            final String creatorUserId,
            final CreateTaskRequest request
    ) {
        String assigneeUserId = normalizeOptionalUserId(request.assigneeUserId());
        boolean unassigned = Boolean.TRUE.equals(request.unassigned());
        if (unassigned && assigneeUserId != null) {
            throw new IllegalArgumentException("task.assignee.choice.conflict");
        }
        if (unassigned) {
            return TaskAssignment.Unassigned.INSTANCE;
        }

        String selectedAssignee = assigneeUserId == null ? creatorUserId : assigneeUserId;
        requireProjectMember(project, selectedAssignee, "task.assignee.not_member");
        return new TaskAssignment.Assigned(new Assignee(selectedAssignee));
    }

    private void applyApprovalAssignment(
            final ProjectResource project,
            final Task task,
            final ApproveTaskRequest request
    ) {
        String assigneeUserId = normalizeOptionalUserId(request.assigneeUserId());
        boolean unassigned = Boolean.TRUE.equals(request.unassigned());
        if (unassigned && assigneeUserId != null) {
            throw new IllegalArgumentException("task.assignee.choice.conflict");
        }
        if (unassigned) {
            task.unassign();
            return;
        }
        if (assigneeUserId != null) {
            requireProjectMember(project, assigneeUserId, "task.assignee.not_member");
            task.assign(new Assignee(assigneeUserId));
        }
    }

    private void verifyRevision(final Task task, final Long requestedRevision) {
        if (requestedRevision == null || requestedRevision < 0) {
            throw new IllegalArgumentException("task.revision.required");
        }
        long actualRevision = task.revision();
        if (requestedRevision != actualRevision) {
            throw new TaskConflictException();
        }
    }

    private void requireCurrentAssignee(final Task task, final String userId) {
        if (!(task.assignment() instanceof TaskAssignment.Assigned assigned)) {
            throw new TaskPermissionException("task.assignee.required");
        }
        if (!assigned.assignee().username().equals(userId)) {
            throw new TaskPermissionException("task.assignee.only");
        }
    }

    private Task loadTask(final long projectId, final long taskId) {
        return taskRepository.findByTaskIdAndProjectId(taskId, projectId)
                .orElseThrow(TaskNotFoundException::new);
    }

    private ProjectResource loadProjectForUpdate(
            final long projectId,
            final String userId,
            final String code
    ) {
        ProjectResource project = projectRepository.findByIdForUpdate(projectId)
                .orElseThrow(ProjectNotFoundException::new);
        requireProjectMember(project, userId, code);
        return project;
    }

    private void requireManager(
            final ProjectResource project,
            final String userId,
            final String code
    ) {
        if (!project.isManager(userId)) {
            throw new TaskPermissionException(code);
        }
    }

    private void requireProjectMember(
            final ProjectResource project,
            final String userId,
            final String code
    ) {
        if (!project.isMember(userId)) {
            throw new ProjectPermissionException(code);
        }
    }

    private String normalizeOptionalUserId(final String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("task.assignee.required");
        }
        return normalized;
    }

    private String normalize(final String value) {
        return value == null ? null : value.trim();
    }
}
