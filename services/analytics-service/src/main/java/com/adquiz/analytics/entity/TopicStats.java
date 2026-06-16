package com.adquiz.analytics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "topic_stats",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "topic_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class TopicStats {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "topic_id", nullable = false)
    private UUID topicId;

    @Column(name = "topic_name", nullable = false)
    private String topicName;

    @Column(name = "parent_topic_name")
    private String parentTopicName;

    @Column(name = "total_answered", nullable = false)
    private int totalAnswered;

    @Column(name = "total_correct", nullable = false)
    private int totalCorrect;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
