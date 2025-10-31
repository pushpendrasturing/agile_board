package com.agile.board.search;

import com.agile.board.domain.Issue;
import com.agile.board.domain.IssuePriority;
import com.agile.board.domain.IssueStatus;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.JoinType;

public final class IssueSearchSpec {
    private IssueSearchSpec() {}

    public static Specification<Issue> hasStatus(IssueStatus status) {
        return (root, q, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<Issue> hasPriority(IssuePriority priority) {
        return (root, q, cb) -> priority == null ? cb.conjunction() : cb.equal(root.get("priority"), priority);
    }

    /** assignee may be username or numeric id */
    public static Specification<Issue> hasAssignee(String assignee) {
        return (root, q, cb) -> {
            if (assignee == null || assignee.isBlank()) return cb.conjunction();
            var join = root.join("assignee", JoinType.LEFT);
            // try numeric id match OR username match (case-insensitive)
            Specification<Issue> byId = (r, qq, cc) -> {
                try {
                    long id = Long.parseLong(assignee);
                    return cc.equal(join.get("id"), id);
                } catch (NumberFormatException nfe) {
                    return cc.disjunction(); // not numeric, no-op here
                }
            };
            Specification<Issue> byUsername = (r, qq, cc) ->
                    cc.equal(cc.lower(join.get("username")), assignee.toLowerCase());

            return Specification.where(byId).or(byUsername).toPredicate(root, q, cb);
        };
    }

    public static Specification<Issue> build(IssueStatus status, IssuePriority priority, String assignee) {
        return Specification.where(hasStatus(status))
                .and(hasPriority(priority))
                .and(hasAssignee(assignee));
    }
}
