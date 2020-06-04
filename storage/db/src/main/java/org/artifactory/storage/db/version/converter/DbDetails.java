package org.artifactory.storage.db.version.converter;

import org.artifactory.storage.db.util.JdbcHelper;
import org.jfrog.storage.DbType;

/**
 * @author Yoaz Menda
 */
public class DbDetails {

    private DbType dbType;
    private JdbcHelper jdbcHelper;

    public DbDetails(DbType dbType, JdbcHelper jdbcHelper) {
        this.dbType = dbType;
        this.jdbcHelper = jdbcHelper;
    }

    public DbType getDbType() {
        return dbType;
    }

    public JdbcHelper getJdbcHelper() {
        return jdbcHelper;
    }
}
