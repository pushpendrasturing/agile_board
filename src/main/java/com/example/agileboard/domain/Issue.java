package com.example.agileboard.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.Instant;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "issue")
public class Issue {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    private IssueStatus status = IssueStatus.OPEN;

    @Enumerated(EnumType.STRING)
    private IssuePriority priority = IssuePriority.MEDIUM;

    @ManyToOne(optional = false)
    private Project project;

    @ManyToOne
    private User assignee;

    private Instant createdAt;
    private Instant updatedAt;
}
