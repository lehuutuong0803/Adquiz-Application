package com.adquiz.content.dto;

import java.util.UUID;

public record SessionStateDto(
        UUID sessionId,
        String status,
        Integer currentQuestionIndex,
        Integer totalQuestions,
        Integer currentDifficulty
) {
}
