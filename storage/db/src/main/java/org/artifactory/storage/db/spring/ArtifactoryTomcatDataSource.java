/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2018 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.artifactory.storage.db.spring;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.apache.tomcat.jdbc.pool.jmx.ConnectionPool;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.config.db.ArtifactoryDbProperties;
import org.artifactory.mbean.MBeanRegistrationService;
import org.jfrog.storage.DbType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.concurrent.TimeUnit;

/**
 * A pooling data source based on tomcat-jdbc library.
 *
 * @author Yossi Shaul
 */
public class ArtifactoryTomcatDataSource extends DataSource implements ArtifactoryDataSource {
    private static final Logger log = LoggerFactory.getLogger(ArtifactoryTomcatDataSource.class);

    private ArtifactoryTomcatDataSource() {
    }

    public ArtifactoryTomcatDataSource(ArtifactoryDbProperties s) {
        // see org.apache.tomcat.jdbc.pool.DataSourceFactory.parsePoolProperties()
        PoolProperties p = new PoolProperties();
        p.setUrl(s.getConnectionUrl());
        p.setDriverClassName(s.getDriverClass());
        p.setUsername(s.getUsername());
        p.setPassword(s.getPassword());
        // validation query for all kind of tests (connect, borrow etc.)
        p.setValidationQuery(s.getProperty("validationQuery", getDefaultValidationQuery(s)));
        p.setDefaultCatalog(s.getProperty("defaultCatalog", null));
        setCommonProperties(s, p);
    }

    void setCommonProperties(ArtifactoryDbProperties s, PoolProperties p) {
        p.setDefaultAutoCommit(s.getBooleanProperty("defaultAutoCommit", true));
        p.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

        p.setInitialSize(s.getIntProperty("initialSize", 1));
        p.setMaxAge(s.getIntProperty("maxAge", 0));
        p.setMaxActive(s.getMaxActiveConnections());
        p.setMaxWait(s.getIntProperty("maxWait", (int) TimeUnit.SECONDS.toMillis(120)));
        p.setMaxIdle(s.getMaxIdleConnections());
        p.setMinIdle(s.getIntProperty("minIdle", 1));
        p.setMinEvictableIdleTimeMillis(
                s.getIntProperty("minEvictableIdleTimeMillis", 300000));
        p.setTimeBetweenEvictionRunsMillis(
                s.getIntProperty("timeBetweenEvictionRunsMillis", 30000));
        p.setInitSQL(s.getProperty("initSQL", null));

        p.setValidationQueryTimeout(s.getIntProperty("validationQueryTimeout", 30));
        p.setValidationInterval(s.getLongProperty("validationInterval", 30000));
        p.setTestOnBorrow(s.getBooleanProperty("testOnBorrow", true));
        p.setTestWhileIdle(s.getBooleanProperty("testWhileIdle", false));
        p.setTestOnReturn(s.getBooleanProperty("testOnReturn", false));
        p.setTestOnConnect(s.getBooleanProperty("testOnConnect", false));

        p.setRemoveAbandoned(s.getBooleanProperty("removeAbandoned", false));
        p.setRemoveAbandonedTimeout(s.getIntProperty("removeAbandonedTimeout", 600));
        p.setSuspectTimeout(s.getIntProperty("suspectTimeout", 600));
        p.setLogAbandoned(s.getBooleanProperty("logAbandoned", false));
        p.setLogValidationErrors(s.getBooleanProperty("logValidationErrors", false));

        p.setJmxEnabled(s.getBooleanProperty("jmxEnabled", true));

        // only applicable if auto commit is false. has high performance penalty and only protects bugs in the code
        p.setRollbackOnReturn(s.getBooleanProperty("rollbackOnReturn", false));
        p.setCommitOnReturn(s.getBooleanProperty("commitOnReturn", false));

        p.setIgnoreExceptionOnPreLoad(s.getBooleanProperty("ignoreExceptionOnPreLoad", false));

        //p.setJdbcInterceptors(s.getProperty("jdbcInterceptors", "ConnectionState;StatementFinalizer"));
        p.setJdbcInterceptors(s.getProperty("jdbcInterceptors", "ConnectionState"));

        setPoolProperties(p);
    }

    public static DataSource createUniqueIdDataSource(ArtifactoryDbProperties s) {
        // see org.apache.tomcat.jdbc.pool.DataSourceFactory.parsePoolProperties()
        PoolProperties p = new PoolProperties();
        p.setUrl(s.getConnectionUrl());
        p.setDriverClassName(s.getDriverClass());
        p.setUsername(s.getUsername());
        p.setPassword(s.getPassword());

        // auto commit is true for the unique id generator
        p.setDefaultAutoCommit(true);
        p.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

        // only one connection is required for the id generator
        p.setInitialSize(0);
        p.setMinIdle(0);
        p.setMaxIdle(1);
        p.setMaxActive(1);
        // Make sure old idle connections are sweep and tested
        p.setTestWhileIdle(true);
        p.setTestOnBorrow(true);
        p.setTestWhileIdle(true);
        p.setRemoveAbandoned(true);
        p.setRemoveAbandonedTimeout((int) ConstantValues.locksTimeoutSecs.getLong()/2);
        p.setSuspectTimeout((int) ConstantValues.locksTimeoutSecs.getLong()/2);
        p.setLogAbandoned(true);
        p.setLogValidationErrors(true);

        // Timeout default to make sure new connection is created
        long timeoutInMillis = TimeUnit.SECONDS.toMillis(ConstantValues.locksTimeoutSecs.getLong());
        p.setMaxAge(timeoutInMillis);
        p.setMaxWait((int) timeoutInMillis);
        // Defaults values are good
        //p.setMinEvictableIdleTimeMillis(60000);
        //p.setTimeBetweenEvictionRunsMillis(5000);

        // Pool sweeper critical here since connection rarely used
        if (!p.isPoolSweeperEnabled()) {
            log.error("ID Generator pool connection should sweep idled connections");
        }

        // validation query for all kind of tests (connect, borrow etc.)
        p.setInitSQL(s.getProperty("initSQL", null));
        p.setValidationQuery(s.getProperty("validationQuery", getDefaultValidationQuery(s)));
        p.setValidationQueryTimeout(s.getIntProperty("validationQueryTimeout", 30));
        p.setValidationInterval(s.getLongProperty("validationInterval", 30000));

        p.setJmxEnabled(false);

        p.setIgnoreExceptionOnPreLoad(s.getBooleanProperty("ignoreExceptionOnPreLoad", false));

        //p.setJdbcInterceptors(s.getProperty("jdbcInterceptors", "ConnectionState;StatementFinalizer"));
        p.setJdbcInterceptors(s.getProperty("jdbcInterceptors", "ConnectionState"));

        p.setDefaultCatalog(s.getProperty("defaultCatalog", null));

        return new DataSource(p);
    }

    public static ArtifactoryTomcatDataSource createDatasourceForLockingOnExternalDb(ArtifactoryDbProperties props){
        ArtifactoryTomcatDataSource dataSource = new ArtifactoryTomcatDataSource();
        // see org.apache.tomcat.jdbc.pool.DataSourceFactory.parsePoolProperties()
        PoolProperties p = new PoolProperties();
        p.setUrl(props.getLockingDbConnectionUrl());
        p.setDriverClassName(props.getLockingDbDriverClass());
        p.setUsername(props.getLockingDbUsername());
        p.setPassword(props.getLockingDbPassword());
        // validation query for all kind of tests (connect, borrow etc.)
        p.setValidationQuery(props.getProperty("lockingdb.validationQuery", getDefaultValidationQueryForExternalDbLocking(props)));
        p.setDefaultCatalog(props.getProperty("lockingdb.defaultCatalog", null));
        dataSource.setCommonProperties(props, p);
        return dataSource;
    }

    private static String getDefaultValidationQuery(ArtifactoryDbProperties s) {
        return getDefaultValidationQuery(s.getDbType());
    }

    private static String getDefaultValidationQueryForExternalDbLocking(ArtifactoryDbProperties s) {
        if (s.getLockingDbSpecificType().isPresent()) {
            return getDefaultValidationQuery(s.getLockingDbSpecificType().get());
        }
        throw new RuntimeException("Another DB was configured for locking but no db type parameter found");
    }

    private static String getDefaultValidationQuery(DbType dbType) {
        switch (dbType) {
            case DERBY:
                return "values(1)";
            case MYSQL:
                // special MySQL lightweight ping query (not supported by the MariaDB connector to-date!)
                return "/* ping */";
            case ORACLE:
                return "SELECT 1 FROM DUAL";
            default:
                return "SELECT 1";
        }
    }

    @Override
    public void registerMBeans(MBeanRegistrationService mbeansService) {
        if (isJmxEnabled()) {
            ConnectionPool jmxPool = getPool().getJmxPool();
            mbeansService.register(jmxPool, "Storage", "Connection Pool");
        }
    }

    @Override
    public int getActiveConnectionsCount() {
        return super.getActive();
    }

    @Override
    public int getIdleConnectionsCount() {
        return super.getIdle();
    }

    @Override
    public void reset() {
        int currentIdleTime = this.getPoolProperties().getMinEvictableIdleTimeMillis();
        this.getPoolProperties().setMinEvictableIdleTimeMillis(0);
        this.purge();
        this.getPoolProperties().setMinEvictableIdleTimeMillis(currentIdleTime);
    }

    @Override
    public void close() {
        close(true);    // close all connections, including active ones
    }
}
