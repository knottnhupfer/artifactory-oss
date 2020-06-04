CREATE TABLE node_events (
  event_id   BIGINT        NOT NULL,
  timestamp  BIGINT        NOT NULL,
  event_type SMALLINT      NOT NULL,
  path       VARCHAR(1344) NOT NULL,
  CONSTRAINT node_events_pk PRIMARY KEY (event_id)
);
CREATE INDEX node_events_full_idx
  ON node_events (timestamp, event_id, event_type, path);
