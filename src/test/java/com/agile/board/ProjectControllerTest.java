package com.agile.board;

import org.junit.jupiter.api.Test;
import static org.hamcrest.Matchers.hasItem;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ProjectControllerTest extends BaseIntegrationTest {

    @Test
    void createAndList() throws Exception {
        String token = getToken();

        mvc.perform(MockMvcRequestBuilders.post("/api/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(JSON)
                        .content("{\"key\":\"PRJ\",\"name\":\"Project 1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("PRJ"));


        mvc.perform(MockMvcRequestBuilders.get("/api/projects")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem("Project 1")));

    }
}