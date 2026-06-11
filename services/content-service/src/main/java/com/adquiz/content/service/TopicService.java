package com.adquiz.content.service;

import com.adquiz.content.dto.SubtopicDto;
import com.adquiz.content.dto.TopicDto;
import com.adquiz.content.entity.Topic;
import com.adquiz.content.mapper.TopicMapper;
import com.adquiz.content.repository.TopicRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;
    private final TopicMapper topicMapper;

    @Transactional(readOnly = true)
    public List<TopicDto> getAllTopics() {
        List<Topic> parentTopics = topicRepository.findByParentIsNull();

        return parentTopics.stream()
                .map(parent -> {
                    TopicDto dto = topicMapper.toTopicDto(parent);
                    return new TopicDto(dto.id(), dto.name(), getSubtopicsByParentId(parent.getId()));
                }).toList();
    }

    @Transactional(readOnly = true)
    public List<SubtopicDto> getSubtopicsByParentId(UUID parentId) {
        List<Topic> subtopics = topicRepository.findByParentId(parentId);
        return topicMapper.toSubtopicDtoList(subtopics);
    }
}
