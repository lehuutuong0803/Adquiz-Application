package com.adquiz.content.service;

import com.adquiz.content.client.AiGenerationClient;
import com.adquiz.content.config.AIGenerationProperties;
import com.adquiz.content.dto.questiongeneration.GeneratedQuestionResponse;
import com.adquiz.content.entity.Question;
import com.adquiz.content.entity.Topic;
import com.adquiz.content.repository.QuestionRepository;
import com.adquiz.content.repository.SessionQuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
    public void generateQuestionsForAllLevels(Topic topic) {
        log.info("Generating questions for all bloom levels for topic '{}'", topic.getName());
        String parentName = topic.getParent() != null ? topic.getParent().getName() : "";

        List<CompletableFuture<List<Question>>> futures= new ArrayList<>();

        for (int level = 1; level <= BLOOM_LEVELS; level++) {
            final int bloomLevel = level;
            CompletableFuture<List<Question>> future = CompletableFuture.supplyAsync(
                    () -> fetchAndMapQuestions(topic, parentName, bloomLevel)
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
    public void topUpIfNeeded(Topic topic, int bloomLevel, Set<UUID> excludedQuestionIds) {
        Set<UUID> answeredIds =

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

    // @Transactional only wraps the DB write - short and fast
    @Transactional
    public void saveAllQuestions(List<Question> questions) {
        questionRepository.saveAll(questions);
        log.info("Saved {} questions", questions.size());
    }

    // Private helper - call AI client and map to entities
    private List<Question> fetchAndMapQuestions(Topic topic, String parentName, int bloomLevel ) {
        List<GeneratedQuestionResponse> responses = aiGenerationClient.generateQuestions(
                topic.getName(), parentName, bloomLevel, QUESTIONS_PER_LEVEL
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
                   return q;
                }).collect(Collectors.toList());

    }

}
