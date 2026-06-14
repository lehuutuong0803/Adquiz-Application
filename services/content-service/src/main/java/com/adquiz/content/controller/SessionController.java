package com.adquiz.content.controller;

import com.adquiz.content.dto.*;
import com.adquiz.content.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @PostMapping
    public ResponseEntity<SessionResponse> createSession(
            @Valid @RequestBody CreateSessionRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sessionService.createSession(request, auth));
    }

    @GetMapping
    public ResponseEntity<List<SessionSummaryDto>> getSessionHistory(Authentication auth) {
        return ResponseEntity.ok(sessionService.getSessionHistory(auth));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionStateDto> getSessionState(@PathVariable UUID id,Authentication auth) {
        return ResponseEntity.ok(sessionService.getSessionState(id, auth));
    }

    @PostMapping("/{id}/answer")
    public ResponseEntity<AnswerResponse> submitAnswer(@PathVariable UUID id,
                                                       @Valid @RequestBody SubmitAnswerRequest request,
                                                       Authentication auth) {
        return ResponseEntity.ok(sessionService.submitAnswer(id, request, auth));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Void> abandonSession(@PathVariable UUID id, Authentication auth) {
        sessionService.abandonSession(id, auth);
        return ResponseEntity.noContent().build();
    }
}
