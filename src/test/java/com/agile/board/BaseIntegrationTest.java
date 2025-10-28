package com.agile.board;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {
    @Autowired protected MockMvc mvc;
    protected MediaType JSON = MediaType.APPLICATION_JSON;

    @BeforeEach
    void setup() {}
}
