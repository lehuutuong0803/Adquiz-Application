package com.adquiz.content.mapper;

import com.adquiz.content.dto.DueReviewDto;
import com.adquiz.content.dto.RateReviewResponse;
import com.adquiz.content.entity.SpacedRepetitionRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SpacedRepetitionMapper {

    @Mapping(target = "topicId", source = "topic.id")
    @Mapping(target = "topicName", source = "topic.name")
    @Mapping(target = "parentTopicName", source = "topic.parent.name")
    DueReviewDto toDueReviewDto(SpacedRepetitionRecord record);

    @Mapping(target = "topicId", source = "topic.id")
    RateReviewResponse toRateReviewResponse(SpacedRepetitionRecord record);
}
