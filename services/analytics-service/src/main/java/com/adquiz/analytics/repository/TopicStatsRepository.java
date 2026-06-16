package com.adquiz.analytics.repository;

import com.adquiz.analytics.entity.TopicStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TopicStatsRepository extends JpaRepository<TopicStats, UUID> {

    Optional<TopicStats> findByUserIdAndTopicId(UUID userId, UUID topicId);

    List<TopicStats> findByUserId(UUID userId);
}
