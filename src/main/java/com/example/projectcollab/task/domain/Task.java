package com.example.projectcollab.task.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.Set;

@Entity(name = "TaskEntity")
@Table(name = "tasks")
public class Task {
    public enum TaskState {
        PENDING,
        REJECTED,
        ACCEPTED,
        IN_PROGRESS,
        IN_REVIEW,
        DONE
    }

    private static final Set<TaskState> ASSIGNMENT_CHANGEABLE_STATES = Set.of(
            TaskState.ACCEPTED,
            TaskState.IN_PROGRESS,
            TaskState.IN_REVIEW
    );

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long taskId;
    @Version @Column(nullable = false) private Long revision;
    @Column(nullable = false) private Long projectId;
    @Column(nullable = false) private Long creatorUserId;
    @Column(nullable = false) private String title;
    @Column(length = 1000, nullable = false) private String description;
    private Long assigneeUserId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private TaskState state;
    private String rejectionReason;
    @Column(nullable = false, updatable = false) private Instant createdAt;
    @Column(nullable = false) private Instant updatedAt;

    protected Task() {
    }

    Task(
            final Long taskId,
            final Long revision,
            final Long projectId,
            final Creator creator,
            final TaskContent content,
            final TaskAssignment assignment,
            final TaskState state,
            final String rejectionReason,
            final Instant createdAt,
            final Instant updatedAt
    ) {
        this.taskId = taskId;
        this.revision = revision;
        this.projectId = Require.positive(projectId, "프로젝트 ID는 양수여야 합니다.");
        this.creatorUserId = Require.notNull(creator, "생성자는 필수입니다.").userId();
        applyContent(content);
        this.assigneeUserId = assigneeUserId(Require.notNull(assignment, "담당자 배정 정보는 필수입니다."));
        this.state = Require.notNull(state, "작업 상태는 필수입니다.");
        this.rejectionReason = rejectionReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    private static Long assigneeUserId(final TaskAssignment assignment) {
        return assignment instanceof TaskAssignment.Assigned assigned ? assigned.assignee().userId() : null;
    }

    public static Task propose(
            final Long projectId,
            final Creator creator,
            final TaskContent content
    ) {
        Creator validatedCreator = Require.notNull(creator, "생성자는 필수입니다.");
        return new Task(
                null,
                null,
                projectId,
                validatedCreator,
                content,
                new TaskAssignment.Assigned(new Assignee(validatedCreator.userId())),
                TaskState.PENDING,
                null,
                null,
                null
        );
    }

    public static Task register(
            final Long projectId,
            final Creator creator,
            final TaskContent content
    ) {
        Creator validatedCreator = Require.notNull(creator, "생성자는 필수입니다.");
        return register(
                projectId,
                validatedCreator,
                content,
                new TaskAssignment.Assigned(new Assignee(validatedCreator.userId()))
        );
    }

    public static Task register(
            final Long projectId,
            final Creator creator,
            final TaskContent content,
            final TaskAssignment assignment
    ) {
        return new Task(
                null,
                null,
                projectId,
                creator,
                content,
                assignment,
                TaskState.ACCEPTED,
                null,
                null,
                null
        );
    }

    public static Task restore(
            final Long taskId,
            final Long revision,
            final Long projectId,
            final Creator creator,
            final TaskContent content,
            final TaskAssignment assignment,
            final TaskState state,
            final String rejectionReason,
            final Instant createdAt,
            final Instant updatedAt
    ) {
        return new Task(
                Require.positive(taskId, "작업 ID는 양수여야 합니다."),
                Require.notNull(revision, "작업 리비전은 필수입니다."),
                projectId,
                creator,
                content,
                assignment,
                state,
                rejectionReason,
                Require.notNull(createdAt, "작업 생성 시각은 필수입니다."),
                Require.notNull(updatedAt, "작업 수정 시각은 필수입니다.")
        );
    }

    public void approve() {
        Require.state(state == TaskState.PENDING, "승인 대기 상태의 작업만 승인할 수 있습니다.");
        state = TaskState.ACCEPTED;
        rejectionReason = null;
    }

    public void reject(final String rejectionReason) {
        Require.state(state == TaskState.PENDING, "승인 대기 상태의 작업만 반려할 수 있습니다.");
        String validatedReason = Require.notBlank(rejectionReason, "반려 사유는 필수입니다.");

        state = TaskState.REJECTED;
        this.rejectionReason = validatedReason;
    }

    public void revise(final TaskContent content) {
        Require.state(
                state == TaskState.PENDING || state == TaskState.REJECTED,
                "승인 대기 또는 반려 상태의 작업만 수정할 수 있습니다."
        );
        applyContent(content);
    }

    public void requestReapproval(final TaskContent content) {
        Require.state(
                ASSIGNMENT_CHANGEABLE_STATES.contains(state),
                "승인된 작업만 재승인을 요청할 수 있습니다."
        );
        Require.state(
                assignment() instanceof TaskAssignment.Assigned,
                "담당자가 없는 작업은 재승인을 요청할 수 없습니다."
        );
        applyContent(content);
        this.state = TaskState.PENDING;
        this.rejectionReason = null;
    }

    public void resubmit() {
        Require.state(state == TaskState.REJECTED, "반려 상태의 작업만 다시 요청할 수 있습니다.");
        state = TaskState.PENDING;
        rejectionReason = null;
    }

    public void edit(final TaskContent content) {
        Require.state(state != TaskState.DONE, "완료된 작업은 수정할 수 없습니다.");
        applyContent(content);
    }

    public void assign(final Assignee assignee) {
        Require.state(
                ASSIGNMENT_CHANGEABLE_STATES.contains(state),
                "승인 대기 또는 반려 상태에서는 담당자를 직접 지정할 수 없습니다."
        );
        assigneeUserId = Require.notNull(assignee, "담당자는 필수입니다.").userId();
    }

    public void unassign() {
        Require.state(
                ASSIGNMENT_CHANGEABLE_STATES.contains(state),
                "승인 대기 또는 반려 상태에서는 담당자를 직접 해제할 수 없습니다."
        );
        Require.state(
                assignment() instanceof TaskAssignment.Assigned,
                "담당자가 없는 작업은 담당자를 해제할 수 없습니다."
        );
        detachAssignee();
    }

    public void relinquish() {
        Require.state(
                ASSIGNMENT_CHANGEABLE_STATES.contains(state),
                "승인 대기 또는 반려 상태에서는 담당을 포기할 수 없습니다."
        );
        detachAssignee();
    }

    public void removeAssigneeForMembershipEnd() {
        detachAssignee();
    }

    public void start() {
        Require.state(state == TaskState.ACCEPTED, "승인된 작업만 시작할 수 있습니다.");
        Require.state(
                assignment() instanceof TaskAssignment.Assigned,
                "담당자가 없는 작업은 시작할 수 없습니다."
        );
        state = TaskState.IN_PROGRESS;
    }

    public void requestReview() {
        Require.state(state == TaskState.IN_PROGRESS, "진행 중인 작업만 검토를 요청할 수 있습니다.");
        Require.state(
                assignment() instanceof TaskAssignment.Assigned,
                "담당자가 없는 작업은 검토를 요청할 수 없습니다."
        );
        state = TaskState.IN_REVIEW;
    }

    public void complete() {
        Require.state(state == TaskState.IN_REVIEW, "검토 중인 작업만 완료할 수 있습니다.");
        state = TaskState.DONE;
    }

    public void requestChanges() {
        Require.state(state == TaskState.IN_REVIEW, "검토 중인 작업에만 보완을 요청할 수 있습니다.");
        Require.state(
                assignment() instanceof TaskAssignment.Assigned,
                "담당자가 없는 작업에는 보완을 요청할 수 없습니다."
        );
        state = TaskState.IN_PROGRESS;
    }

    private void detachAssignee() {
        assigneeUserId = null;

        if (state == TaskState.IN_PROGRESS || state == TaskState.IN_REVIEW) {
            state = TaskState.ACCEPTED;
        }
    }

    public void validateWithdrawal() {
        Require.state(
                state == TaskState.PENDING || state == TaskState.REJECTED,
                "승인 대기 또는 반려 상태의 제안만 철회할 수 있습니다."
        );
    }

    public Long projectId() {
        return projectId;
    }

    public Long taskId() {
        return taskId;
    }

    public Long revision() {
        return revision;
    }

    public Creator creator() {
        return new Creator(creatorUserId);
    }

    public TaskContent content() {
        return new TaskContent(title, description);
    }

    public TaskAssignment assignment() {
        return assigneeUserId == null ? TaskAssignment.Unassigned.INSTANCE : new TaskAssignment.Assigned(new Assignee(assigneeUserId));
    }

    public TaskState state() {
        return state;
    }

    public String rejectionReason() {
        return rejectionReason;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public Long creatorUserId() { return creatorUserId; }
    public String title() { return title; }
    public String description() { return description; }
    public Long assigneeUserId() { return assigneeUserId; }

    @PrePersist
    private void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() { updatedAt = Instant.now(); }

    private void applyContent(final TaskContent content) {
        TaskContent validatedContent = Require.notNull(content, "작업 내용은 필수입니다.");
        title = validatedContent.title();
        description = validatedContent.description();
    }
}
