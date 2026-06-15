package com.adquiz.content.repository;

import com.adquiz.content.entity.SpacedRepetitionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpacedRepetitionRecordRepository extends JpaRepository<SpacedRepetitionRecord, UUID> {

    Optional<SpacedRepetitionRecord> findByUserIdAndTopicId(UUID userId, UUID topicId);

    @Query("""
            SELECT r FROM SpacedRepetitionRecord r
            JOIN FETCH r.topic t
            LEFT JOIN FETCH t.parent
            WHERE r.userId = :userId AND r.nextReviewDate <= :date
            """)
    List<SpacedRepetitionRecord> findDueForReview(@Param("userId") UUID userId, @Param("date") LocalDate date);
}
