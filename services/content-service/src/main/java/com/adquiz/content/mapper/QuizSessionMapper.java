package com.adquiz.content.mapper;

import com.adquiz.content.dto.SessionStateDto;
import com.adquiz.content.dto.SessionSummaryDto;
import com.adquiz.content.entity.QuizSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface QuizSessionMapper {

    @Mapping(target = "sessionId", source = "id")
    @Mapping(target = "topicId", source = "topic.id")
    @Mapping(target = "topicName", source = "topic.name")
    SessionSummaryDto toSessionSummaryDto(QuizSession session);

    @Mapping(target = "sessionId", source = "id")
    SessionStateDto toSessionStateDto(QuizSession session);
}
