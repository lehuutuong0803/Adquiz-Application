package com.adquiz.content.dto;

import java.time.LocalDate;
import java.util.UUID;

public record RateReviewResponse(
        UUID topicId,
        LocalDate nextReviewDate,
        Integer intervalDays
) {
}
