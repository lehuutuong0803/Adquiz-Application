package com.adquiz.content.repository;

import com.adquiz.content.entity.SessionQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface SessionQuestionRepository extends JpaRepository<SessionQuestion, UUID> {

    List<SessionQuestion> findBySessionIdOrderByQuestionIndex(UUID sessionId);

    Optional<SessionQuestion> findBySessionIdAndQuestionIndex(UUID sessionId, Short questionIndex);

    @Query("""
        SELECT sq.question.id FROM SessionQuestion sq
        WHERE sq.session.userId = :userId
        AND sq.session.topic.id = :topicId
        AND sq.question.bloomLevel = :bloomLevel
        """)
    Set<UUID> findAnswerdQuestionIds(
            @Param("userId") UUID userId,
            @Param("topicId") UUID topicId,
            @Param("bloomLevel") Short bloomLevel);
}
