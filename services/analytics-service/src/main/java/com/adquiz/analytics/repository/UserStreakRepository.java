package com.adquiz.analytics.repository;

import com.adquiz.analytics.entity.UserStreak;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserStreakRepository extends JpaRepository<UserStreak, UUID> {
}
