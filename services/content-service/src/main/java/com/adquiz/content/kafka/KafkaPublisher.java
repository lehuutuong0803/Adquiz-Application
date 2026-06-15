package com.adquiz.content.kafka;

import com.adquiz.content.entity.Topic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
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
            Topic topic,
            int bloomLevel,
            boolean isCorrect,
            int confidenceRating,
            String targetAudience
    ) {
        AnswerSubmittedEvent event = new AnswerSubmittedEvent(
                UUID.randomUUID(),
                "ANSWER_SUBMITTED",
                userId,
                sessionId,
                questionId,
                topic.getId(),
                topic.getName(),
                topic.getParent() != null ? topic.getParent().getName() : null,
                bloomLevel,
                isCorrect,
                confidenceRating,
                targetAudience,
                LocalDateTime.now()
        );

        kafkaTemplate.send(TOPIC, sessionId.toString(), event);
        log.info("Published ANSWER_SUBMITTED event for session {}", sessionId);
    }

    public void publishSessionCompleted(UUID userId, UUID sessionId, Topic topic,
                                        int totalQuestions, int correctAnswers) {
        double finalAccuracy = totalQuestions == 0 ? 0.0 : (double) correctAnswers / totalQuestions;

        SessionCompletedEvent event = new SessionCompletedEvent(
                UUID.randomUUID(),
                "SESSION_COMPLETED",
                userId,
                sessionId,
                topic.getId(),
                topic.getName(),
                topic.getParent() != null ? topic.getParent().getName() : null,
                totalQuestions,
                correctAnswers,
                finalAccuracy,
                LocalDateTime.now());

        kafkaTemplate.send(TOPIC, sessionId.toString(), event);
        log.info("Published SESSION_COMPLETED event for session {}", sessionId);
    }
}
