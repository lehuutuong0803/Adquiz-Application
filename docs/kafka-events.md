# AdQuiz — Kafka Event Schema

---

## Overview

- **Kafka Topic:** `quiz-events`
- **Producer:** `content-service`
- **Consumer:** `analytics-service`
- All events share the same topic. The `event_type` field determines how the consumer handles each event.

---

## Event 1 — `ANSWER_SUBMITTED`

Published every time a student answers a question.

```json
{
  "eventId"          : "uuid",
  "eventType"        : "ANSWER_SUBMITTED",
  "userId"           : "uuid",
  "sessionId"        : "uuid",
  "questionId"       : "uuid",
  "topicId"          : "uuid",
  "bloomLevel"       : 3,
  "isCorrect"        : true,
  "confidenceRating" : 2,
  "answeredAt"       : "2026-06-07T10:30:00"
}
```

### Field Reference

| Field | Type | Description |
|---|---|---|
| `eventId` | UUID | Unique event identifier — prevents duplicate processing if Kafka delivers the event twice |
| `eventType` | String | Always `ANSWER_SUBMITTED` for this event |
| `userId` | UUID | Student identifier from Keycloak JWT (`sub` claim) |
| `sessionId` | UUID | Links to the active quiz session — also used as the Kafka message key |
| `questionId` | UUID | The question that was answered |
| `topicId` | UUID | Subtopic the question belongs to |
| `bloomLevel` | int (1-6) | Difficulty level of the question |
| `isCorrect` | Boolean | Whether the student answered correctly |
| `confidenceRating` | int (1-3) | 1=Low, 2=Medium, 3=High |
| `answeredAt` | LocalDateTime | Actual time the student answered — used for streak and daily activity |

**Note:** `remaining_questions` was dropped from the event — `content-service`'s own `KafkaConsumer` recomputes the student's remaining unseen questions itself (via `SessionQuestionRepository`) before deciding whether to top up the question bank. Field names are `camelCase` (Java record field names), not `snake_case`, since `analytics-service` is not yet implemented and no cross-service contract is fixed yet.

### Updates triggered in analytics-service

| Table | Action |
|---|---|
| `user_stats` | Increment `total_questions`, update `current_streak` and `last_active_date` |
| `topic_accuracy` | Increment `total_answered`, increment `total_correct` if correct, recompute `accuracy` |
| `daily_activity` | UPSERT — increment `questions_answered`, increment `correct_answers` if correct |

---

## Event 2 — `SESSION_COMPLETED`

Published when a student completes a quiz session.

```json
{
  "event_id"        : "uuid",
  "event_type"      : "SESSION_COMPLETED",
  "timestamp"       : "2026-06-07T10:45:00Z",
  "user_id"         : "uuid",
  "session_id"      : "uuid",
  "topic_id"        : "uuid",
  "total_questions" : 10,
  "correct_answers" : 7,
  "final_accuracy"  : 0.70,
  "completed_at"    : "2026-06-07T10:45:00Z"
}
```

### Field Reference

| Field | Type | Description |
|---|---|---|
| `event_id` | UUID | Unique event identifier — prevents duplicate processing |
| `event_type` | String | Always `SESSION_COMPLETED` for this event |
| `timestamp` | ISO 8601 | When the event was published to Kafka |
| `user_id` | UUID | Student identifier from Keycloak JWT |
| `session_id` | UUID | The completed session |
| `topic_id` | UUID | Subtopic the session was on |
| `total_questions` | SMALLINT | Total questions in the session |
| `correct_answers` | SMALLINT | Number of correct answers |
| `final_accuracy` | DECIMAL | Pre-computed: correct_answers / total_questions |
| `completed_at` | ISO 8601 | When the session was completed — used for daily activity date |

### Updates triggered in analytics-service

| Table | Action |
|---|---|
| `user_stats` | Increment `total_sessions` |
| `daily_activity` | UPSERT — increment `sessions_completed` |

---

## Key Design Decisions

| Decision | Reasoning |
|---|---|
| Single Kafka topic `quiz-events` | Keeps consumer logic in one place, `event_type` field routes handling |
| `event_id` on every event | Prevents duplicate processing — Kafka can deliver events more than once |
| `timestamp` vs `answered_at` / `completed_at` | `timestamp` = when published to Kafka, action time fields = when the action actually happened |
| `final_accuracy` pre-computed in event | Avoids division in analytics-service, keeps consumer logic simple |
| Separate `SESSION_COMPLETED` event | Cleaner than having analytics-service infer session completion from answer count |
