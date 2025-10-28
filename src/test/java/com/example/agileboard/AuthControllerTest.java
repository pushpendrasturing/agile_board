package com.example.agileboard;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthControllerTest extends BaseIntegrationTest {
    @Test
    void registerAndLogin() throws Exception {
        String reg = "{\"username\":\"u1\",\"email\":\"u1@x.com\",\"password\":\"p\"}";
        mvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(JSON)
                        .content(reg))
                .andExpect(status().isOk());

        String login = "{\"username\":\"u1\",\"password\":\"p\"}";
        mvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(JSON)
                        .content(login))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

}
