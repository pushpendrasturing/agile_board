package com.agile.board;

import org.junit.jupiter.api.*;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserControllerProfileTest extends BaseIntegrationTest {

    private String tk;

    @BeforeAll
    void setupOnce() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(JSON)
                        .content("{\"username\":\"meu\",\"email\":\"meu@x.com\",\"password\":\"secret0\"}"))
                .andReturn();

        MvcResult res = mvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(JSON)
                        .content("{\"username\":\"meu\",\"password\":\"secret0\"}"))
                .andExpect(status().isOk())
                .andReturn();

        tk = res.getResponse().getContentAsString()
                .replaceAll(".*:\\\"(.*)\\\".*", "$1");
    }

    @Test
    void updateMe_name_and_email() throws Exception {
        String payload = "{\"name\":\"Jane Doe\",\"email\":\"jane@x.com\"}";

        mvc.perform(MockMvcRequestBuilders.put("/api/users/me")
                        .header("Authorization", "Bearer " + tk)
                        .contentType(JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.email").value("jane@x.com"))
                .andExpect(jsonPath("$.username").value("meu"));
    }

    @Test
    void updateMe_changePassword_requiresCurrent() throws Exception {
        String bad = "{\"newPassword\":\"newpass123\"}";

        mvc.perform(MockMvcRequestBuilders.put("/api/users/me")
                        .header("Authorization", "Bearer " + tk)
                        .contentType(JSON)
                        .content(bad))
                .andExpect(status().isBadRequest());
    }

}