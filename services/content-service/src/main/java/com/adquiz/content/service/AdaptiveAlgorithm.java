package com.adquiz.content.service;

import org.springframework.stereotype.Component;

@Component
public class AdaptiveAlgorithm {

    private static final int MIN_DIFFICULTY = 1;
    private static final int MAX_DIFFICULTY = 6;

    public int calculateNextDifficulty(
            int currentDifficulty,
            boolean isCorrect,
            int confidenceRating,
            int consecutiveCorrect
    ) {
        if (!isCorrect) {
            return Math.max(MIN_DIFFICULTY, currentDifficulty - 1);
        }

        if (confidenceRating == 3 && consecutiveCorrect >= 2) {
            return Math.min(MAX_DIFFICULTY, currentDifficulty + 2);
        }

        if (consecutiveCorrect >= 2) {
            return  Math.min(MAX_DIFFICULTY, currentDifficulty + 1);
        }
        return currentDifficulty;
    }
}
