package com.agile.board.security;

import com.agile.board.BaseIntegrationTest;
import com.agile.board.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TokenVersionInvalidationTest extends BaseIntegrationTest {

    @Autowired
    UserRepository users;

    private void register(String u, String e, String p) throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(JSON).content("{\"username\":\"" + u + "\",\"email\":\"" + e + "\",\"password\":\"" + p + "\"}"))
                .andExpect(status().isOk());
    }

    private String login(String u, String p) throws Exception {
        var res = mvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(JSON).content("{\"username\":\"" + u + "\",\"password\":\"" + p + "\"}"))
                .andReturn();
        return res.getResponse().getContentAsString().replaceAll(".*:\"(.*)\".*", "$1");
    }

    private void promoteToAdmin(String u) {
        users.findByUsername(u).ifPresent(x -> {
            x.setRole("ADMIN");
            users.save(x);
        });
    }

    private void bumpVersion(String u) {
        users.findByUsername(u).ifPresent(x -> {
            x.setTokenVersion(x.getTokenVersion() + 1);
            users.save(x);
        });
    }


    @Test
    void user_token_forbidden_on_project_create() throws Exception {
        register("ver_u1", "ver_u1@x.com", "p@ss");
        String t1 = login("ver_u1", "p@ss");

        mvc.perform(MockMvcRequestBuilders.post("/api/projects")
                        .header("Authorization", "Bearer " + t1)
                        .contentType(JSON).content("{\"key\":\"TVU1\",\"name\":\"Try\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void role_change_without_version_bump_still_forbidden_with_old_token() throws Exception {
        register("ver_u2", "ver_u2@x.com", "p@ss");
        String t1 = login("ver_u2", "p@ss");

        // change role to ADMIN but keep same tokenVersion
        promoteToAdmin("ver_u2");

        // old token still has USER claims → forbidden
        mvc.perform(MockMvcRequestBuilders.post("/api/projects")
                        .header("Authorization", "Bearer " + t1)
                        .contentType(JSON).content("{\"key\":\"TVU2\",\"name\":\"Still No\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void version_bump_invalidates_old_token() throws Exception {
        register("ver_u3", "ver_u3@x.com", "p@ss");
        String t1 = login("ver_u3", "p@ss");

        promoteToAdmin("ver_u3");
        bumpVersion("ver_u3"); // invalidate previously issued tokens

        // depending on your security, this may yield 401 or 403; you observed 403
        mvc.perform(MockMvcRequestBuilders.post("/api/projects")
                        .header("Authorization", "Bearer " + t1)
                        .contentType(JSON).content("{\"key\":\"TVU3\",\"name\":\"Invalidated\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void relogin_after_version_bump_allows_admin_create() throws Exception {
        register("ver_u4", "ver_u4@x.com", "p@ss");
        String old = login("ver_u4", "p@ss");

        promoteToAdmin("ver_u4");
        bumpVersion("ver_u4");

        // new token carries ADMIN + new version
        String t2 = login("ver_u4", "p@ss");

        mvc.perform(MockMvcRequestBuilders.post("/api/projects")
                        .header("Authorization", "Bearer " + t2)
                        .contentType(JSON).content("{\"key\":\"TVU4\",\"name\":\"Now OK\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key", is("TVU4")));
    }
}
