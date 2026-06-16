package com.adquiz.analytics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "daily_activity",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "activity_date"})
)
@Getter
@Setter
@NoArgsConstructor
public class DailyActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "activity_date", nullable = false)
    private LocalDate activityDate;

    @Column(name = "questions_answered", nullable = false)
    private int questionsAnswered;

    @Column(name = "correct_answers", nullable = false)
    private int correctAnswers;

    @Column(name = "sessions_completed", nullable = false)
    private int sessionsCompleted;
}
