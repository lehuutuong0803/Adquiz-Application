# AdQuiz — Data Model

---

## content-service (PostgreSQL)

### `topics`
```
id          | UUID, primary key
name        | VARCHAR, not null
parent_id   | UUID, nullable, references topics(id)
created_at  | TIMESTAMP
```
**Notes:**
- `parent_id = NULL` → top-level topic (e.g. "Data Structures")
- `parent_id = <id>` → subtopic (e.g. "Arrays")
- All foreign keys in other tables point to subtopics only
- Parent topic is always derivable via join on parent_id

---

### `questions`
```
id              | UUID, primary key
topic_id        | UUID, references topics(id)  ← always a subtopic
bloom_level     | SMALLINT (1-6, Bloom's Taxonomy)
question_text   | TEXT, not null
options         | JSONB  (display only, no correct flag)
correct_answer  | VARCHAR  (option id, server-side only, never sent to frontend)
explanation     | TEXT  (shown after student answers)
created_by      | VARCHAR  ('AI' or 'MANUAL')
created_at      | TIMESTAMP
```
**Options JSONB format:**
```json
[
  {"id": "a", "text": "O(n log n)"},
  {"id": "b", "text": "O(n²)"},
  {"id": "c", "text": "O(n)"},
  {"id": "d", "text": "O(log n)"}
]
```
**Notes:**
- `correct_answer` stores the option id (e.g. "a") — never included in API responses to frontend
- `bloom_level` maps to Bloom's Taxonomy: 1=Remember, 2=Understand, 3=Apply, 4=Analyze, 5=Evaluate, 6=Create
- `created_by` enables filtering by source and supports future manual question input feature

---

### `quiz_sessions`
```
id                      | UUID, primary key
user_id                 | UUID  (from Keycloak JWT)
topic_id                | UUID, references topics(id)  ← always a subtopic
mode                    | VARCHAR ('ADAPTIVE', 'RANDOM', 'WEAK_AREAS')
status                  | VARCHAR ('IN_PROGRESS', 'COMPLETED', 'ABANDONED')
total_questions         | SMALLINT
current_question_index  | SMALLINT (starts at 1)
current_difficulty      | SMALLINT (1-6, current Bloom level)
started_at              | TIMESTAMP
ended_at                | TIMESTAMP, nullable
```
**Notes:**
- `current_difficulty` stored here so adaptive algorithm does not need to recalculate from session history on every question
- `status = ABANDONED` when student closes app mid-session

---

### `session_questions`
```
id                | UUID, primary key
session_id        | UUID, references quiz_sessions(id)
question_id       | UUID, references questions(id)
question_index    | SMALLINT (position in session, 1..n)
selected_option   | VARCHAR (option id the student picked)
is_correct        | BOOLEAN
confidence_rating | SMALLINT (1=Low, 2=Medium, 3=High)
time_spent        | SMALLINT (seconds — collected but not used in algorithm yet)
answered_at       | TIMESTAMP
```
**Notes:**
- `time_spent` is collected now for potential future use in adaptive algorithm (quick correct = truly knows it, slow correct = uncertain)
- `confidence_rating` combined with `is_correct` drives the adaptive difficulty adjustment

---

### `spaced_repetition_records`
```
id                | UUID, primary key
user_id           | UUID (from Keycloak JWT)
topic_id          | UUID, references topics(id)  ← always a subtopic
ease_factor       | DECIMAL (SM-2 value, starts at 2.5)
interval_days     | SMALLINT (days until next review, grows exponentially)
repetitions       | SMALLINT (number of successful reviews)
next_review_date  | DATE
last_review_date  | DATE
created_at        | TIMESTAMP
```
**Notes:**
- One record per user per subtopic
- `ease_factor` decreases when student struggles, increases when student finds it easy
- Frontend displays "Arrays (Data Structures)" by joining topic to its parent — no extra column needed
- SM-2 quality scores map to student ratings: Forgot=0, Hard=2, Got it=4, Easy=5

---

## analytics-service (PostgreSQL)

### `user_stats`
```
id                  | UUID, primary key
user_id             | UUID (from Keycloak JWT), unique
current_streak      | SMALLINT (pre-computed, updated on every event)
longest_streak      | SMALLINT (pre-computed, updated when current > longest)
last_active_date    | DATE (used to skip redundant streak recalculation)
total_sessions      | INTEGER
total_questions     | INTEGER
created_at          | TIMESTAMP
updated_at          | TIMESTAMP
```
**Notes:**
- One record per user — `user_id` is unique
- Hybrid approach: stats are pre-computed on every event, but `last_active_date` check skips redundant streak recalculation if the event's date equals today
- `longest_streak` updated only when `current_streak` exceeds it

---

### `topic_accuracy`
```
id              | UUID, primary key
user_id         | UUID
topic_id        | UUID (subtopic reference, no FK — analytics owns its own data)
total_answered  | INTEGER
total_correct   | INTEGER
accuracy        | DECIMAL (pre-computed: total_correct / total_answered)
updated_at      | TIMESTAMP
```
**Notes:**
- Pre-computed `accuracy` enables fast sorting for weak area detection
- Weak areas query: `SELECT * FROM topic_accuracy WHERE user_id = ? ORDER BY accuracy ASC LIMIT 5`
- No foreign key to topics table — analytics-service does not share databases with content-service

---

### `daily_activity`
```
id                  | UUID, primary key
user_id             | UUID
activity_date       | DATE
questions_answered  | SMALLINT
correct_answers     | SMALLINT
sessions_completed  | SMALLINT
created_at          | TIMESTAMP
```
**Notes:**
- Unique constraint on `(user_id, activity_date)` — one record per student per day
- UPSERT pattern: new event for same day updates existing record instead of inserting a new one
- Powers the streak heatmap on the progress screen

---

## Key Design Decisions

| Decision | Reasoning |
|---|---|
| Options stored as JSONB | Questions and options always fetched together — locality of reference |
| `correct_answer` separate column | Never accidentally leaked to frontend, easier to query than JSONB |
| `parent_id` self-reference on topics | One table handles both topics and subtopics, parent always derivable |
| All FKs point to subtopics | Subtopics give precision, parent topic always reachable via join |
| `current_difficulty` on session | Avoids recalculating from session history on every question |
| `time_spent` collected unused | Cheap to collect now, expensive to backfill later |
