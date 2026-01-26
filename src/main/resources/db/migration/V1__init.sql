/* =========================
   users
========================= */
CREATE TABLE users (
                       id         BIGINT PRIMARY KEY,
                       uuid       uuid      NOT NULL,
                       email      varchar   NOT NULL,
                       password   varchar   NOT NULL,
                       locked     boolean,
                       created_at timestamp NOT NULL,
                       updated_at timestamp,
                       CONSTRAINT uk_users_uuid UNIQUE (uuid),
                       CONSTRAINT uk_users_email UNIQUE (email)
);

/* =========================
   profiles (1:1 users)
========================= */
CREATE TABLE profiles (
                          id                BIGINT PRIMARY KEY,
                          uuid              uuid      NOT NULL,
                          user_id           BIGINT    NOT NULL,
                          profile_image_url varchar,
                          name              varchar   NOT NULL,
                          created_at        timestamp NOT NULL,
                          updated_at        timestamp,
                          CONSTRAINT uk_profiles_uuid UNIQUE (uuid),
                          CONSTRAINT uk_profiles_user UNIQUE (user_id),
                          CONSTRAINT fk_profiles_user
                              FOREIGN KEY (user_id) REFERENCES users (id)
);

/* =========================
   roles / user_roles
========================= */
CREATE TABLE roles (
                       id         BIGINT PRIMARY KEY,
                       uuid       uuid      NOT NULL,
                       name       varchar   NOT NULL,
                       is_admin   boolean   NOT NULL,
                       created_at timestamp NOT NULL,
                       updated_at timestamp,
                       CONSTRAINT uk_roles_uuid UNIQUE (uuid),
                       CONSTRAINT uk_roles_name UNIQUE (name)
);

CREATE TABLE user_roles (
                            id         BIGINT PRIMARY KEY,
                            user_id    BIGINT    NOT NULL,
                            role_id    BIGINT    NOT NULL,
                            created_at timestamp NOT NULL,
                            CONSTRAINT uk_user_roles UNIQUE (user_id, role_id),
                            CONSTRAINT fk_user_roles_user
                                FOREIGN KEY (user_id) REFERENCES users (id),
                            CONSTRAINT fk_user_roles_role
                                FOREIGN KEY (role_id) REFERENCES roles (id)
);

/* =========================
   contents
========================= */
CREATE TABLE contents (
                          id            BIGINT PRIMARY KEY,
                          uuid          uuid      NOT NULL,
                          type          varchar   NOT NULL,
                          title         varchar   NOT NULL,
                          description   varchar   NOT NULL,
                          thumbnail_url varchar,
                          created_at    timestamp NOT NULL,
                          updated_at    timestamp,
                          CONSTRAINT uk_contents_uuid UNIQUE (uuid)
);

/* =========================
   tags
========================= */
CREATE TABLE tags (
                      id         BIGINT PRIMARY KEY,
                      uuid       uuid      NOT NULL,
                      name       varchar   NOT NULL,
                      created_at timestamp NOT NULL,
                      updated_at timestamp,
                      CONSTRAINT uk_tags_uuid UNIQUE (uuid),
                      CONSTRAINT uk_tags_name UNIQUE (name)
);

/* =========================
   content_tags (N:M)
========================= */
CREATE TABLE content_tags (
                              id         BIGINT PRIMARY KEY,
                              content_id BIGINT    NOT NULL,
                              tag_id     BIGINT    NOT NULL,
                              created_at timestamp NOT NULL,
                              CONSTRAINT uk_content_tags UNIQUE (content_id, tag_id),
                              CONSTRAINT fk_content_tags_content
                                  FOREIGN KEY (content_id) REFERENCES contents (id),
                              CONSTRAINT fk_content_tags_tag
                                  FOREIGN KEY (tag_id) REFERENCES tags (id)
);

/* =========================
   playlists
========================= */
CREATE TABLE playlists (
                           id          BIGINT PRIMARY KEY,
                           uuid        uuid      NOT NULL,
                           title       varchar   NOT NULL,
                           user_id     BIGINT    NOT NULL,
                           description text      NOT NULL,
                           created_at  timestamp NOT NULL,
                           updated_at  timestamp,
                           CONSTRAINT uk_playlists_uuid UNIQUE (uuid),
                           CONSTRAINT fk_playlists_user
                               FOREIGN KEY (user_id) REFERENCES users (id)
);

/* =========================
   playlist_contents (N:M)
========================= */
CREATE TABLE playlist_contents (
                                   id          BIGINT PRIMARY KEY,
                                   content_id  BIGINT    NOT NULL,
                                   playlist_id BIGINT    NOT NULL,
                                   created_at  timestamp NOT NULL,
                                   CONSTRAINT uk_playlist_contents UNIQUE (content_id, playlist_id),
                                   CONSTRAINT fk_playlist_contents_content
                                       FOREIGN KEY (content_id) REFERENCES contents (id),
                                   CONSTRAINT fk_playlist_contents_playlist
                                       FOREIGN KEY (playlist_id) REFERENCES playlists (id)
);

/* =========================
   subscribes
========================= */
CREATE TABLE subscribes (
                            id          BIGINT PRIMARY KEY,
                            uuid        uuid      NOT NULL,
                            user_id     BIGINT    NOT NULL,
                            playlist_id BIGINT    NOT NULL,
                            created_at  timestamp NOT NULL,
                            CONSTRAINT uk_subscribes UNIQUE (user_id, playlist_id),
                            CONSTRAINT fk_subscribes_user
                                FOREIGN KEY (user_id) REFERENCES users (id),
                            CONSTRAINT fk_subscribes_playlist
                                FOREIGN KEY (playlist_id) REFERENCES playlists (id)
);

/* =========================
   follows (self N:M)
========================= */
CREATE TABLE follows (
                         id          BIGINT PRIMARY KEY,
                         uuid        uuid      NOT NULL,
                         follower_id BIGINT    NOT NULL,
                         followee_id BIGINT    NOT NULL,
                         created_at  timestamp NOT NULL,
                         CONSTRAINT uk_follows UNIQUE (follower_id, followee_id),
                         CONSTRAINT fk_follows_follower
                             FOREIGN KEY (follower_id) REFERENCES users (id),
                         CONSTRAINT fk_follows_followee
                             FOREIGN KEY (followee_id) REFERENCES users (id)
);

/* =========================
   reviews
========================= */
CREATE TABLE reviews (
                         id         BIGINT PRIMARY KEY,
                         uuid       uuid      NOT NULL,
                         user_id    BIGINT    NOT NULL,
                         content_id BIGINT    NOT NULL,
                         rating     double precision NOT NULL,
                         text       text      NOT NULL,
                         created_at timestamp NOT NULL,
                         updated_at timestamp,
                         CONSTRAINT uk_reviews_uuid UNIQUE (uuid),
                         CONSTRAINT uk_reviews UNIQUE (user_id, content_id),
                         CONSTRAINT fk_reviews_user
                             FOREIGN KEY (user_id) REFERENCES users (id),
                         CONSTRAINT fk_reviews_content
                             FOREIGN KEY (content_id) REFERENCES contents (id)
);

/* =========================
   direct conversations / messages
========================= */
CREATE TABLE direct_conversations (
                                      id         BIGINT PRIMARY KEY,
                                      uuid       uuid      NOT NULL,
                                      creator_id BIGINT    NOT NULL,
                                      join_id    BIGINT    NOT NULL,
                                      created_at timestamp NOT NULL,
                                      CONSTRAINT uk_direct_conversations_uuid UNIQUE (uuid)
);

CREATE TABLE direct_messages (
                                 id              BIGINT PRIMARY KEY,
                                 uuid            uuid      NOT NULL,
                                 sender_id       BIGINT    NOT NULL,
                                 receiver_id     BIGINT    NOT NULL,
                                 conversation_id BIGINT    NOT NULL,
                                 content         text      NOT NULL,
                                 read_status     boolean   NOT NULL,
                                 created_at      timestamp NOT NULL,
                                 CONSTRAINT uk_direct_messages_uuid UNIQUE (uuid),
                                 CONSTRAINT fk_direct_messages_conversation
                                     FOREIGN KEY (conversation_id)
                                         REFERENCES direct_conversations (id)
);

/* =========================
   temporary_password
========================= */
CREATE TABLE temporary_password (
                                    id         BIGINT PRIMARY KEY,
                                    user_id    BIGINT    NOT NULL,
                                    password   varchar   NOT NULL,
                                    created_at timestamp NOT NULL,
                                    CONSTRAINT fk_temporary_password_user
                                        FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE BATCH_JOB_INSTANCE  (
                                     JOB_INSTANCE_ID BIGINT  NOT NULL PRIMARY KEY ,
                                     VERSION BIGINT ,
                                     JOB_NAME VARCHAR(100) NOT NULL,
                                     JOB_KEY VARCHAR(32) NOT NULL,
                                     constraint JOB_INST_UN unique (JOB_NAME, JOB_KEY)
) ;

CREATE TABLE BATCH_JOB_EXECUTION  (
                                      JOB_EXECUTION_ID BIGINT  NOT NULL PRIMARY KEY ,
                                      VERSION BIGINT  ,
                                      JOB_INSTANCE_ID BIGINT NOT NULL,
                                      CREATE_TIME TIMESTAMP NOT NULL,
                                      START_TIME TIMESTAMP DEFAULT NULL ,
                                      END_TIME TIMESTAMP DEFAULT NULL ,
                                      STATUS VARCHAR(10) ,
                                      EXIT_CODE VARCHAR(2500) ,
                                      EXIT_MESSAGE VARCHAR(2500) ,
                                      LAST_UPDATED TIMESTAMP,
                                      constraint JOB_INST_EXEC_FK foreign key (JOB_INSTANCE_ID)
                                          references BATCH_JOB_INSTANCE(JOB_INSTANCE_ID)
) ;

CREATE TABLE BATCH_JOB_EXECUTION_PARAMS  (
                                             JOB_EXECUTION_ID BIGINT NOT NULL ,
                                             PARAMETER_NAME VARCHAR(100) NOT NULL ,
                                             PARAMETER_TYPE VARCHAR(100) NOT NULL ,
                                             PARAMETER_VALUE VARCHAR(2500) ,
                                             IDENTIFYING CHAR(1) NOT NULL ,
                                             constraint JOB_EXEC_PARAMS_FK foreign key (JOB_EXECUTION_ID)
                                                 references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ;

CREATE TABLE BATCH_STEP_EXECUTION  (
                                       STEP_EXECUTION_ID BIGINT  NOT NULL PRIMARY KEY ,
                                       VERSION BIGINT NOT NULL,
                                       STEP_NAME VARCHAR(100) NOT NULL,
                                       JOB_EXECUTION_ID BIGINT NOT NULL,
                                       CREATE_TIME TIMESTAMP NOT NULL,
                                       START_TIME TIMESTAMP DEFAULT NULL ,
                                       END_TIME TIMESTAMP DEFAULT NULL ,
                                       STATUS VARCHAR(10) ,
                                       COMMIT_COUNT BIGINT ,
                                       READ_COUNT BIGINT ,
                                       FILTER_COUNT BIGINT ,
                                       WRITE_COUNT BIGINT ,
                                       READ_SKIP_COUNT BIGINT ,
                                       WRITE_SKIP_COUNT BIGINT ,
                                       PROCESS_SKIP_COUNT BIGINT ,
                                       ROLLBACK_COUNT BIGINT ,
                                       EXIT_CODE VARCHAR(2500) ,
                                       EXIT_MESSAGE VARCHAR(2500) ,
                                       LAST_UPDATED TIMESTAMP,
                                       constraint JOB_EXEC_STEP_FK foreign key (JOB_EXECUTION_ID)
                                           references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ;

CREATE TABLE BATCH_STEP_EXECUTION_CONTEXT  (
                                               STEP_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
                                               SHORT_CONTEXT VARCHAR(2500) NOT NULL,
                                               SERIALIZED_CONTEXT TEXT ,
                                               constraint STEP_EXEC_CTX_FK foreign key (STEP_EXECUTION_ID)
                                                   references BATCH_STEP_EXECUTION(STEP_EXECUTION_ID)
) ;

CREATE TABLE BATCH_JOB_EXECUTION_CONTEXT  (
                                              JOB_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
                                              SHORT_CONTEXT VARCHAR(2500) NOT NULL,
                                              SERIALIZED_CONTEXT TEXT ,
                                              constraint JOB_EXEC_CTX_FK foreign key (JOB_EXECUTION_ID)
                                                  references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ;

CREATE SEQUENCE BATCH_STEP_EXECUTION_SEQ MAXVALUE 9223372036854775807 NO CYCLE;
CREATE SEQUENCE BATCH_JOB_EXECUTION_SEQ MAXVALUE 9223372036854775807 NO CYCLE;
CREATE SEQUENCE BATCH_JOB_SEQ MAXVALUE 9223372036854775807 NO CYCLE;