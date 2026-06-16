package com.adquiz.analytics.controller;

import com.adquiz.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/streak")
    public ResponseEntity<?> getStreak(Authentication auth) {
        return ResponseEntity.ok(analyticsService.getStreak(extractUserId(auth)));
    }

    @GetMapping("/accuracy")
    public ResponseEntity<?> getAccuracy(Authentication auth) {
        return ResponseEntity.ok(analyticsService.getAccuracy(extractUserId(auth)));
    }

    @GetMapping("/weak-areas")
    public ResponseEntity<?> getWeakAreas(Authentication auth) {
        return ResponseEntity.ok(analyticsService.getWeakAreas(extractUserId(auth)));
    }

    @GetMapping("/activity")
    public ResponseEntity<?> getActivity(Authentication auth) {
        return ResponseEntity.ok(analyticsService.getActivity(extractUserId(auth)));
    }

    private UUID extractUserId(Authentication auth) {
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) auth;
        Jwt jwt = (Jwt) jwtAuth.getCredentials();
        return UUID.fromString(jwt.getSubject());
    }
}
