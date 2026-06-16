package com.adquiz.analytics.kafka;

import com.adquiz.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@KafkaListener(topics = "quiz-events", groupId = "analytics-service-group")
public class KafkaConsumer {

    private final AnalyticsService analyticsService;

    @KafkaHandler
    public void consumeAnswerSubmitted(AnswerSubmittedEvent event) {
        log.info("Consumed ANSWER_SUBMITTED for user {}, topic {}", event.userId(), event.topicId());
        analyticsService.handleAnswerSubmitted(event);
    }

    @KafkaHandler
    public void consumeSessionCompleted(SessionCompletedEvent event) {
        log.info("Consumed SESSION_COMPLETED for user {}", event.userId());
        analyticsService.handleSessionCompleted(event.userId(), event.completedAt().toLocalDate());
    }
}
