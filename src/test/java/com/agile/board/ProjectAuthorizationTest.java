package com.agile.board;

import com.agile.board.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ProjectAuthorizationTest extends BaseIntegrationTest {

    @Autowired
    UserRepository users;

    private String registerAndLogin(String u, String e, String p) throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(JSON).content("{\"username\":\"" + u + "\",\"email\":\"" + e + "\",\"password\":\"" + p + "\"}"))
                .andExpect(status().isOk());
        var res = mvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(JSON).content("{\"username\":\"" + u + "\",\"password\":\"" + p + "\"}"))
                .andReturn();
        return res.getResponse().getContentAsString()
                .replaceAll(".*:\"(.*)\".*", "$1");
    }

    @Test
    void user_project_create_test() throws Exception {
        // USER
        String userToken = registerAndLogin("rb_user", "rb_user@x.com", "p@ss");

        mvc.perform(MockMvcRequestBuilders.post("/api/projects")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(JSON).content("{\"key\":\"RB_U1\",\"name\":\"User Try\"}"))
                .andExpect(status().isForbidden());

    }

    @Test
    void admin_project_create_test() throws Exception {
        // ADMIN (set role directly in DB)
        String adminToken = registerAndLogin("rb_admin", "rb_admin@x.com", "p@ss");
        users.findByUsername("rb_admin").ifPresent(a -> {
            a.setRole("ADMIN");
            users.save(a);
        });

        mvc.perform(MockMvcRequestBuilders.post("/api/projects")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(JSON).content("{\"key\":\"RB_A1\",\"name\":\"Admin OK\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key", is("RB_A1")));
    }
}