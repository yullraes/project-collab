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
import com.example.projectcollab.task.persistence.TaskEntity;
import com.example.projectcollab.task.persistence.TaskMapper;
import com.example.projectcollab.task.persistence.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class TaskServiceIntegrationTests {
    private static final String OWNER = "owner";
    private static final String MEMBER = "member";

    @Autowired
    private TaskService taskService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void cleanDatabase() {
        taskRepository.deleteAll();
        projectRepository.deleteAll();
    }

    @Test
    void memberProposalCanCompleteThroughTheDocumentedLifecycle() {
        long projectId = projectWithMember();

        TaskResponse proposed = taskService.createTask(
                projectId,
                new CreateTaskRequest("  첫 작업  ", "  설명  ", null, false),
                MEMBER
        );
        assertThat(proposed.state()).isEqualTo(Task.TaskState.PENDING.name());
        assertThat(proposed.title()).isEqualTo("첫 작업");
        assertThat(proposed.creatorUserId()).isEqualTo(MEMBER);
        assertThat(proposed.assigneeUserId()).isEqualTo(MEMBER);
        assertThat(proposed.revision()).isZero();

        TaskResponse approved = taskService.approve(
                projectId,
                proposed.taskId(),
                new ApproveTaskRequest(null, false, proposed.revision()),
                OWNER
        );
        TaskResponse started = taskService.start(projectId, proposed.taskId(), approved.revision(), MEMBER);
        TaskResponse inReview = taskService.requestReview(projectId, proposed.taskId(), started.revision(), MEMBER);
        TaskResponse done = taskService.complete(projectId, proposed.taskId(), inReview.revision(), OWNER);

        assertThat(approved.state()).isEqualTo(Task.TaskState.ACCEPTED.name());
        assertThat(started.state()).isEqualTo(Task.TaskState.IN_PROGRESS.name());
        assertThat(inReview.state()).isEqualTo(Task.TaskState.IN_REVIEW.name());
        assertThat(done.state()).isEqualTo(Task.TaskState.DONE.name());
        assertThat(done.assigneeUserId()).isEqualTo(MEMBER);
    }

    @Test
    void roleDeterminesInitialStateAndOnlyManagersCanSelectAnAssignee() {
        long projectId = projectWithMember();

        TaskResponse managerTask = taskService.createTask(
                projectId,
                new CreateTaskRequest("관리자 작업", "설명", null, true),
                OWNER
        );

        assertThat(managerTask.state()).isEqualTo(Task.TaskState.ACCEPTED.name());
        assertThat(managerTask.assigneeUserId()).isNull();
        assertThatThrownBy(() -> taskService.createTask(
                projectId,
                new CreateTaskRequest("멤버 작업", "설명", null, true),
                MEMBER
        )).isInstanceOf(TaskPermissionException.class);
    }

    @Test
    void assigneeMustBeACurrentProjectMemberAndCreatorCannotSelfApprove() {
        long projectId = projectWithMember();
        TaskResponse accepted = taskService.createTask(
                projectId,
                new CreateTaskRequest("관리자 작업", "설명", null, false),
                OWNER
        );

        assertThatThrownBy(() -> taskService.assign(
                projectId,
                accepted.taskId(),
                new AssignTaskRequest("outsider", accepted.revision()),
                OWNER
        )).isInstanceOf(ProjectPermissionException.class);

        TaskResponse proposed = taskService.createTask(
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

        assertThatThrownBy(() -> taskService.approve(
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
        TaskResponse alpha = taskService.createTask(
                projectId,
                new CreateTaskRequest("Alpha task", "검색 설명", null, false),
                MEMBER
        );
        taskService.createTask(
                projectId,
                new CreateTaskRequest("Beta task", "다른 설명", null, false),
                OWNER
        );

        TaskResponse revised = taskService.editTask(
                projectId,
                alpha.taskId(),
                new ReviseTaskRequest("Alpha revised", "검색 설명", alpha.revision()),
                MEMBER
        );

        assertThatThrownBy(() -> taskService.editTask(
                projectId,
                alpha.taskId(),
                new ReviseTaskRequest("오래된 변경", "덮어쓰면 안 됨", alpha.revision()),
                MEMBER
        )).isInstanceOf(TaskConflictException.class);

        TaskPageResponse page = taskService.listTasks(
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
        TaskResponse proposed = taskService.createTask(
                projectId,
                new CreateTaskRequest("진행 작업", "설명", null, false),
                MEMBER
        );
        TaskResponse approved = taskService.approve(
                projectId,
                proposed.taskId(),
                new ApproveTaskRequest(null, false, proposed.revision()),
                OWNER
        );
        TaskResponse inProgress = taskService.start(projectId, proposed.taskId(), approved.revision(), MEMBER);

        projectService.removeMember(projectId, OWNER, MEMBER);

        TaskResponse normalized = taskService.getTaskDetail(projectId, proposed.taskId(), OWNER);
        assertThat(normalized.state()).isEqualTo(Task.TaskState.ACCEPTED.name());
        assertThat(normalized.assigneeUserId()).isNull();
        assertThat(normalized.revision()).isGreaterThan(inProgress.revision());
        assertThatThrownBy(() -> taskService.getTaskDetail(projectId, proposed.taskId(), MEMBER))
                .isInstanceOf(ProjectPermissionException.class);
    }

    @Test
    void projectDeletionDeletesItsTasksInTheSameCommand() {
        long projectId = createProject();
        taskService.createTask(
                projectId,
                new CreateTaskRequest("삭제될 작업", "설명", null, false),
                OWNER
        );

        projectService.deleteProject(projectId, OWNER);

        assertThat(taskRepository.count()).isZero();
        assertThat(projectRepository.findById(projectId)).isEmpty();
    }

    @Test
    void creatorCanReviseAndResubmitARejectedProposal() {
        long projectId = projectWithMember();
        TaskResponse proposed = taskService.createTask(
                projectId,
                new CreateTaskRequest("초안", "설명", null, false),
                MEMBER
        );
        TaskResponse rejected = taskService.reject(
                projectId,
                proposed.taskId(),
                new RejectTaskRequest("내용 보완", proposed.revision()),
                OWNER
        );
        TaskResponse revised = taskService.editTask(
                projectId,
                proposed.taskId(),
                new ReviseTaskRequest("수정안", "보완한 설명", rejected.revision()),
                MEMBER
        );
        TaskResponse resubmitted = taskService.resubmit(
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
        TaskResponse unassigned = taskService.createTask(
                projectId,
                new CreateTaskRequest("미할당 작업", "설명", null, true),
                OWNER
        );

        assertThatThrownBy(() -> taskService.unassign(
                projectId,
                unassigned.taskId(),
                unassigned.revision(),
                OWNER
        )).isInstanceOf(IllegalStateException.class)
                .hasMessage("담당자가 없는 작업은 담당자를 해제할 수 없습니다.");

        TaskResponse unchanged = taskService.getTaskDetail(projectId, unassigned.taskId(), OWNER);
        assertThat(unchanged.assigneeUserId()).isNull();
        assertThat(unchanged.revision()).isEqualTo(unassigned.revision());
    }

    @Test
    void taskQueriesDoNotReturnRowsAfterMembershipWasRemovedFollowingAnEarlierCheck() {
        long projectId = projectWithMember();
        TaskResponse task = taskService.createTask(
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

        boolean detailVisible = transaction.execute(status -> taskRepository
                .findReadableTask(projectId, task.taskId(), MEMBER)
                .isPresent());
        boolean listVisible = transaction.execute(status -> !taskRepository
                .searchReadable(projectId, MEMBER, null, null, PageRequest.of(0, 20))
                .isEmpty());
        assertThat(detailVisible).isFalse();
        assertThat(listVisible).isFalse();
        assertThat(taskRepository.findByTaskIdAndProjectId(task.taskId(), projectId)).isPresent();
    }

    @Test
    void managerCanEditAndDeleteAnApprovedTaskCreatedBySomeoneElse() {
        long projectId = projectWithMember();
        TaskResponse proposed = taskService.createTask(
                projectId,
                new CreateTaskRequest("멤버 제안", "설명", null, false),
                MEMBER
        );
        TaskResponse approved = taskService.approve(
                projectId,
                proposed.taskId(),
                new ApproveTaskRequest(null, false, proposed.revision()),
                OWNER
        );

        TaskResponse edited = taskService.editTask(
                projectId,
                approved.taskId(),
                new ReviseTaskRequest("관리자 수정", "승인 후 수정", approved.revision()),
                OWNER
        );
        taskService.removeTask(projectId, edited.taskId(), edited.revision(), OWNER);

        assertThat(edited.state()).isEqualTo(Task.TaskState.ACCEPTED.name());
        assertThat(edited.title()).isEqualTo("관리자 수정");
        assertThat(taskRepository.findByTaskIdAndProjectId(edited.taskId(), projectId)).isEmpty();
    }

    @Test
    void jpaVersionRejectsAChangeThatRacesAfterTheExplicitRevisionCheck() {
        long projectId = createProject();
        TaskResponse created = taskService.createTask(
                projectId,
                new CreateTaskRequest("동시 수정", "설명", null, false),
                OWNER
        );
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        TaskEntity first = transaction.execute(status -> taskRepository
                .findByTaskIdAndProjectId(created.taskId(), projectId)
                .orElseThrow());
        TaskEntity second = transaction.execute(status -> taskRepository
                .findByTaskIdAndProjectId(created.taskId(), projectId)
                .orElseThrow());

        Task firstChange = TaskMapper.toDomain(first);
        firstChange.edit(new TaskContent("첫 변경", "먼저 반영"));
        TaskMapper.apply(first, firstChange);
        Task secondChange = TaskMapper.toDomain(second);
        secondChange.edit(new TaskContent("두 번째 변경", "덮어쓰면 안 됨"));
        TaskMapper.apply(second, secondChange);

        transaction.executeWithoutResult(status -> taskRepository.saveAndFlush(first));

        assertThatThrownBy(() -> transaction.executeWithoutResult(
                status -> taskRepository.saveAndFlush(second)
        )).isInstanceOf(ObjectOptimisticLockingFailureException.class);
        assertThat(taskService.getTaskDetail(projectId, created.taskId(), OWNER).title()).isEqualTo("첫 변경");
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
