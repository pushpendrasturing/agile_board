package com.agile.board.web;

import com.agile.board.dto.ProjectDtos.*;
import com.agile.board.mapper.Mappers;
import com.agile.board.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController @RequestMapping("/api/projects") @RequiredArgsConstructor
public class ProjectController {
    private final ProjectService service;
    private final Mappers mapper;

    @GetMapping
    public ResponseEntity<?> list() {
        var views = service.list().stream().map(mapper::toView).collect(Collectors.toList());
        return ResponseEntity.ok(views);
    }

    @PreAuthorize("hasAuthority('PROJECT_CREATE')")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ProjectCreate req) {
        var created = service.create(req.key(), req.name());
        return ResponseEntity.ok(mapper.toView(created));
    }
}
