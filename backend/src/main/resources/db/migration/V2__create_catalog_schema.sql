CREATE TABLE artists (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    biography VARCHAR(2000),
    country VARCHAR(100),
    active_from DATE,
    active_to DATE
);

CREATE TABLE albums (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    release_date DATE,
    cover_art_url VARCHAR(500),
    artist_id BIGINT NOT NULL REFERENCES artists(id)
);

CREATE TABLE genres (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(1000)
);

CREATE TABLE tracks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    duration INTEGER NOT NULL,
    release_date DATE,
    description VARCHAR(2000),
    album_id BIGINT REFERENCES albums(id)
);

CREATE TABLE track_genres (
    track_id BIGINT NOT NULL REFERENCES tracks(id),
    genre_id BIGINT NOT NULL REFERENCES genres(id),
    PRIMARY KEY (track_id, genre_id)
);

CREATE TABLE ratings (
    id BIGSERIAL PRIMARY KEY,
    track_id BIGINT NOT NULL REFERENCES tracks(id),
    user_id VARCHAR(100) NOT NULL,
    rating_value INTEGER NOT NULL CHECK (rating_value BETWEEN 1 AND 5),
    review VARCHAR(1000)
);

CREATE INDEX idx_albums_artist_id ON albums(artist_id);
CREATE INDEX idx_tracks_album_id ON tracks(album_id);
CREATE INDEX idx_ratings_track_id ON ratings(track_id);