package com.example.projectcollab.task.domain;

public sealed interface TaskAssignment
        permits TaskAssignment.Assigned, TaskAssignment.Unassigned {

    record Assigned(Assignee assignee) implements TaskAssignment {
        public Assigned {
            assignee = Require.notNull(assignee, "담당자는 필수입니다.");
        }
    }

    enum Unassigned implements TaskAssignment {
        INSTANCE
    }
}
