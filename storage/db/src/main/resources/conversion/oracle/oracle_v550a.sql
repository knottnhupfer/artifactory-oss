ALTER TABLE nodes ADD (
  sha256 CHAR(64),
  repo_path_checksum CHAR(40)
);