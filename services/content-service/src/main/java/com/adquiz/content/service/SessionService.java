package com.adquiz.content.service;

import com.adquiz.content.dto.*;
import com.adquiz.content.entity.Question;
import com.adquiz.content.entity.QuizSession;
import com.adquiz.content.entity.SessionQuestion;
import com.adquiz.content.entity.Topic;
import com.adquiz.content.exception.ResourceNotFoundException;
import com.adquiz.content.kafka.KafkaPublisher;
import com.adquiz.content.mapper.QuizSessionMapper;
import com.adquiz.content.repository.QuizSessionRepository;
import com.adquiz.content.repository.SessionQuestionRepository;
import com.adquiz.content.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final QuizSessionRepository quizSessionRepository;
    private final SessionQuestionRepository sessionQuestionRepository;
    private final TopicRepository topicRepository;
    private final QuestionService questionService;
    private final AdaptiveAlgorithm adaptiveAlgorithm;
    private final QuizSessionMapper quizSessionMapper;
    private final KafkaPublisher kafkaPublisher;

    @Transactional
    public SessionResponse createSession(CreateSessionRequest request, Authentication auth) {
        UUID userId = extractUserId(auth);

        Topic topic = resolveTopic(request);

        if (questionService.isQuestionBankEmpty(topic.getId())) {
            log.info("Question Bank empty for topic: '{}'", topic.getName());
            questionService.generateQuestionsForAllLevels(topic);
        }

        QuizSession quizSession = new QuizSession();
        quizSession.setUserId(userId);
        quizSession.setTopic(topic);
        quizSession.setMode(request.mode());
        quizSession.setStatus("IN_PROGRESS");
        quizSession.setTotalQuestions(request.totalQuestions().shortValue());
        quizSession.setCurrentQuestionIndex((short) 1);
        quizSession.setCurrentDifficulty((short) 1);
        quizSession.setStartedAt(LocalDateTime.now());
        quizSessionRepository.save(quizSession);

        Question firstQuestion = questionService.pickNextQuestion(
                topic.getId(), 1, Set.of());

        SessionQuestion sessionQuestion = new SessionQuestion();
        sessionQuestion.setSession(quizSession);
        sessionQuestion.setQuestion(firstQuestion);
        sessionQuestion.setQuestionIndex((short) 1);
        sessionQuestionRepository.save(sessionQuestion);

        return new SessionResponse(
                quizSession.getId(),
                quizSession.getStatus(),
                buildQuestionDto(firstQuestion, 1, request.totalQuestions())
        );

    }

    // Submit an answer
    @Transactional
    public AnswerResponse submitAnswer(UUID sessionId, SubmitAnswerRequest request, Authentication auth) {
        UUID userId = extractUserId(auth);

        QuizSession session = quizSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Session not found: "+ sessionId));

        if (!session.getStatus().equals("IN_PROGRESS")) {
            throw new IllegalStateException("Session is not in progress");
        }

        SessionQuestion current = sessionQuestionRepository
                .findBySessionIdAndQuestionIndex(sessionId, session.getCurrentQuestionIndex())
                .orElseThrow(() -> new ResourceNotFoundException("Current question not found"));

        Question question = current.getQuestion();
        boolean isCorrect = question.getCorrectAnswer().equals(request.selectedOption());

        current.setSelectedOption(request.selectedOption());
        current.setIsCorrect(isCorrect);
        current.setConfidenceRating(request.confidenceRating().shortValue());
        current.setAnsweredAt(LocalDateTime.now());
        sessionQuestionRepository.save(current);

        int consecutiveCorrect = countConsecutiveCorrect(sessionId);
        int newDifficulty = adaptiveAlgorithm.calculateNextDifficulty(
                session.getCurrentDifficulty(),
                isCorrect,
                request.confidenceRating(),
                consecutiveCorrect
        );

        boolean isLastQuestion = session.getCurrentQuestionIndex() >= session.getTotalQuestions();

        // finish the session
        if (isLastQuestion) {
            session.setStatus("COMPLETED");
            session.setEndedAt(LocalDateTime.now());
            quizSessionRepository.save(session);

            return new AnswerResponse(
              isCorrect,
              question.getCorrectAnswer(),
              question.getExplanation(),
              null,
              "COMPLETED"
            );
        }

        // continue the session
        session.setCurrentDifficulty((short) newDifficulty);
        session.setCurrentQuestionIndex((short) (session.getCurrentQuestionIndex() +1));
        quizSessionRepository.save(session);

        Set<UUID> answeredIds = sessionQuestionRepository
                .findBySessionIdOrderByQuestionIndex(sessionId)
                .stream()
                .map(sq -> sq.getQuestion().getId())
                .collect(Collectors.toSet());

        Question nextQuestion = questionService.pickNextQuestion(
                session.getTopic().getId(), newDifficulty, answeredIds);

        SessionQuestion nextSessionQuestion = new SessionQuestion();
        nextSessionQuestion.setSession(session);
        nextSessionQuestion.setQuestion(nextQuestion);
        nextSessionQuestion.setQuestionIndex(session.getCurrentQuestionIndex());
        sessionQuestionRepository.save(nextSessionQuestion);

        kafkaPublisher.publishAnswerSubmitted(
                userId, session.getId(), question.getId(),session.getTopic().getId(), newDifficulty, isCorrect,
                request.confidenceRating());

        return new AnswerResponse(
                isCorrect,
                question.getCorrectAnswer(),
                question.getExplanation(),
                buildQuestionDto(nextQuestion,
                        session.getCurrentQuestionIndex(),
                        (int) session.getTotalQuestions()),
                "IN_PROGRESS"
        );

    }

    // Abandon a session
    @Transactional
    public void abandonSession(UUID sessionId, Authentication auth) {
        UUID userId = extractUserId(auth);
        QuizSession session = quizSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Session not found: "+ sessionId));
        session.setStatus("ABANDONED");
        session.setEndedAt(LocalDateTime.now());
        quizSessionRepository.save(session);
    }

    // Get session history
    @Transactional(readOnly = true)
    public List<SessionSummaryDto> getSessionHistory(Authentication auth) {
        UUID userId = extractUserId(auth);
        return quizSessionRepository.findByUserIdOrderByStartedAtDesc(userId)
                .stream()
                .map(quizSessionMapper::toSessionSummaryDto)
                .collect(Collectors.toList());
    }

    // Get session state
    @Transactional(readOnly = true)
    public SessionStateDto getSessionState(UUID sessionId, Authentication auth) {
        UUID userId = extractUserId(auth);
        QuizSession session = quizSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Session Not Found" + sessionId));

        return quizSessionMapper.toSessionStateDto(session);
    }

    private int countConsecutiveCorrect(UUID sessionId) {
        List<SessionQuestion> answered = sessionQuestionRepository.findBySessionIdOrderByQuestionIndex(sessionId);

        int count = 0;
        for (int i = answered.size() - 1; i >=0; i--) {
            Boolean correct = answered.get(i).getIsCorrect();
            if (correct != null && correct) {
                count++;
            } else {
                break;
            }

        }
        return count;
    }

    private QuestionDto buildQuestionDto(Question question, int index, int total) {
        return new QuestionDto(
                question.getId(),
                question.getQuestionText(),
                question.getOptions().stream()
                        .map(o -> new OptionDto(o.getId(), o.getText()))
                        .collect(Collectors.toList()),
                question.getBloomLevel(),
                index,
                total
        );
    }

    private Topic resolveTopic(CreateSessionRequest request) {
        // User selected an existing subtopic from suggestions
        if (request.topicId() != null) {
            return topicRepository.findById(request.topicId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Topic not found: " + request.topicId()));
        }

        // check topicName
        if (request.topicName() == null || request.topicName().isBlank()) {
            throw new IllegalArgumentException(
                    "Either topicId or topicName must be provided");
        }

        // Resolve parent topic
        Topic parent;
        if (request.parentTopicId() != null) {
            // User selected an existing parent from suggestions
            parent = topicRepository.findById(request.parentTopicId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Parent topic not found: " + request.parentTopicId()));
        } else {
            // Parent is also new - find by name or create
            if (request.parentTopicName() == null || request.parentTopicName().isBlank()) {
                throw new IllegalArgumentException(
                        "parenTopicId or parentTopicName must be provided");
            }
            parent = topicRepository.findByNameIgnoreCase(request.parentTopicName())
                    .orElseGet(() -> {
                        Topic p = new Topic();
                        p.setName(request.parentTopicName());
                        p.setCreatedAt(LocalDateTime.now());
                        return topicRepository.save(p);
                    });
        }

        // Create a new subtopic under resolved parent
        Topic newTopic = new Topic();
        newTopic.setName(request.topicName());
        newTopic.setParent(parent);
        newTopic.setCreatedAt(LocalDateTime.now());
        return topicRepository.save(newTopic);
    }

    private UUID extractUserId(Authentication auth) {
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) auth;
        Jwt jwt = (Jwt) jwtAuth.getCredentials();
        return UUID.fromString(jwt.getSubject());
    }

}
