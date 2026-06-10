package com.adquiz.content.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "quiz_sessions")
@Getter
@Setter
@NoArgsConstructor
public class QuizSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(nullable = false)
    private String mode;

    @Column(nullable = false)
    private String status;

    @Column(name = "total_questions", nullable = false)
    private Short totalQuestions;

    @Column(name = "current_question_index", nullable = false)
    private Short currentQuestionIndex;

    @Column(name = "current_difficulty", nullable = false)
    private Short currentDifficulty;

    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;
}
