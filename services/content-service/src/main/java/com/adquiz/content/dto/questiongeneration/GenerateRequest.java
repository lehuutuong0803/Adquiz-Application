package com.adquiz.content.dto.questiongeneration;

public record GenerateRequest(
        String topicName,
        String parentTopicName,
        int bloomLevel,
        int count,
        String targetAudience
) {
}
