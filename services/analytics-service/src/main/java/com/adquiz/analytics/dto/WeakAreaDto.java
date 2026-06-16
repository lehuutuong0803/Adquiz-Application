package com.adquiz.analytics.dto;

import java.util.UUID;

public record WeakAreaDto(
        UUID topicId,
        String topicName,
        String parentTopicName,
        int totalAnswered,
        double accuracy
) {
}
