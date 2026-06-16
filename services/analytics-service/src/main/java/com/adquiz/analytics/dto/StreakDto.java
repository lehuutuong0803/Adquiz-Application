package com.adquiz.analytics.dto;

import java.time.LocalDate;

public record StreakDto(
        int currentStreak,
        int longestStreak,
        LocalDate lastActiveDate
) {
}
