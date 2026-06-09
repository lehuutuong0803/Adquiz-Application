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
