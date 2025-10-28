package com.agile.board.service;

import com.agile.board.domain.Issue;
import com.agile.board.domain.IssuePriority;
import com.agile.board.domain.Project;
import com.agile.board.domain.User;
import com.agile.board.repo.IssueRepository;
import com.agile.board.repo.ProjectRepository;
import com.agile.board.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IssueServiceTest {

    @Mock IssueRepository issues;
    @Mock ProjectRepository projects;
    @Mock UserRepository users;
    @InjectMocks IssueService service;

    @Test
    void create_defaultsPriority_and_setsTimestamps() {
        Project project = Project.builder().id(10L).key("P").name("P").build();
        when(projects.findById(10L)).thenReturn(Optional.of(project));
        when(users.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).username("u").build()));
        when(issues.save(any())).thenAnswer(a -> a.getArguments()[0]);

        Issue i = service.create("T", "D", null, 10L, 1L);
        assertEquals(IssuePriority.MEDIUM, i.getPriority());
        assertNotNull(i.getCreatedAt());
        assertNotNull(i.getUpdatedAt());
        assertEquals(project, i.getProject());
        verify(issues).save(any());
    }
}
