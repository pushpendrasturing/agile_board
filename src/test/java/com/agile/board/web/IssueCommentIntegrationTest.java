package com.agile.board.web;

import com.agile.board.BaseIntegrationTest;
import com.agile.board.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class IssueCommentIntegrationTest extends BaseIntegrationTest {

    @Autowired
    UserRepository users;

    private String adminLogin(String u, String e, String p) throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(JSON).content("{\"username\":\"" + u + "\",\"email\":\"" + e + "\",\"password\":\"" + p + "\"}"))
                .andExpect(status().isOk());
        users.findByUsername(u).ifPresent(a -> {
            a.setRole("ADMIN");
            users.save(a);
        });
        var res = mvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(JSON).content("{\"username\":\"" + u + "\",\"password\":\"" + p + "\"}"))
                .andReturn();
        return res.getResponse().getContentAsString().replaceAll(".*:\"(.*)\".*", "$1");
    }

    private long project(String tok, String key) throws Exception {
        var r = mvc.perform(MockMvcRequestBuilders.post("/api/projects")
                        .header("Authorization", "Bearer " + tok)
                        .contentType(JSON).content("{\"key\":\"" + key + "\",\"name\":\"N\"}"))
                .andExpect(status().isOk()).andReturn();
        return Long.parseLong(r.getResponse().getContentAsString().replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    private long issue(String tok, long pid) throws Exception {
        var r = mvc.perform(MockMvcRequestBuilders.post("/api/issues")
                        .header("Authorization", "Bearer " + tok)
                        .contentType(JSON).content("{\"title\":\"Bug\",\"projectId\":" + pid + "}"))
                .andExpect(status().isOk()).andReturn();
        return Long.parseLong(r.getResponse().getContentAsString().replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    @Test
    void add_comment_success_and_list_contains_it() throws Exception {
        String admin = adminLogin("ic_admin1", "ic_admin1@x.com", "p@ss");
        long pid = project(admin, "CMT1");
        long iid = issue(admin, pid);

        // add
        mvc.perform(MockMvcRequestBuilders.post("/api/issues/" + iid + "/comments")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(JSON).content("{\"text\":\"first comment\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("first comment"))
                .andExpect(jsonPath("$.createdBy").value("ic_admin1"))
                .andExpect(jsonPath("$.createdAt").exists());

        // list
        mvc.perform(MockMvcRequestBuilders.get("/api/issues/" + iid + "/comments")
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].text").value("first comment"));
    }

    @Test
    void empty_comment_text_rejected() throws Exception {
        String admin = adminLogin("ic_admin2", "ic_admin2@x.com", "p@ss");
        long pid = project(admin, "CMT2");
        long iid = issue(admin, pid);

        mvc.perform(MockMvcRequestBuilders.post("/api/issues/" + iid + "/comments")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(JSON).content("{\"text\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void comments_are_scoped_to_issue() throws Exception {
        String admin = adminLogin("ic_admin3", "ic_admin3@x.com", "p@ss");
        long pid = project(admin, "CMT3");
        long i1 = issue(admin, pid);
        long i2 = issue(admin, pid);

        mvc.perform(MockMvcRequestBuilders.post("/api/issues/" + i1 + "/comments")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(JSON).content("{\"text\":\"c1\"}"))
                .andExpect(status().isOk());

        mvc.perform(MockMvcRequestBuilders.post("/api/issues/" + i2 + "/comments")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(JSON).content("{\"text\":\"c2\"}"))
                .andExpect(status().isOk());

        mvc.perform(MockMvcRequestBuilders.get("/api/issues/" + i1 + "/comments")
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].text", hasItem("c1")))
                .andExpect(jsonPath("$[*].text", not(hasItem("c2"))));
    }
}