package com.adquiz.content.service;

import com.adquiz.content.dto.CreateSessionRequest;
import com.adquiz.content.dto.SessionResponse;
import com.adquiz.content.entity.Topic;
import com.adquiz.content.mapper.QuizSessionMapper;
import com.adquiz.content.repository.QuizSessionRepository;
import com.adquiz.content.repository.SessionQuestionRepository;
import com.adquiz.content.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionQuestion {

    private final QuizSessionRepository quizSessionRepository;
    private final SessionQuestionRepository sessionQuestionRepository;
    private final TopicRepository topicRepository;
    private final QuestionService questionService;
    private final AdaptiveAlgorithm adaptiveAlgorithm;
    private final QuizSessionMapper quizSessionMapper;

    @Transactional
    public SessionResponse createSession(CreateSessionRequest request, Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());

        Topic topic = topicRepository.findById(request.topicId())
    }

}
