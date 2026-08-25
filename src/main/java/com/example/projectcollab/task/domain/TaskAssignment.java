package com.example.projectcollab.task.domain;

public abstract sealed class TaskAssignment
        permits TaskAssignment.Assigned, TaskAssignment.Unassigned {

    private TaskAssignment() {
    }

    public static TaskAssignment assigned(final Assignee assignee) {
        return new Assigned(assignee);
    }

    public static TaskAssignment unassigned() {
        return Unassigned.INSTANCE;
    }

    public static final class Assigned extends TaskAssignment {
        private final Assignee assignee;

        private Assigned(final Assignee assignee) {
            this.assignee = Require.notNull(assignee, "담당자는 필수입니다.");
        }

        public Assignee assignee() {
            return assignee;
        }
    }

    public static final class Unassigned extends TaskAssignment {
        private static final Unassigned INSTANCE = new Unassigned();

        private Unassigned() {
        }
    }
}
