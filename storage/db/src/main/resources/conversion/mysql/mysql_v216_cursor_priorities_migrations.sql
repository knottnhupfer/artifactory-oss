CREATE TABLE node_event_cursor (
  operator_id       VARCHAR(255) NOT NULL,
  event_marker      BIGINT       NOT NULL,
  CONSTRAINT operator_id_pk PRIMARY KEY (operator_id)
);

CREATE TABLE node_event_priorities (
  priority_id      BIGINT        NOT NULL,
  path             VARCHAR(1024) NOT NULL,
  type             VARCHAR(1024) NOT NULL,
  operator_id      VARCHAR(255)  NOT NULL,
  priority         SMALLINT      NOT NULL,
  timestamp        BIGINT        NOT NULL,
  retry_count      SMALLINT      NOT NULL,
  CONSTRAINT priority_id_pk PRIMARY KEY (priority_id)
);

CREATE TABLE migration_status (
  identifier          VARCHAR(255)         NOT NULL,
  started             BIGINT               NOT NULL,
  finished            BIGINT               NOT NULL,
  migration_info_blob LONGBLOB,
  CONSTRAINT identifier_pk PRIMARY KEY (identifier)
);