package com.adquiz.content.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SubmitAnswerRequest(
        @NotNull(message = "question_id is required")
        UUID questionId,

        @NotNull(message = "selected_option is required")
        String selectedOption,

        @NotNull
        @Min(value = 1, message = "Confidence rating minimum is 1")
        @Max(value = 3, message = "Confidence rating maximum is 3")
        Integer confidenceRating
) {
}
