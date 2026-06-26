package com.adquiz.content.controller;

import com.adquiz.content.dto.SubtopicDto;
import com.adquiz.content.dto.TopicDto;
import com.adquiz.content.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/search-parents")
    public ResponseEntity<List<SubtopicDto>> searchParents(@RequestParam String q) {
        return ResponseEntity.ok(topicService.searchParentTopics(q));
    }

    @GetMapping("/search")
    public ResponseEntity<List<SubtopicDto>> searchSubtopics(
            @RequestParam String q,
            @RequestParam UUID parentId) {
        return ResponseEntity.ok(topicService.searchSubtopics(q, parentId));
    }
}
