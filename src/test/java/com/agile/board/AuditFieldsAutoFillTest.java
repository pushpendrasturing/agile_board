package com.agile.board;

import com.agile.board.domain.Project;
import com.agile.board.repo.ProjectRepository;
import com.agile.board.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuditFieldsAutoFillTest extends BaseIntegrationTest {

    @Autowired UserRepository users;
    @Autowired ProjectRepository projects;

    private String registerAndLoginAsAdmin(String u, String e, String p) throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(JSON).content("{\"username\":\""+u+"\",\"email\":\""+e+"\",\"password\":\""+p+"\"}"))
                .andExpect(status().isOk());
        users.findByUsername(u).ifPresent(a -> { a.setRole("ADMIN"); users.save(a); });
        var res = mvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(JSON).content("{\"username\":\""+u+"\",\"password\":\""+p+"\"}"))
                .andReturn();
        return res.getResponse().getContentAsString().replaceAll(".*:\"(.*)\".*", "$1");
    }

    private long createProject(String token, String key, String name) throws Exception {
        var r = mvc.perform(MockMvcRequestBuilders.post("/api/projects")
                        .header("Authorization","Bearer "+token)
                        .contentType(JSON).content("{\"key\":\""+key+"\",\"name\":\""+name+"\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return Long.parseLong(r.getResponse().getContentAsString().replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    private void renameProject(String token, long id, String newName) throws Exception {
        mvc.perform(MockMvcRequestBuilders.patch("/api/projects/"+id+"/rename")
                        .header("Authorization","Bearer "+token)
                        .contentType(JSON).content("{\"name\":\""+newName+"\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void created_fields_are_set_on_create() throws Exception {
        String admin = registerAndLoginAsAdmin("aud_c1","aud_c1@x.com","p@ss");
        long id = createProject(admin, "AUDC1", "Audited One");

        Project p = projects.findById(id).orElseThrow();
        assertEquals("aud_c1", p.getCreatedBy(), "createdBy must be creator");
        assertNotNull(p.getCreatedAt(), "createdAt must be set");
        assertNotNull(p.getUpdatedAt(), "updatedAt must be set on first persist");
        assertEquals("aud_c1", p.getUpdatedBy(), "updatedBy typically matches creator on first persist");
    }

    @Test
    void updated_fields_change_when_renamed_by_same_user() throws Exception {
        String admin = registerAndLoginAsAdmin("aud_same","aud_same@x.com","p@ss");
        long id = createProject(admin, "AUDS1", "Same User");

        Project before = projects.findById(id).orElseThrow();
        var prevUpdatedAt = before.getUpdatedAt();

        Thread.sleep(5); // ensure timestamp moves
        renameProject(admin, id, "Same User Renamed");

        Project after = projects.findById(id).orElseThrow();
        assertEquals("aud_same", after.getUpdatedBy(), "updatedBy should stay same user");
        assertTrue(after.getUpdatedAt().isAfter(prevUpdatedAt), "updatedAt should advance");
        assertEquals("aud_same", after.getCreatedBy(), "createdBy stays original");
    }

    @Test
    void updatedBy_reflects_different_user_on_rename_and_updatedAt_advances() throws Exception {
        String admin1 = registerAndLoginAsAdmin("aud_admin1","aud_admin1@x.com","p@ss");
        long id = createProject(admin1, "AUD1", "Audited");

        Project p1 = projects.findById(id).orElseThrow();
        var prevUpdatedAt = p1.getUpdatedAt();
        assertEquals("aud_admin1", p1.getCreatedBy());

        Thread.sleep(5);

        String admin2 = registerAndLoginAsAdmin("aud_admin2","aud_admin2@x.com","p@ss");
        mvc.perform(MockMvcRequestBuilders.patch("/api/projects/"+id+"/rename")
                        .header("Authorization","Bearer "+admin2)
                        .contentType(JSON).content("{\"name\":\"AuditedRenamed\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key", is("AUD1")));

        Project p2 = projects.findById(id).orElseThrow();
        assertEquals("aud_admin1", p2.getCreatedBy(), "createdBy must remain original creator");
        assertEquals("aud_admin2", p2.getUpdatedBy(), "updatedBy must reflect last modifier");
        assertTrue(p2.getUpdatedAt().isAfter(prevUpdatedAt), "updatedAt should advance");
    }
}
