update trusted_keys set alias = kid where alias is null;
ALTER TABLE trusted_keys MODIFY alias VARCHAR2(255) NOT NULL;
CREATE UNIQUE INDEX trusted_keys_alias ON trusted_keys (alias);
