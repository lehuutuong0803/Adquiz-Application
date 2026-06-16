package com.adquiz.analytics.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AnswerSubmittedEvent (
        UUID userId,
        UUID topicId,
        String topicName,
        String parentTopicName,
        boolean isCorrect,
        LocalDateTime answeredAt
){
}
