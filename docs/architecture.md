# AdQuiz — System Architecture

## Overview

AdQuiz is an AI-powered adaptive quiz platform for university students.
Built as a microservices application using Spring Boot, React, Kafka, and Keycloak.

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                                │
│                                                                     │
│    ┌─────────────────┐              ┌─────────────────┐            │
│    │   React App     │◄────────────►│    Keycloak     │            │
│    │   (Frontend)    │   login/     │  (Auth Server)  │            │
│    │                 │   register   │                 │            │
│    └────────┬────────┘              └────────┬────────┘            │
│             │ JWT token                      │ manages              │
│             │ + API calls                    │ its own DB           │
└─────────────┼──────────────────────────────┼─────────────────────┘
              │                              │
              ▼                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         GATEWAY LAYER                               │
│                                                                     │
│    ┌────────────────────────────────────────────────────────┐      │
│    │                    API Gateway                          │      │
│    │            (Spring Cloud Gateway)                       │      │
│    │                                                         │      │
│    │   • Routes requests to correct service                  │      │
│    │   • Validates JWT with Keycloak on every request        │      │
│    │   • Single entry point for all client requests          │      │
│    └───────────┬──────────────────────────┬─────────────────┘      │
└───────────────┼──────────────────────────┼──────────────────────────┘
                │                          │
                │ REST                     │ REST
                ▼                          ▼
┌──────────────────────────┐  ┌───────────────────────────┐
│     CONTENT SERVICE      │  │     ANALYTICS SERVICE     │
│     (Spring Boot)        │  │     (Spring Boot)         │
│                          │  │                           │
│  • Quiz session manager  │  │  • Streaks                │
│  • Adaptive algorithm    │  │  • Accuracy by topic      │
│  • Spaced repetition     │  │  • Weak areas             │
│  • Question bank         │  │  • Daily activity         │
│                          │  │  • Dashboard stats        │
│  ┌────────────────────┐  │  │                           │
│  │   PostgreSQL       │  │  │  ┌─────────────────────┐ │
│  │                    │  │  │  │    PostgreSQL        │ │
│  │  • questions       │  │  │  │                     │ │
│  │  • sessions        │  │  │  │  • events           │ │
│  │  • sr_records      │  │  │  │  • user_stats       │ │
│  └────────────────────┘  │  │  │  • topic_accuracy   │ │
│             │             │  │  └─────────────────────┘ │
│    (REST when             │  │            ▲              │
│     bank empty)           │  │            │ consumes     │
│             │             │  └────────────┼─────────────┘
│             ▼             │               │
│  ┌──────────────────────┐ │               │
│  │  AI GENERATION       │ │    ┌──────────────────────┐
│  │  SERVICE             │ │    │       KAFKA          │
│  │  (Spring Boot)       │ │    │  (Message Broker)    │
│  │                      │ │    │                      │
│  │  • Calls OpenAI API  │ │    │  topic:              │
│  │  • Builds prompts    │ │    │  answer-submitted     │
│  │  • Returns questions │ │    │                      │
│  │  • Stateless         │ │    └──────────────────────┘
│  └──────────────────────┘ │               ▲
│                            │               │ publishes
└────────────────────────────┘               │
                                   (content-service
                                    fires event after
                                    each answer)
```

---

## Services

| Service | Framework | Database | Role |
|---|---|---|---|
| `api-gateway` | Spring Cloud Gateway | — | Routes requests, validates JWT |
| `auth-service` | Keycloak | PostgreSQL (managed by Keycloak) | Authentication & authorization |
| `content-service` | Spring Boot | PostgreSQL | Quiz sessions, adaptive algorithm, spaced repetition |
| `ai-generation-service` | Spring Boot | — | Stateless, calls OpenAI to generate questions |
| `analytics-service` | Spring Boot | PostgreSQL | Kafka consumer, tracks stats and streaks |
| `frontend` | React | — | Student-facing UI |

---

## Communication Patterns

| From | To | Protocol | Why |
|---|---|---|---|
| React | Keycloak | REST | Direct auth, get JWT token |
| React | API Gateway | REST + JWT | All business API calls |
| API Gateway | Keycloak | REST | Validate JWT on every request |
| API Gateway | Content Service | REST | Forward quiz/session requests |
| API Gateway | Analytics Service | REST | Forward dashboard/stats requests |
| Content Service | AI Generation Service | REST | Request new questions when bank is empty |
| Content Service | Kafka | Event (publish) | Fire `answer-submitted` after each answer |
| Analytics Service | Kafka | Event (consume) | Listen for `answer-submitted`, update stats |

---

## Key Design Decisions

1. **React talks to Keycloak directly** — auth is handled outside the gateway, keeps auth flow standard and decoupled
2. **AI Generation is a separate service** — isolated from content-service so OpenAI slowness or downtime never blocks quiz sessions
3. **Analytics is event-driven via Kafka** — analytics is never in the critical path of a quiz session, fire and forget
4. **Each service owns its own database** — no shared databases between services, true microservice isolation
5. **SM-2 algorithm for spaced repetition** — battle-tested memory scheduling algorithm (same as Anki)
6. **Bloom's Taxonomy for difficulty levels** — principled difficulty classification (Remember → Understand → Apply → Analyze → Evaluate → Create)
