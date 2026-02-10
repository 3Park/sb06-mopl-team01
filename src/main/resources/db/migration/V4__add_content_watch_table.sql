CREATE TABLE contents_watching_count (
                                         id         BIGINT NOT NULL ,
                                         content_id BIGINT NOT NULL,
                                         watcher_count BIGINT NOT NULL,
                                         updated_at TIMESTAMP
);

ALTER TABLE contents_watching_count
    ADD CONSTRAINT fk_contents_watching_count_content
        FOREIGN KEY (content_id) REFERENCES contents (id);
