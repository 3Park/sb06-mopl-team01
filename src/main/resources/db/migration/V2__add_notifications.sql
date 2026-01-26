CREATE TABLE notifications
(
    id          BIGINT    NOT NULL,
    uuid        UUID      NOT NULL,
    level       VARCHAR   NOT NULL,
    receiver_id BIGINT    NOT NULL,
    title       VARCHAR   NOT NULL,
    content     TEXT,
    created_at  TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT UK_notifications_uuid UNIQUE (uuid)
);

ALTER TABLE notifications
    ADD CONSTRAINT FK_users_TO_notifications
        FOREIGN KEY (receiver_id)
            REFERENCES users (id);