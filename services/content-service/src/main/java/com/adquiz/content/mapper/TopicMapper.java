package com.adquiz.content.mapper;

import com.adquiz.content.dto.SubtopicDto;
import com.adquiz.content.dto.TopicDto;
import com.adquiz.content.entity.Topic;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TopicMapper {

    @Mapping(target = "subtopics", ignore = true)
    TopicDto toTopicDto(Topic topic);

    SubtopicDto toSubtopicDto(Topic topic);

    List<SubtopicDto> toSubtopicDtoList(List<Topic> topics);
}
