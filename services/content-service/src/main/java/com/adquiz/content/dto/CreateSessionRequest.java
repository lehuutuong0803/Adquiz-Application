package com.adquiz.content.dto;

import jakarta.validation.constraints.*;

import java.util.UUID;

public record CreateSessionRequest(
        //Option A - user selected existing topics from suggestions
        UUID topicId, // present if user picked a suggestion

        //Option B - user typed new topic name
        String topicName,
        UUID parentTopicId,// confirmed parent Id from step 1
        String parentTopicName, // only used if parent is also new

        @NotNull(message = "mode is required")
        @Pattern(regexp = "ADAPTIVE|REVIEW", message = "mode must be ADAPTIVE or REVIEW")
        String mode,

        @NotNull
        @Min(value = 2, message = "Minimum 5 questions per session")
        @Max(value = 20, message = "Maximum 20 questions per session")
        Integer totalQuestions,

        @NotBlank(message = "targetAudience is required")
        @Pattern(regexp = "UNIVERSITY_STUDENT|INTERVIEW_PREP", message = "targetAudience must be UNIVERSITY_STUDENT or" +
                " INTEVIEW_PREP")
        String targetAudience
) {
}
