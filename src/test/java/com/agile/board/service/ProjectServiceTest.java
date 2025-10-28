package com.agile.board.service;

import com.agile.board.domain.Project;
import com.agile.board.repo.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock ProjectRepository repo;
    @InjectMocks ProjectService service;

    @Test
    void create_setsCreatedAt_andPersists() {
        when(repo.save(any())).thenAnswer(a -> a.getArguments()[0]);
        Project p = service.create("PRJ","Name");
        assertEquals("PRJ", p.getKey());
        assertNotNull(p.getCreatedAt());
        verify(repo).save(any());
    }
}
