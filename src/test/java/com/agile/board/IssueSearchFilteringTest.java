package com.agile.board;

import com.agile.board.domain.IssuePriority;
import com.agile.board.domain.IssueStatus;
import com.agile.board.domain.User;
import com.agile.board.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class IssueSearchFilteringTest extends BaseIntegrationTest {

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

    private long project(String t, String key) throws Exception {
        var r = mvc.perform(MockMvcRequestBuilders.post("/api/projects")
                        .header("Authorization","Bearer "+t)
                        .contentType(JSON).content("{\"key\":\""+key+"\",\"name\":\"N\"}"))
                .andExpect(status().isOk()).andReturn();
        return Long.parseLong(r.getResponse().getContentAsString().replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    private long issue(String t, long pid, String title, IssuePriority prio, Long assigneeId) throws Exception {
        String payload = "{\"title\":\""+title+"\",\"description\":\"d\",\"priority\":\""+(prio==null?"MEDIUM":prio)+"\",\"projectId\":"+pid+(assigneeId!=null?(",\"assigneeId\":"+assigneeId):"")+"}";
        var r = mvc.perform(MockMvcRequestBuilders.post("/api/issues")
                        .header("Authorization","Bearer "+t)
                        .contentType(JSON).content(payload))
                .andExpect(status().isOk()).andReturn();
        return Long.parseLong(r.getResponse().getContentAsString().replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    private void patchStatus(String t, long id, IssueStatus status) throws Exception {
        mvc.perform(MockMvcRequestBuilders.patch("/api/issues/"+id)
                        .header("Authorization","Bearer "+t)
                        .contentType(JSON).content("{\"status\":\""+status+"\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void filter_by_status_only() throws Exception {
        String admin = adminLogin("isf_admin1","isf_admin1@x.com","p@ss");
        long pid = project(admin, "SRCH1");

        long i1 = issue(admin, pid, "A", IssuePriority.LOW, null);              // OPEN
        long i2 = issue(admin, pid, "B", IssuePriority.HIGH, null);             // OPEN then IN_PROGRESS
        patchStatus(admin, i2, IssueStatus.IN_PROGRESS);

        mvc.perform(MockMvcRequestBuilders.get("/api/issues/search")
                        .header("Authorization","Bearer "+admin)
                        .param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem((int)i2)))
                .andExpect(jsonPath("$[*].id", not(hasItem((int)i1))));
    }

    @Test
    void filter_by_status_and_priority() throws Exception {
        String admin = adminLogin("isf_admin2","isf_admin2@x.com","p@ss");
        long pid = project(admin, "SRCH2");

        long i1 = issue(admin, pid, "A", IssuePriority.LOW, null);              // OPEN, LOW
        long i2 = issue(admin, pid, "B", IssuePriority.CRITICAL, null);         // OPEN, CRITICAL
        patchStatus(admin, i2, IssueStatus.IN_PROGRESS);                        // now IN_PROGRESS, CRITICAL

        mvc.perform(MockMvcRequestBuilders.get("/api/issues/search")
                        .header("Authorization","Bearer "+admin)
                        .param("status", "IN_PROGRESS")
                        .param("priority", "CRITICAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value((int)i2));
    }

    @Test
    void filter_by_assignee_username_or_id() throws Exception {
        String admin = adminLogin("isf_admin3","isf_admin3@x.com","p@ss");
        long pid = project(admin, "SRCH3");

        // create assignee user
        mvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(JSON).content("{\"username\":\"dev1\",\"email\":\"dev1@x.com\",\"password\":\"p@ss\"}"))
                .andExpect(status().isOk());
        Long devId = users.findByUsername("dev1").map(User::getId).orElseThrow();

        long i1 = issue(admin, pid, "A", IssuePriority.MEDIUM, devId);
        long i2 = issue(admin, pid, "B", IssuePriority.MEDIUM, null);

        // by username
        mvc.perform(MockMvcRequestBuilders.get("/api/issues/search")
                        .header("Authorization","Bearer "+admin)
                        .param("assignee", "dev1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem((int)i1)))
                .andExpect(jsonPath("$[*].id", not(hasItem((int)i2))));

        // by id
        mvc.perform(MockMvcRequestBuilders.get("/api/issues/search")
                        .header("Authorization","Bearer "+admin)
                        .param("assignee", String.valueOf(devId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem((int)i1)))
                .andExpect(jsonPath("$[*].id", not(hasItem((int)i2))));
    }
}
