package com.adquiz.analytics.repository;

import com.adquiz.analytics.entity.DailyActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DailyActivityRepository extends JpaRepository<DailyActivity, UUID> {

    Optional<DailyActivity> findByUserIdAndActivityDate(UUID userId, LocalDate date);

    List<DailyActivity> findByUserIdOrderByActivityDateDesc(UUID userId);
}
