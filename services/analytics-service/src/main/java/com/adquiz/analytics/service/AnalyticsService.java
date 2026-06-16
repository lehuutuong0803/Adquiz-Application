package com.adquiz.analytics.service;

import com.adquiz.analytics.dto.DailyActivityDto;
import com.adquiz.analytics.dto.StreakDto;
import com.adquiz.analytics.dto.TopicAccuracyDto;
import com.adquiz.analytics.dto.WeakAreaDto;
import com.adquiz.analytics.entity.DailyActivity;
import com.adquiz.analytics.entity.TopicStats;
import com.adquiz.analytics.entity.UserStreak;
import com.adquiz.analytics.kafka.AnswerSubmittedEvent;
import com.adquiz.analytics.repository.DailyActivityRepository;
import com.adquiz.analytics.repository.TopicStatsRepository;
import com.adquiz.analytics.repository.UserStreakRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final UserStreakRepository userStreakRepository;
    private final DailyActivityRepository dailyActivityRepository;
    private final TopicStatsRepository topicStatsRepository;

    @Transactional
    public void handleAnswerSubmitted(AnswerSubmittedEvent event) {
        LocalDate eventDate = event.answeredAt().toLocalDate();
        updateStreak(event.userId(), eventDate);
        updateTopicStats(event.userId(), event);
        updateDailyActivityForAnswer(event.userId(), eventDate, event.isCorrect());
    }

    @Transactional
    public void handleSessionCompleted(UUID userId, LocalDate eventDate) {
        updateDailyActivityForSession(userId, eventDate);
    }

    @Transactional(readOnly = true)
    public StreakDto getStreak(UUID userId) {
        return userStreakRepository.findById(userId)
                .map(s -> new StreakDto(s.getCurrentStreak(),s.getLongestStreak(),s.getLastActiveDate()))
                .orElse(new StreakDto(0,0, null));
    }

    @Transactional(readOnly = true)
    public List<TopicAccuracyDto> getAccuracy(UUID userId) {
        return topicStatsRepository.findByUserId(userId).stream()
                .map(s -> new TopicAccuracyDto(
                        s.getTopicId(),
                        s.getTopicName(),
                        s.getParentTopicName(),
                        s.getTotalAnswered(),
                        s.getTotalCorrect(),
                        s.getTotalAnswered() == 0 ? 0.0 : (double) s.getTotalCorrect()/s.getTotalAnswered()
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WeakAreaDto> getWeakAreas(UUID userId) {
        return topicStatsRepository.findByUserId(userId).stream()
                .filter(s -> s.getTotalAnswered() >= 5)
                .map(s -> new WeakAreaDto(
                        s.getTopicId(),
                        s.getTopicName(),
                        s.getParentTopicName(),
                        s.getTotalAnswered(),
                        (double) s.getTotalCorrect()/s.getTotalAnswered()
                ))
                .sorted(Comparator.comparingDouble(WeakAreaDto::accuracy))
                .limit(5)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DailyActivityDto> getActivity(UUID userId) {
        return dailyActivityRepository.findByUserIdOrderByActivityDateDesc(userId).stream()
                .map(a -> new DailyActivityDto(
                        a.getActivityDate(),
                        a.getQuestionsAnswered(),
                        a.getCorrectAnswers(),
                        a.getSessionsCompleted()
                ))
                .collect(Collectors.toList());
    }

    private void updateStreak(UUID userId, LocalDate eventDate) {
        UserStreak streak = userStreakRepository.findById(userId)
                .orElseGet(() -> {
                    UserStreak s = new UserStreak();
                    s.setUserId(userId);
                    s.setCurrentStreak(0);
                    s.setLongestStreak(0);
                    return s;
                });

        LocalDate lastActive = streak.getLastActiveDate();

        if (eventDate.equals(lastActive)) {
            return;
        }

        if (lastActive == null || lastActive.isBefore(eventDate.minusDays(1))) {
            streak.setCurrentStreak(1);
        } else if (lastActive.equals(eventDate.minusDays(1))) {
            streak.setCurrentStreak(streak.getCurrentStreak() + 1);
        }

        streak.setLastActiveDate(eventDate);
        streak.setLongestStreak(Math.max(streak.getLongestStreak(), streak.getCurrentStreak()));
        streak.setUpdatedAt(LocalDateTime.now());
        userStreakRepository.save(streak);
    }

    private void updateTopicStats(UUID userId, AnswerSubmittedEvent event) {
        TopicStats stats = topicStatsRepository
                .findByUserIdAndTopicId(userId, event.topicId())
                .orElseGet(() -> {
                    TopicStats s = new TopicStats();
                    s.setUserId(userId);
                    s.setTopicId(event.topicId());
                    s.setTopicName(event.topicName());
                    s.setParentTopicName(event.parentTopicName());
                    s.setTotalAnswered(0);
                    s.setTotalCorrect(0);
                    return s;
                });

        stats.setTotalAnswered(stats.getTotalAnswered() + 1);
        if (event.isCorrect()) {
            stats.setTotalCorrect(stats.getTotalCorrect() + 1);
        }

        stats.setUpdatedAt(LocalDateTime.now());
        topicStatsRepository.save(stats);
    }

    private void updateDailyActivityForAnswer(UUID userId, LocalDate date, boolean isCorrect) {
        DailyActivity dailyActivity = dailyActivityRepository
                .findByUserIdAndActivityDate(userId, date)
                .orElseGet(() -> {
                    DailyActivity a = new DailyActivity();
                    a.setUserId(userId);
                    a.setActivityDate(date);
                    return a;
                });

        dailyActivity.setQuestionsAnswered(dailyActivity.getQuestionsAnswered() + 1);
        if (isCorrect) {
            dailyActivity.setCorrectAnswers(dailyActivity.getCorrectAnswers() +1);
        }

        dailyActivityRepository.save(dailyActivity);
    }

    private void updateDailyActivityForSession(UUID userId, LocalDate date) {
        DailyActivity dailyActivity = dailyActivityRepository
                .findByUserIdAndActivityDate(userId, date)
                .orElseGet(() -> {
                    DailyActivity d = new DailyActivity();
                    d.setUserId(userId);
                    d.setActivityDate(date);
                    return d;
                });

        dailyActivity.setSessionsCompleted(dailyActivity.getSessionsCompleted() + 1);
        dailyActivityRepository.save(dailyActivity);
    }
}
