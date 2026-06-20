CREATE TABLE IF NOT EXISTS courses (
    id VARCHAR(16) PRIMARY KEY,
    name TEXT,
    description TEXT,
    credits NUMERIC,
    requirement_text TEXT,
    udem_website TEXT,
    raw_data JSONB NOT NULL
);

CREATE TABLE IF NOT EXISTS programs (
    id TEXT PRIMARY KEY,
    name TEXT,
    raw_data JSONB NOT NULL
);

CREATE TABLE IF NOT EXISTS schedules (
    course_id VARCHAR(16) NOT NULL,
    semester TEXT NOT NULL,
    raw_data JSONB NOT NULL,
    fetched_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (course_id, semester)
);

CREATE INDEX IF NOT EXISTS idx_courses_name ON courses USING gin (to_tsvector('simple', coalesce(name, '')));
CREATE INDEX IF NOT EXISTS idx_courses_description ON courses USING gin (to_tsvector('simple', coalesce(description, '')));
CREATE INDEX IF NOT EXISTS idx_schedules_semester ON schedules (semester);
