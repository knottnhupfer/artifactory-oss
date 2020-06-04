CREATE TABLE replication_errors (
 error_id         NUMBER(19, 0)   NOT NULL,
 first_error_time NUMBER(19, 0)   NOT NULL,
 last_error_time  NUMBER(19, 0)   NOT NULL,
 error_count      NUMBER(5, 0)    NOT NULL,
 error_message    VARCHAR2(4000)  NOT NULL,
 replication_key  VARCHAR2(255)   NOT NULL,
 task_time        NUMBER(19, 0)   NOT NULL,
 task_type        NUMBER(5, 0)    NOT NULL,
 task_path        VARCHAR2(1344)  NOT NULL,
 CONSTRAINT replication_errors_pk PRIMARY KEY (error_id)
);
CREATE INDEX replication_errors_rep_key_idx ON replication_errors (replication_key);