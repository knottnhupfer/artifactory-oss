CREATE TABLE jobs (
  job_id               NUMBER(19, 0)      NOT NULL,
  job_type             VARCHAR(32)        NOT NULL,
  job_status           VARCHAR(32)        NOT NULL,
  started              NUMBER(19, 0)      NOT NULL,
  finished             NUMBER(19, 0),
  additional_details   VARCHAR(2048),
  CONSTRAINT jobs_pk PRIMARY KEY (job_id)
);
CREATE INDEX jobs_index ON jobs (started, finished, job_status, job_type);