CREATE TABLE billboard_chart_entries (
    id BIGSERIAL PRIMARY KEY,
    chart_year INTEGER NOT NULL CHECK (chart_year BETWEEN 1980 AND 1989),
    chart_week INTEGER NOT NULL CHECK (chart_week BETWEEN 1 AND 53),
    chart_rank INTEGER NOT NULL CHECK (chart_rank BETWEEN 1 AND 100),
    track_id BIGINT NOT NULL REFERENCES tracks(id),
    UNIQUE (chart_year, chart_week, chart_rank)
);

CREATE INDEX idx_billboard_entries_track_id ON billboard_chart_entries(track_id);