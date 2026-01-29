CREATE TABLE contents_stats
(
    id             bigint  NOT NULL,
    contents_id    BIGINT  NOT NULL,
    rating_count   bigint  NOT NULL DEFAULT 0,
    rating_sum     bigint  NOT NULL DEFAULT 0,
    rating_average decimal NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);

CREATE TABLE playlists_stats
(
    id              bigint NOT NULL,
    playlist_id     BIGINT NOT NULL,
    subscribe_count BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);

ALTER TABLE contents_stats
    ADD CONSTRAINT FK_contents_TO_contents_stats
        FOREIGN KEY (contents_id)
            REFERENCES contents (id);

ALTER TABLE playlists_stats
    ADD CONSTRAINT FK_playlists_TO_playlists_stats
        FOREIGN KEY (playlist_id)
            REFERENCES playlists (id);
