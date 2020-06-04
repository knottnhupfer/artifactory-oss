DROP INDEX name_version_status_idx ;
CREATE INDEX name_ver_sta_date_typ_repo_idx ON artifact_bundles(name,version,status,date_created,type,storing_repo);