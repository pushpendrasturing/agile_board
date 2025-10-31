package com.agile.board;

import com.agile.board.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class IssueTransitionWorkflowTest extends BaseIntegrationTest {

    @Autowired UserRepository users;

    private String adminLogin(String u, String e, String p) throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(JSON).content("{\"username\":\""+u+"\",\"email\":\""+e+"\",\"password\":\""+p+"\"}"))
                .andExpect(status().isOk());
        users.findByUsername(u).ifPresent(a -> { a.setRole("ADMIN"); users.save(a); });
        var res = mvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(JSON).content("{\"username\":\""+u+"\",\"password\":\""+p+"\"}"))
                .andReturn();
        return res.getResponse().getContentAsString().replaceAll(".*:\"(.*)\".*", "$1");
    }

    private long createProject(String tok, String key) throws Exception {
        var pr = mvc.perform(MockMvcRequestBuilders.post("/api/projects")
                        .header("Authorization","Bearer "+tok)
                        .contentType(JSON).content("{\"key\":\""+key+"\",\"name\":\"N\"}"))
                .andExpect(status().isOk()).andReturn();
        return Long.parseLong(pr.getResponse().getContentAsString().replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    private long createIssue(String tok, long projectId) throws Exception {
        var ir = mvc.perform(MockMvcRequestBuilders.post("/api/issues")
                        .header("Authorization","Bearer "+tok)
                        .contentType(JSON).content("{\"title\":\"T\",\"projectId\":"+projectId+"}"))
                .andExpect(status().isOk()).andReturn();
        return Long.parseLong(ir.getResponse().getContentAsString().replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    @Test
    void valid_sequential_transitions() throws Exception {
        String admin = adminLogin("iw_admin1","iw_admin1@x.com","p@ss");
        long pid = createProject(admin, "ITW1");
        long iid = createIssue(admin, pid);

        // OPEN -> IN_PROGRESS
        mvc.perform(MockMvcRequestBuilders.patch("/api/issues/"+iid)
                        .header("Authorization","Bearer "+admin)
                        .contentType(JSON).content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));

        // IN_PROGRESS -> IN_REVIEW
        mvc.perform(MockMvcRequestBuilders.patch("/api/issues/"+iid)
                        .header("Authorization","Bearer "+admin)
                        .contentType(JSON).content("{\"status\":\"IN_REVIEW\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("IN_REVIEW")));

        // IN_REVIEW -> DONE
        mvc.perform(MockMvcRequestBuilders.patch("/api/issues/"+iid)
                        .header("Authorization","Bearer "+admin)
                        .contentType(JSON).content("{\"status\":\"DONE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("DONE")));
    }

    @Test
    void invalid_open_to_done_rejected() throws Exception {
        String admin = adminLogin("iw_admin2","iw_admin2@x.com","p@ss");
        long pid = createProject(admin, "ITW2");
        long iid = createIssue(admin, pid);

        // OPEN -> DONE is invalid
        mvc.perform(MockMvcRequestBuilders.patch("/api/issues/"+iid)
                        .header("Authorization","Bearer "+admin)
                        .contentType(JSON).content("{\"status\":\"DONE\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalid_from_done_rejected() throws Exception {
        String admin = adminLogin("iw_admin3","iw_admin3@x.com","p@ss");
        long pid = createProject(admin, "ITW3");
        long iid = createIssue(admin, pid);

        // Move to DONE via valid path
        mvc.perform(MockMvcRequestBuilders.patch("/api/issues/"+iid)
                        .header("Authorization","Bearer "+admin)
                        .contentType(JSON).content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk());
        mvc.perform(MockMvcRequestBuilders.patch("/api/issues/"+iid)
                        .header("Authorization","Bearer "+admin)
                        .contentType(JSON).content("{\"status\":\"IN_REVIEW\"}"))
                .andExpect(status().isOk());
        mvc.perform(MockMvcRequestBuilders.patch("/api/issues/"+iid)
                        .header("Authorization","Bearer "+admin)
                        .contentType(JSON).content("{\"status\":\"DONE\"}"))
                .andExpect(status().isOk());

        // DONE -> IN_PROGRESS should fail
        mvc.perform(MockMvcRequestBuilders.patch("/api/issues/"+iid)
                        .header("Authorization","Bearer "+admin)
                        .contentType(JSON).content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isBadRequest());
    }
}