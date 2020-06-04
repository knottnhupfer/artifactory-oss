set @exist := (select count(*) from information_schema.statistics where table_name = 'nodes' and index_name = 'nodes_repo_path_checksum' and table_schema = database());
set @sqlstmt := if( @exist > 0, 'ALTER TABLE nodes DROP INDEX nodes_repo_path_checksum', 'select 1');
set @sqlstmt2 := if( @exist > 0, 'CREATE INDEX nodes_repo_path_checksum ON nodes (repo_path_checksum)', 'select 1');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
PREPARE stmt2 FROM @sqlstmt2;
EXECUTE stmt2;
