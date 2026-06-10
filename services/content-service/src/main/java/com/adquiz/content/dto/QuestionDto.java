package com.adquiz.content.dto;

import java.util.List;
import java.util.UUID;

public record QuestionDto (
        UUID id,
        String text,
        List<OptionDto> options,
        Short bloomLevel,
        Integer questionIndex,
        Integer totalQuestions
) {}
