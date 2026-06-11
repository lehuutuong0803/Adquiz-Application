package com.adquiz.content.controller;

import com.adquiz.content.dto.SubtopicDto;
import com.adquiz.content.dto.TopicDto;
import com.adquiz.content.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
public class TopicController {
    private final TopicService topicService;

    @GetMapping
    public ResponseEntity<List<TopicDto>> getAllTopics() {
        return ResponseEntity.ok(topicService.getAllTopics());
    }

    @GetMapping("/{id}/subtopics")
    public ResponseEntity<List<SubtopicDto>> getSubtopics(@PathVariable UUID id) {
        return ResponseEntity.ok(topicService.getSubtopicsByParentId(id));
    }
}
