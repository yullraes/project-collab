package com.example.projectcollab.bootstrap;

import com.example.projectcollab.project.application.ProjectService;
import com.example.projectcollab.project.application.dto.AddProjectMemberRequest;
import com.example.projectcollab.project.application.dto.ChangeProjectRoleRequest;
import com.example.projectcollab.project.application.dto.CreateProjectRequest;
import com.example.projectcollab.project.application.dto.ProjectResponse;
import com.example.projectcollab.project.domain.ProjectRole;
import com.example.projectcollab.task.application.TaskWriteService;
import com.example.projectcollab.task.application.dto.CreateTaskRequest;
import com.example.projectcollab.task.application.dto.RejectTaskRequest;
import com.example.projectcollab.task.application.dto.TaskResponse;
import com.example.projectcollab.user.application.UserService;
import com.example.projectcollab.user.application.dto.CreateUserRequest;
import com.example.projectcollab.user.application.dto.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(prefix = "app.demo-data", name = "enabled", havingValue = "true")
public class DemoDataInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);

    private final UserService userService;
    private final ProjectService projectService;
    private final TaskWriteService taskWriteService;

    public DemoDataInitializer(
            final UserService userService,
            final ProjectService projectService,
            final TaskWriteService taskWriteService
    ) {
        this.userService = userService;
        this.projectService = projectService;
        this.taskWriteService = taskWriteService;
    }

    @Override
    @Transactional
    public void run(final ApplicationArguments args) {
        UserResponse owner = userService.createUser(
                new CreateUserRequest("데모 소유자", "owner@project-collab.example")
        );
        UserResponse admin = userService.createUser(
                new CreateUserRequest("데모 관리자", "admin@project-collab.example")
        );
        UserResponse member = userService.createUser(
                new CreateUserRequest("데모 멤버", "member@project-collab.example")
        );

        ProjectResponse project = projectService.createProject(
                new CreateProjectRequest(
                        "Swagger 데모 프로젝트",
                        "Swagger UI에서 프로젝트 권한과 작업 상태 전이를 확인하기 위한 예제 프로젝트"
                ),
                owner.userId()
        );

        projectService.addMember(
                project.projectId(),
                owner.userId(),
                new AddProjectMemberRequest(admin.userId())
        );
        projectService.changeMemberRole(
                project.projectId(),
                owner.userId(),
                admin.userId(),
                new ChangeProjectRoleRequest(ProjectRole.ADMIN)
        );
        projectService.addMember(
                project.projectId(),
                owner.userId(),
                new AddProjectMemberRequest(member.userId())
        );

        TaskResponse pending = createMemberProposal(
                project.projectId(),
                member.userId(),
                "승인 대기 예제",
                "일반 멤버가 제안하여 관리자의 판단을 기다리는 작업"
        );

        TaskResponse rejectedProposal = createMemberProposal(
                project.projectId(),
                member.userId(),
                "반려 예제",
                "반려 사유와 재요청 흐름을 확인하기 위한 작업"
        );
        TaskResponse rejected = taskWriteService.reject(
                project.projectId(),
                rejectedProposal.taskId(),
                new RejectTaskRequest("완료 조건을 더 구체적으로 작성해 주세요.", rejectedProposal.revision()),
                admin.userId()
        );

        TaskResponse accepted = createManagerTask(
                project.projectId(),
                owner.userId(),
                member.userId(),
                "승인 완료 예제",
                "담당자가 지정되어 바로 시작할 수 있는 작업"
        );

        TaskResponse inProgressSeed = createManagerTask(
                project.projectId(),
                owner.userId(),
                member.userId(),
                "진행 중 예제",
                "담당자가 작업을 시작한 상태의 예제"
        );
        TaskResponse inProgress = taskWriteService.start(
                project.projectId(),
                inProgressSeed.taskId(),
                inProgressSeed.revision(),
                member.userId()
        );

        TaskResponse inReviewSeed = createManagerTask(
                project.projectId(),
                owner.userId(),
                member.userId(),
                "검토 중 예제",
                "담당자가 완료 검토를 요청한 상태의 예제"
        );
        TaskResponse inReviewStarted = taskWriteService.start(
                project.projectId(),
                inReviewSeed.taskId(),
                inReviewSeed.revision(),
                member.userId()
        );
        TaskResponse inReview = taskWriteService.requestReview(
                project.projectId(),
                inReviewStarted.taskId(),
                inReviewStarted.revision(),
                member.userId()
        );

        TaskResponse doneSeed = createManagerTask(
                project.projectId(),
                owner.userId(),
                member.userId(),
                "완료 예제",
                "관리자가 최종 완료로 승인한 작업의 예제"
        );
        TaskResponse doneStarted = taskWriteService.start(
                project.projectId(),
                doneSeed.taskId(),
                doneSeed.revision(),
                member.userId()
        );
        TaskResponse doneInReview = taskWriteService.requestReview(
                project.projectId(),
                doneStarted.taskId(),
                doneStarted.revision(),
                member.userId()
        );
        TaskResponse done = taskWriteService.complete(
                project.projectId(),
                doneInReview.taskId(),
                doneInReview.revision(),
                admin.userId()
        );

        log.info("Swagger demo data initialized: projectId={}", project.projectId());
        log.info(
                "Swagger demo users: OWNER userId={}, ADMIN userId={}, MEMBER userId={}",
                owner.userId(),
                admin.userId(),
                member.userId()
        );
        log.info(
                "Swagger demo tasks: PENDING taskId={} revision={}, REJECTED taskId={} revision={}, "
                        + "ACCEPTED taskId={} revision={}, IN_PROGRESS taskId={} revision={}, "
                        + "IN_REVIEW taskId={} revision={}, DONE taskId={} revision={}",
                pending.taskId(),
                pending.revision(),
                rejected.taskId(),
                rejected.revision(),
                accepted.taskId(),
                accepted.revision(),
                inProgress.taskId(),
                inProgress.revision(),
                inReview.taskId(),
                inReview.revision(),
                done.taskId(),
                done.revision()
        );
    }

    private TaskResponse createMemberProposal(
            final long projectId,
            final long memberUserId,
            final String title,
            final String description
    ) {
        return taskWriteService.createTask(
                projectId,
                new CreateTaskRequest(title, description, null, false),
                memberUserId
        );
    }

    private TaskResponse createManagerTask(
            final long projectId,
            final long managerUserId,
            final long assigneeUserId,
            final String title,
            final String description
    ) {
        return taskWriteService.createTask(
                projectId,
                new CreateTaskRequest(title, description, assigneeUserId, false),
                managerUserId
        );
    }
}
