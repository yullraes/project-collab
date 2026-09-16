package com.example.projectcollab.task.domain;

import java.util.Set;

public final class TaskProjectSnapshot {
    private final Set<Long> ownerUserIds;
    private final Set<Long> adminUserIds;
    private final Set<Long> memberUserIds;

    public TaskProjectSnapshot(
            final Set<Long> ownerUserIds,
            final Set<Long> adminUserIds,
            final Set<Long> memberUserIds
    ) {
        this.ownerUserIds = Set.copyOf(ownerUserIds);
        this.adminUserIds = Set.copyOf(adminUserIds);
        this.memberUserIds = Set.copyOf(memberUserIds);
    }

    public boolean isMember(final long userId) {
        return isManager(userId) || memberUserIds.contains(userId);
    }

    public boolean isManager(final long userId) {
        return ownerUserIds.contains(userId) || adminUserIds.contains(userId);
    }
}
