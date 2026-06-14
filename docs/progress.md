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
- [ ] Set up api-gateway (Spring Cloud Gateway + JWT validation)

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
- [ ] Implement spaced repetition (SM-2 algorithm) + SpacedRepetitionService + ReviewController
- [ ] Implement SESSION_COMPLETED Kafka event publishing
- [ ] Write unit tests for adaptive algorithm and SM-2
- [ ] Rename TopicRepository.findSimilarityParentTopic -> findSimilarParentTopics (naming consistency)

---

## Phase 5 — Analytics Service

- [ ] Set up Spring Boot project
- [ ] Implement Kafka consumer (answer-submitted)
- [ ] Implement streak tracking
- [ ] Implement accuracy by topic
- [ ] Implement weak area detection
- [ ] Implement daily activity tracking
- [ ] Expose REST API for dashboard stats

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
