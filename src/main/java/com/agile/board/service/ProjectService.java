package com.agile.board.service;

import com.agile.board.domain.Project;
import com.agile.board.repo.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service @RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projects;

    public List<Project> list() { return projects.findAll(); }

    @Transactional
    public Project create(String key, String name) {
        Project p = Project.builder().key(key).name(name).createdAt(Instant.now()).build();
        return projects.save(p);
    }
}
