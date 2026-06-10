package com.adquiz.content.dto;

import java.time.LocalDate;
import java.util.UUID;

public record DueReviewDto(
        UUID topicId,
        String topicName,
        String parentTopicName,
        LocalDate lastReviewDate,
        Integer intervalDays
) {
}
