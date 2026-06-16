# AdQuiz — Project Progress

This file tracks what has been completed, what is in progress, and what comes next.
Update this file at the end of every session.

---

## Current Status: Phase 4 — Content Service (In Progress)

---

## Phase 1 — Architecture & Planning

- [x] Define project requirements and core concepts
- [x] Identify service boundaries
- [x] Choose tech stack
- [x] Design system architecture diagram
- [x] Document architectural decisions (decisions.md)
- [x] Design data model — content-service
- [x] Design data model — analytics-service
- [x] Define Kafka event schema (answer-submitted)
- [x] Define REST API contracts per service

---

## Phase 2 — Project Setup

- [x] Create monorepo folder structure
- [x] Set up Docker Compose (Keycloak, Kafka, PostgreSQL instances)
- [x] Configure Keycloak realm, client, and roles
- [x] Set up Docker Compose and verify all infrastructure containers healthy
- [x] Set up api-gateway (Spring Cloud Gateway + JWT validation)

---

## Phase 3 — AI Generation Service

- [x] Set up full Spring Boot project structure
- [x] Integrate Spring AI + OpenAI
- [x] Design prompt template (topic + difficulty + existing questions)
- [x] Implement question generation endpoint
- [x] Handle OpenAI errors and timeouts gracefully

---

## Phase 4 — Content Service

- [x] Set up full Spring Boot project structure
- [x] Implement database schema (Flyway migrations V1-V6)
- [x] Create JPA entities (Topic, Question, QuizSession, SessionQuestion, SpacedRepetitionRecord)
- [x] Create repositories with custom query methods
- [x] Create SecurityConfig (JWT validation, stateless, health endpoint public)
- [x] Create AiGenerationProperties (@ConfigurationProperties)
- [x] Create DTOs (13 records covering all API contracts)
- [x] Create MapStruct mappers (TopicMapper, QuizSessionMapper, SpacedRepetitionMapper)
- [x] Create GlobalExceptionHandler (404, 400, 500)
- [x] Implement TopicService + TopicController
- [x] Seed test data (V6__seed_topics.sql)
- [x] Verified GET /api/topics and GET /api/topics/{id}/subtopics work end to end
- [x] Implement question bank (topic + difficulty storage)
- [x] Implement fuzzy topic matching (V7__enable_pg_trgm.sql, findSimilarSubtopics / findSimilarityParentTopic)
- [x] Implement quiz session management (create, track, end session)
- [x] Implement adaptive difficulty algorithm
- [x] Implement Kafka event publishing (ANSWER_SUBMITTED) + self-consumption for question bank top-up
- [x] Integrate with ai-generation-service (REST call when bank is empty, CompletableFuture parallel generation across all 6 Bloom levels)
- [x] Implement SessionController (all 5 endpoints)
- [x] Boot content-service + ai-generation-service and verify session flow end to end (create session, submit answer, complete session)
- [x] Implement spaced repetition (SM-2 algorithm) + SpacedRepetitionService + ReviewController
- [x] Implement SESSION_COMPLETED Kafka event publishing
- [ ] Write unit tests for adaptive algorithm and SM-2 (deferred to a dedicated testing pass once the application is feature-complete)
- [x] Rename TopicRepository.findSimilarityParentTopic -> findSimilarParentTopics (naming consistency)
- [ ] Refactor QuizSession.mode/status (and CreateSessionRequest.mode) from String to Java enums for type safety (deferred cleanup; String constants used for now)

---

## Phase 5 — Analytics Service

- [x] Set up Spring Boot project
- [x] Implement Kafka consumer (answer-submitted + session-completed)
- [x] Implement streak tracking
- [x] Implement accuracy by topic
- [x] Implement weak area detection
- [x] Implement daily activity tracking
- [x] Expose REST API for dashboard stats

---

## Phase 5.5 — Keycloak Theme

- [ ] Create custom Keycloak theme to match React app UI
- [ ] Create adquiz theme folder structure under infrastructure/keycloak/themes/
- [ ] Build custom login page template (login.ftl)
- [ ] Apply matching CSS and branding
- [ ] Register theme in realm-config.json

---

## Phase 6 — Frontend (React)

- [ ] Set up React project
- [ ] Integrate Keycloak (login, register, JWT)
- [ ] Build landing page
- [ ] Build dashboard (streak, due reviews, accuracy)
- [ ] Build quiz session flow (question, confidence rating, feedback)
- [ ] Build review queue (spaced repetition)
- [ ] Build progress screen (heatmap, accuracy charts, weak areas)

---

## Phase 7 — Integration & Testing

- [ ] End-to-end integration testing
- [ ] Docker Compose full stack run
- [ ] Performance check on adaptive algorithm
- [ ] Error handling and fallback scenarios

---

## Session Log

### Session 1 — [2026-06-07]
**Discussed:**
- Defined project requirements from project description document
- Agreed on collaboration style (guided learning, propose options, explain decisions)
- Identified service boundaries and reasoning
- Chose tech stack (Spring Boot 3.5.14, React, PostgreSQL, Kafka, Keycloak, OpenAI)
- Designed system architecture diagram
- Created docs/ folder with architecture.md, decisions.md, progress.md
- Designed data models for content-service and analytics-service
- Defined Kafka event schema (ANSWER_SUBMITTED, SESSION_COMPLETED)
- Defined REST API contracts for all services
- Created patterns-and-concepts.md glossary
- Set up Docker Compose (PostgreSQL, Zookeeper, Kafka, Keycloak)
- Created Keycloak realm-config.json (adquiz realm, STUDENT role, testuser)
- Verified all infrastructure containers healthy
- Started ai-generation-service:
  - Generated project from start.spring.io
  - Fixed package name to com.adquiz.aigeneration
  - Updated pom.xml to inherit from adquiz-parent
  - Created DTOs: GenerateQuestionRequest, GeneratedQuestionDto, ErrorResponse
  - Created service: QuestionGenerationService (prompt engineering, OpenAI call)
  - Created controller: QuestionGenerationController
  - Created config: OpenAiConfig
  - Created exception: GlobalExceptionHandler
  - Added targetAudience field (UNIVERSITY_STUDENT, INTERVIEW_PREP)
  - Added detailed Bloom's Taxonomy descriptions to prompt

**Stopped at:** About to run ai-generation-service for the first time

---

### Session 2 — [2026-06-09]
**Discussed:**
- Ran ai-generation-service successfully on port 8083
- Tested POST /api/generate/questions — all 3 tests passed (200, 400 validation, interview prep)
- Started content-service setup:
  - Generated project from start.spring.io
  - Updated pom.xml to inherit from adquiz-parent
  - Added oauth2-resource-server dependency
  - Created application.yaml with all sections:
    - server port 8081
    - datasource (content_db)
    - JPA (ddl-auto: validate)
    - Flyway (db/migration)
    - Kafka (producer + consumer)
    - Security (JWT issuer-uri)
    - Custom ai-generation properties
    - Actuator
  - Created package structure:
    controller, service, repository, entity,
    dto, mapper, kafka, config, exception
  - Created db/migration folder for Flyway scripts
- Decided to implement JWT validation at every service (Zero Trust — ADR-010)
- Updated Spring Boot to 3.5.14, Spring Cloud to 2025.0.0, Spring AI to 1.0.8

**Stopped at:** About to write Flyway migration scripts for content-service

---

### Session 3 — [2026-06-11]
**Discussed:**
- Wrote Flyway migration scripts V1-V5 (topics, questions, quiz_sessions, session_questions, spaced_repetition_records)
- Wrote V6__seed_topics.sql (2 parent topics, 5 subtopics)
- Created JPA entities for all 5 tables
- Created 5 repositories with custom query methods
- Created SecurityConfig.java (JWT validation, stateless, health public)
- Created AiGenerationProperties.java (@ConfigurationProperties)
- Created 13 DTOs as Java records
- Created 3 MapStruct mappers
- Created GlobalExceptionHandler (404, 400, 500)
- Created TopicService + TopicController
- Fixed MapStruct annotation processor in pom.xml build section
- Fixed Keycloak directAccessGrantsEnabled and VERIFY_PROFILE issues
- Verified GET /api/topics and GET /api/topics/{id}/subtopics end to end

**Stopped at:** TopicService and TopicController verified working

**Next session should:**
1. Implement SessionService + SessionController
2. Implement adaptive difficulty algorithm
3. Implement KafkaPublisher (ANSWER_SUBMITTED, SESSION_COMPLETED events)
4. Integrate with ai-generation-service (REST call when question bank is low)

---

### Session 4 — [2026-06-14]
**Discussed:**
- Designed topic resolution flow for session creation: user types a topic + parent topic name, server uses pg_trgm fuzzy similarity (`similarity() > 0.4`) to suggest existing matches, scoped separately for parent topics (`parent_id IS NULL`) and subtopics (`parent_id = :parentId`) to avoid ambiguity between same-named subtopics under different parents
- Created `V7__enable_pg_trgm.sql` (enables `pg_trgm` extension + GIN trigram index on `topics.name`)
- Implemented `AdaptiveAlgorithm` (`@Component`): wrong answer → difficulty -1 (floored at 1); 2 consecutive correct → difficulty +1 (capped at 6); 2 consecutive correct with confidence=High (3) → difficulty +2. The "every 2 consecutive correct" rule uses `consecutiveCorrect % 2 == 0 && consecutiveCorrect > 0` so difficulty doesn't increase on every correct answer after the first jump
- Implemented `QuestionService`: `isQuestionBankEmpty`, `generateQuestionsForAllLevels` (parallel `CompletableFuture` calls across all 6 Bloom levels, no `@Transactional` to avoid holding a DB connection during the HTTP call to ai-generation-service), `topUpIfNeeded` (per-user remaining-question check against `questionThreshold`), `pickNextQuestion` (excludes answered question ids), `saveAllQuestions` (`@Transactional`)
- Implemented `AiGenerationClient` + `RestClientConfig` (separate `@Configuration` for the `RestClient` bean, `AIGenerationProperties` for `serviceUrl`/`questionThreshold`)
- Implemented Kafka flow: `KafkaPublisher.publishAnswerSubmitted` (topic `quiz-events`, keyed by `sessionId`), `KafkaConsumer` listens on `quiz-events`, filters `eventType == "ANSWER_SUBMITTED"`, queries answered question ids itself via `SessionQuestionRepository.findAnswerdQuestionIds`, calls `questionService.topUpIfNeeded` — keeps the answer endpoint non-blocking (self-consumption pattern)
- `AnswerSubmittedEvent` final shape: `eventId, eventType, userId, sessionId, questionId, topicId, bloomLevel, isCorrect, confidenceRating, answeredAt` (no `remaining_questions` field — consumer recomputes this itself)
- Implemented `SessionService`: `createSession` (resolves topic via `resolveTopic`, generates questions for all levels if bank empty, creates `QuizSession` with `currentDifficulty = 1`, picks + persists first `SessionQuestion`), `submitAnswer` (validates `IN_PROGRESS` status, records answer, counts consecutive correct via simple backward walk, calls `AdaptiveAlgorithm`, completes session on last question or picks next question excluding session-wide answered ids, publishes `ANSWER_SUBMITTED` with the real `question.getId()`), `abandonSession`, `getSessionHistory`, `getSessionState`
- `extractUserId(Authentication auth)` casts to `JwtAuthenticationToken` and reads `jwt.getSubject()` (the `sub` claim, a UUID) — not `auth.getName()`, which returns the username string
- Implemented `SessionController` — `POST /api/sessions`, `GET /api/sessions`, `GET /api/sessions/{id}`, `POST /api/sessions/{id}/answer`, `POST /api/sessions/{id}/complete`
- Fixed `ai-generation-service/pom.xml` — same Lombok annotation-processor issue as content-service earlier (missing `maven-compiler-plugin` + `annotationProcessorPaths` for `lombok`), which caused `@Data`/`@Slf4j`-generated methods to not compile
- Booted both `content-service` (port 8081) and `ai-generation-service` (port 8083) successfully against the existing Docker infrastructure (Postgres, Kafka, Zookeeper, Keycloak)

**Stopped at:** Both services running, fresh JWT token obtained, about to test the full session flow end to end (create session → submit answer → check state → complete session)

**Next session should:**
1. Test session flow end to end (create session, submit answer with `topUpIfNeeded` triggering AI generation, get session state, complete session)
2. Implement `SpacedRepetitionService` (SM-2 algorithm) + `ReviewController` (`GET /api/reviews/due`, `POST /api/reviews/{topicId}/rate`)
3. Implement `SESSION_COMPLETED` Kafka event publishing
4. Write unit tests for `AdaptiveAlgorithm` and SM-2
5. Rename `TopicRepository.findSimilarityParentTopic` → `findSimilarParentTopics`

---

### Session 5 — [2026-06-15]
**Discussed:**
- Verified session flow end to end (create session, submit answer, complete session) — confirmed working
- Fixed Kafka `MessageConversionException`: refactored `KafkaConsumer` from a single-typed `@KafkaListener` method to a class-level `@KafkaListener` + per-type `@KafkaHandler` methods (`consumeAnswerSubmitted(AnswerSubmittedEvent)`, `consumeSessionCompleted(SessionCompletedEvent)`) — Spring now dispatches by deserialized type via the `__TypeId__` header
- Implemented `SESSION_COMPLETED` Kafka event: `SessionCompletedEvent` record + `KafkaPublisher.publishSessionCompleted` (computes `finalAccuracy`), published from `SessionService.submitAnswer` when the last question is answered
- Renamed `TopicRepository.findSimilarityParentTopic` → `findSimilarParentTopics`
- Designed and implemented review-session flow:
  - Added `CreateSessionRequest.mode` validation: `@Pattern(regexp = "ADAPTIVE|REVIEW")`
  - Aligned exclusion logic for new (`ADAPTIVE`) sessions — both `createSession` and `submitAnswer` now exclude all-time answered question ids per (user, topic, bloomLevel) via `findAnsweredQuestionIds`, so repeat sessions surface fresh questions instead of repeating ones from earlier sessions
  - Added `SessionQuestionRepository.findAnsweredByUserAndTopic` (all-time answered questions for a user+topic, with `JOIN FETCH sq.question`)
  - Implemented `QuestionService.pickReviewQuestion(userId, topicId, excludedIds)` + `weaknessScore(SessionQuestion)`: deterministically picks the highest-weakness previously-answered question, excluding ids already used this session. Weakness score = `(isCorrect ? 1 : 10) + (4 - confidenceRating) * 2 + min(daysSinceLastAnswered, 14)` — correctness dominates, confidence and recency are secondary factors, recency capped at 14 days
  - `SessionService.createSession`/`submitAnswer` branch on `mode == REVIEW` to call `pickReviewQuestion` instead of `pickNextQuestion`, and set `currentDifficulty` from the picked question's `bloomLevel`
- Added string constants to `SessionService` for `mode`/`status` values (`MODE_REVIEW`, `STATUS_IN_PROGRESS`, `STATUS_COMPLETED`, `STATUS_ABANDONED`) — replaced hardcoded literals throughout
- Designed and implemented SM-2 spaced repetition:
  - `SpacedRepetitionRecordRepository.findDueForReview(userId, date)` — `JOIN FETCH r.topic t LEFT JOIN FETCH t.parent WHERE r.nextReviewDate <= :date`
  - `SpacedRepetitionService.recordSessionCompletion(userId, topic, finalAccuracy)` — maps accuracy to SM-2 quality (`>=0.9→5, >=0.7→4, >=0.4→3, else 0`), applies SM-2 (`applySm2`): quality<3 resets `repetitions=0, intervalDays=1`; quality>=3 grows interval (`1 → 6 → round(prevInterval * easeFactor)`), capped at `MAX_INTERVAL_DAYS = 180` to prevent unbounded/overflow growth from an ever-increasing `easeFactor`; updates `easeFactor` (floor 1.3, no ceiling) using the standard SM-2 formula, rounded to 2 decimals via `BigDecimal.setScale(2, HALF_UP)`
  - `SpacedRepetitionService.getDueReviews(userId)` — maps due records to `DueReviewDto`
  - `recordSessionCompletion` is called from `SessionService.submitAnswer` when the last question is answered, before publishing `SESSION_COMPLETED`
  - Created `ReviewController` — `GET /api/reviews/due`
  - Removed dead `/rate` endpoint code: `RateReviewRequest`, `RateReviewResponse`, `SpacedRepetitionMapper.toRateReviewResponse`
- Updated `docs/api-contracts.md`: documented `mode = ADAPTIVE | REVIEW` in session creation, SM-2 side effects on session completion, removed `/rate` endpoint, updated `/api/reviews/due` response to camelCase

- Verified the full SM-2 + review-session flow end to end against the running `content-service`:
  - Created an `ADAPTIVE` session (topic "Linked Lists", 5 questions), answered all correctly with high confidence — confirmed adaptive difficulty `+1`/`+2` jump rules (bloomLevel progression `1 → 1 → 3 → 3 → 5`)
  - Last answer triggered `SESSION_COMPLETED` (published + consumed cleanly, no `MessageConversionException`) and created a new `SpacedRepetitionRecord` (`easeFactor 2.50 → 2.60`, `repetitions = 1`, `intervalDays = 1`, `nextReviewDate = +1 day`)
  - `GET /api/reviews/due` returned the topic correctly (after temporarily backdating `nextReviewDate` for the test, since a fresh record is never due same-day)
  - Created a `REVIEW` session for the same topic (3 questions) — `pickReviewQuestion` correctly selected only previously-answered questions, and `excludedIds` prevented repeats within the session
  - Completing the review session triggered a **second** SM-2 update: `repetitions 1 → 2`, `intervalDays` jumped to the hardcoded `6`, `easeFactor 2.60 → 2.70`, `nextReviewDate = +6 days`

**Stopped at:** SM-2 + review-session feature fully implemented and verified end to end. Phase 4 (Content Service) is functionally complete aside from deferred items (unit tests, enum refactor).

**Next session should:**
1. Decide next focus: set up `api-gateway` (Phase 2 remaining item), start Phase 5 (analytics-service), or start Phase 6 (React frontend)
2. Backlog carried forward: unit tests for `AdaptiveAlgorithm` and SM-2 (deferred); refactor `mode`/`status` from String constants to Java enums (deferred)

---

### Session 6 — [2026-06-16]
**Discussed:**
- Decided to implement `analytics-service` before `api-gateway` (so gateway can route all services at once)
- Set up `analytics-service` Spring Boot project (port 8082, `analytics_db`, package `com.adquiz.analytics`)
- Fixed typo in generated project: `analytics-serivce` → `analytics-service` (folder, artifactId, class names)
- Designed and created 3 Flyway migrations: `user_streaks` (V1, natural UUID PK), `topic_stats` (V2, surrogate UUID PK + unique constraint on user_id/topic_id), `daily_activity` (V3, surrogate UUID PK + unique constraint on user_id/activity_date) — chose surrogate keys over composite PKs to avoid JPA `@IdClass`/`@EmbeddedId` boilerplate, consistent with `SpacedRepetitionRecord` pattern
- Created 3 JPA entities (`UserStreak`, `TopicStats`, `DailyActivity`), 3 repositories, `SecurityConfig`, `GlobalExceptionHandler`
- Extended `content-service` Kafka events for Option 1 (topic name denormalization): added `topicName`/`parentTopicName` to `AnswerSubmittedEvent` and `SessionCompletedEvent`; updated `KafkaPublisher` to accept `Topic` entity instead of `topicId`; fixed `completeAt` → `completedAt` typo in `SessionCompletedEvent`; updated `SessionService` call sites
- Created local event records in `analytics-service` (`AnswerSubmittedEvent`, `SessionCompletedEvent` with `@JsonIgnoreProperties(ignoreUnknown = true)`) + configured `spring.json.type.mapping` in `application.yaml` to translate content-service type headers to local types
- Implemented `AnalyticsService` with Kafka handlers refactored out of `KafkaConsumer` (separation of concerns): `handleAnswerSubmitted` updates all 3 tables; `handleSessionCompleted` updates `daily_activity.sessionsCompleted`; streak logic uses early-return guard when `lastActiveDate == today` to avoid redundant DB writes
- Fixed bug: last question's answer was never published as `ANSWER_SUBMITTED` (only `SESSION_COMPLETED` was fired) — added `publishAnswerSubmitted` call in the `isLastQuestion` branch of `SessionService.submitAnswer` before `publishSessionCompleted`
- Created `AnalyticsController` (`GET /api/analytics/streak`, `/accuracy`, `/weak-areas`, `/activity`) + 4 DTOs (`StreakDto`, `TopicAccuracyDto`, `WeakAreaDto`, `DailyActivityDto`)
- Verified all 4 endpoints end to end against live data: streak, accuracy, weak-areas (filtered by `totalAnswered >= 5`), daily activity all returned correct values

**Stopped at:** Phase 5 (analytics-service) fully implemented and verified. All backend services (content-service, ai-generation-service, analytics-service) are complete.

**Next session should:**
1. Set up `api-gateway` (Spring Cloud Gateway + JWT validation, routes all 3 services) — Phase 2 remaining item
2. Then start Phase 6 (React frontend)
3. Backlog carried forward: unit tests (deferred); enum refactor for `mode`/`status` (deferred)

---

### Session 7 — [2026-06-16]
**Discussed:**
- Set up `api-gateway` (port 8080, `com.adquiz.gateway`) using Spring Cloud Gateway WebFlux (`spring-cloud-starter-gateway-server-webflux`)
- Chose WebFlux reactive gateway over MVC gateway for richer filter ecosystem and maturity
- Configured routes: `/api/sessions/**, /api/topics/**, /api/reviews/**` → `content-service:8081`; `/api/analytics/**` → `analytics-service:8082`; `ai-generation-service` intentionally not exposed (internal only)
- Updated config key from deprecated `spring.cloud.gateway.routes` to `spring.cloud.gateway.server.webflux.routes` per Spring Cloud migration warning
- Wrote `SecurityConfig` using reactive Spring Security (`@EnableWebFluxSecurity`, `SecurityWebFilterChain`, `ServerHttpSecurity`) — different API from MVC `SecurityFilterChain` but same intent
- Fixed two startup issues: wrong Keycloak port (`8081` → `8180`) in `issuer-uri`; added `netty-resolver-dns-native-macos` (classifier `osx-aarch_64`) to fix macOS Netty DNS warning
- Verified gateway end to end: requests with valid JWT routed correctly to downstream services; requests without JWT rejected with `401` at the gateway before reaching any service

**Stopped at:** All backend services complete and verified through the gateway. Ready to start Phase 6 (React frontend).

**Next session should:**
1. Start Phase 6 — React frontend setup (Vite + React, Keycloak integration, routing)
2. Backlog carried forward: unit tests (deferred); enum refactor for `mode`/`status` (deferred)

---
