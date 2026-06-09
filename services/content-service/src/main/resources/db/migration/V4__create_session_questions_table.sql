CREATE TABLE session_questions (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id          UUID        NOT NULL REFERENCES quiz_sessions(id),
    question_id         UUID        NOT NULL REFERENCES questions(id),
    question_index      SMALLINT    NOT NULL,
    selected_option     VARCHAR,
    is_correct          BOOLEAN,
    confidence_rating   SMALLINT,
    time_spent          SMALLINT,
    answered_at         TIMESTAMP
);

CREATE INDEX idx_session_questions_session_id ON session_questions(session_id)