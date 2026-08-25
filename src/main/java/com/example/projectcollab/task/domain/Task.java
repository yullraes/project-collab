package com.example.projectcollab.task.domain;

public final class Task {
    public enum TaskState {
        PENDING,
        REJECTED,
        ACCEPTED,
        IN_PROGRESS,
        IN_REVIEW,
        DONE
    }

    private final Long projectId;
    private final Creator creator;

    private TaskContent content;
    private TaskAssignment assignment;
    private TaskState state;
    private String rejectionReason;

    Task(
            final Long projectId,
            final Creator creator,
            final TaskContent content,
            final TaskAssignment assignment,
            final TaskState state,
            final String rejectionReason
    ) {
        this.projectId = Require.positive(projectId, "프로젝트 ID는 양수여야 합니다.");
        this.creator = Require.notNull(creator, "생성자는 필수입니다.");
        this.content = Require.notNull(content, "작업 내용은 필수입니다.");
        this.assignment = Require.notNull(assignment, "담당자 배정 정보는 필수입니다.");
        this.state = Require.notNull(state, "작업 상태는 필수입니다.");
        this.rejectionReason = rejectionReason;
    }

    public static Task propose(
            final Long projectId,
            final Creator creator,
            final TaskContent content
    ) {
        return new Task(
                projectId,
                creator,
                content,
                TaskAssignment.assigned(Assignee.from(creator)),
                TaskState.PENDING,
                null
        );
    }

    public static Task register(
            final Long projectId,
            final Creator creator,
            final TaskContent content
    ) {
        return new Task(
                projectId,
                creator,
                content,
                TaskAssignment.assigned(Assignee.from(creator)),
                TaskState.ACCEPTED,
                null
        );
    }

    public void approve() {
        requireState(TaskState.PENDING, "승인 대기 상태의 작업만 승인할 수 있습니다.");
        state = TaskState.ACCEPTED;
        rejectionReason = null;
    }

    public void reject(final String rejectionReason) {
        requireState(TaskState.PENDING, "승인 대기 상태의 작업만 반려할 수 있습니다.");
        String validatedReason = Require.notBlank(rejectionReason, "반려 사유는 필수입니다.");

        state = TaskState.REJECTED;
        this.rejectionReason = validatedReason;
    }

    public void revise(final TaskContent content) {
        Require.state(
                state == TaskState.PENDING || state == TaskState.REJECTED,
                "승인 대기 또는 반려 상태의 작업만 수정할 수 있습니다."
        );
        this.content = Require.notNull(content, "작업 내용은 필수입니다.");
    }

    public void resubmit() {
        Require.state(state == TaskState.REJECTED, "반려 상태의 작업만 다시 요청할 수 있습니다.");
        state = TaskState.PENDING;
        rejectionReason = null;
    }

    public void edit(final TaskContent content) {
        this.content = Require.notNull(content, "작업 내용은 필수입니다.");
    }

    public void assign(final Assignee assignee) {
        requireActive("승인 대기, 반려 또는 완료 상태에서는 담당자를 직접 지정할 수 없습니다.");
        assignment = TaskAssignment.assigned(
                Require.notNull(assignee, "담당자는 필수입니다.")
        );
    }

    public void unassign() {
        requireActive("승인 대기, 반려 또는 완료 상태에서는 담당자를 직접 해제할 수 없습니다.");
        detachAssignee();
    }

    public void relinquish() {
        requireActive("승인 대기, 반려 또는 완료 상태에서는 담당을 포기할 수 없습니다.");
        detachAssignee();
    }

    public void start() {
        requireState(TaskState.ACCEPTED, "승인된 작업만 시작할 수 있습니다.");
        requireAssignee("담당자가 없는 작업은 시작할 수 없습니다.");
        state = TaskState.IN_PROGRESS;
    }

    public void requestReview() {
        requireState(TaskState.IN_PROGRESS, "진행 중인 작업만 검토를 요청할 수 있습니다.");
        requireAssignee("담당자가 없는 작업은 검토를 요청할 수 없습니다.");
        state = TaskState.IN_REVIEW;
    }

    public void complete() {
        requireState(TaskState.IN_REVIEW, "검토 중인 작업만 완료할 수 있습니다.");
        state = TaskState.DONE;
    }

    public void requestChanges() {
        requireState(TaskState.IN_REVIEW, "검토 중인 작업에만 보완을 요청할 수 있습니다.");
        requireAssignee("담당자가 없는 작업에는 보완을 요청할 수 없습니다.");
        state = TaskState.IN_PROGRESS;
    }

    private void detachAssignee() {
        assignment = TaskAssignment.unassigned();

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

    public Creator creator() {
        return creator;
    }

    public TaskContent content() {
        return content;
    }

    public TaskAssignment assignment() {
        return assignment;
    }

    public TaskState state() {
        return state;
    }

    String rejectionReason() {
        return rejectionReason;
    }

    private void requireState(final TaskState expected, final String message) {
        Require.state(state == expected, message);
    }

    private void requireActive(final String message) {
        Require.state(
                state == TaskState.ACCEPTED
                        || state == TaskState.IN_PROGRESS
                        || state == TaskState.IN_REVIEW,
                message
        );
    }

    private void requireAssignee(final String message) {
        Require.state(assignment instanceof TaskAssignment.Assigned, message);
    }
}
