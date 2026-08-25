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
import com.example.projectcollab.task.application.dto.TaskPageResponse;
import com.example.projectcollab.task.application.dto.TaskResponse;
import com.example.projectcollab.task.domain.Assignee;
import com.example.projectcollab.task.domain.Creator;
import com.example.projectcollab.task.domain.Task;
import com.example.projectcollab.task.domain.TaskAssignment;
import com.example.projectcollab.task.domain.TaskContent;
import com.example.projectcollab.task.persistence.TaskEntity;
import com.example.projectcollab.task.persistence.TaskMapper;
import com.example.projectcollab.task.persistence.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TaskService {
    private static final int MAX_PAGE_SIZE = 100;

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    public TaskService(final TaskRepository taskRepository, final ProjectRepository projectRepository) {
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

        return saveAndRespond(TaskMapper.toEntity(task));
    }

    public TaskPageResponse listTasks(
            final long projectId,
            final String userId,
            final String keyword,
            final Task.TaskState state,
            final int page,
            final int size
    ) {
        loadReadableProject(projectId, userId, "task.read.forbidden");
        if (page < 0) {
            throw new IllegalArgumentException("task.page.invalid");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("task.page_size.invalid");
        }

        String normalizedKeyword = normalize(keyword);
        if (normalizedKeyword != null && normalizedKeyword.isBlank()) {
            normalizedKeyword = null;
        }
        String stateName = state == null ? null : state.name();
        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("taskId"))
        );
        Page<TaskResponse> tasks = taskRepository.searchReadable(
                        projectId,
                        userId,
                        normalizedKeyword,
                        stateName,
                        pageRequest
                )
                .map(TaskResponse::from);
        return TaskPageResponse.from(tasks);
    }

    public TaskResponse getTaskDetail(final long projectId, final long taskId, final String userId) {
        loadReadableProject(projectId, userId, "task.read.forbidden");
        TaskEntity task = taskRepository.findReadableTask(projectId, taskId, userId)
                .orElseThrow(TaskNotFoundException::new);
        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse editTask(
            final long projectId,
            final long taskId,
            final ReviseTaskRequest request,
            final String userId
    ) {
        ProjectResource project = loadProjectForUpdate(projectId, userId, "task.modify.forbidden");
        TaskEntity current = loadTask(projectId, taskId);
        verifyRevision(current, request.revision());

        Task task = TaskMapper.toDomain(current);
        TaskContent content = new TaskContent(normalize(request.title()), normalize(request.description()));
        if (project.isManager(userId)) {
            task.edit(content);
        } else {
            if (!task.creator().username().equals(userId)) {
                throw new TaskPermissionException("task.creator.only");
            }
            task.revise(content);
        }
        TaskMapper.apply(current, task);
        return saveAndRespond(current);
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
        TaskEntity current = loadTask(projectId, taskId);
        verifyRevision(current, request.revision());

        String assigneeUserId = normalizeOptionalUserId(request.assigneeUserId());
        if (assigneeUserId == null) {
            throw new IllegalArgumentException("task.assignee.required");
        }
        requireProjectMember(project, assigneeUserId, "task.assignee.not_member");
        Task task = TaskMapper.toDomain(current);
        task.assign(new Assignee(assigneeUserId));
        TaskMapper.apply(current, task);
        return saveAndRespond(current);
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
        TaskEntity current = loadTask(projectId, taskId);
        verifyRevision(current, revision);

        Task task = TaskMapper.toDomain(current);
        task.unassign();
        TaskMapper.apply(current, task);
        return saveAndRespond(current);
    }

    @Transactional
    public TaskResponse releaseTask(
            final long projectId,
            final long taskId,
            final long revision,
            final String userId
    ) {
        loadProjectForUpdate(projectId, userId, "task.relinquish.forbidden");
        TaskEntity current = loadTask(projectId, taskId);
        verifyRevision(current, revision);

        Task task = TaskMapper.toDomain(current);
        requireCurrentAssignee(task, userId);
        task.relinquish();
        TaskMapper.apply(current, task);
        return saveAndRespond(current);
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
        TaskEntity current = loadTask(projectId, taskId);
        verifyRevision(current, request.revision());
        if (current.creatorUserId().equals(userId)) {
            throw new TaskPermissionException("task.self_approval.forbidden");
        }

        Task task = TaskMapper.toDomain(current);
        task.approve();
        applyApprovalAssignment(project, task, request);
        TaskMapper.apply(current, task);
        return saveAndRespond(current);
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
        TaskEntity current = loadTask(projectId, taskId);
        verifyRevision(current, request.revision());

        Task task = TaskMapper.toDomain(current);
        task.reject(normalize(request.rejectionReason()));
        TaskMapper.apply(current, task);
        return saveAndRespond(current);
    }

    @Transactional
    public TaskResponse resubmit(
            final long projectId,
            final long taskId,
            final long revision,
            final String userId
    ) {
        loadProjectForUpdate(projectId, userId, "task.resubmit.forbidden");
        TaskEntity current = loadTask(projectId, taskId);
        verifyRevision(current, revision);

        Task task = TaskMapper.toDomain(current);
        if (!task.creator().username().equals(userId)) {
            throw new TaskPermissionException("task.creator.only");
        }
        task.resubmit();
        TaskMapper.apply(current, task);
        return saveAndRespond(current);
    }

    @Transactional
    public TaskResponse start(
            final long projectId,
            final long taskId,
            final long revision,
            final String userId
    ) {
        loadProjectForUpdate(projectId, userId, "task.start.forbidden");
        TaskEntity current = loadTask(projectId, taskId);
        verifyRevision(current, revision);

        Task task = TaskMapper.toDomain(current);
        requireCurrentAssignee(task, userId);
        task.start();
        TaskMapper.apply(current, task);
        return saveAndRespond(current);
    }

    @Transactional
    public TaskResponse requestReview(
            final long projectId,
            final long taskId,
            final long revision,
            final String userId
    ) {
        loadProjectForUpdate(projectId, userId, "task.review.forbidden");
        TaskEntity current = loadTask(projectId, taskId);
        verifyRevision(current, revision);

        Task task = TaskMapper.toDomain(current);
        requireCurrentAssignee(task, userId);
        task.requestReview();
        TaskMapper.apply(current, task);
        return saveAndRespond(current);
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
        TaskEntity current = loadTask(projectId, taskId);
        verifyRevision(current, revision);

        Task task = TaskMapper.toDomain(current);
        task.requestChanges();
        TaskMapper.apply(current, task);
        return saveAndRespond(current);
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
        TaskEntity current = loadTask(projectId, taskId);
        verifyRevision(current, revision);

        Task task = TaskMapper.toDomain(current);
        task.complete();
        TaskMapper.apply(current, task);
        return saveAndRespond(current);
    }

    @Transactional
    public void removeTask(
            final long projectId,
            final long taskId,
            final long revision,
            final String userId
    ) {
        ProjectResource project = loadProjectForUpdate(projectId, userId, "task.delete.forbidden");
        TaskEntity current = loadTask(projectId, taskId);
        verifyRevision(current, revision);

        Task task = TaskMapper.toDomain(current);
        if (!project.isManager(userId)) {
            requireCurrentAssignee(task, userId);
            task.validateWithdrawal();
        }
        taskRepository.delete(current);
        taskRepository.flush();
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

    private TaskResponse saveAndRespond(final TaskEntity task) {
        TaskEntity saved = taskRepository.saveAndFlush(task);
        return TaskResponse.from(saved);
    }

    private void verifyRevision(final TaskEntity task, final Long requestedRevision) {
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

    private TaskEntity loadTask(final long projectId, final long taskId) {
        return taskRepository.findByTaskIdAndProjectId(taskId, projectId)
                .orElseThrow(TaskNotFoundException::new);
    }

    private ProjectResource loadReadableProject(
            final long projectId,
            final String userId,
            final String code
    ) {
        ProjectResource project = projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);
        requireProjectMember(project, userId, code);
        return project;
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
