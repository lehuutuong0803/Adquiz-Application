CREATE TABLE quiz_sessions (
    id                      UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                 UUID        NOT NULL,
    topic_id                UUID        NOT NULL REFERENCES topics(id),
    mode                    VARCHAR     NOT NULL,
    status                  VARCHAR     NOT NULL DEFAULT 'IN_PROGRESS',
    total_questions         SMALLINT    NOT NULL,
    current_question_index  SMALLINT    NOT NULL DEFAULT 1,
    current_difficulty      SMALLINT    NOT NULL DEFAULT 1,
    started_at              TIMESTAMP   NOT NULL DEFAULT NOW(),
    ended_at                TIMESTAMP
);

CREATE INDEX idx_quiz_sessions_user_id ON quiz_sessions(user_id);