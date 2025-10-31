package com.agile.board.web;

import com.agile.board.domain.Issue;
import com.agile.board.domain.IssuePriority;
import com.agile.board.domain.IssueStatus;
import com.agile.board.dto.IssueDtos.*;
import com.agile.board.dto.IssueUpdateDtos;
import com.agile.board.mapper.Mappers;
import com.agile.board.service.IssueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController @RequestMapping("/api/issues") @RequiredArgsConstructor
public class IssueController {
    private final IssueService service;
    private final Mappers mapper;

    @PostMapping
    public ResponseEntity<IssueView> create(@Valid @RequestBody IssueCreate req) {
        Issue i = service.create(req.title(), req.description(), req.priority(), req.projectId(), req.assigneeId());
        return ResponseEntity.ok(mapper.toView(i));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<IssueView> updateStatus(@PathVariable Long id, @Valid @RequestBody IssueUpdateDtos.UpdateStatus req) {
        Issue updated = service.transitionStatus(id, req.status());
        return ResponseEntity.ok(mapper.toView(updated));
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam(required = false) IssueStatus status,
                                    @RequestParam(required = false) IssuePriority priority,
                                    @RequestParam(required = false, name = "assignee") String assignee) {
        var result = service.search(status, priority, assignee)
                .stream().map(mapper::toView).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}
