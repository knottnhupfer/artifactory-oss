ALTER TABLE artifact_bundles ADD type VARCHAR(32) DEFAULT 'TARGET' NOT NULL ;
ALTER TABLE artifact_bundles ADD storing_repo VARCHAR(64);
DROP INDEX name_version_idx;
CREATE UNIQUE INDEX name_version_idx ON artifact_bundles (name, version, type);