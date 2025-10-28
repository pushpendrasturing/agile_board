package com.example.agileboard.web;

import com.example.agileboard.domain.Issue;
import com.example.agileboard.dto.IssueDtos.*;
import com.example.agileboard.mapper.Mappers;
import com.example.agileboard.service.IssueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/issues") @RequiredArgsConstructor
public class IssueController {
    private final IssueService service;
    private final Mappers mapper;

    @PostMapping
    public ResponseEntity<IssueView> create(@Valid @RequestBody IssueCreate req) {
        Issue i = service.create(req.title(), req.description(), req.priority(), req.projectId(), req.assigneeId());
        return ResponseEntity.ok(mapper.toView(i));
    }
}
