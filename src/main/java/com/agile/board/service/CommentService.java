package com.agile.board.service;

import com.agile.board.domain.Comment;
import com.agile.board.domain.Issue;
import com.agile.board.repo.CommentRepository;
import com.agile.board.repo.IssueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final IssueRepository issues;
    private final CommentRepository comments;

    @Transactional
    public Comment addComment(Long issueId, String text) {
        Issue issue = issues.findById(issueId).orElseThrow();
        Comment c = Comment.builder().text(text).issue(issue).build();
        // due to cascade it's fine either way; we save directly for clarity
        return comments.save(c);
    }

    public List<Comment> list(Long issueId) {
        return comments.findByIssueIdOrderByCreatedAtAsc(issueId);
    }
}
