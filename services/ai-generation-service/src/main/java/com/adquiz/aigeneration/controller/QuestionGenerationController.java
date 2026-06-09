package com.adquiz.aigeneration.controller;

import com.adquiz.aigeneration.dto.GenerateQuestionRequest;
import com.adquiz.aigeneration.dto.GeneratedQuestionDto;
import com.adquiz.aigeneration.service.QuestionGenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/generate")
public class QuestionGenerationController {

    private final QuestionGenerationService questionGenerationService;

    @PostMapping("/questions")
    public ResponseEntity<List<GeneratedQuestionDto>> generateQuestions(
            @Valid @RequestBody GenerateQuestionRequest request) {
        log.info("Received question generation request for topic: {}", request.getTopicName());
        List<GeneratedQuestionDto> questionDtoList = questionGenerationService.generateQuestions(request);

        return ResponseEntity.ok(questionDtoList);
    }
}
