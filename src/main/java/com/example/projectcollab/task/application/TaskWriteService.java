package com.example.projectcollab.task.application;

import com.example.projectcollab.project.domain.ProjectNotFoundException;
import com.example.projectcollab.project.domain.ProjectPermissionException;
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
import com.example.projectcollab.task.domain.TaskProjectSnapshot;
import com.example.projectcollab.task.domain.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskWriteService {
    private final TaskRepository taskRepository;

    public TaskWriteService(final TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional
    public TaskResponse createTask(final long projectId, final CreateTaskRequest request, final long userId) {
        TaskProjectSnapshot project = loadProjectForUpdate(projectId, userId, "task.create.forbidden");
        TaskContent content = new TaskContent(normalize(request.title()), normalize(request.description()));
        Creator creator = new Creator(String.valueOf(userId));

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
    public TaskResponse editTask(final long projectId, final long taskId, final ReviseTaskRequest request, final long userId) {
        TaskProjectSnapshot project = loadProjectForUpdate(projectId, userId, "task.modify.forbidden");
        Task task = loadTask(projectId, taskId);
        verifyRevision(task, request.revision());
        TaskContent content = new TaskContent(normalize(request.title()), normalize(request.description()));
        if (project.isManager(userId)) {
            task.edit(content);
        } else {
            if (task.state() == Task.TaskState.PENDING || task.state() == Task.TaskState.REJECTED) {
                if (!isCreator(task, userId)) {
                    throw new TaskPermissionException("task.creator.only");
                }
                task.revise(content);
            } else {
                requireCurrentAssignee(task, userId);
                task.requestReapproval(content);
            }
        }
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse assign(final long projectId, final long taskId, final AssignTaskRequest request, final long userId) {
        TaskProjectSnapshot project = loadProjectForUpdate(projectId, userId, "task.assign.forbidden");
        requireManager(project, userId, "task.assign.forbidden");
        Task task = loadTask(projectId, taskId);
        verifyRevision(task, request.revision());

        Long assigneeUserId = normalizeOptionalUserId(request.assigneeUserId());
        if (assigneeUserId == null) {
            throw new IllegalArgumentException("task.assignee.required");
        }
        requireProjectMember(project, assigneeUserId, "task.assignee.not_member");
        task.assign(new Assignee(String.valueOf(assigneeUserId)));
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse unassign(final long projectId, final long taskId, final long revision, final long userId) {
        TaskProjectSnapshot project = loadProjectForUpdate(projectId, userId, "task.unassign.forbidden");
        requireManager(project, userId, "task.unassign.forbidden");
        Task task = loadTask(projectId, taskId);
        verifyRevision(task, revision);
        task.unassign();
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse releaseTask(final long projectId, final long taskId, final long revision, final long userId) {
        loadProjectForUpdate(projectId, userId, "task.relinquish.forbidden");
        Task task = loadTask(projectId, taskId);
        verifyRevision(task, revision);
        requireCurrentAssignee(task, userId);
        task.relinquish();
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse approve(final long projectId, final long taskId, final ApproveTaskRequest request, final long userId) {
        TaskProjectSnapshot project = loadProjectForUpdate(projectId, userId, "task.approve.forbidden");
        requireManager(project, userId, "task.approve.forbidden");
        Task task = loadTask(projectId, taskId);
        verifyRevision(task, request.revision());
        if (isCreator(task, userId)) {
            throw new TaskPermissionException("task.self_approval.forbidden");
        }

        task.approve();
        applyApprovalAssignment(project, task, request);
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse reject(final long projectId, final long taskId, final RejectTaskRequest request, final long userId) {
        TaskProjectSnapshot project = loadProjectForUpdate(projectId, userId, "task.reject.forbidden");
        requireManager(project, userId, "task.reject.forbidden");
        Task task = loadTask(projectId, taskId);
        verifyRevision(task, request.revision());
        task.reject(normalize(request.rejectionReason()));
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse resubmit(final long projectId, final long taskId, final long revision, final long userId) {
        loadProjectForUpdate(projectId, userId, "task.resubmit.forbidden");
        Task task = loadTask(projectId, taskId);
        verifyRevision(task, revision);
        if (!isCreator(task, userId)) {
            throw new TaskPermissionException("task.creator.only");
        }
        task.resubmit();
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse start(final long projectId, final long taskId, final long revision, final long userId) {
        loadProjectForUpdate(projectId, userId, "task.start.forbidden");
        Task task = loadTask(projectId, taskId);
        verifyRevision(task, revision);
        requireCurrentAssignee(task, userId);
        task.start();
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse requestReview(final long projectId, final long taskId, final long revision, final long userId) {
        loadProjectForUpdate(projectId, userId, "task.review.forbidden");
        Task task = loadTask(projectId, taskId);
        verifyRevision(task, revision);
        requireCurrentAssignee(task, userId);
        task.requestReview();
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse requestChanges(final long projectId, final long taskId, final long revision, final long userId) {
        TaskProjectSnapshot project = loadProjectForUpdate(projectId, userId, "task.request_changes.forbidden");
        requireManager(project, userId, "task.request_changes.forbidden");
        Task task = loadTask(projectId, taskId);
        verifyRevision(task, revision);
        task.requestChanges();
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse complete(final long projectId, final long taskId, final long revision, final long userId) {
        TaskProjectSnapshot project = loadProjectForUpdate(projectId, userId, "task.complete.forbidden");
        requireManager(project, userId, "task.complete.forbidden");
        Task task = loadTask(projectId, taskId);
        verifyRevision(task, revision);
        task.complete();
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public void removeTask(final long projectId, final long taskId, final long revision, final long userId) {
        TaskProjectSnapshot project = loadProjectForUpdate(projectId, userId, "task.delete.forbidden");
        Task task = loadTask(projectId, taskId);
        verifyRevision(task, revision);
        if (!project.isManager(userId)) {
            requireCurrentAssignee(task, userId);
            task.validateWithdrawal();
        }
        taskRepository.delete(task);
    }

    private TaskAssignment resolveCreationAssignment(
            final TaskProjectSnapshot project,
            final long creatorUserId,
            final CreateTaskRequest request
    ) {
        Long assigneeUserId = normalizeOptionalUserId(request.assigneeUserId());
        boolean unassigned = Boolean.TRUE.equals(request.unassigned());
        if (unassigned && assigneeUserId != null) {
            throw new IllegalArgumentException("task.assignee.choice.conflict");
        }
        if (unassigned) {
            return TaskAssignment.Unassigned.INSTANCE;
        }

        long selectedAssignee = assigneeUserId == null ? creatorUserId : assigneeUserId;
        requireProjectMember(project, selectedAssignee, "task.assignee.not_member");
        return new TaskAssignment.Assigned(new Assignee(String.valueOf(selectedAssignee)));
    }

    private void applyApprovalAssignment(
            final TaskProjectSnapshot project,
            final Task task,
            final ApproveTaskRequest request
    ) {
        Long assigneeUserId = normalizeOptionalUserId(request.assigneeUserId());
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
            task.assign(new Assignee(String.valueOf(assigneeUserId)));
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

    private void requireCurrentAssignee(final Task task, final long userId) {
        if (!(task.assignment() instanceof TaskAssignment.Assigned assigned)) {
            throw new TaskPermissionException("task.assignee.required");
        }
        if (Long.parseLong(assigned.assignee().username()) != userId) {
            throw new TaskPermissionException("task.assignee.only");
        }
    }

    private Task loadTask(final long projectId, final long taskId) {
        return taskRepository.findByTaskIdAndProjectId(taskId, projectId)
                .orElseThrow(TaskNotFoundException::new);
    }

    private TaskProjectSnapshot loadProjectForUpdate(final long projectId, final long userId, final String code) {
        TaskProjectSnapshot project = taskRepository.findProjectSnapshotForUpdate(projectId)
                .orElseThrow(ProjectNotFoundException::new);
        requireProjectMember(project, userId, code);
        return project;
    }

    private void requireManager(final TaskProjectSnapshot project, final long userId, final String code) {
        if (!project.isManager(userId)) {
            throw new TaskPermissionException(code);
        }
    }

    private void requireProjectMember(final TaskProjectSnapshot project, final long userId, final String code) {
        if (!project.isMember(userId)) {
            throw new ProjectPermissionException(code);
        }
    }

    private Long normalizeOptionalUserId(final Long value) {
        if (value == null) {
            return null;
        }
        if (value <= 0) {
            throw new IllegalArgumentException("task.assignee.required");
        }
        return value;
    }

    private boolean isCreator(final Task task, final long userId) {
        return Long.parseLong(task.creator().username()) == userId;
    }

    private String normalize(final String value) {
        return value == null ? null : value.trim();
    }
}
