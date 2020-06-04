package org.artifactory.storage.db.validators;

public interface CollationQuery {
    String MYSQL = "/validators/mysql_collation_validator.sql";
    String MARIA_DB = "/validators/mysql_collation_validator.sql";
    String MSSQL = "/validators/mssql_collation_validator.sql";
    String ORACLE = "/validators/oracle_collation_validator.sql";
}
