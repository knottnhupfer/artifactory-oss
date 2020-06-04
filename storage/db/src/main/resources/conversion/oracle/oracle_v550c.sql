CREATE TABLE distributed_locks (
  category       VARCHAR(64)    NOT NULL,
  lock_key       VARCHAR(255)   NOT NULL,
  owner          VARCHAR(64)    NOT NULL,
  owner_thread   NUMBER(19, 0)  NOT NULL,
  owner_thread_name     VARCHAR(64)   NOT NULL,
  acquire_time   NUMBER(19, 0)  NOT NULL,
  CONSTRAINT locks_pk PRIMARY KEY (category,lock_key)
);
CREATE INDEX distributed_locks_owner ON distributed_locks (owner);
CREATE INDEX distributed_locks_owner_thread ON distributed_locks (owner_thread);