DROP TABLE configs;

CREATE TABLE configs (
  config_name   VARCHAR(255)   NOT NULL,
  last_modified BIGINT         NOT NULL,
  data          VARBINARY(MAX) NOT NULL,
  CONSTRAINT configs_pk PRIMARY KEY (config_name)
);