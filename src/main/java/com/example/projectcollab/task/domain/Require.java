package com.example.projectcollab.task.domain;

final class Require {
    private Require() {
    }

    static <T> T notNull(final T value, final String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    static String notBlank(final String value, final String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    static <T extends Number> T positive(final T number, final String message) {
        if (number == null || number.doubleValue() <= 0) {
            throw new IllegalArgumentException(message);
        }
        return number;
    }

    static void state(final boolean expression, final String message) {
        if (!expression) {
            throw new IllegalStateException(message);
        }
    }
}
