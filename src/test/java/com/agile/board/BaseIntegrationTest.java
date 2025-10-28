package com.agile.board;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {
    @Autowired protected MockMvc mvc;
    protected MediaType JSON = MediaType.APPLICATION_JSON;

    @BeforeEach
    void setup() {}

    public String getToken() throws Exception {
        // Register (ignore if already exists)
        mvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(JSON)
                        .content("{\"username\":\"erru\",\"email\":\"erru@x.com\",\"password\":\"p@ssw0rd\"}"))
                .andReturn();

        // Login and extract token
        MvcResult res = mvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(JSON)
                        .content("{\"username\":\"erru\",\"password\":\"p@ssw0rd\"}"))
                .andExpect(status().isOk())
                .andReturn();

        return res.getResponse().getContentAsString()
                .replaceAll(".*:\"(.*)\".*", "$1");
    }

}
