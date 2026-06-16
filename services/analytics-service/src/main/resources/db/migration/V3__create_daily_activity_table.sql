CREATE TABLE daily_activity (
    id                   UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id              UUID        NOT NULL,
    activity_date        DATE        NOT NULL,
    questions_answered   INT         NOT NULL DEFAULT 0,
    correct_answers      INT         NOT NULL DEFAULT 0,
    sessions_completed   INT         NOT NULL DEFAULT 0,
    CONSTRAINT uq_daily_activity_user_date UNIQUE (user_id, activity_date)
)