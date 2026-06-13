package com.adquiz.content.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaPublisher {
    private static final String TOPIC = "quiz-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishAnswerSubmitted(
            UUID userId,
            UUID sessionId,
            UUID questionId,
            UUID topicId,
            int bloomLevel,
            boolean isCorrect,
            int confidenceRating
    ) {
        AnswerSubmittedEvent event = new AnswerSubmittedEvent(
                UUID.randomUUID(),
                "ANSWER_SUBMITTED",
                userId,
                sessionId,
                questionId,
                topicId,
                bloomLevel,
                isCorrect,
                confidenceRating,
                LocalDateTime.now()
        );

        kafkaTemplate.send(TOPIC, sessionId.toString(), event);
        log.info("Published ANSWER_SUBMITTED event for session {}", sessionId);
    }
}
