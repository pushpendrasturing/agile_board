package com.example.agileboard;

import org.junit.jupiter.api.Test;
import static org.hamcrest.Matchers.hasItem;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ProjectControllerTest extends BaseIntegrationTest {

    @Test
    void createAndList() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(JSON)
                        .content("{\"username\":\"u21\",\"email\":\"u2@x.com\",\"password\":\"p1\"}"))
                .andExpect(status().isOk());

        var res = mvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(JSON)
                        .content("{\"username\":\"u21\",\"password\":\"p1\"}"))
                .andReturn();

        String token = res.getResponse().getContentAsString()
                .replaceAll(".*:\\\"(.*)\\\".*", "$1");

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