package com.adquiz.content.repository;

import com.adquiz.content.entity.SpacedRepetitionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpacedRepetitionRecordRepository extends JpaRepository<SpacedRepetitionRecord, UUID> {

    List<SpacedRepetitionRecord> findByUserIdAndNextReviewDateLessThanEqual (UUID userId, LocalDate date);

    Optional<SpacedRepetitionRecord> findByUserIdAndTopicId(UUID userId, UUID topicId);
}
