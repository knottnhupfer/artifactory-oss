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

package org.artifactory.storage.db.itest.spring;

import com.codahale.metrics.MetricRegistry;
import org.apache.commons.lang.StringUtils;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.config.db.ArtifactoryDbProperties;
import org.artifactory.common.property.LinkedProperties;
import org.artifactory.mbean.MBeanRegistrationService;
import org.artifactory.storage.db.spring.ArtifactoryHikariDataSource;
import org.artifactory.storage.db.spring.ArtifactoryTomcatDataSource;
import org.artifactory.storage.db.util.JdbcHelper;
import org.jfrog.common.ResourceUtils;
import org.jfrog.storage.DbType;
import org.jfrog.storage.util.querybuilder.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * A Spring {@link org.springframework.context.annotation.Configuration} to initialized database beans.
 *
 * @author Yossi Shaul
 */
@Configuration
public class DbTestConfigFactory implements BeanFactoryAware {

    public static final String DERBY = "derby";
    private BeanFactory beanFactory;

    @Bean(name = "dataSource")
    public DataSource createDataSource() {
        ArtifactoryDbProperties dbProperties = beanFactory.getBean("dbProperties", ArtifactoryDbProperties.class);
        String poolType = getPoolType(dbProperties);
        DataSource dataSource;
        if (poolType.equalsIgnoreCase("hikari")) {
            dataSource = new ArtifactoryHikariDataSource(dbProperties);
        } else {
            dataSource = new ArtifactoryTomcatDataSource(dbProperties);
        }
        return dataSource;
    }

    /**
     * @return Auto-commit datasource for the unique ids generator.
     * @see org.artifactory.storage.db.spring.DbConfigFactory#createUniqueIdsDataSource()
     */
    @Bean(name = "uniqueIdsDataSource")
    public DataSource createUniqueIdsDataSource() {
        ArtifactoryDbProperties dbProperties = beanFactory.getBean("dbProperties", ArtifactoryDbProperties.class);
        String poolType = getPoolType(dbProperties);
        if (poolType.equalsIgnoreCase("hikari")) {
            return ArtifactoryHikariDataSource.createUniqueIdDataSource(dbProperties);
        } else {
            return ArtifactoryTomcatDataSource.createUniqueIdDataSource(dbProperties);
        }
    }

    private String getPoolType(ArtifactoryDbProperties dbProperties) {
        String poolTypeSysProp = System.getProperty("artifactory.db.config.pool.type");
        if (StringUtils.isNotBlank(poolTypeSysProp)) {
            return poolTypeSysProp;
        } else {
            return dbProperties.getProperty(ArtifactoryDbProperties.Key.poolType);
        }
    }

    /**
     * Returns a separate non-transactional auto-commit datasource. This data source is currently used only by HA locking.
     *
     * @return An auto-commit non-transactional datasource.
     */
    @Bean(name = "uniqueLockDataSource")
    public DataSource createUniqueLockDataSource() {
        ArtifactoryDbProperties dbProperties = beanFactory.getBean("dbProperties", ArtifactoryDbProperties.class);
        ArtifactoryTomcatDataSource dataSource = new ArtifactoryTomcatDataSource(dbProperties);
        // the db tests has limited tx support (which is mostly controlled by the business logic layer) so we are using
        // auto commit
        dataSource.setDefaultAutoCommit(true);
        return dataSource;
    }

    @Bean(name = "dbProperties")
    public ArtifactoryDbProperties getDbProperties() throws IOException {
        File dbPropsFile;
        String generatedDbConfigLocation = getGeneratedDbConfigLocation();
        if (generatedDbConfigLocation != null) {
            dbPropsFile = new File(generatedDbConfigLocation);
        } else {
            String dbConfigName = System.getProperty("artifactory.db.config.name", DERBY);
            dbPropsFile = ResourceUtils.getResourceAsFile("/db/" + dbConfigName + ".properties");
        }

        Properties originalProps = new LinkedProperties();
        try (FileInputStream fis = new FileInputStream(dbPropsFile)) {
            originalProps.load(fis);
        }

        if (DERBY.equals(originalProps.getProperty("type"))) {

            File workDir = ArtifactoryHome.get().getDataDir().getAbsoluteFile();
            dbPropsFile = new File(workDir, ArtifactoryHome.DB_PROPS_FILE_NAME);
            System.setProperty("derby.stream.error.file", new File(workDir, "derby.log").getAbsolutePath());
            Files.createDirectories(workDir.toPath());
            File dbWorkDir = new File(workDir, DERBY).getAbsoluteFile();
            originalProps.setProperty("db.home", dbWorkDir.getAbsolutePath());
            try (FileOutputStream fos = new FileOutputStream(dbPropsFile)) {
                originalProps.store(fos, "Modified db.properties");
            }
            // Make sure that the dbProperties in Artifactory home contains the same data
            try (FileOutputStream fos = new FileOutputStream(ArtifactoryHome.get().getDBPropertiesFile())) {
                originalProps.store(fos, "Modified db.properties");
            }
        }
        return new ArtifactoryDbProperties(ArtifactoryHome.get(), dbPropsFile);
    }

    @Bean
    public MetricRegistry getMetricRegistry() {
        return new MetricRegistry();
    }

    @Bean
    public MBeanRegistrationService getMBeanRegistrationService() {
        return new MBeanRegistrationService(getMetricRegistry());
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    private String getGeneratedDbConfigLocation() {
        String generatedConfig = System.getProperty("artifactory.db.generated.config");
        // reset the value to make sure it's not propagated to other tests
        System.clearProperty("artifactory.db.generated.config");
        return generatedConfig;
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
                queryBuilder = new SqlServerQueryBuilder();
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
