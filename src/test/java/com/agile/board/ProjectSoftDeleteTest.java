package com.agile.board;

import com.agile.board.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ProjectSoftDeleteTest extends BaseIntegrationTest {

    @Autowired UserRepository users;

    private String login(String u, String p) throws Exception {
        var res = mvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(JSON).content("{\"username\":\""+u+"\",\"password\":\""+p+"\"}"))
                .andReturn();
        return res.getResponse().getContentAsString().replaceAll(".*:\"(.*)\".*", "$1");
    }

    private String makeAdminAndLogin(String u, String e, String p) throws Exception {
        // register if first-time
        mvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(JSON).content("{\"username\":\""+u+"\",\"email\":\""+e+"\",\"password\":\""+p+"\"}"))
                .andExpect(status().isOk());
        users.findByUsername(u).ifPresent(a -> { a.setRole("ADMIN"); users.save(a); });
        return login(u, p);
    }

    @Test
    void softDeleted_project_hidden_from_list_and_get() throws Exception {
        String admin = makeAdminAndLogin("sd_admin","sd_admin@x.com","p@ss");

        // Create two projects
        mvc.perform(MockMvcRequestBuilders.post("/api/projects")
                        .header("Authorization","Bearer "+admin)
                        .contentType(JSON).content("{\"key\":\"SD1\",\"name\":\"P1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("SD1"));

        var res = mvc.perform(MockMvcRequestBuilders.post("/api/projects")
                        .header("Authorization","Bearer "+admin)
                        .contentType(JSON).content("{\"key\":\"SD2\",\"name\":\"P2\"}"))
                .andExpect(status().isOk())
                .andReturn();
        Long id2 = Long.parseLong(res.getResponse().getContentAsString().replaceAll(".*\"id\":(\\d+).*", "$1"));

        // List shows both
        mvc.perform(MockMvcRequestBuilders.get("/api/projects")
                        .header("Authorization","Bearer "+admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));

        // Soft delete SD2
        mvc.perform(MockMvcRequestBuilders.delete("/api/projects/"+id2)
                        .header("Authorization","Bearer "+admin))
                .andExpect(status().isNoContent());

        // List now should not include SD2 (size decreased by 1)
        mvc.perform(MockMvcRequestBuilders.get("/api/projects")
                        .header("Authorization","Bearer "+admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].key", not(hasItem("SD2"))));

        // GET by id for deleted should be 404 (handled by service->orElseThrow + global handler)
        mvc.perform(MockMvcRequestBuilders.get("/api/projects/"+id2)
                        .header("Authorization","Bearer "+admin))
                .andExpect(status().isNotFound());
    }

    @Test
    void restore_makes_project_visible_again() throws Exception {
        String admin = makeAdminAndLogin("sd_admin2","sd_admin2@x.com","p@ss");

        var res = mvc.perform(MockMvcRequestBuilders.post("/api/projects")
                        .header("Authorization","Bearer "+admin)
                        .contentType(JSON).content("{\"key\":\"SDR\",\"name\":\"Restore Me\"}"))
                .andExpect(status().isOk())
                .andReturn();
        Long id = Long.parseLong(res.getResponse().getContentAsString().replaceAll(".*\"id\":(\\d+).*", "$1"));

        // soft delete
        mvc.perform(MockMvcRequestBuilders.delete("/api/projects/"+id)
                        .header("Authorization","Bearer "+admin))
                .andExpect(status().isNoContent());

        // restore
        mvc.perform(MockMvcRequestBuilders.patch("/api/projects/"+id+"/restore")
                        .header("Authorization","Bearer "+admin))
                .andExpect(status().isNoContent());

        // visible again in list and GET
        mvc.perform(MockMvcRequestBuilders.get("/api/projects")
                        .header("Authorization","Bearer "+admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].key", hasItem("SDR")));

        mvc.perform(MockMvcRequestBuilders.get("/api/projects/"+id)
                        .header("Authorization","Bearer "+admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("SDR"));
    }

    @Test
    void user_cannot_soft_delete() throws Exception {
        // regular user (no PROJECT_EDIT)
        mvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(JSON).content("{\"username\":\"sd_user\",\"email\":\"sd_user@x.com\",\"password\":\"p@ss\"}"))
                .andExpect(status().isOk());
        String userTok = login("sd_user","p@ss");

        // admin creates a project to target
        String admin = makeAdminAndLogin("sd_admin3","sd_admin3@x.com","p@ss");
        var res = mvc.perform(MockMvcRequestBuilders.post("/api/projects")
                        .header("Authorization","Bearer "+admin)
                        .contentType(JSON).content("{\"key\":\"SDX\",\"name\":\"Target\"}"))
                .andExpect(status().isOk())
                .andReturn();
        Long id = Long.parseLong(res.getResponse().getContentAsString().replaceAll(".*\"id\":(\\d+).*", "$1"));

        // USER tries to delete → 403
        mvc.perform(MockMvcRequestBuilders.delete("/api/projects/"+id)
                        .header("Authorization","Bearer "+userTok))
                .andExpect(status().isForbidden());
    }
}
