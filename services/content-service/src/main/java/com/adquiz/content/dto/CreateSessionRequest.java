package com.adquiz.content.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateSessionRequest(
        @NotNull(message = "topic_id is required")
        UUID topicId,

        @NotNull(message = "mode is required")
        String mode,

        @NotNull
        @Min(value = 5, message = "Minimum 5 questions per session")
        @Max(value = 20, message = "Maximum 20 questions per session")
        Integer totalQuestions
) {
}
