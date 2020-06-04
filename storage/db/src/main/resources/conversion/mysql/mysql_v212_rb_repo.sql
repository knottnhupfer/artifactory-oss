ALTER TABLE artifact_bundles ADD type VARCHAR(32) NOT NULL DEFAULT 'TARGET';
ALTER TABLE artifact_bundles ADD storing_repo VARCHAR(64);
ALTER TABLE artifact_bundles DROP INDEX name_version_idx;
CREATE UNIQUE INDEX name_version_idx ON artifact_bundles (name, version, type);