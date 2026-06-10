package com.adquiz.content.repository;

import com.adquiz.content.entity.SessionQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionQuestionRepository extends JpaRepository<SessionQuestion, UUID> {

    List<SessionQuestion> findBySessionIdOrderByQuestionIndex(UUID sessionId);

    Optional<SessionQuestion> findBySessionIdAndQuestionIndex(UUID sessionId, Short questionIndex);
}
