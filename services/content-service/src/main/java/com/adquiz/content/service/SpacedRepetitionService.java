package com.adquiz.content.service;

import com.adquiz.content.dto.DueReviewDto;
import com.adquiz.content.entity.SpacedRepetitionRecord;
import com.adquiz.content.entity.Topic;
import com.adquiz.content.mapper.SpacedRepetitionMapper;
import com.adquiz.content.repository.SpacedRepetitionRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpacedRepetitionService {

    private static final long MAX_INTERVAL_DAYS = 180;

    private final SpacedRepetitionRecordRepository spacedRepetitionRecordRepository;
    private final SpacedRepetitionMapper spacedRepetitionMapper;

    @Transactional(readOnly = true)
    public List<DueReviewDto> getDueReviews(UUID userId) {
        return spacedRepetitionRecordRepository
                .findDueForReview(userId, LocalDate.now())
                .stream()
                .map(spacedRepetitionMapper::toDueReviewDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void recordSessionCompletion(UUID userId, Topic topic, double finalAccuracy) {
        SpacedRepetitionRecord record = spacedRepetitionRecordRepository
                .findByUserIdAndTopicId(userId, topic.getId())
                .orElseGet(() -> {
                    SpacedRepetitionRecord newRecord = new SpacedRepetitionRecord();
                    newRecord.setUserId(userId);
                    newRecord.setTopic(topic);
                    newRecord.setEaseFactor(BigDecimal.valueOf(2.5));
                    newRecord.setRepetitions((short) 0);
                    newRecord.setIntervalDays((short) 1);
                    newRecord.setCreatedAt(LocalDateTime.now());
                    return newRecord;
                });
        int quality = mapAccuracyToQuality(finalAccuracy);
        applySm2(record, quality);
        spacedRepetitionRecordRepository.save(record);
    }

    private int mapAccuracyToQuality(double accuracy) {
        if (accuracy >= 0.9) return 5;
        if (accuracy >= 0.7) return 4;
        if (accuracy >= 0.4) return 3;
        return 0;
    }

    private void applySm2(SpacedRepetitionRecord record, int quality) {
        if (quality < 3) {
            record.setRepetitions((short) 0);
            record.setIntervalDays((short) 1);
        } else {
            short repetition = (short) (record.getRepetitions() + 1);
            record.setRepetitions(repetition);

            short interval;
            if (repetition == 1) {
                interval = 1;
            } else if (repetition == 2) {
                interval = 6;
            } else {
                long rawInterval = Math.round(
                        record.getIntervalDays() * record.getEaseFactor().doubleValue());
                interval = (short) Math.min(rawInterval, MAX_INTERVAL_DAYS);
            }
            record.setIntervalDays(interval);
        }

        double ef = record.getEaseFactor().doubleValue();
        double newEf= ef + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
        if (newEf < 1.3) {
            newEf = 1.3;
        }

        record.setEaseFactor(BigDecimal.valueOf(newEf).setScale(2, RoundingMode.HALF_UP));
        LocalDate today = LocalDate.now();
        record.setLastReviewDate(today);
        record.setNextReviewDate(today.plusDays(record.getIntervalDays()));
    }
}
