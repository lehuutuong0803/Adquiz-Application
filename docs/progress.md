# AdQuiz — Project Progress

This file tracks what has been completed, what is in progress, and what comes next.
Update this file at the end of every session.

---

## Current Status: Architecture & Planning Phase

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

- [ ] Set up full Spring Boot project structure
- [ ] Implement database schema (Flyway migrations)
- [ ] Implement question bank (topic + difficulty storage)
- [ ] Implement quiz session management (create, track, end session)
- [ ] Implement adaptive difficulty algorithm
- [ ] Implement spaced repetition (SM-2 algorithm)
- [ ] Implement Kafka event publishing (answer-submitted)
- [ ] Integrate with ai-generation-service (REST call when bank is empty)
- [ ] Write unit tests for adaptive algorithm and SM-2

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

**Next session should:**
1. Write Flyway migration scripts (V1 through V5) for content-service tables:
   - V1__create_topics_table.sql
   - V2__create_questions_table.sql
   - V3__create_quiz_sessions_table.sql
   - V4__create_session_questions_table.sql
   - V5__create_spaced_repetition_records_table.sql
2. Create JPA entities for each table
3. Create repositories
4. Create SecurityConfig.java
5. Create AiGenerationProperties.java (@ConfigurationProperties)

---
