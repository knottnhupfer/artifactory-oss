IF EXISTS (SELECT * FROM sys.indexes
WHERE name='binaries_sha256_idx' AND object_id = OBJECT_ID('binaries'))
  BEGIN
    DROP INDEX binaries_sha256_idx ON binaries
    ALTER TABLE binaries ALTER COLUMN sha256 CHAR(64) NOT NULL
    CREATE INDEX binaries_sha256_idx ON binaries (sha256)
  END
ELSE
  BEGIN
    ALTER TABLE binaries ALTER COLUMN sha256 CHAR(64) NOT NULL
  END;