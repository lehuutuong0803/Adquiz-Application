CREATE TABLE questions (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    topic_id        UUID        NOT NULL REFERENCES topics(id),
    bloom_level     SMALLINT    NOT NULL CHECK (bloom_level BETWEEN 1 AND 6),
    question_text   TEXT        NOT NULL,
    options         JSONB       NOT NULL,
    correct_answer  VARCHAR     NOT NULL,
    explanation     TEXT,
    created_by      VARCHAR     NOT NULL,
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_questions_topic_bloom ON questions(topic_id, bloom_level)