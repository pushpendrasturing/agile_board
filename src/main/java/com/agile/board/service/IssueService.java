package com.agile.board.service;

import com.agile.board.domain.Issue;
import com.agile.board.domain.IssuePriority;
import com.agile.board.domain.Project;
import com.agile.board.domain.User;
import com.agile.board.repo.IssueRepository;
import com.agile.board.repo.ProjectRepository;
import com.agile.board.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service @RequiredArgsConstructor
public class IssueService {
    private final IssueRepository issues;
    private final ProjectRepository projects;
    private final UserRepository users;

    @Transactional
    public Issue create(String title, String desc, IssuePriority priority, Long projectId, Long assigneeId) {
        Project project = projects.findById(projectId).orElseThrow();
        User assignee = assigneeId == null ? null : users.findById(assigneeId).orElseThrow();
        Issue i = Issue.builder().title(title).description(desc)
                .priority(priority == null ? IssuePriority.MEDIUM : priority)
                .project(project).assignee(assignee)
                .createdAt(Instant.now()).updatedAt(Instant.now()).build();
        return issues.save(i);
    }
}
