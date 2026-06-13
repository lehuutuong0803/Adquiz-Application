CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX idx_topics_name_trgmm ON topics USING gin(name gin_trgm_ops)