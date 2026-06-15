package com.adquiz.content.service;

import com.adquiz.content.client.AiGenerationClient;
import com.adquiz.content.config.AIGenerationProperties;
import com.adquiz.content.dto.questiongeneration.GeneratedQuestionResponse;
import com.adquiz.content.entity.Question;
import com.adquiz.content.entity.SessionQuestion;
import com.adquiz.content.entity.Topic;
import com.adquiz.content.repository.QuestionRepository;
import com.adquiz.content.repository.SessionQuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionService {

    private static final int QUESTIONS_PER_LEVEL = 3;
    private static final int BLOOM_LEVELS = 6;

    private final QuestionRepository questionRepository;
    private final AiGenerationClient aiGenerationClient;
    private final AIGenerationProperties properties;
    private final SessionQuestionRepository sessionQuestionRepository;

    // Check if question bank is empty for a topic
    public boolean isQuestionBankEmpty(UUID id) {
        return questionRepository.countByTopicId(id) == 0;
    }

    // Generate questions for all 6 bloom levels in parallel
    public void generateQuestionsForAllLevels(Topic topic, String targetAudience) {
        log.info("Generating questions for all bloom levels for topic '{}'", topic.getName());
        String parentName = topic.getParent() != null ? topic.getParent().getName() : "";

        List<CompletableFuture<List<Question>>> futures= new ArrayList<>();

        for (int level = 1; level <= BLOOM_LEVELS; level++) {
            final int bloomLevel = level;
            CompletableFuture<List<Question>> future = CompletableFuture.supplyAsync(
                    () -> fetchAndMapQuestions(topic, parentName, bloomLevel, targetAudience)
            );
            futures.add(future);
        }

        List<Question> allQuestions = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        ).thenApply(v -> futures.stream()
                .flatMap(f -> f.join().stream())
                .collect(Collectors.toList())
        ).join();

        saveAllQuestions(allQuestions);
    }

    // Top up a specific bloom level if below threshold
    public void topUpIfNeeded(Topic topic, int bloomLevel,UUID userId, Set<UUID> answeredIds, String targetAudience) {

        long totalInBank = questionRepository.countByTopicIdAndBloomLevel(topic.getId(), (short) bloomLevel);

        long remainingForUser = totalInBank - answeredIds.size();

        if (remainingForUser < properties.getQuestionThreshold()) {
            log.info("User {} has fewer than {} unseen questions for topic '{}' level {} — topping up",
                    userId, properties.getQuestionThreshold(), topic.getName(), bloomLevel);

            String parentName = topic.getParent() != null ? topic.getParent().getName() : "";
            List<Question> newQuestions = fetchAndMapQuestions(topic, parentName,bloomLevel, targetAudience);
            saveAllQuestions(newQuestions);
        }

    }

    // Pick next question excluding already seen ones
    public Question pickNextQuestion(UUID topicId, int bloomLevel, Set<UUID> excludedIds) {
        Set<UUID> safeExcludedIds = excludedIds.isEmpty()
                ? Set.of(UUID.randomUUID())
                : excludedIds;

        return questionRepository
                .findFirstUnansweredQuestion(topicId, (short) bloomLevel, safeExcludedIds)
                .orElseThrow(() -> new RuntimeException(
                        "No available questions for topic " + topicId + " at level " + bloomLevel
                ));
    }

    // Pick a question for a REVIEW session, weighted toward weak areas
    public Question pickReviewQuestion(UUID userId, UUID topicId, Set<UUID> excludedIds) {
        List<SessionQuestion> answered = sessionQuestionRepository.findAnsweredByUserAndTopic(userId, topicId);

        Map<UUID, SessionQuestion> lastestAttemps = answered.stream()
                .collect(Collectors.toMap(
                        sq -> sq.getQuestion().getId(),
                        sq -> sq,
                        (a, b) -> a.getAnsweredAt().isAfter(b.getAnsweredAt()) ? a : b
                ));

        return lastestAttemps.values().stream()
                .filter(sq -> !excludedIds.contains(sq.getQuestion().getId()))
                .max(Comparator.comparingInt(this::weaknessScore))
                .map(SessionQuestion::getQuestion)
                .orElseThrow(() -> new RuntimeException(
                        "No available review question for topic: " + topicId));

    }

    private int weaknessScore(SessionQuestion sq) {
        int correctnessScore = Boolean.TRUE.equals(sq.getIsCorrect()) ? 1 : 10;
        int confidenceScore = (4 - sq.getConfidenceRating()) * 2;

        long daysSinceAnswered = ChronoUnit.DAYS.between(sq.getAnsweredAt().toLocalDate(), LocalDate.now());
        int recencyScore = (int) Math.min(daysSinceAnswered, 14);

        return correctnessScore + confidenceScore + recencyScore;
    }

    // @Transactional only wraps the DB write - short and fast
    @Transactional
    public void saveAllQuestions(List<Question> questions) {
        questionRepository.saveAll(questions);
        log.info("Saved {} questions", questions.size());
    }

    // Private helper - call AI client and map to entities
    private List<Question> fetchAndMapQuestions(Topic topic, String parentName, int bloomLevel, String targetAudience ) {
        List<GeneratedQuestionResponse> responses = aiGenerationClient.generateQuestions(
                topic.getName(), parentName, bloomLevel, QUESTIONS_PER_LEVEL, targetAudience
        );

        return responses.stream()
                .map(r -> {
                   Question q = new Question();
                   q.setTopic(topic);
                   q.setBloomLevel((short) bloomLevel);
                   q.setQuestionText(r.questionText());
                   q.setOptions(r.options().stream()
                           .map( o -> {
                               Question.OptionDto opt = new Question.OptionDto();
                               opt.setId(o.id());
                               opt.setText(o.text());
                               return opt;
                           }).collect(Collectors.toList()));
                   q.setCorrectAnswer(r.correctAnswer());
                   q.setExplanation(r.explanation());
                   q.setCreatedBy("AI");
                   q.setCreatedAt(LocalDateTime.now());
                   return q;
                }).collect(Collectors.toList());

    }

}
