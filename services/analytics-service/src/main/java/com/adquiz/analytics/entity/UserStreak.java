package com.adquiz.analytics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_streaks")
@Getter
@Setter
@NoArgsConstructor
public class UserStreak {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "current_streak", nullable = false)
    private int currentStreak;

    @Column(name = "longest_streak", nullable = false)
    private int longestStreak;

    @Column(name = "last_active_date")
    private LocalDate lastActiveDate;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
