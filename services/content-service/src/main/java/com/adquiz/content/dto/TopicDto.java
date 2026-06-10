package com.adquiz.content.dto;

import java.util.List;
import java.util.UUID;

public record TopicDto(
        UUID id,
        String name,
        List<SubtopicDto> subtopics
) {}
