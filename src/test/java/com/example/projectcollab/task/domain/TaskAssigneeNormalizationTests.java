package com.example.projectcollab.task.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class TaskAssigneeNormalizationTests {
    @ParameterizedTest
    @MethodSource("statesAfterMembershipEnd")
    void membershipEndUnassignsAndNormalizesEveryState(
            final Task.TaskState currentState,
            final Task.TaskState expectedState
    ) {
        Task task = Task.restore(
                1L,
                new Creator("creator"),
                new TaskContent("제목", "설명"),
                new TaskAssignment.Assigned(new Assignee("member")),
                currentState,
                currentState == Task.TaskState.REJECTED ? "반려 사유" : null
        );

        task.removeAssigneeForMembershipEnd();

        assertThat(task.assignment()).isInstanceOf(TaskAssignment.Unassigned.class);
        assertThat(task.state()).isEqualTo(expectedState);
    }

    private static Stream<Arguments> statesAfterMembershipEnd() {
        return Stream.of(
                Arguments.of(Task.TaskState.PENDING, Task.TaskState.PENDING),
                Arguments.of(Task.TaskState.REJECTED, Task.TaskState.REJECTED),
                Arguments.of(Task.TaskState.ACCEPTED, Task.TaskState.ACCEPTED),
                Arguments.of(Task.TaskState.IN_PROGRESS, Task.TaskState.ACCEPTED),
                Arguments.of(Task.TaskState.IN_REVIEW, Task.TaskState.ACCEPTED),
                Arguments.of(Task.TaskState.DONE, Task.TaskState.DONE)
        );
    }
}
