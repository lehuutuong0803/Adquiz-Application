package com.adquiz.content.kafka;

import java.time.LocalDateTime;
import java.util.UUID;

public record SessionCompletedEvent (
        UUID eventId,
        String eventType,
        UUID userId,
        UUID sessionId,
        UUID topicId,
        String topicName,
        String parentTopicName,
        int totalQuestions,
        int correctAnswers,
        double finalAccuracy,
        LocalDateTime completedAt
){
}
