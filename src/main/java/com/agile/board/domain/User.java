package com.agile.board.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Set;

@Entity @Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Column(unique = true)
    private String username;

    @NotBlank
    @Column(name = "password_hash")
    private String passwordHash;

    @Email @NotBlank
    private String email;

    private String name;

    // USER, ADMIN
    private String role;

    // Used to invalidate previously issued tokens when role/privilege changes
    @Column(nullable = false)
    private int tokenVersion = 0;

    @ManyToMany(mappedBy = "members")
    private Set<Project> projects;
}
