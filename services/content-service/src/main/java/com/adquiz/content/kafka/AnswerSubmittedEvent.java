package com.adquiz.content.kafka;

import java.time.LocalDateTime;
import java.util.UUID;

public record AnswerSubmittedEvent (
        UUID eventId,
        String eventType,
        UUID userId,
        UUID sessionId,
        UUID questionId,
        UUID topicId,
        int bloomLevel,
        boolean isCorrect,
        int confidenceRating,
        LocalDateTime answeredAt
){}
