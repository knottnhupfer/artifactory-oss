CREATE TABLE replication_errors (
 error_id         BIGINT          NOT NULL,
 first_error_time BIGINT          NOT NULL,
 last_error_time  BIGINT          NOT NULL,
 error_count      SMALLINT        NOT NULL,
 error_message    VARCHAR(4000)   NOT NULL,
 replication_key  VARCHAR(255)    NOT NULL,
 task_time        BIGINT          NOT NULL,
 task_type        SMALLINT        NOT NULL,
 task_path        VARCHAR(1344)   NOT NULL,
 CONSTRAINT replication_errors_pk PRIMARY KEY (error_id)
);
CREATE INDEX replication_errors_rep_key_idx ON replication_errors (replication_key);