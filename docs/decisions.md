# AdQuiz — Architectural Decision Records (ADR)

A log of every major decision made during the project, with reasoning.
Update this file whenever a new architectural decision is made.

---

## ADR-001 — Microservices Architecture

**Decision:** Build AdQuiz as a microservices application, not a monolith.

**Reasoning:**
- Primary goal is to learn real-world architecture patterns
- Each service has a clearly distinct responsibility and failure profile
- Services can be developed, deployed, and scaled independently

**Trade-off:** More complex setup than a monolith, but worth it for the learning objective.

---

## ADR-002 — Service Boundaries

**Decision:** Split the system into 5 services: `api-gateway`, `auth-service` (Keycloak), `content-service`, `ai-generation-service`, `analytics-service`.

**Reasoning:**
- `api-gateway` — single entry point, centralizes JWT validation and routing
- `auth-service` — security-critical, must be isolated from business logic
- `content-service` — owns the core learning loop (sessions, adaptive algorithm, spaced repetition). These responsibilities share the same data and change for the same reason, so they stay together
- `ai-generation-service` — separated because it has a different failure profile (OpenAI can be slow or down). Content-service must not be blocked by LLM latency
- `analytics-service` — fully decoupled via Kafka so analytics never slows down a quiz session

---

## ADR-003 — Keycloak for Authentication

**Decision:** Use Keycloak as the identity provider instead of a custom auth service.

**Reasoning:**
- Provides login, registration, JWT tokens, and refresh tokens out of the box
- Services remain stateless — they only validate JWT tokens, never manage passwords
- Industry-standard pattern used in real companies
- React talks to Keycloak directly for auth flows; API Gateway validates JWT on every request

**Trade-off:** Heavier setup than custom auth, mitigated by running Keycloak via Docker.

---

## ADR-004 — Kafka for Analytics Events

**Decision:** Use Kafka as the message broker between `content-service` and `analytics-service`.

**Reasoning:**
- Analytics should never be in the critical path of a quiz session
- After a student answers a question, content-service fires an event and moves on immediately
- Analytics-service consumes events independently and catches up on its own time
- The two services never talk to each other directly — true decoupling

---

## ADR-005 — PostgreSQL for All Services

**Decision:** Use PostgreSQL as the database for `content-service` and `analytics-service`.

**Reasoning:**
- Quiz data (questions, sessions, spaced repetition records) is highly structured and relational
- Analytics aggregations (streaks, accuracy by topic) are much easier with SQL
- MongoDB offers no advantage here — it shines when data shape varies wildly, which is not the case
- Each service owns its own PostgreSQL instance — no shared databases

---

## ADR-006 — OpenAI for Question Generation

**Decision:** Use OpenAI as the LLM provider via Spring AI.

**Reasoning:**
- Well-documented, reliable API
- Spring AI provides native integration, reducing boilerplate
- AI generation service is stateless — it receives topic + difficulty, calls OpenAI, returns questions
- Existing questions are passed to the prompt to avoid duplicates

---

## ADR-007 — SM-2 Algorithm for Spaced Repetition

**Decision:** Use the SM-2 algorithm for scheduling spaced repetition reviews.

**Reasoning:**
- Battle-tested memory scheduling algorithm (same as Anki)
- Student ratings (Forgot / Hard / Got it / Easy) map directly to SM-2 quality scores (0-5)
- No need to invent a custom algorithm when SM-2 is well-researched and proven

---

## ADR-008 — Bloom's Taxonomy for Difficulty Levels

**Decision:** Use Bloom's Taxonomy as the difficulty framework for questions.

**Reasoning:**
- Principled, research-backed classification system
- 6 levels: Remember → Understand → Apply → Analyze → Evaluate → Create
- Gives the adaptive algorithm a structured scale to move up and down
- Maps well to university-level academic content

---

## ADR-010 — Security at Every Service Layer

**Decision:** Implement JWT validation at both the API gateway and each individual service.

**Reasoning:**
- Network isolation alone is not sufficient — internal threats still exist
- A compromised service could call another service without authentication
- Developer mistakes could create silent security holes
- Zero Trust principle — every service independently validates identity
- Per-service security adds minimal code but significant protection

**Implementation:**
- API Gateway — first line of defense, validates JWT and blocks invalid requests
- Each service — independently validates JWT signature using Keycloak public keys
- `/actuator/health` endpoint is public on all services — needed for Docker healthchecks
- All other endpoints require a valid JWT token

---

## ADR-011 — Fuzzy Topic Matching via pg_trgm

**Decision:** When a student creates a session by typing a topic/subtopic name, use PostgreSQL's `pg_trgm` extension (`similarity(name, :query) > 0.4`) to suggest existing topics instead of creating duplicates.

**Reasoning:**
- Students will type "Linked List" vs "Linked Lists" vs "linked list" — exact match (`findByNameIgnoreCase`) misses near-duplicates
- Reusing existing topics means reusing their existing question bank — avoids redundant AI generation
- Parent topics (`parent_id IS NULL`) and subtopics (`parent_id = :parentId`) are matched separately — a subtopic name match must be scoped to the chosen parent, otherwise the same subtopic name under a different parent would cause ambiguity
- Considered LLM-based duplicate detection (more powerful, semantic) but rejected for now — too expensive per session creation; `pg_trgm` is fast, free, and good enough for typo/casing variants

**Implementation:** `V7__enable_pg_trgm.sql` enables the extension and adds a GIN trigram index on `topics.name`. `TopicRepository.findSimilarSubtopics` and `findSimilarityParentTopic` (to be renamed `findSimilarParentTopics`) return top-5 matches above the similarity threshold.

---

## ADR-012 — Adaptive Difficulty Requires 2 Consecutive Correct Answers

**Decision:** The adaptive algorithm only increases difficulty after the student answers **2 consecutive questions correctly**, using `consecutiveCorrect % 2 == 0 && consecutiveCorrect > 0` rather than tracking per-difficulty-level counters.

**Reasoning:**
- A single correct answer isn't strong enough evidence the student has mastered the level — 2 in a row reduces the chance of a lucky guess driving difficulty up
- A naive "count consecutive correct, increase every time count >= 2" rule over-accelerates: once the streak passes 2, *every subsequent* correct answer would also satisfy `>= 2` and keep bumping difficulty
- Per-difficulty-level counters (reset the counter whenever the level changes) were considered but rejected as more stateful and harder to reason about
- The modulo trick (`% 2 == 0`) naturally produces "every 2nd consecutive correct answer" without any extra state beyond the existing consecutive-correct count, and correctly resets the moment a wrong answer breaks the streak (since `countConsecutiveCorrect` walks backward and stops at the first wrong answer)

**Rules (implemented in `AdaptiveAlgorithm.calculateNextDifficulty`):**
- Wrong answer → `currentDifficulty - 1`, floored at 1 (`Math.max`)
- 2 consecutive correct + confidence rating = High (3) → `currentDifficulty + 2`, capped at 6 (`Math.min`)
- 2 consecutive correct (any other confidence) → `currentDifficulty + 1`, capped at 6
- Otherwise → difficulty unchanged

---

## ADR-013 — Question Bank Top-Up via Kafka Self-Consumption

**Decision:** `content-service` publishes `ANSWER_SUBMITTED` to Kafka and also consumes that same event to check whether the student's remaining unseen questions (for that topic + bloom level) have dropped below `questionThreshold`, triggering AI generation if so.

**Reasoning:**
- Calling `ai-generation-service` synchronously inside `POST /api/sessions/{id}/answer` would add OpenAI latency directly to the student's response time
- The "remaining questions" check is per-user (total questions in bank minus questions this user has already answered), not a global bank count — a popular topic could be globally well-stocked but exhausted for one specific student
- Self-consumption keeps `submitAnswer` itself simple (record answer, calculate difficulty, pick next question, publish event, return) while the top-up logic lives entirely in `KafkaConsumer` + `QuestionService.topUpIfNeeded`
- `topUpIfNeeded` (not `submitAnswer`) is responsible for deciding *whether* to generate — guarantees that by the time the student submits their *next* answer, questions are available, without ever blocking the current request

---

## ADR-009 — Tech Stack

**Decision:** The following tech stack was chosen for each service.

| Service | Framework | Database |
|---|---|---|
| `api-gateway` | Spring Cloud Gateway | — |
| `auth-service` | Keycloak | PostgreSQL (Keycloak-managed) |
| `content-service` | Spring Boot | PostgreSQL |
| `ai-generation-service` | Spring Boot | — (stateless) |
| `analytics-service` | Spring Boot | PostgreSQL |
| `frontend` | React | — |

**Reasoning:**
- Java/Spring Boot: developer's existing experience (3 years)
- React: developer's preference for frontend
- PostgreSQL: relational data, strong SQL support for analytics
