package com.adquiz.content.kafka;

import com.adquiz.content.entity.Topic;
import com.adquiz.content.exception.ResourceNotFoundException;
import com.adquiz.content.repository.SessionQuestionRepository;
import com.adquiz.content.repository.TopicRepository;
import com.adquiz.content.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumer {

    private final QuestionService questionService;
    private final TopicRepository topicRepository;
    private final SessionQuestionRepository sessionQuestionRepository;

    @KafkaListener(topics = "quiz-events", groupId = "content-service-group")
    public void consume(AnswerSubmittedEvent event) {
        if (!"ANSWER_SUBMITTED".equals(event.eventType())) {
            log.warn("Wrong Event Type for ANSWER_SUBMITTED consumer for session {}", event.sessionId());
            return;
        }

        log.info("Consumed ANSWER_SUBMITTED event for session {}", event.sessionId());

        Topic topic = topicRepository.findById(event.topicId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Topic not found: " + event.topicId()));

        Set<UUID> answeredIds = sessionQuestionRepository.findAnswerdQuestionIds(
                event.userId(), event.topicId(), (short) event.bloomLevel());

        questionService.topUpIfNeeded(topic, event.bloomLevel(), event.userId(), answeredIds);


    }
}
