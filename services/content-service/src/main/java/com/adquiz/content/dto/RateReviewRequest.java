package com.adquiz.content.dto;

import jakarta.validation.constraints.NotNull;

public record RateReviewRequest(
        @NotNull(message = "rating is required")
        String rating
) {
}
