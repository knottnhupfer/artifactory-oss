CREATE TABLE blob_infos (
  checksum CHAR(64) NOT NULL,
  blob_info LONGBLOB NOT NULL,
  CONSTRAINT blob_infos_checksum PRIMARY KEY (checksum)
);