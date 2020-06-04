DROP TABLE configs;

CREATE TABLE configs (
  config_name    VARCHAR2(255)   NOT NULL,
  last_modified  NUMBER(19, 0),
  data           BLOB            NOT NULL,
  CONSTRAINT configs_pk PRIMARY KEY (config_name)
);