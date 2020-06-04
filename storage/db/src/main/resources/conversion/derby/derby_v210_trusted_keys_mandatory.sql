update trusted_keys set alias = kid where alias is null;
ALTER TABLE trusted_keys ALTER COLUMN alias NOT NULL;
CREATE UNIQUE INDEX trusted_keys_alias ON trusted_keys (alias);
