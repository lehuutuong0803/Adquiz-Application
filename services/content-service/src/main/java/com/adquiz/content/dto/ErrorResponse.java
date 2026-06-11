package com.adquiz.content.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
        int status,
        String Error,
        String message,
        LocalDateTime timestamp
) {
}
