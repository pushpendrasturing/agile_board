package com.agile.board.exception;

import com.agile.board.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ErrorHandlingTest extends BaseIntegrationTest {

    private String tk;

    @BeforeAll
    void setupOnce() throws Exception {
        tk = getToken();
    }

    @Test
    void validationError_returnsStructuredApiError() throws Exception {
        String payload = "{\"title\":\"\",\"projectId\":1}";
        mvc.perform(MockMvcRequestBuilders.post("/api/issues")
                        .header("Authorization", "Bearer " + tk)
                        .contentType(JSON).content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/issues"));
    }

    @Test
    void notFound_returns404ApiError() throws Exception {
        String payload = "{\"title\":\"X\",\"projectId\":999999}";
        mvc.perform(MockMvcRequestBuilders.post("/api/issues")
                        .header("Authorization", "Bearer " + tk)
                        .contentType(JSON).content(payload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/issues"));
    }

    @Test
    void malformedJson_returns400ApiError() throws Exception {
        String badJson = "{\"username\":\"x\" "; // missing closing brace
        mvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(JSON).content(badJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_JSON"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/auth/login"));
    }
}