package com.adquiz.analytics.dto;

import java.time.LocalDate;

public record DailyActivityDto(
        LocalDate date,
        int questionsAnswered,
        int correctAnswers,
        int sessionsCompleted
) {
}
