package com.adquiz.aigeneration.service;

import com.adquiz.aigeneration.dto.GenerateQuestionRequest;
import com.adquiz.aigeneration.dto.GeneratedQuestionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionGenerationService {
    private final ChatClient chatClient;

    public List<GeneratedQuestionDto> generateQuestions(GenerateQuestionRequest request) {
        log.info("Generating {} questions for topic: {} ({}), bloom level: {}, audience: {}",
                request.getCount(),
                request.getTopicName(),
                request.getParentTopicName(),
                request.getBloomLevel(),
                request.getTargetAudience());
        String promptText = buildPrompt(request);
        log.debug("Prompt for generating Questions: " +promptText);
        List<GeneratedQuestionDto> questions = chatClient.prompt()
                .user(promptText)
                .call()
                .entity(new ParameterizedTypeReference<List<GeneratedQuestionDto>>() {});

        log.info("Successfully generated {} questions", questions.size());

        return questions;

    }

    private String buildPrompt(GenerateQuestionRequest request) {
        return """
                You are an expert %s creating multiple choice quiz questions.
                
                Generate exactly %d questions about "%s" under the subject "%s".
                
                Difficulty level: Bloom's Taxonomy Level %d — %s
                
                Target audience context:
                %s
                
                Requirements:
                - Each question must have exactly 4 options labeled a, b, c, d
                - Exactly one option must be correct
                - Do not repeat or closely resemble these existing questions: %s
                
                Respond ONLY with a valid JSON array in this exact format:
                [
                  {
                    "questionText": "...",
                    "options": [
                      {"id": "a", "text": "..."},
                      {"id": "b", "text": "..."},
                      {"id": "c", "text": "..."},
                      {"id": "d", "text": "..."}
                    ],
                    "correctAnswer": "a",
                    "explanation": "...",
                    "bloomLevel": %d
                  }
                ]
                """.formatted(
                audienceRole(request.getTargetAudience()),
                request.getCount(),
                request.getTopicName(),
                request.getParentTopicName(),
                request.getBloomLevel(),
                bloomLevelDescription(request.getBloomLevel()),
                audienceContext(request.getTargetAudience()),
                request.getExistingQuestions(),
                request.getBloomLevel()
        );
    }

    private String audienceRole(GenerateQuestionRequest.TargetAudience audience) {
        return switch (audience) {
            case UNIVERSITY_STUDENT -> "university professor";
            case INTERVIEW_PREP -> "senior software engineer at a top tech company";
        };
    }

    private String bloomLevelDescription(int level) {
        return switch (level) {
            case 1 -> """
                    Remember — Test recall of facts, definitions, and basic concepts.
                    Questions should ask students to identify, list, or name specific information.
                    Example: "What is the time complexity of binary search?"
                    """;
            case 2 -> """
                    Understand — Test comprehension and ability to explain ideas.
                    Questions should ask students to describe, interpret, or summarize concepts.
                    Example: "Why does a hash table have O(1) average lookup time?"
                    """;
            case 3 -> """
                    Apply — Test ability to use knowledge in new situations.
                    Questions should present a scenario and ask students to solve or implement.
                    Example: "Given an unsorted array, which sorting algorithm minimizes memory usage?"
                    """;
            case 4 -> """
                    Analyze — Test ability to break down and compare information.
                    Questions should ask students to differentiate, examine trade-offs, or find relationships.
                    Example: "Compare the trade-offs between a linked list and an array for frequent insertions."
                    """;
            case 5 -> """
                    Evaluate — Test ability to justify decisions and critique solutions.
                    Questions should present multiple approaches and ask students to defend the best choice.
                    Example: "Which database indexing strategy is most appropriate for this query pattern and why?"
                    """;
            case 6 -> """
                    Create — Test ability to design and construct original solutions.
                    Questions should ask students to propose a design or architect a solution from scratch.
                    Example: "How would you design a cache eviction system that handles 1 million requests per second?"
                    """;
            default -> "Unknown level";
        };
    }

    private String audienceContext(GenerateQuestionRequest.TargetAudience audience) {
        return switch (audience) {
            case UNIVERSITY_STUDENT -> """
                    - Questions are for university students preparing for academic exams
                    - Focus on theoretical understanding and academic concepts
                    - Use formal academic language
                    - Questions should align with university curriculum standards
                    """;
            case INTERVIEW_PREP -> """
                    - Questions are for software developers preparing for technical interviews
                    - Focus on practical application and real-world scenarios
                    - Use industry terminology and realistic constraints
                    - Questions should reflect what top tech companies ask (Google, Meta, Amazon, etc.)
                    - Emphasize problem-solving approach and trade-off analysis
                    """;
        };
    }

}
