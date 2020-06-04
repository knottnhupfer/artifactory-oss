CREATE TABLE node_events (
  event_id   NUMBER(19, 0)        NOT NULL,
  timestamp  NUMBER(19, 0)        NOT NULL,
  event_type NUMBER(5, 0)  NOT NULL,
  path       VARCHAR(1344) NOT NULL,
  CONSTRAINT node_events_pk PRIMARY KEY (event_id)
);
CREATE INDEX node_events_full_idx
  ON node_events (timestamp, event_id, event_type, path);