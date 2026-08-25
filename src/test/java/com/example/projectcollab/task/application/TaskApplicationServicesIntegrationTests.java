package com.example.projectcollab.task.application;

import com.example.projectcollab.project.application.ProjectService;
import com.example.projectcollab.project.application.dto.AddProjectMemberRequest;
import com.example.projectcollab.project.application.dto.ChangeProjectRoleRequest;
import com.example.projectcollab.project.application.dto.CreateProjectRequest;
import com.example.projectcollab.project.domain.ProjectPermissionException;
import com.example.projectcollab.project.domain.ProjectRepository;
import com.example.projectcollab.project.domain.ProjectRole;
import com.example.projectcollab.task.application.dto.ApproveTaskRequest;
import com.example.projectcollab.task.application.dto.AssignTaskRequest;
import com.example.projectcollab.task.application.dto.CreateTaskRequest;
import com.example.projectcollab.task.application.dto.RejectTaskRequest;
import com.example.projectcollab.task.application.dto.ReviseTaskRequest;
import com.example.projectcollab.task.application.dto.TaskPageResponse;
import com.example.projectcollab.task.application.dto.TaskResponse;
import com.example.projectcollab.task.domain.Task;
import com.example.projectcollab.task.domain.TaskContent;
import com.example.projectcollab.task.domain.TaskRepository;
import com.example.projectcollab.task.persistence.SpringDataTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class TaskApplicationServicesIntegrationTests {
    private static final String OWNER = "owner";
    private static final String MEMBER = "member";

    @Autowired
    private TaskWriteService taskWriteService;

    @Autowired
    private TaskReadService taskReadService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskQueryRepository taskQueryRepository;

    @Autowired
    private SpringDataTaskRepository springDataTaskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void cleanDatabase() {
        springDataTaskRepository.deleteAll();
        projectRepository.deleteAll();
    }

    @Test
    void memberProposalCanCompleteThroughTheDocumentedLifecycle() {
        long projectId = projectWithMember();

        TaskResponse proposed = taskWriteService.createTask(
                projectId,
                new CreateTaskRequest("  첫 작업  ", "  설명  ", null, false),
                MEMBER
        );
        assertThat(proposed.state()).isEqualTo(Task.TaskState.PENDING.name());
        assertThat(proposed.title()).isEqualTo("첫 작업");
        assertThat(proposed.creatorUserId()).isEqualTo(MEMBER);
        assertThat(proposed.assigneeUserId()).isEqualTo(MEMBER);
        assertThat(proposed.revision()).isZero();

        TaskResponse approved = taskWriteService.approve(
                projectId,
                proposed.taskId(),
                new ApproveTaskRequest(null, false, proposed.revision()),
                OWNER
        );
        TaskResponse started = taskWriteService.start(projectId, proposed.taskId(), approved.revision(), MEMBER);
        TaskResponse inReview = taskWriteService.requestReview(projectId, proposed.taskId(), started.revision(), MEMBER);
        TaskResponse done = taskWriteService.complete(projectId, proposed.taskId(), inReview.revision(), OWNER);

        assertThat(approved.state()).isEqualTo(Task.TaskState.ACCEPTED.name());
        assertThat(started.state()).isEqualTo(Task.TaskState.IN_PROGRESS.name());
        assertThat(inReview.state()).isEqualTo(Task.TaskState.IN_REVIEW.name());
        assertThat(done.state()).isEqualTo(Task.TaskState.DONE.name());
        assertThat(done.assigneeUserId()).isEqualTo(MEMBER);
    }

    @Test
    void roleDeterminesInitialStateAndOnlyManagersCanSelectAnAssignee() {
        long projectId = projectWithMember();

        TaskResponse managerTask = taskWriteService.createTask(
                projectId,
                new CreateTaskRequest("관리자 작업", "설명", null, true),
                OWNER
        );

        assertThat(managerTask.state()).isEqualTo(Task.TaskState.ACCEPTED.name());
        assertThat(managerTask.assigneeUserId()).isNull();
        assertThatThrownBy(() -> taskWriteService.createTask(
                projectId,
                new CreateTaskRequest("멤버 작업", "설명", null, true),
                MEMBER
        )).isInstanceOf(TaskPermissionException.class);
    }

    @Test
    void assigneeMustBeACurrentProjectMemberAndCreatorCannotSelfApprove() {
        long projectId = projectWithMember();
        TaskResponse accepted = taskWriteService.createTask(
                projectId,
                new CreateTaskRequest("관리자 작업", "설명", null, false),
                OWNER
        );

        assertThatThrownBy(() -> taskWriteService.assign(
                projectId,
                accepted.taskId(),
                new AssignTaskRequest("outsider", accepted.revision()),
                OWNER
        )).isInstanceOf(ProjectPermissionException.class);

        TaskResponse proposed = taskWriteService.createTask(
                projectId,
                new CreateTaskRequest("승격 전 제안", "설명", null, false),
                MEMBER
        );
        projectService.changeMemberRole(
                projectId,
                OWNER,
                MEMBER,
                new ChangeProjectRoleRequest(ProjectRole.ADMIN)
        );

        assertThatThrownBy(() -> taskWriteService.approve(
                projectId,
                proposed.taskId(),
                new ApproveTaskRequest(null, false, proposed.revision()),
                MEMBER
        )).isInstanceOf(TaskPermissionException.class)
                .hasMessage("task.self_approval.forbidden");
    }

    @Test
    void staleRevisionIsRejectedAndSearchFilterPagingAreAppliedTogether() {
        long projectId = projectWithMember();
        TaskResponse alpha = taskWriteService.createTask(
                projectId,
                new CreateTaskRequest("Alpha task", "검색 설명", null, false),
                MEMBER
        );
        taskWriteService.createTask(
                projectId,
                new CreateTaskRequest("Beta task", "다른 설명", null, false),
                OWNER
        );

        TaskResponse revised = taskWriteService.editTask(
                projectId,
                alpha.taskId(),
                new ReviseTaskRequest("Alpha revised", "검색 설명", alpha.revision()),
                MEMBER
        );

        assertThatThrownBy(() -> taskWriteService.editTask(
                projectId,
                alpha.taskId(),
                new ReviseTaskRequest("오래된 변경", "덮어쓰면 안 됨", alpha.revision()),
                MEMBER
        )).isInstanceOf(TaskConflictException.class);

        TaskPageResponse page = taskReadService.listTasks(
                projectId,
                MEMBER,
                "alpha",
                Task.TaskState.PENDING,
                0,
                1
        );
        assertThat(page.totalElements()).isEqualTo(1);
        assertThat(page.content()).extracting(TaskResponse::taskId).containsExactly(alpha.taskId());
        assertThat(page.content().get(0).revision()).isEqualTo(revised.revision());
    }

    @Test
    void removingMemberUnassignsAndNormalizesTheirTasks() {
        long projectId = projectWithMember();
        TaskResponse proposed = taskWriteService.createTask(
                projectId,
                new CreateTaskRequest("진행 작업", "설명", null, false),
                MEMBER
        );
        TaskResponse approved = taskWriteService.approve(
                projectId,
                proposed.taskId(),
                new ApproveTaskRequest(null, false, proposed.revision()),
                OWNER
        );
        TaskResponse inProgress = taskWriteService.start(projectId, proposed.taskId(), approved.revision(), MEMBER);

        projectService.removeMember(projectId, OWNER, MEMBER);

        TaskResponse normalized = taskReadService.getTaskDetail(projectId, proposed.taskId(), OWNER);
        assertThat(normalized.state()).isEqualTo(Task.TaskState.ACCEPTED.name());
        assertThat(normalized.assigneeUserId()).isNull();
        assertThat(normalized.revision()).isGreaterThan(inProgress.revision());
        assertThatThrownBy(() -> taskReadService.getTaskDetail(projectId, proposed.taskId(), MEMBER))
                .isInstanceOf(ProjectPermissionException.class);
    }

    @Test
    void projectDeletionDeletesItsTasksInTheSameCommand() {
        long projectId = createProject();
        taskWriteService.createTask(
                projectId,
                new CreateTaskRequest("삭제될 작업", "설명", null, false),
                OWNER
        );

        projectService.deleteProject(projectId, OWNER);

        assertThat(springDataTaskRepository.count()).isZero();
        assertThat(projectRepository.findById(projectId)).isEmpty();
    }

    @Test
    void creatorCanReviseAndResubmitARejectedProposal() {
        long projectId = projectWithMember();
        TaskResponse proposed = taskWriteService.createTask(
                projectId,
                new CreateTaskRequest("초안", "설명", null, false),
                MEMBER
        );
        TaskResponse rejected = taskWriteService.reject(
                projectId,
                proposed.taskId(),
                new RejectTaskRequest("내용 보완", proposed.revision()),
                OWNER
        );
        TaskResponse revised = taskWriteService.editTask(
                projectId,
                proposed.taskId(),
                new ReviseTaskRequest("수정안", "보완한 설명", rejected.revision()),
                MEMBER
        );
        TaskResponse resubmitted = taskWriteService.resubmit(
                projectId,
                proposed.taskId(),
                revised.revision(),
                MEMBER
        );

        assertThat(rejected.state()).isEqualTo(Task.TaskState.REJECTED.name());
        assertThat(rejected.rejectionReason()).isEqualTo("내용 보완");
        assertThat(revised.state()).isEqualTo(Task.TaskState.REJECTED.name());
        assertThat(resubmitted.state()).isEqualTo(Task.TaskState.PENDING.name());
        assertThat(resubmitted.rejectionReason()).isNull();
    }

    @Test
    void taskWithoutAssigneeCannotBeUnassignedAgain() {
        long projectId = createProject();
        TaskResponse unassigned = taskWriteService.createTask(
                projectId,
                new CreateTaskRequest("미할당 작업", "설명", null, true),
                OWNER
        );

        assertThatThrownBy(() -> taskWriteService.unassign(
                projectId,
                unassigned.taskId(),
                unassigned.revision(),
                OWNER
        )).isInstanceOf(IllegalStateException.class)
                .hasMessage("담당자가 없는 작업은 담당자를 해제할 수 없습니다.");

        TaskResponse unchanged = taskReadService.getTaskDetail(projectId, unassigned.taskId(), OWNER);
        assertThat(unchanged.assigneeUserId()).isNull();
        assertThat(unchanged.revision()).isEqualTo(unassigned.revision());
    }

    @Test
    void taskQueriesDoNotReturnRowsAfterMembershipWasRemovedFollowingAnEarlierCheck() {
        long projectId = projectWithMember();
        TaskResponse task = taskWriteService.createTask(
                projectId,
                new CreateTaskRequest("권한 경계", "설명", null, false),
                MEMBER
        );
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        boolean wasMember = transaction.execute(status -> projectRepository
                .findById(projectId)
                .orElseThrow()
                .isMember(MEMBER));
        assertThat(wasMember).isTrue();

        projectService.removeMember(projectId, OWNER, MEMBER);

        boolean detailVisible = transaction.execute(status -> taskQueryRepository
                .findReadableTask(projectId, task.taskId(), MEMBER)
                .isPresent());
        boolean listVisible = transaction.execute(status -> !taskQueryRepository
                .searchReadable(projectId, MEMBER, null, null, 0, 20)
                .isEmpty());
        assertThat(detailVisible).isFalse();
        assertThat(listVisible).isFalse();
        assertThat(taskRepository.findByTaskIdAndProjectId(task.taskId(), projectId)).isPresent();
    }

    @Test
    void managerCanEditAndDeleteAnApprovedTaskCreatedBySomeoneElse() {
        long projectId = projectWithMember();
        TaskResponse proposed = taskWriteService.createTask(
                projectId,
                new CreateTaskRequest("멤버 제안", "설명", null, false),
                MEMBER
        );
        TaskResponse approved = taskWriteService.approve(
                projectId,
                proposed.taskId(),
                new ApproveTaskRequest(null, false, proposed.revision()),
                OWNER
        );

        TaskResponse edited = taskWriteService.editTask(
                projectId,
                approved.taskId(),
                new ReviseTaskRequest("관리자 수정", "승인 후 수정", approved.revision()),
                OWNER
        );
        taskWriteService.removeTask(projectId, edited.taskId(), edited.revision(), OWNER);

        assertThat(edited.state()).isEqualTo(Task.TaskState.ACCEPTED.name());
        assertThat(edited.title()).isEqualTo("관리자 수정");
        assertThat(taskRepository.findByTaskIdAndProjectId(edited.taskId(), projectId)).isEmpty();
    }

    @Test
    void jpaVersionRejectsAChangeThatRacesAfterTheExplicitRevisionCheck() {
        long projectId = createProject();
        TaskResponse created = taskWriteService.createTask(
                projectId,
                new CreateTaskRequest("동시 수정", "설명", null, false),
                OWNER
        );
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        Task first = transaction.execute(status -> taskRepository
                .findByTaskIdAndProjectId(created.taskId(), projectId)
                .orElseThrow());
        Task second = transaction.execute(status -> taskRepository
                .findByTaskIdAndProjectId(created.taskId(), projectId)
                .orElseThrow());

        first.edit(new TaskContent("첫 변경", "먼저 반영"));
        second.edit(new TaskContent("두 번째 변경", "덮어쓰면 안 됨"));

        transaction.executeWithoutResult(status -> taskRepository.save(first));

        assertThatThrownBy(() -> transaction.executeWithoutResult(
                status -> taskRepository.save(second)
        )).isInstanceOf(ObjectOptimisticLockingFailureException.class);
        assertThat(taskReadService.getTaskDetail(projectId, created.taskId(), OWNER).title()).isEqualTo("첫 변경");
    }

    private long projectWithMember() {
        long projectId = createProject();
        projectService.addMember(projectId, OWNER, new AddProjectMemberRequest(MEMBER));
        return projectId;
    }

    private long createProject() {
        return projectService.createProject(
                new CreateProjectRequest("프로젝트", "설명"),
                OWNER
        ).projectId();
    }
}
