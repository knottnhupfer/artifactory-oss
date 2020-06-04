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

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.zaxxer.hikari.HikariDataSource;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.config.db.ArtifactoryDbProperties;
import org.artifactory.mbean.MBeanRegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

/**
 * A pooling data source based on Hikari pool library.
 * See https://github.com/brettwooldridge/HikariCP for configuration details.
 *
 * @author Yossi Shaul
 */
public class ArtifactoryHikariDataSource extends HikariDataSource implements ArtifactoryDataSource {
    private static final Logger log = LoggerFactory.getLogger(ArtifactoryHikariDataSource.class);
    private ArtifactoryDbProperties p;

    public ArtifactoryHikariDataSource(ArtifactoryDbProperties p) {
        log.debug("Initializing Hikari pooling data source");
        this.p = p;

        setJdbcUrl(p.getConnectionUrl());
        setDriverClassName(p.getDriverClass());
        setUsername(p.getUsername());
        setPassword(p.getPassword());
        setPoolName("Artifactory HikariCP");

        setAutoCommit(p.getBooleanProperty("defaultAutoCommit", true));
        setTransactionIsolation("TRANSACTION_READ_COMMITTED");

        setMaximumPoolSize(p.getMaxActiveConnections());
        setMaxLifetime(p.getIntProperty("maxAge", 1800000));

        setMinimumIdle(p.getIntProperty("minIdle", 1));
        // minimum time before idle connection can be evicted (minimum is 10000)
        setIdleTimeout(p.getIntProperty("minEvictableIdleTimeMillis", 300000));

        // maximum time to get a connection from the pool
        long timeoutInMillis = TimeUnit.SECONDS.toMillis(ConstantValues.locksTimeoutSecs.getLong());
        setConnectionTimeout(p.getLongProperty("maxWait", timeoutInMillis));

        setConnectionInitSql(p.getProperty("initSQL", null));

        setConnectionTestQuery(p.getProperty("validationQuery", null)); // use jdbc4 isValid by default
        setValidationTimeout(p.getIntProperty("validationQueryTimeout", 5000));

        setLeakDetectionThreshold(p.getLongProperty("leakDetectionThreshold", 0));

        setAllowPoolSuspension(p.getBooleanProperty("allowPoolSuspension", true));

        setCatalog(p.getProperty("defaultCatalog", null));

        setInitializationFailTimeout(p.getLongProperty("initializationFailTimeout", 1));
        setIsolateInternalQueries(p.getBooleanProperty("isolateInternalQueries", false));

        //TODO: [by YS] these are mysql specific settings
        /*addDataSourceProperty("cachePrepStmts", "true");
        addDataSourceProperty("prepStmtCacheSize", "250");
        addDataSourceProperty("prepStmtCacheSqlLimit", "2048");*/
    }

    @Override
    public void registerMBeans(MBeanRegistrationService mbeansService) {
        if (p.getBooleanProperty("jmxEnabled", true)) {
            mbeansService.register(getHikariPoolMXBean(), "Storage", "Connection Pool");
            mbeansService.register(getHikariConfigMXBean(), "Storage", "Connection Pool Config");
            if (p.getBooleanProperty("metricsEnabled", false)) {
                setupMetrics(mbeansService);
            }
        }
    }

    private void setupMetrics(MBeanRegistrationService mbeansService) {
        MetricRegistry metricRegistry = mbeansService.getMetricRegistry();
        setMetricRegistry(metricRegistry);
        JmxReporter jmxReporter = JmxReporter.forRegistry(metricRegistry)
                .createsObjectNamesWith(
                        (type, domain, name) -> mbeansService.createObjectName("Metrics, name=JDBC", name))
                .filter((name, metric) -> name.contains("HikariCP"))
                .build();
        jmxReporter.start();
    }

    @Override
    public int getActiveConnectionsCount() {
        return getHikariPoolMXBean().getActiveConnections();
    }

    @Override
    public int getIdleConnectionsCount() {
        return getHikariPoolMXBean().getIdleConnections();
    }

    @Override
    public int getMaxActive() {
        return getHikariConfigMXBean().getMaximumPoolSize();
    }

    @Override
    public int getMaxIdle() {
        return -1;
    }

    @Override
    public int getMaxWait() {
        return (int) getConnectionTimeout();
    }

    @Override
    public int getMinIdle() {
        return getHikariConfigMXBean().getMinimumIdle();
    }

    @Override
    public String getUrl() {
        return p.getConnectionUrl();
    }

    @Override
    public void reset() {
        this.getHikariPoolMXBean().softEvictConnections();

    }

    public static DataSource createUniqueIdDataSource(ArtifactoryDbProperties p) {
        log.debug("Initializing Hikari pooling data source");

        HikariDataSource ds = new HikariDataSource();

        ds.setJdbcUrl(p.getConnectionUrl());
        ds.setDriverClassName(p.getDriverClass());
        ds.setUsername(p.getUsername());
        ds.setPassword(p.getPassword());
        ds.setPoolName("Artifactory Main Hikari JDBC Pool");

        // auto commit is true for the unique id generator
        ds.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        ds.setAutoCommit(true);

        ds.setMaximumPoolSize(1);
        ds.setMaxLifetime(p.getIntProperty("maxAge", 1800000));

        ds.setMinimumIdle(p.getIntProperty("minIdle", 0));
        // minimum time before idle connection can be evicted (minimum is 10000)
        ds.setIdleTimeout(p.getIntProperty("minEvictableIdleTimeMillis", 300000));

        // maximum time to get a connection from the pool
        long timeoutInMillis = TimeUnit.SECONDS.toMillis(ConstantValues.locksTimeoutSecs.getLong());
        ds.setConnectionTimeout(p.getLongProperty("maxWait", timeoutInMillis));

        ds.setConnectionInitSql(p.getProperty("initSQL", null));

        ds.setConnectionTestQuery(p.getProperty("validationQuery", null)); // use jdbc4 isValid by default
        ds.setValidationTimeout(p.getIntProperty("validationQueryTimeout", 1000));

        ds.setLeakDetectionThreshold(p.getLongProperty("leakDetectionThreshold", timeoutInMillis / 2));

        ds.setAllowPoolSuspension(p.getBooleanProperty("allowPoolSuspension", true));
        ds.setCatalog(p.getProperty("defaultCatalog", null));

        ds.setInitializationFailTimeout(p.getLongProperty("initializationFailTimeout", 1));
        ds.setIsolateInternalQueries(p.getBooleanProperty("isolateInternalQueries", false));

        return ds;
    }
}