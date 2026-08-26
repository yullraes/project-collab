package com.example.projectcollab.task.domain;

import java.util.Set;

public final class TaskProjectSnapshot {
    private final Long ownerUserId;
    private final Set<Long> adminUserIds;
    private final Set<Long> memberUserIds;

    public TaskProjectSnapshot(
            final Long ownerUserId,
            final Set<Long> adminUserIds,
            final Set<Long> memberUserIds
    ) {
        this.ownerUserId = ownerUserId;
        this.adminUserIds = Set.copyOf(adminUserIds);
        this.memberUserIds = Set.copyOf(memberUserIds);
    }

    public boolean isMember(final long userId) {
        return isManager(userId) || memberUserIds.contains(userId);
    }

    public boolean isManager(final long userId) {
        return ownerUserId != null && ownerUserId == userId
                || adminUserIds.contains(userId);
    }
}
