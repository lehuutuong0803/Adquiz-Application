CREATE TABLE daily_activity (
    user_id                 UUID        NOT NULL,
    activity_date           DATE        NOT NULL,
    questions_answered      INT         NOT NULL DEFAULT 0,
    correct_answers         INT         NOT NULL DEFAULT 0,
    sessions_completed      INT         NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id, activity_date)
)