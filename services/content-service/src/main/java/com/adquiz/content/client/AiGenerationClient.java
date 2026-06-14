package com.adquiz.content.client;

import com.adquiz.content.config.AIGenerationProperties;
import com.adquiz.content.dto.questiongeneration.GenerateRequest;
import com.adquiz.content.dto.questiongeneration.GeneratedQuestionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiGenerationClient {

    private final AIGenerationProperties properties;
    private final RestClient restClient;

    public List<GeneratedQuestionResponse> generateQuestions(
            String topicName,
            String parentTopicName,
            int bloomLevel,
            int count,
            String targetAudience
    ) {
        log.info("Requesting {} questions for topic '{}' at bloom level {}",
                count, topicName, bloomLevel);
        return restClient.post()
                .uri(properties.getServiceUrl() + "/api/generate/questions")
                .body(new GenerateRequest(topicName, parentTopicName, bloomLevel, count, targetAudience))
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<>() {});
    }
}
