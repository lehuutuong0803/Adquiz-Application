CREATE TABLE spaced_repetition_records (
    id                UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID        NOT NULL,
    topic_id          UUID        NOT NULL REFERENCES topics(id),
    ease_factor       DECIMAL     NOT NULL DEFAULT 2.5,
    interval_days     SMALLINT    NOT NULL DEFAULT 1,
    repetitions       SMALLINT    NOT NULL DEFAULT 0,
    next_review_date  DATE        NOT NULL,
    last_review_date  DATE        NOT NULL,
    created_at        TIMESTAMP   NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_sr_user_topic UNIQUE (user_id, topic_id)
);

CREATE INDEX idx_sr_records_user_next_review ON spaced_repetition_records(user_id, next_review_date);