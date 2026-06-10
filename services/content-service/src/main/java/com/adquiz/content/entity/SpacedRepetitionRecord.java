package com.adquiz.content.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "spaced_repetition_records",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "topic_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class SpacedRepetitionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(name = "ease_factor", nullable = false)
    private BigDecimal easeFactor;

    @Column(name = "interval_days", nullable = false)
    private Short intervalDays;

    @Column(nullable = false)
    private Short repetitions;

    @Column(name = "next_review_date", nullable = false)
    private LocalDate nextReviewDate;

    @Column(name = "last_review_date", nullable = false)
    private LocalDate lastReviewDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
