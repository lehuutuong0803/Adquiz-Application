package com.adquiz.aigeneration.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class GenerateQuestionRequest {

    private UUID topicId;

    private String topicName;

    private String parentTopicName;

    // Bloom's Taxonomy level 1-6 - tell OpenAI the cognitive difficulty required
    @Min(value = 1, message = "Bloom level must be at least 1")
    @Max(value = 6, message = "Bloom level must be at most 6")
    private int bloomLevel;

    // How many questions to generate in this request
    @Min(value = 1, message = "Count must be at least 1")
    @Max(value = 10, message = "Count must be at most 10")
    private int count;

    // Target audience — determines question style and context
    @NotNull(message = "Target audience is required")
    private TargetAudience targetAudience;

    // Existing question texts - passed to OpenAI to avoid generating duplicates
    private List<String> existingQuestions;

    public enum TargetAudience {
        UNIVERSITY_STUDENT,
        INTERVIEW_PREP
    }
}
