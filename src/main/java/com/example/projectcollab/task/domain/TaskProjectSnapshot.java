package com.example.projectcollab.task.domain;

import java.util.Set;

public final class TaskProjectSnapshot {
    private final String ownerUserId;
    private final Set<String> adminUserIds;
    private final Set<String> memberUserIds;

    public TaskProjectSnapshot(
            final String ownerUserId,
            final Set<String> adminUserIds,
            final Set<String> memberUserIds
    ) {
        this.ownerUserId = ownerUserId;
        this.adminUserIds = Set.copyOf(adminUserIds);
        this.memberUserIds = Set.copyOf(memberUserIds);
    }

    public boolean isMember(final String userId) {
        return isManager(userId) || userId != null && memberUserIds.contains(userId);
    }

    public boolean isManager(final String userId) {
        return ownerUserId != null && ownerUserId.equals(userId)
                || userId != null && adminUserIds.contains(userId);
    }
}
