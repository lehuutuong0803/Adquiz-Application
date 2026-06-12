package com.adquiz.content.dto.questiongeneration;

import java.util.List;

public record GeneratedQuestionResponse(
        String questionText,
        List<Option> options,
        String correctAnswer,
        String explanation,
        int bloomLevel
) {
}
