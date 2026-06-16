package com.adquiz.analytics.dto;

import java.util.UUID;

public record TopicAccuracyDto(
        UUID topicId,
        String topicName,
        String parentTopicName,
        int totalAnswered,
        int totalCorrect,
        double accuracy
) {
}
