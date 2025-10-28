package com.example.agileboard.repo;

import com.example.agileboard.domain.Issue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueRepository extends JpaRepository<Issue, Long> {}
