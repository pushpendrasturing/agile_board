package com.example.agileboard.dto;

import com.example.agileboard.domain.IssuePriority;
import com.example.agileboard.domain.IssueStatus;
import jakarta.validation.constraints.NotBlank;

public class IssueDtos {
    public record IssueCreate(@NotBlank String title, String description,
                              IssuePriority priority, Long projectId, Long assigneeId) {}
    public record IssueView(Long id, String title, String description,
                            IssueStatus status, IssuePriority priority,
                            Long projectId, Long assigneeId) {}
}
