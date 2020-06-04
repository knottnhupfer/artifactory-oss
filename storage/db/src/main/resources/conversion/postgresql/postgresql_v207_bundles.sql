CREATE TABLE artifact_bundles (
  id           BIGINT        NOT NULL,
  name         VARCHAR(255)  NOT NULL,
  version      VARCHAR(30)   NOT NULL,
  status       VARCHAR(64)   NOT NULL,
  date_created BIGINT          NOT NULL,
  signature    VARCHAR(1024) NOT NULL,
  CONSTRAINT bundle_pk PRIMARY KEY (id)
);
CREATE INDEX name_version_status_idx ON artifact_bundles (name,version, status);
CREATE UNIQUE INDEX name_version_idx ON artifact_bundles (name, version);

CREATE TABLE bundle_files (
  id                    BIGINT       NOT NULL,
  node_id               BIGINT       NOT NULL,
  bundle_id             BIGINT       NOT NULL,
  repo_path             VARCHAR(1090) NOT NULL,
  CONSTRAINT node_id_nodes_fk FOREIGN KEY (node_id) REFERENCES nodes (node_id),
  CONSTRAINT bundle_id_bundle_files_fk FOREIGN KEY (bundle_id) REFERENCES artifact_bundles (id),
  CONSTRAINT bundle_node_pk PRIMARY KEY (id)
);

CREATE TABLE bundle_blobs (
  id        BIGINT NOT NULL,
  data      BYTEA   NOT NULL,
  bundle_id BIGINT NOT NULL,
  CONSTRAINT bundle_blobs_pk PRIMARY KEY (id),
  CONSTRAINT bundle_id_bundle_blobs_fk FOREIGN KEY (bundle_id) REFERENCES artifact_bundles (id)
);
CREATE UNIQUE INDEX bundle_id_idx on bundle_blobs (bundle_id);

CREATE TABLE trusted_keys (
  kid         VARCHAR(6)    NOT NULL,
  trusted_key VARCHAR(4000) NOT NULL,
  fingerprint VARCHAR(255)  NOT NULL,
  alias       VARCHAR(255),
  issued      BIGINT,
  issued_by   VARCHAR(255),
  expiry      BIGINT,
  CONSTRAINT trusted_keys_pk PRIMARY KEY (kid)
);
