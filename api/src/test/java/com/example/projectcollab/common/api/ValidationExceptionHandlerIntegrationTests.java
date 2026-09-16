package com.example.projectcollab.common.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ValidationExceptionHandlerIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void malformedJsonUsesTheCommonErrorResponse() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("request.invalid"))
                .andExpect(jsonPath("$.message").value("request.invalid"))
                .andExpect(jsonPath("$.violations").isEmpty());
    }

    @Test
    void missingQueryParameterUsesTheCommonErrorResponse() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("request.invalid"))
                .andExpect(jsonPath("$.violations[0].field").value("userId"))
                .andExpect(jsonPath("$.violations[0].reason").isNotEmpty());
    }

    @Test
    void pathVariableTypeMismatchUsesTheCommonErrorResponse() throws Exception {
        mockMvc.perform(get("/api/users/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("request.invalid"))
                .andExpect(jsonPath("$.violations[0].field").value("userId"))
                .andExpect(jsonPath("$.violations[0].reason").isNotEmpty());
    }
}
