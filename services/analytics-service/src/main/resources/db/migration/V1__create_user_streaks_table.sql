CREATE TABLE user_streaks (
    user_id             UUID        PRIMARY KEY,
    current_streak      INT         NOT NULL DEFAULT 0,
    longest_streak      INT         NOT NULL DEFAULT 0,
    last_active_date    DATE,
    updated_at          TIMESTAMP   NOT NULL DEFAULT NOW()
)