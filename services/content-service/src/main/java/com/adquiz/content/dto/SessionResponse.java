package com.adquiz.content.dto;

import java.util.UUID;

public record SessionResponse(
        UUID sessionId,
        String status,
        QuestionDto firstQuestion
) {
}
