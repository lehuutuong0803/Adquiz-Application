package com.adquiz.content.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "session_questions")
@Getter
@Setter
@NoArgsConstructor
public class SessionQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private QuizSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "question_index", nullable = false)
    private Short questionIndex;

    @Column(name = "selected_option")
    private String selectedOption;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "confidence_rating")
    private Short confidenceRating;

    @Column(name = "time_spent")
    private Short timeSpent;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;
}
