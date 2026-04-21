CREATE TABLE artists (
    artist_id    UUID         NOT NULL PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    label        VARCHAR(255),
    biography    TEXT,
    version      BIGINT,
    created_at   TIMESTAMP(6)  NOT NULL,
    updated_at   TIMESTAMP(6)  NOT NULL
);

CREATE TABLE products (
    product_id    UUID           NOT NULL PRIMARY KEY,
    type          VARCHAR(50)    NOT NULL,
    status        VARCHAR(50)    NOT NULL,
    title         VARCHAR(255)   NOT NULL,
    genre         VARCHAR(255)   NOT NULL,
    release_date  DATE           NOT NULL,
    artwork_url   VARCHAR(512),
    status_reason VARCHAR(512),
    price         DECIMAL(10, 2),
    version       BIGINT,
    created_at    TIMESTAMP(6)    NOT NULL,
    updated_at    TIMESTAMP(6)    NOT NULL
);

CREATE TABLE product_artist (
    product_id UUID NOT NULL,
    artist_id  UUID NOT NULL,
    PRIMARY KEY (product_id, artist_id),
    CONSTRAINT fk_product_artist_product FOREIGN KEY (product_id) REFERENCES products (product_id),
    CONSTRAINT fk_product_artist_artist  FOREIGN KEY (artist_id)  REFERENCES artists (artist_id)
);

CREATE TABLE tracks (
    track_id         UUID         NOT NULL PRIMARY KEY,
    title            VARCHAR(255) NOT NULL,
    track_number     INT          NOT NULL,
    duration_seconds INT          NOT NULL,
    audio_url        VARCHAR(512) NOT NULL,
    product_id       UUID         NOT NULL,
    version          BIGINT,
    created_at       TIMESTAMP(6)  NOT NULL,
    updated_at       TIMESTAMP(6)  NOT NULL,
    CONSTRAINT fk_track_product FOREIGN KEY (product_id) REFERENCES products (product_id)
);

CREATE TABLE track_artist (
    track_id  UUID NOT NULL,
    artist_id UUID NOT NULL,
    PRIMARY KEY (track_id, artist_id),
    CONSTRAINT fk_track_artist_track  FOREIGN KEY (track_id)  REFERENCES tracks (track_id),
    CONSTRAINT fk_track_artist_artist FOREIGN KEY (artist_id) REFERENCES artists (artist_id)
);

CREATE TABLE outbox_events (
    outbox_event_id UUID         NOT NULL PRIMARY KEY,
    aggregate_id    UUID         NOT NULL,
    event_type      VARCHAR(255) NOT NULL,
    payload         TEXT         NOT NULL,
    status          VARCHAR(50)  NOT NULL,
    created_at      TIMESTAMP(6)  NOT NULL,
    sent_at         TIMESTAMP(6)
);

CREATE TABLE processed_events (
    event_id     UUID        NOT NULL PRIMARY KEY,
    processed_at TIMESTAMP(6) NOT NULL
);
