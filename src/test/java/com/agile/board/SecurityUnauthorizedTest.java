package com.agile.board;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SecurityUnauthorizedTest extends BaseIntegrationTest {

    @Test
    void projectsRequireAuth() throws Exception {
        ResultActions c = mvc.perform(MockMvcRequestBuilders.get("/api/projects")
                .header("Authorization", "Bearer test")
                .content("{\"key\":\"PRJ\",\"name\":\"Project 1\"}")
                .contentType(JSON)
        ).andExpect(status().isForbidden());
    }
}
