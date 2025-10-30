package com.agile.board.service;

import com.agile.board.domain.Project;
import com.agile.board.repo.ProjectRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service @RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projects;

    /** List only non-deleted projects */
    public List<Project> listActive() {
        return projects.findAllActive();
    }

    /** Create remains the same, but you could enforce uniqueness among active keys here if needed */
    @Transactional
    public Project create(String key, String name) {
        Project p = Project.builder().key(key).name(name).createdAt(Instant.now()).build();
        return projects.save(p);
    }

    public Project getActive(Long id) {
        return projects.findActiveById(id).orElseThrow();
    }

    /** Mark as deleted (soft) */
    @Transactional
    public void softDelete(Long id) {
        int updated = projects.softDeleteById(id);
        if (updated == 0) throw new IllegalArgumentException("project not found");
    }

    /** Restore previously soft-deleted project */
    @Transactional
    public void restore(Long id) {
        int updated = projects.restoreById(id);
        if (updated == 0) throw new IllegalArgumentException("project not found");
    }
}