package com.adquiz.content.dto;
import java.time.LocalDateTime;
import java.util.UUID;

public record SessionSummaryDto(
        UUID sessionId,
        UUID topicId,
        String topicName,
        String mode,
        String status,
        Integer totalQuestions,
        LocalDateTime startedAt,
        LocalDateTime endedAt
) {
}
