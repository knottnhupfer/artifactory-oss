ALTER TABLE artifact_bundles ADD type VARCHAR(32) NOT NULL DEFAULT 'TARGET';
ALTER TABLE artifact_bundles ADD storing_repo VARCHAR(64);
IF EXISTS (SELECT * FROM sys.indexes
WHERE name='name_version_idx' AND object_id = OBJECT_ID('artifact_bundles'))
  BEGIN
    DROP INDEX name_version_idx ON artifact_bundles
  END;

CREATE UNIQUE INDEX name_version_idx ON artifact_bundles (name, version, type);