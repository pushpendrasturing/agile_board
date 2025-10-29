package com.agile.board;

import com.agile.board.repo.PasswordResetTokenRepository;
import com.agile.board.repo.UserRepository;
import com.agile.board.utils.TokenUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Instant;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class PasswordResetTest extends BaseIntegrationTest {

    @Autowired UserRepository users;
    @Autowired PasswordResetTokenRepository tokens;

    private void register(String u, String e, String p) throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(JSON).content("{\"username\":\""+u+"\",\"email\":\""+e+"\",\"password\":\""+p+"\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void reset_flow_success_and_reuse_rejected() throws Exception {
        register("reset_u1","reset_u1@x.com","oldPass1");

        // Request reset → receive token
        var res = mvc.perform(MockMvcRequestBuilders.post("/api/auth/request-reset")
                        .contentType(JSON).content("{\"emailOrUsername\":\"reset_u1@x.com\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String token = res.getResponse().getContentAsString().replaceAll(".*:\"(.*)\".*", "$1");

        // Reset with token
        mvc.perform(MockMvcRequestBuilders.post("/api/auth/reset")
                        .contentType(JSON).content("{\"token\":\""+token+"\",\"newPassword\":\"newPass1\"}"))
                .andExpect(status().isOk());

        // Reuse should fail
        mvc.perform(MockMvcRequestBuilders.post("/api/auth/reset")
                        .contentType(JSON).content("{\"token\":\""+token+"\",\"newPassword\":\"newPass2\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void expired_token_rejected() throws Exception {
        register("reset_u2","reset_u2@x.com","oldPass2");

        var res = mvc.perform(MockMvcRequestBuilders.post("/api/auth/request-reset")
                        .contentType(JSON).content("{\"emailOrUsername\":\"reset_u2\"}"))
                .andReturn();
        String token = res.getResponse().getContentAsString().replaceAll(".*:\"(.*)\".*", "$1");

        // Force expire in DB
        String hash = TokenUtil.sha256Hex(token);
        tokens.findByTokenHash(hash).ifPresent(t -> {
            t.setExpiresAt(Instant.now().minusSeconds(1));
            tokens.save(t);
        });

        mvc.perform(MockMvcRequestBuilders.post("/api/auth/reset")
                        .contentType(JSON).content("{\"token\":\""+token+"\",\"newPassword\":\"newPass\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknown_token_rejected() throws Exception {
        register("reset_u3","reset_u3@x.com","oldPass3");

        mvc.perform(MockMvcRequestBuilders.post("/api/auth/reset")
                        .contentType(JSON).content("{\"token\":\"does-not-exist\",\"newPassword\":\"newPass\"}"))
                .andExpect(status().isBadRequest());
    }
}