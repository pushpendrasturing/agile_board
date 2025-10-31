package com.agile.board.service;

import com.agile.board.domain.Issue;
import com.agile.board.domain.IssuePriority;
import com.agile.board.domain.IssueStatus;
import com.agile.board.domain.Project;
import com.agile.board.domain.User;
import com.agile.board.repo.IssueRepository;
import com.agile.board.repo.ProjectRepository;
import com.agile.board.repo.UserRepository;
import com.agile.board.search.IssueSearchSpec;
import com.agile.board.workflow.IssueTransitionPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

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
                .status(IssueStatus.OPEN)
                .project(project).assignee(assignee)
                .createdAt(Instant.now()).updatedAt(Instant.now()).build();
        return issues.save(i);
    }

    public List<Issue> search(IssueStatus status, IssuePriority priority, String assignee) {
        Specification<Issue> spec = IssueSearchSpec.build(status, priority, assignee);
        return issues.findAll(spec);
    }

    /** Transition issue status with policy validation */
    @Transactional
    public Issue transitionStatus(Long id, IssueStatus newStatus) {
        Issue i = issues.findById(id).orElseThrow();
        IssueStatus from = i.getStatus();
        if (from == newStatus) return i; // no-op
        if (!IssueTransitionPolicy.isAllowed(from, newStatus)) {
            throw new IllegalArgumentException(
                    "Invalid transition: " + from + " -> " + newStatus + ". Allowed: " + IssueTransitionPolicy.nextAllowed(from)
            );
        }
        i.setStatus(newStatus);
        i.setUpdatedAt(Instant.now());
        return issues.save(i);
    }
}
