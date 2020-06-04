CREATE TABLE node_event_cursor (
  operator_id       VARCHAR2(255)       NOT NULL,
  event_marker      NUMBER(19, 0)       NOT NULL,
  CONSTRAINT operator_id_pk PRIMARY KEY (operator_id)
);

CREATE TABLE node_event_priorities (
  priority_id      NUMBER(19, 0)        NOT NULL,
  path             VARCHAR2(1024)       NOT NULL,
  type             VARCHAR2(1024)       NOT NULL,
  operator_id      VARCHAR2(255)        NOT NULL,
  priority         NUMBER(19, 0)        NOT NULL,
  timestamp        NUMBER(19, 0)        NOT NULL,
  retry_count      NUMBER(1, 0)         NOT NULL,
  CONSTRAINT priority_id_pk PRIMARY KEY (priority_id)
);

CREATE TABLE migration_status (
  identifier          VARCHAR2(255)         NOT NULL,
  started             NUMBER(19, 0)         NOT NULL,
  finished            NUMBER(19, 0)         NOT NULL,
  migration_info_blob BLOB,
  CONSTRAINT identifier_pk PRIMARY KEY (identifier)
);