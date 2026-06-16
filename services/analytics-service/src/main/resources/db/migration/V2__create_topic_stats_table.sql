CREATE TABLE topic_stats (
    id                 UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id            UUID        NOT NULL,
    topic_id           UUID        NOT NULL,
    topic_name         VARCHAR     NOT NULL,
    parent_topic_name  VARCHAR,
    total_answered     INT         NOT NULL DEFAULT 0,
    total_correct      INT         NOT NULL DEFAULT 0,
    updated_at         TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_topic_stats_user_topic UNIQUE (user_id, topic_id)
)