package com.adquiz.content.repository;

import com.adquiz.content.entity.QuizSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuizSessionRepository extends JpaRepository<QuizSession, UUID> {

    List<QuizSession> findByUserIdOrderByStartedAtDesc(UUID userId);

    Optional<QuizSession> findByIdAndUserId(UUID id, UUID userId);
}
