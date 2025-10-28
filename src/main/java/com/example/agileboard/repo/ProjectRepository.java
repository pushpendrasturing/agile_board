package com.example.agileboard.repo;

import com.example.agileboard.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByKey(String key);
}
