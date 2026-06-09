-- Runs once on first container start
-- Creates analytics_db since PostgreSQL only auto-creates content_db
CREATE DATABASE analytics_db;
GRANT ALL PRIVILEGES ON DATABASE analytics_db to adquiz;

