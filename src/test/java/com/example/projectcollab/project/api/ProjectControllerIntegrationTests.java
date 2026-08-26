package com.example.projectcollab.project.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.projectcollab.project.persistence.ProjectMemberRepository;
import com.example.projectcollab.project.persistence.ProjectRepository;
import com.example.projectcollab.user.persistence.UserEntity;
import com.example.projectcollab.user.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectControllerIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private UserRepository userRepository;

    private long ownerId;
    private long memberId;
    private long outsiderId;

    @BeforeEach
    void cleanDatabase() {
        projectMemberRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        ownerId = userRepository.saveAndFlush(UserEntity.create("owner", "owner@example.com")).userId();
        memberId = userRepository.saveAndFlush(UserEntity.create("member", "member@example.com")).userId();
        outsiderId = userRepository.saveAndFlush(UserEntity.create("outsider", "outsider@example.com")).userId();
    }

    @Test
    @DisplayName("프로젝트를 생성하면 생성자가 OWNER가 되고 멤버십 목록에서 조회된다")
    void createsProjectAndOwnerMembership() throws Exception {
        String response = mockMvc.perform(post("/api/projects")
                        .queryParam("actorUserId", String.valueOf(ownerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"  프로젝트  \",\"description\":\"  설명  \"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectId").isNumber())
                .andExpect(jsonPath("$.name").value("프로젝트"))
                .andExpect(jsonPath("$.description").value("설명"))
                .andExpect(jsonPath("$.ownerUserId").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long projectId = objectMapper.readTree(response).get("projectId").asLong();

        mockMvc.perform(get("/api/projects/{projectId}/members", projectId)
                        .queryParam("actorUserId", String.valueOf(ownerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(ownerId))
                .andExpect(jsonPath("$[0].role").value("OWNER"))
                .andExpect(jsonPath("$[0].joinedAt").isNotEmpty());
    }

    @Test
    @DisplayName("OWNER가 등록된 사용자를 MEMBER로 추가하고 역할을 변경할 수 있다")
    void managesProjectMembers() throws Exception {
        long projectId = createProject();

        mockMvc.perform(post("/api/projects/{projectId}/members", projectId)
                        .queryParam("actorUserId", String.valueOf(ownerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":" + memberId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(memberId))
                .andExpect(jsonPath("$.role").value("MEMBER"));

        mockMvc.perform(put("/api/projects/{projectId}/members/{userId}/role", projectId, memberId)
                        .queryParam("actorUserId", String.valueOf(ownerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("중복 멤버와 프로젝트 외부 사용자의 접근을 거부한다")
    void rejectsDuplicateMemberAndOutsiderAccess() throws Exception {
        long projectId = createProject();
        String memberRequest = "{\"userId\":" + memberId + "}";

        mockMvc.perform(post("/api/projects/{projectId}/members", projectId)
                        .queryParam("actorUserId", String.valueOf(ownerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(memberRequest))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/projects/{projectId}/members", projectId)
                        .queryParam("actorUserId", String.valueOf(ownerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(memberRequest))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("project.member.already_exists"));

        mockMvc.perform(get("/api/projects/{projectId}", projectId)
                        .queryParam("actorUserId", String.valueOf(outsiderId)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("project.access.forbidden"));
    }

    @Test
    @DisplayName("내 프로젝트 목록에는 현재 멤버인 프로젝트만 포함된다")
    void listsProjectsForCurrentMember() throws Exception {
        long firstProjectId = createProject();
        long secondProjectId = createProject();

        mockMvc.perform(post("/api/projects/{projectId}/members", firstProjectId)
                        .queryParam("actorUserId", String.valueOf(ownerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":" + memberId + "}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/projects")
                        .queryParam("actorUserId", String.valueOf(memberId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].projectId").value(firstProjectId))
                .andExpect(jsonPath("$[1]").doesNotExist());

        mockMvc.perform(get("/api/projects")
                        .queryParam("actorUserId", String.valueOf(ownerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].projectId").value(secondProjectId))
                .andExpect(jsonPath("$[1].projectId").value(firstProjectId));
    }

    @Test
    @DisplayName("MEMBER는 프로젝트를 수정할 수 없고 OWNER는 자신을 제거할 수 없다")
    void protectsProjectManagerActions() throws Exception {
        long projectId = createProject();

        mockMvc.perform(post("/api/projects/{projectId}/members", projectId)
                        .queryParam("actorUserId", String.valueOf(ownerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":" + memberId + "}"))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/api/projects/{projectId}", projectId)
                        .queryParam("actorUserId", String.valueOf(memberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"변경\",\"description\":\"변경\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("project.access.forbidden"));

        mockMvc.perform(delete("/api/projects/{projectId}/members/{targetUserId}", projectId, ownerId)
                        .queryParam("actorUserId", String.valueOf(ownerId)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("project.owner.cannot_remove"));
    }

    @Test
    @DisplayName("등록되지 않은 사용자는 프로젝트를 만들거나 멤버로 추가할 수 없다")
    void rejectsUnknownUsers() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .queryParam("actorUserId", "999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"프로젝트\",\"description\":\"설명\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("user.not_found"));

        long projectId = createProject();
        mockMvc.perform(post("/api/projects/{projectId}/members", projectId)
                        .queryParam("actorUserId", String.valueOf(ownerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":999999}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("user.not_found"));
    }

    @Test
    @DisplayName("OWNER가 프로젝트를 수정하고 삭제하면 멤버십도 함께 삭제된다")
    void updatesAndDeletesProject() throws Exception {
        long projectId = createProject();

        mockMvc.perform(put("/api/projects/{projectId}", projectId)
                        .queryParam("actorUserId", String.valueOf(ownerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"변경된 프로젝트\",\"description\":\"변경된 설명\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("변경된 프로젝트"));

        mockMvc.perform(delete("/api/projects/{projectId}", projectId)
                        .queryParam("actorUserId", String.valueOf(ownerId)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/projects/{projectId}", projectId)
                        .queryParam("actorUserId", String.valueOf(ownerId)))
                .andExpect(status().isNotFound());
    }

    private long createProject() throws Exception {
        String response = mockMvc.perform(post("/api/projects")
                        .queryParam("actorUserId", String.valueOf(ownerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"프로젝트\",\"description\":\"설명\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("projectId").asLong();
    }
}
