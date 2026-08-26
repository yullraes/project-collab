package com.example.projectcollab.user.application;

import java.util.Locale;

public final class UserInputNormalizer {
    private UserInputNormalizer() {
    }

    public static String name(final String value) {
        return value.trim();
    }

    public static String email(final String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
