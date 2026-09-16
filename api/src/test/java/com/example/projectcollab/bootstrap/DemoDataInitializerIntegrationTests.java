package com.example.projectcollab.bootstrap;

import com.example.projectcollab.project.domain.ProjectRole;
import com.example.projectcollab.project.persistence.ProjectEntity;
import com.example.projectcollab.project.persistence.ProjectMemberEntity;
import com.example.projectcollab.project.persistence.ProjectMemberRepository;
import com.example.projectcollab.project.persistence.ProjectRepository;
import com.example.projectcollab.task.persistence.SpringDataTaskRepository;
import com.example.projectcollab.task.domain.Task;
import com.example.projectcollab.user.persistence.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.demo-data.enabled=true",
        "spring.datasource.url=jdbc:h2:mem:demo-data-enabled;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
@AutoConfigureMockMvc
class DemoDataInitializerIntegrationTests {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private SpringDataTaskRepository taskRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void initializesDemoUsersProjectRolesAndTaskStates() throws Exception {
        assertThat(userRepository.count()).isEqualTo(3);
        assertThat(projectRepository.count()).isEqualTo(1);
        assertThat(projectMemberRepository.count()).isEqualTo(3);
        assertThat(taskRepository.count()).isEqualTo(6);

        ProjectEntity project = projectRepository.findAll().get(0);
        List<ProjectMemberEntity> members = projectMemberRepository
                .findAllByProjectIdOrderByProjectMemberId(project.projectId());
        assertThat(members)
                .extracting(ProjectMemberEntity::role)
                .containsExactly(ProjectRole.OWNER, ProjectRole.ADMIN, ProjectRole.MEMBER);
        assertThat(taskRepository.findAll())
                .extracting(Task::state)
                .containsExactlyInAnyOrder(
                        Task.TaskState.PENDING,
                        Task.TaskState.REJECTED,
                        Task.TaskState.ACCEPTED,
                        Task.TaskState.IN_PROGRESS,
                        Task.TaskState.IN_REVIEW,
                        Task.TaskState.DONE
                );

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Project Collab API"))
                .andExpect(jsonPath("$.paths['/api/users'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/api/users'].post.responses['200']").doesNotExist())
                .andExpect(jsonPath("$.paths['/api/projects/{projectId}'].delete.responses['204']").exists())
                .andExpect(jsonPath("$.paths['/api/projects/{projectId}'].delete.responses['200']").doesNotExist())
                .andExpect(jsonPath(
                        "$.paths['/api/projects/{projectId}/tasks/{taskId}/approve'].post.responses['409']"
                ).exists())
                .andExpect(jsonPath("$.components.schemas.ApiErrorResponse.properties.violations").exists())
                .andExpect(jsonPath("$.components.schemas.CreateTaskRequest.properties.title.example")
                        .value("결제 오류 재현"));

        long adminUserId = members.stream()
                .filter(member -> member.role() == ProjectRole.ADMIN)
                .findFirst()
                .orElseThrow()
                .userId();
        Task pending = taskRepository.findAll().stream()
                .filter(task -> task.state() == Task.TaskState.PENDING)
                .findFirst()
                .orElseThrow();
        String approveBody = "{\"revision\":" + pending.revision() + "}";

        mockMvc.perform(post(
                        "/api/projects/{projectId}/tasks/{taskId}/approve",
                        project.projectId(),
                        pending.taskId()
                )
                        .queryParam("userId", String.valueOf(adminUserId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(approveBody))
                .andExpect(status().isOk());

        mockMvc.perform(post(
                        "/api/projects/{projectId}/tasks/{taskId}/approve",
                        project.projectId(),
                        pending.taskId()
                )
                        .queryParam("userId", String.valueOf(adminUserId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(approveBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("task.revision.conflict"))
                .andExpect(jsonPath("$.message").value("작업이 다른 요청에 의해 변경되었습니다."))
                .andExpect(jsonPath("$.violations").isEmpty());
    }
}
