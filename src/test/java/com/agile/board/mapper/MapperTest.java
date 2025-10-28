package com.agile.board.mapper;

import com.agile.board.domain.Project;
import com.agile.board.domain.User;
import com.agile.board.dto.ProjectDtos;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MapperTest {

    @Autowired Mappers mapper;

    @Test
    void projectToView_mapsMembers() {
        User u = User.builder().id(1L).username("u").build();
        Project p = Project.builder().id(2L).key("K").name("N").members(Set.of(u)).build();
        ProjectDtos.ProjectView view = mapper.toView(p);
        assertEquals(2L, view.id());
        assertTrue(view.memberIds().contains(1L));
    }
}
