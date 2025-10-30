package com.agile.board.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "project")
@EntityListeners(AuditingEntityListener.class)
public class Project {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Column(unique = true, name = "project_key")
    private String key;

    @NotBlank
    private String name;

    private Instant createdAt;

    /** Automatically set when updated */
    @LastModifiedDate
    @Column
    private Instant updatedAt;

    /** Automatically populated with current user on creation */
    @CreatedBy
    @Column(updatable = false, length = 100)
    private String createdBy;

    /** Automatically populated with current user on update */
    @LastModifiedBy
    @Column(length = 100)
    private String updatedBy;

    /** Soft delete marker */
    @Column(nullable = false)
    private boolean deleted = false;

    @ManyToMany
    @JoinTable(name = "project_members",
        joinColumns = @JoinColumn(name = "project_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> members = new HashSet<>();
}
