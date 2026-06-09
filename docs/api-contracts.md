# AdQuiz — REST API Contracts

All requests (except auth) go through `api-gateway`.
JWT token required in `Authorization: Bearer <token>` header for all endpoints.

---

## content-service

Base URL: `/api`

### Topics

#### `GET /api/topics`
Returns all top-level topics with their subtopics.

**Response:**
```json
[
  {
    "id": "uuid",
    "name": "Data Structures",
    "subtopics": [
      { "id": "uuid", "name": "Arrays" },
      { "id": "uuid", "name": "Linked Lists" }
    ]
  }
]
```

---

#### `GET /api/topics/{id}/subtopics`
Returns all subtopics of a given topic.

**Response:**
```json
[
  { "id": "uuid", "name": "Arrays" },
  { "id": "uuid", "name": "Linked Lists" }
]
```

---

### Sessions

#### `POST /api/sessions`
Starts a new quiz session.

**Request:**
```json
{
  "topic_id"        : "uuid",
  "mode"            : "ADAPTIVE",
  "total_questions" : 10
}
```

**Response:**
```json
{
  "session_id"      : "uuid",
  "status"          : "IN_PROGRESS",
  "first_question"  : {
    "id"      : "uuid",
    "text"    : "What is the time complexity of merge sort?",
    "options" : [
      { "id": "a", "text": "O(n log n)" },
      { "id": "b", "text": "O(n²)" },
      { "id": "c", "text": "O(n)" },
      { "id": "d", "text": "O(log n)" }
    ],
    "bloom_level"       : 1,
    "question_index"    : 1,
    "total_questions"   : 10
  }
}
```

---

#### `GET /api/sessions`
Returns the student's session history.

**Response:**
```json
[
  {
    "session_id"      : "uuid",
    "topic_id"        : "uuid",
    "topic_name"      : "Arrays",
    "mode"            : "ADAPTIVE",
    "status"          : "COMPLETED",
    "total_questions" : 10,
    "started_at"      : "2026-06-07T10:00:00Z",
    "ended_at"        : "2026-06-07T10:15:00Z"
  }
]
```

---

#### `GET /api/sessions/{id}`
Returns the current state of a session.

**Response:**
```json
{
  "session_id"              : "uuid",
  "status"                  : "IN_PROGRESS",
  "current_question_index"  : 4,
  "total_questions"         : 10,
  "current_difficulty"      : 3
}
```

---

#### `POST /api/sessions/{id}/answer`
Submits a student's answer to the current question.

**Request:**
```json
{
  "question_id"       : "uuid",
  "selected_option"   : "a",
  "confidence_rating" : 2
}
```

**Response:**
```json
{
  "is_correct"      : true,
  "correct_answer"  : "a",
  "explanation"     : "Merge sort divides the array recursively...",
  "next_question"   : {
    "id"            : "uuid",
    "text"          : "Which data structure uses LIFO ordering?",
    "options"       : [
      { "id": "a", "text": "Queue" },
      { "id": "b", "text": "Stack" },
      { "id": "c", "text": "Heap" },
      { "id": "d", "text": "Tree" }
    ],
    "bloom_level"       : 2,
    "question_index"    : 5,
    "total_questions"   : 10
  },
  "session_status"  : "IN_PROGRESS"
}
```

**Notes:**
- `next_question` is `null` when `session_status = COMPLETED`
- Side effects (non-blocking): publishes `ANSWER_SUBMITTED` to Kafka
- Side effects (non-blocking): content-service consumes its own event, triggers AI generation if remaining questions < 3

---

#### `POST /api/sessions/{id}/complete`
Ends a session early (student exits before finishing).

**Response:**
```json
{
  "session_id"  : "uuid",
  "status"      : "ABANDONED"
}
```

**Notes:**
- Side effects: publishes `SESSION_COMPLETED` to Kafka with final stats

---

### Spaced Repetition

#### `GET /api/reviews/due`
Returns subtopics due for review today.

**Response:**
```json
[
  {
    "topic_id"          : "uuid",
    "topic_name"        : "Arrays",
    "parent_topic_name" : "Data Structures",
    "last_review_date"  : "2026-06-04",
    "interval_days"     : 3
  }
]
```

---

#### `POST /api/reviews/{topicId}/rate`
Submits the student's recall rating after a review session.

**Request:**
```json
{
  "rating": "GOT_IT"
}
```
**Rating values:** `FORGOT` | `HARD` | `GOT_IT` | `EASY`

**Response:**
```json
{
  "topic_id"          : "uuid",
  "next_review_date"  : "2026-06-14",
  "interval_days"     : 7
}
```

---

## analytics-service

Base URL: `/api/analytics`

**Note:** Each endpoint is called independently by the frontend so dashboard widgets load in parallel.

---

#### `GET /api/analytics/streak`
Returns the student's streak information.

**Response:**
```json
{
  "current_streak"    : 7,
  "longest_streak"    : 15,
  "last_active_date"  : "2026-06-07"
}
```

---

#### `GET /api/analytics/accuracy`
Returns accuracy per subtopic.

**Response:**
```json
[
  {
    "topic_id"          : "uuid",
    "topic_name"        : "Arrays",
    "parent_topic_name" : "Data Structures",
    "total_answered"    : 45,
    "total_correct"     : 38,
    "accuracy"          : 0.84
  }
]
```

---

#### `GET /api/analytics/weak-areas`
Returns the student's 5 weakest subtopics by accuracy.

**Response:**
```json
[
  {
    "topic_id"          : "uuid",
    "topic_name"        : "Binary Trees",
    "parent_topic_name" : "Data Structures",
    "accuracy"          : 0.42,
    "total_answered"    : 20
  }
]
```

---

#### `GET /api/analytics/activity`
Returns daily activity for the streak heatmap.

**Response:**
```json
[
  {
    "date"                : "2026-06-07",
    "questions_answered"  : 25,
    "correct_answers"     : 20,
    "sessions_completed"  : 2
  }
]
```

---

## ai-generation-service

Base URL: `/api/generate`

**Note:** Internal service only — not exposed through api-gateway. Only callable by content-service.

---

#### `POST /api/generate/questions`
Generates new questions for a given subtopic and difficulty level.

**Request:**
```json
{
  "topic_id"          : "uuid",
  "topic_name"        : "Arrays",
  "parent_topic_name" : "Data Structures",
  "bloom_level"       : 3,
  "count"             : 5,
  "target_audience"   : "UNIVERSITY_STUDENT",
  "existing_questions": ["What is...", "Which of the following..."]
}
```

**Response:**
```json
[
  {
    "question_text"   : "Given an array of n elements, what is...",
    "options"         : [
      { "id": "a", "text": "O(n)" },
      { "id": "b", "text": "O(n²)" },
      { "id": "c", "text": "O(log n)" },
      { "id": "d", "text": "O(1)" }
    ],
    "correct_answer"  : "a",
    "explanation"     : "Because we iterate through...",
    "bloom_level"     : 3
  }
]
```

---

## Key Design Decisions

| Decision | Reasoning |
|---|---|
| Dashboard split into separate endpoints | Each widget loads independently and in parallel — graceful degradation if one fails |
| `next_question` returned with answer response | Avoids extra round trip — frontend gets result and next question in one call (response piggybacking) |
| AI generation triggered via Kafka self-consumption | Keeps answer endpoint clean and focused — prefetch logic separated from answer processing |
| `ai-generation-service` not exposed through gateway | Internal implementation detail — React should never call it directly |
| Remaining questions threshold = 3 | Generate early so there is always a buffer — never let the student wait for AI generation |
