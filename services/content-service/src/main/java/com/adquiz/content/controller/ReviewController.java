package com.adquiz.content.controller;

import com.adquiz.content.dto.DueReviewDto;
import com.adquiz.content.service.SpacedRepetitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final SpacedRepetitionService spacedRepetitionService;

    @GetMapping("/due")
    public ResponseEntity<List<DueReviewDto>> getDueReviews(Authentication auth) {
        return ResponseEntity.ok(spacedRepetitionService.getDueReviews(extractUserId(auth)));
    }

    private UUID extractUserId(Authentication auth) {
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) auth;
        Jwt jwt = (Jwt) jwtAuth.getCredentials();
        return UUID.fromString(jwt.getSubject());
    }
}
