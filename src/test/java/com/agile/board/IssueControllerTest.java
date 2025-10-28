package com.agile.board;

import org.junit.jupiter.api.*;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IssueControllerTest extends BaseIntegrationTest {

    private String tk;
    private long pid;

    @BeforeAll
    void setupOnce() throws Exception {
        tk = getToken();
        MvcResult resp = mvc.perform(MockMvcRequestBuilders.post("/api/projects")
                        .header("Authorization", "Bearer " + tk)
                        .contentType(JSON)
                        .content("{\"key\":\"IPRJ\",\"name\":\"Issue Project\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String body = resp.getResponse().getContentAsString();
        pid = Long.parseLong(body.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    @Test
    void createIssue_ok() throws Exception {
        String payload = String.format("{\"title\":\"Bug A\",\"description\":\"desc\",\"projectId\":%d}", pid);

        mvc.perform(MockMvcRequestBuilders.post("/api/issues")
                        .header("Authorization", "Bearer " + tk)
                        .contentType(JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Bug A"))
                .andExpect(jsonPath("$.projectId").value((int) pid));
    }

    @Test
    void createIssue_validationFails() throws Exception {
        String payload = String.format("{\"title\":\"\",\"projectId\":%d}", pid);

        mvc.perform(MockMvcRequestBuilders.post("/api/issues")
                        .header("Authorization", "Bearer " + tk)
                        .contentType(JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }
}