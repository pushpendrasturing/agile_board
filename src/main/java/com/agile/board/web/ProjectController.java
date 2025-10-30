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

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService service;
    private final Mappers mapper;

    /** GET only active (non-deleted) projects */
    @GetMapping
    public ResponseEntity<?> list() {
        var views = service.listActive().stream().map(mapper::toView).collect(Collectors.toList());
        return ResponseEntity.ok(views);
    }

    /** Only ADMIN (PROJECT_CREATE) can create */
    @PreAuthorize("hasAuthority('PROJECT_CREATE')")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ProjectCreate req) {
        var created = service.create(req.key(), req.name());
        return ResponseEntity.ok(mapper.toView(created));
    }

    /** Fetch one active (404 if soft-deleted or missing) */
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        var p = service.getActive(id);
        return ResponseEntity.ok(mapper.toView(p));
    }

    /** Soft delete (requires PROJECT_EDIT) */
    @PreAuthorize("hasAuthority('PROJECT_EDIT')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> softDelete(@PathVariable Long id) {
        service.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    /** Restore soft-deleted project (requires PROJECT_EDIT) */
    @PreAuthorize("hasAuthority('PROJECT_EDIT')")
    @PatchMapping("/{id}/restore")
    public ResponseEntity<?> restore(@PathVariable Long id) {
        service.restore(id);
        return ResponseEntity.noContent().build();
    }
}
