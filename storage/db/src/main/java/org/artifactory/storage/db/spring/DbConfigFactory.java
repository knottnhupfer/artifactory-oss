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

import com.codahale.metrics.MetricRegistry;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.config.db.ArtifactoryDbProperties;
import org.artifactory.storage.db.util.JdbcHelper;
import org.artifactory.storage.db.util.querybuilder.ArtifactorySqlServerQueryBuilder;
import org.jfrog.storage.DbType;
import org.jfrog.storage.util.querybuilder.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.util.StringUtils;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * A Spring {@link org.springframework.context.annotation.Configuration} to initialized database beans.
 *
 * @author Yossi Shaul
 */
@Configuration
public class DbConfigFactory implements BeanFactoryAware {

    public static final String BEAN_PREFIX = "bean:";
    public static final String JNDI_PREFIX = "jndi:";
    private BeanFactory beanFactory;

    @Bean(name = "dataSource")
    public DataSource createDataSource() {
        ArtifactoryDbProperties dbProperties = beanFactory.getBean("dbProperties", ArtifactoryDbProperties.class);
        DataSource result = getDataSourceFromBeanOrJndi(dbProperties, "");
        if (result != null) {
            return result;
        } else {
            return getDataSource(dbProperties);
        }
    }

    private DataSource getDataSourceFromBeanOrJndi(ArtifactoryDbProperties dbProperties, String suffix) {
        DataSource result = null;
        String connectionUrl = dbProperties.getConnectionUrl();
        if (StringUtils.startsWithIgnoreCase(connectionUrl, BEAN_PREFIX)) {
            result = beanFactory.getBean(connectionUrl.substring(BEAN_PREFIX.length()) + suffix, DataSource.class);
        } else if (StringUtils.startsWithIgnoreCase(connectionUrl, JNDI_PREFIX)) {
            String jndiName = connectionUrl.substring(JNDI_PREFIX.length());
            JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
            jndiObjectFactoryBean.setJndiName(jndiName + suffix);
            try {
                jndiObjectFactoryBean.afterPropertiesSet();
            } catch (NamingException e) {
                throw new RuntimeException(e);
            }
            result = (DataSource) jndiObjectFactoryBean.getObject();
        }
        return result;
    }

    /**
     * Returns a separate non-transactional auto-commit datasource. This data source is currently used only by the id
     * generator.
     *
     * @return An auto-commit non-transactional datasource.
     */
    @Bean(name = "uniqueIdsDataSource")
    public DataSource createUniqueIdsDataSource() {
        ArtifactoryDbProperties dbProperties = beanFactory.getBean(ArtifactoryDbProperties.class);
        DataSource result = getDataSourceFromBeanOrJndi(dbProperties, "noTX");
        if (result != null) {
            return result;
        } else {
            String poolType = dbProperties.getProperty(ArtifactoryDbProperties.Key.poolType);
            if (poolType.equalsIgnoreCase("hikari")) {
                return ArtifactoryHikariDataSource.createUniqueIdDataSource(dbProperties);
            } else {
                return ArtifactoryTomcatDataSource.createUniqueIdDataSource(dbProperties);
            }
        }
    }

    /**
     * Returns a separate non-transactional auto-commit datasource. This data source is currently used only by HA locking.
     *
     * @return An auto-commit non-transactional datasource.
     */
    @Bean(name = "uniqueLockDataSource")
    public DataSource createUniqueLockDataSource() {
        ArtifactoryDbProperties dbProperties = beanFactory.getBean(ArtifactoryDbProperties.class);
        DataSource result = getDataSourceFromBeanOrJndi(dbProperties, "locks");
        if (result != null) {
            return result;
        } else {
            return dbProperties.getLockingDbSpecificType().isPresent() ?
                    getDifferentDatasourceForDbLock(dbProperties) : getDataSource(dbProperties);
        }
    }

    private DataSource getDataSource(ArtifactoryDbProperties dbProperties) {
        String poolType = dbProperties.getProperty(ArtifactoryDbProperties.Key.poolType);
        DataSource dataSource;
        if (poolType.equalsIgnoreCase("hikari")) {
            dataSource = new ArtifactoryHikariDataSource(dbProperties);
        } else {
            dataSource = new ArtifactoryTomcatDataSource(dbProperties);
        }
        return dataSource;
    }

    private DataSource getDifferentDatasourceForDbLock(ArtifactoryDbProperties dbProperties) {
        return ArtifactoryTomcatDataSource.createDatasourceForLockingOnExternalDb(dbProperties);
    }


    @Bean(name = "dbProperties")
    public ArtifactoryDbProperties getDbProperties() {
        ArtifactoryHome artifactoryHome = ContextHelper.get().getArtifactoryHome();
        ArtifactoryDbProperties dbProperties = artifactoryHome.getDBProperties();
        // first for loading of the driver class. automatic registration doesn't work on some Tomcat installations
        String driver = dbProperties.getDriverClass();
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load JDBC driver '" + driver + "'", e);
        }
        return dbProperties;
    }

    @Bean
    public MetricRegistry getMetricRegistry() {
        return new MetricRegistry();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    /**
     * create  a query builder instance per db type
     *
     * @return query builder instance
     */
    @Bean(name = "queryBuilder", autowire = Autowire.BY_TYPE)
    public IQueryBuilder createSqlBuilder() throws SQLException {
        JdbcHelper jdbcHelper = beanFactory.getBean(JdbcHelper.class);
        ArtifactoryDbProperties dbProperties = beanFactory.getBean(ArtifactoryDbProperties.class);
        DbType dbType = dbProperties.getDbType();
        Connection connection = jdbcHelper.getDataSource().getConnection();
        connection.close();
        IQueryBuilder queryBuilder;
        switch (dbType) {
            case ORACLE:
                queryBuilder = new OracleQueryBuilder();
                break;
            case MSSQL:
                queryBuilder = new ArtifactorySqlServerQueryBuilder();
                break;
            case DERBY:
                queryBuilder = new DerbyQueryBuilder();
                break;
            case POSTGRESQL:
                queryBuilder = new PostgresqlQueryBuilder();
                break;
            case MYSQL:
                queryBuilder = new MysqlQueryBuilder();
                break;
            case MARIADB:
                // MariaDB is treated as MySql
                queryBuilder = new MysqlQueryBuilder();
                break;
            default:
                queryBuilder = new DerbyQueryBuilder();
                break;
        }
        return queryBuilder;
    }
}
