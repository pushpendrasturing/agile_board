package com.agile.board.web;

import com.agile.board.domain.Comment;
import com.agile.board.dto.CommentDtos.*;
import com.agile.board.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/issues/{id}/comments")
@RequiredArgsConstructor
public class IssueCommentController {
    private final CommentService service;

    @PostMapping
    public ResponseEntity<CommentView> add(@PathVariable("id") Long issueId, @Valid @RequestBody CreateComment req) {
        Comment saved = service.addComment(issueId, req.text());
        return ResponseEntity.ok(toView(saved));
    }

    @GetMapping
    public ResponseEntity<List<CommentView>> list(@PathVariable("id") Long issueId) {
        var out = service.list(issueId).stream().map(this::toView).toList();
        return ResponseEntity.ok(out);
    }

    private CommentView toView(Comment c) {
        return new CommentView(c.getId(), c.getText(), c.getCreatedBy(), c.getCreatedAt());
    }
}
