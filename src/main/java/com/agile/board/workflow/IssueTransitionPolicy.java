package com.agile.board.workflow;

import com.agile.board.domain.IssueStatus;

import java.util.*;

public final class IssueTransitionPolicy {
    private static final Map<IssueStatus, Set<IssueStatus>> ALLOWED;

    static {
        Map<IssueStatus, Set<IssueStatus>> m = new EnumMap<>(IssueStatus.class);
        m.put(IssueStatus.OPEN, EnumSet.of(IssueStatus.IN_PROGRESS));
        m.put(IssueStatus.IN_PROGRESS, EnumSet.of(IssueStatus.IN_REVIEW));
        m.put(IssueStatus.IN_REVIEW, EnumSet.of(IssueStatus.DONE));
        m.put(IssueStatus.DONE, Collections.emptySet()); // terminal
        ALLOWED = Collections.unmodifiableMap(m);
    }

    private IssueTransitionPolicy() {}

    public static boolean isAllowed(IssueStatus from, IssueStatus to) {
        return ALLOWED.getOrDefault(from, Collections.emptySet()).contains(to);
    }

    public static Set<IssueStatus> nextAllowed(IssueStatus from) {
        return ALLOWED.getOrDefault(from, Collections.emptySet());
    }
}