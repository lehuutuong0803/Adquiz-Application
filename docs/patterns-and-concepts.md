# AdQuiz — Patterns & Concepts Glossary

Every architectural pattern, design pattern, and technical concept applied in this project.
Use this document for revision and to understand the reasoning behind each decision.

---

## Architecture Patterns

### Microservices Architecture
**What it is:** An architectural style where an application is built as a collection of small, independent services, each running in its own process and communicating via APIs or events.

**Where we use it:** The entire AdQuiz system is split into 5 independent services: api-gateway, auth-service, content-service, ai-generation-service, analytics-service.

**Why we use it:** Each service can be developed, deployed, and scaled independently. A failure in one service does not bring down the entire system.

**Trade-off:** More complex setup and infrastructure compared to a monolith.

---

### API Gateway Pattern
**What it is:** A single entry point for all client requests. The gateway handles cross-cutting concerns like authentication, routing, and rate limiting before forwarding requests to the appropriate service.

**Where we use it:** `api-gateway` (Spring Cloud Gateway) sits between React and all backend services.

**Why we use it:** Avoids duplicating JWT validation and routing logic in every service. One place to change security policy.

---

### Event-Driven Architecture
**What it is:** Services communicate by publishing and consuming events through a message broker (Kafka) rather than calling each other directly.

**Where we use it:** `content-service` publishes `ANSWER_SUBMITTED` and `SESSION_COMPLETED` events. `analytics-service` consumes them independently.

**Why we use it:** Analytics is never in the critical path of a quiz session. The student doesn't wait for stats to be recorded. Services are fully decoupled.

---

### Database per Service Pattern
**What it is:** Each microservice owns its own database. No two services share a database.

**Where we use it:** `content-service` has its own PostgreSQL instance. `analytics-service` has its own PostgreSQL instance.

**Why we use it:** True service isolation. A schema change in one service cannot break another. Each service can choose the best database for its needs.

---

### Self-Consumption Pattern (Kafka)
**What it is:** A service publishes an event to Kafka and also consumes that same event itself to trigger secondary logic.

**Where we use it:** `content-service` publishes `ANSWER_SUBMITTED` and consumes it to check if the question bank is running low, then triggers AI generation if needed.

**Why we use it:** Keeps the answer endpoint clean and focused. Prefetch logic is separated from answer processing, independently testable.

---

## Design Patterns

### Single Responsibility Principle (SRP)
**What it is:** Every module, class, or service should have one reason to change.

**Where we use it:** Each service has a single responsibility — content-service owns the learning loop, analytics-service owns stats, ai-generation-service owns question generation.

**Why we use it:** When a service has one responsibility, changes are isolated and don't ripple across the system.

---

### Locality of Reference
**What it is:** Data that is always accessed together should be stored together.

**Where we use it:** Question options are stored as JSONB inside the `questions` table rather than a separate `question_options` table.

**Why we use it:** Questions and their options are always fetched together — one query instead of two.

---

### Materialized Stats (Pre-computation)
**What it is:** Storing computed values (like counts, averages, streaks) directly in the database rather than recalculating them on every read.

**Where we use it:** `user_stats.current_streak`, `user_stats.longest_streak`, `topic_accuracy.accuracy` are all pre-computed and updated on every Kafka event.

**Why we use it:** Dashboard reads are always fast regardless of how much historical data exists. Read performance is prioritized over write complexity.

---

### Lazy Computation with Cache Invalidation
**What it is:** Only recompute a value when the cached version is potentially stale, using a condition to skip unnecessary work.

**Where we use it:** Streak recalculation is skipped if `last_active_date` already equals today — no need to recompute what hasn't changed.

**Why we use it:** Reduces redundant computation on the write side while keeping pre-computed values accurate.

---

### Response Piggybacking
**What it is:** Bundling data the client will immediately need into the current response, avoiding an extra round trip.

**Where we use it:** `POST /api/sessions/{id}/answer` returns both the answer result AND the next question in a single response.

**Why we use it:** The frontend always needs the next question immediately after submitting an answer — one call instead of two reduces latency.

---

### Background Prefetching
**What it is:** Proactively loading or generating data before it is explicitly requested, so it is ready when needed.

**Where we use it:** When remaining questions for a topic + bloom level drop below 3, `content-service` triggers `ai-generation-service` in the background to generate new questions.

**Why we use it:** The student never waits for AI generation. Questions are always available when the next one is needed.

---

### UPSERT Pattern
**What it is:** A database operation that inserts a new record if it doesn't exist, or updates the existing one if it does.

**Where we use it:** `daily_activity` table — one record per user per day. New events for the same day update the existing record instead of creating duplicates.

**Why we use it:** Ensures clean, non-duplicated daily records without needing to check existence before every write.

---

### Self-Referencing Foreign Key
**What it is:** A table that has a foreign key pointing to itself, used to represent parent-child or hierarchical relationships within a single table.

**Where we use it:** `topics.parent_id` references `topics.id` — topics and subtopics live in the same table.

**Why we use it:** One table handles both levels of the hierarchy. Parent topic is always derivable via a join. No need for a separate subtopics table.

---

## Algorithms

### SM-2 (Spaced Repetition Algorithm)
**What it is:** A memory scheduling algorithm that calculates the optimal time to review a piece of information — just before you're about to forget it. Intervals grow exponentially as knowledge becomes solid.

**Where we use it:** `spaced_repetition_records` — calculates `next_review_date` and `interval_days` based on student recall ratings.

**Key values:**
- `ease_factor` starts at 2.5, adjusted based on performance
- Ratings map to quality scores: Forgot=0, Hard=2, Got it=4, Easy=5
- Interval grows when quality ≥ 3, resets when quality < 3

**Why we use it:** Battle-tested, research-backed algorithm. Same algorithm used by Anki. No need to invent a custom solution.

---

### Adaptive Difficulty (Bloom's Taxonomy)
**What it is:** Dynamically adjusting question difficulty based on student performance in real time.

**Where we use it:** `content-service` adaptive algorithm updates `current_difficulty` in `quiz_sessions` after each answer.

**Rules:**
- 2 consecutive correct answers → difficulty goes up 1 level
- Wrong answer → difficulty drops 1 level
- High confidence + 2 correct → difficulty jumps 2 levels
- Difficulty scale: Bloom's Taxonomy levels 1-6 (Remember → Understand → Apply → Analyze → Evaluate → Create)

**Why we use it:** Keeps the student in the optimal learning zone — challenged but not overwhelmed. Backed by educational psychology research.

---

## Security Concepts

### JWT (JSON Web Token)
**What it is:** A compact, self-contained token that carries user identity and claims. Signed by the auth server, verified by services without calling the auth server on every request.

**Where we use it:** Keycloak issues JWT tokens after login. React attaches the token to every API request. API Gateway validates the token on every request.

**Why we use it:** Stateless authentication — services don't need to store session state. Scales well across microservices.

---

### Identity Provider (IdP)
**What it is:** A dedicated service responsible for managing user identities, authentication, and authorization.

**Where we use it:** Keycloak acts as the IdP for AdQuiz. React talks to Keycloak directly for login and registration.

**Why we use it:** Centralizes all auth logic. Business services never handle passwords. Industry-standard approach.

---

## Performance Concepts

### Asynchronous Processing
**What it is:** Operations that run independently without blocking the main execution flow.

**Where we use it:** Kafka event publishing after answer submission — content-service publishes the event and returns the response immediately without waiting for analytics to process it.

**Why we use it:** The student's response time is not affected by background operations.

---

### Graceful Degradation
**What it is:** A system's ability to continue functioning at a reduced level when part of it fails.

**Where we use it:** Dashboard widgets each call separate endpoints. If one endpoint fails (e.g., streak), the other widgets (accuracy, activity) still load.

**Why we use it:** Partial failure should never break the entire user experience.

---

## Spring Concepts

### Auto-configuration
**What it is:** Spring Boot automatically configures beans based on what dependencies are on the classpath and what properties are defined in `application.yaml`. You rarely need to configure things manually.

**Where we use it:** `ChatClient.Builder` is auto-configured by Spring AI using `api-key`, `model`, and `temperature` from `application.yaml`. We just call `.build()`.

**Why we use it:** Reduces boilerplate configuration code. Convention over configuration — sensible defaults are applied automatically.

---

### Dependency Injection (DI)
**What it is:** Instead of creating objects manually with `new`, Spring creates and manages them as beans and injects them where needed.

**Where we use it:** `ChatClient` is defined as a `@Bean` in `OpenAiConfig` and injected into `QuestionGenerationService` automatically.

**Why we use it:** Loose coupling — classes don't create their own dependencies, making them easier to test and swap out.

---

## Integration Patterns

### Anti-Corruption Layer (ACL)
**What it is:** A layer that translates between your internal model and an external system's model, protecting your system from being affected by external API changes.

**Where we use it:** `ai-generation-service` DTO layer — OpenAI responses are mapped to our own `GeneratedQuestionDto` before being returned to `content-service`. If OpenAI changes their response format, only the mapping layer changes.

**Why we use it:** Decouples your system from external dependencies. Switching from OpenAI to another LLM provider requires changing only the mapping layer — nothing in `content-service` changes.

---

## Data Concepts

### JSONB (PostgreSQL)
**What it is:** A binary JSON column type in PostgreSQL that is stored in a decomposed binary format — faster to process than plain JSON, and supports indexing.

**Where we use it:** `questions.options` — stores answer choices as a JSONB array.

**Why we use it:** Combines the flexibility of JSON with the query performance of a structured column. Options are always read with questions — no join needed.

---

### Denormalization
**What it is:** Intentionally storing redundant or pre-computed data to improve read performance, at the cost of more complex writes.

**Where we use it:** `topic_accuracy.accuracy` is pre-computed even though it could be derived from `total_correct / total_answered`.

**Why we use it:** Enables fast sorting and filtering without recalculating on every read. Worth the trade-off for frequently-read dashboard data.

---
