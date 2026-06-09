CREATE TABLE topics (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR     NOT NULL,
    parent_id   UUID        REFERENCES topics(id),
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW()
)