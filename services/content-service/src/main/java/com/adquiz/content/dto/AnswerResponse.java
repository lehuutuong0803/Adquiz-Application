package com.adquiz.content.dto;

public record AnswerResponse(
        boolean isCorrect,
        String correctAnswer,
        String explanation,
        QuestionDto nextQuestion,
        String sessionStatus
) {
}
