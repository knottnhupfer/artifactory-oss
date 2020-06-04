CREATE TABLE jobs (
  job_id               BIGINT      NOT NULL,
  job_type             VARCHAR(32) NOT NULL,
  job_status           VARCHAR(32) NOT NULL,
  started              BIGINT      NOT NULL,
  finished             BIGINT,
  additional_details   VARCHAR(2048),
  CONSTRAINT jobs_pk PRIMARY KEY (job_id)
);
CREATE INDEX jobs_index ON jobs (started, finished, job_status, job_type);