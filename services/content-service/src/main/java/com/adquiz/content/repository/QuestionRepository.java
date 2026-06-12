package com.adquiz.content.repository;

import com.adquiz.content.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {

    List<Question> findByTopicIdAndBloomLevel(UUID topicId, Short bloomLevel);

    Long countByTopicIdAndBloomLevel(UUID topicId, Short bloomLevel);

    Long countByTopicId(UUID topicId);

    @Query(
            """
            SELECT q FROM Question q
            WHERE q.topic.id = :topicId
            AND q.bloomLevel = :bloomLevel
            AND q.id NOT IN :excludedIds
            ORDER BY FUNCTION('RANDOM')
            LIMIT 1        
            """
    )
    Optional<Question> findFirstUnansweredQuestion(
            @Param("topicId") UUID topicId,
            @Param("bloomLevel") Short bloomLevel,
            @Param("excludedIds") Set<UUID> excludedIds
            );

}
