ALTER TABLE artifact_bundles ADD COLUMN type VARCHAR(32) NOT NULL DEFAULT 'TARGET';
ALTER TABLE artifact_bundles ADD COLUMN storing_repo VARCHAR(64);
DROP INDEX name_version_idx;
CREATE INDEX name_version_idx ON  artifact_bundles(name, version, type);