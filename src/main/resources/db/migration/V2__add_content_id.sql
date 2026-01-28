ALTER TABLE contents ADD COLUMN external_id VARCHAR(255);
ALTER TABLE contents
    ADD CONSTRAINT unique_external_id UNIQUE (external_id);