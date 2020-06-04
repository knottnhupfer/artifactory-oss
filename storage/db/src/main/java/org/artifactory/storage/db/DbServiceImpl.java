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

package org.artifactory.storage.db;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.config.db.ArtifactoryDbProperties;
import org.artifactory.common.storage.db.properties.DbVersionInfo;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.mbean.MBeanRegistrationService;
import org.artifactory.spring.Reloadable;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.db.fs.dao.NodesDao;
import org.artifactory.storage.db.mbean.ManagedDataSource;
import org.artifactory.storage.db.mbean.NewDbInstallation;
import org.artifactory.storage.db.properties.service.ArtifactoryDbPropertiesService;
import org.artifactory.storage.db.spring.ArtifactoryDataSource;
import org.artifactory.storage.db.util.IdGenerator;
import org.artifactory.storage.db.util.JdbcHelper;
import org.artifactory.storage.db.validators.DBSchemeCollationValidatorFactory;
import org.artifactory.storage.db.version.ArtifactoryDBVersion;
import org.artifactory.storage.db.version.converter.DbSqlConverterUtil;
import org.artifactory.version.CompoundVersionDetails;
import org.jfrog.common.ResourceUtils;
import org.jfrog.common.config.diff.DataDiff;
import org.jfrog.storage.DbType;
import org.jfrog.storage.priviledges.DBPrivilegesVerifierFactory;
import org.jfrog.storage.util.DbStatementUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import static org.jfrog.storage.util.DbUtils.*;

/**
 * @author Yossi Shaul
 */
@Repository
@Reloadable(beanClass = DbService.class)
public class DbServiceImpl implements InternalDbService {
    private static final Logger log = LoggerFactory.getLogger(DbServiceImpl.class);

    private static final double MYSQL_MIN_VERSION = 5.5;

    //I am the god of legacy code, gaze upon me and despair :(
    private boolean sha256Ready = false;
    private boolean uniqueRepoPathChecksumReady = false;
    long waitBetweenInitialConnectionRetry = 2000;

    @Autowired
    private JdbcHelper jdbcHelper;

    @Autowired
    @Qualifier("dbProperties")
    private ArtifactoryDbProperties dbProperties;

    @Autowired
    @Qualifier("uniqueLockDataSource")
    private DataSource lockDatasource;

    @Autowired
    private IdGenerator idGenerator;

    @Autowired
    private ArtifactoryDbPropertiesService dbPropertiesService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Override
    @PostConstruct
    public void initDb() throws Exception {
        try (Connection con = retryGetConnection()) {
            printConnectionInfo(con, dbProperties.getDbType());
            // check if db tables exist and initialize if not
            if (!isSchemaExist(con)) {
                // if using mySQL, check version compatibility
                if (dbProperties.getDbType() == DbType.MYSQL) {
                    checkMySqlMinVersion();
                }
                // read ddl from file and execute
                log.info("***Creating database schema***");
                DbStatementUtils.executeSqlStream(con, getDbSchemaSql());
                broadcastNewDbInstallation();
                updateDbProperties();
                //new schema means nothing to convert.
                sha256Ready = true;
                uniqueRepoPathChecksumReady = true;
                log.info("***Database schema created***");
            } else {
                if (dbPropertiesService.getDbVersionInfo() == null) {
                    broadcastNewDbInstallation();
                }
            }
            DBSchemeCollationValidatorFactory
                    .create(dbProperties, jdbcHelper).validate();
        }
        initDbLockSchemaIfNeeded();

        // initialize id generator
        initializeIdGenerator();
    }

    private void initDbLockSchemaIfNeeded() throws Exception {
        log.debug("Checking if additional database was provided for DB locking");
        if (dbProperties.getLockingDbSpecificType().isPresent()) {
            log.info("Additional database details found for DB locking");
            DbType dbType = dbProperties.getLockingDbSpecificType().get();
            try (Connection con = lockDatasource.getConnection()) {
                printConnectionInfo(con, dbType);
                if (!isAnotherDbLockSchemaExist(con, dbType)) {
                    log.info("***Creating DB lock database schema ***");
                    DbStatementUtils.executeSqlStream(con, getDbLockSchemaSql());
                }
            }
        }
    }

    private Connection retryGetConnection() throws SQLException {
        // 3 tries overall.
        for (int retriesLeft = 0; retriesLeft < 2; retriesLeft++) {
            try {
                return jdbcHelper.getDataSource().getConnection();
            } catch (SQLException e) {
                log.error("Failed to connect to database: ", e);
                log.warn("Retrying database connection in 2 seconds...");
            }

            // Sleep between retries
            try {
                Thread.sleep(waitBetweenInitialConnectionRetry);
            } catch (InterruptedException e) {
                log.warn("Sleep interrupted while getting database connection");
                log.debug("", e);
            }
        }
        // Final retry
        return jdbcHelper.getDataSource().getConnection();
    }

    private void broadcastNewDbInstallation() {
        publisher.publishEvent(new NewDbInstallation(getClass().getSimpleName()));
    }

    private void updateDbProperties() {
        // Update DBProperties
        long installTime = System.currentTimeMillis();
        CompoundVersionDetails versionDetails = ArtifactoryHome.get().getRunningArtifactoryVersion();
        String versionStr = versionDetails.getVersion().getVersion();
        long timestamp = versionDetails.getTimestamp();
        int revisionInt = (int) versionDetails.getRevision();
        dbPropertiesService.updateDbVersionInfo(new DbVersionInfo(installTime, versionStr, revisionInt, timestamp));
    }

    @Override
    public void init() {
        registerDataSourceMBean();
        verifySha256State();
        verifyUniqueRepoPathChecksumState();
    }

    @Override
    public DbType getDatabaseType() {
        return dbProperties.getDbType();
    }

    @Override
    public DbMetaData getDbMetaData() {
        try {
            return withMetadata(jdbcHelper, metaData -> new DbMetaData(metaData));
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }

    @Override
    public long nextId() {
        return idGenerator.nextId();
    }

    @Override
    public void compressDerbyDb(BasicStatusHolder statusHolder) {
        DerbyUtils.compress(statusHolder, dbProperties.getDbType());
    }

    @Override
    public <T> T invokeInTransaction(String transactionName, Callable<T> execute) {
        if (StringUtils.isNotBlank(transactionName)) {
            TransactionSynchronizationManager.setCurrentTransactionName(transactionName);
        }
        try {
            return execute.call();
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
    }

    //used via reflection by DbBaseTest
    private void initializeIdGenerator() throws SQLException {
        idGenerator.initializeIdGenerator();
    }

    private InputStream getDbLockSchemaSql() throws IOException {
        Optional<DbType> lockingSpecificDbType = dbProperties.getLockingDbSpecificType();
        if (lockingSpecificDbType.isPresent()) {
            String dbTypeName = DbSqlConverterUtil.getDbTypeNameForSqlResources(lockingSpecificDbType.get());
            // run the converter that creates the db locking table
            String resourcePath = "/conversion/" + dbTypeName + "/" + dbTypeName + "_v550c.sql";
            InputStream resource = ResourceUtils.getResource(resourcePath);
            if (resource == null) {
                throw new IOException("Database DDL resource not found at: '" + resourcePath + "'");
            }
            return resource;
        } else {
            throw new RuntimeException("Another DB was configured for locking but no db type found");
        }
    }

    private InputStream getDbSchemaSql() throws IOException {
        String dbTypeName = DbSqlConverterUtil.getDbTypeNameForSqlResources(dbProperties.getDbType());
        String resourcePath = "/" + dbTypeName + "/" + dbTypeName + ".sql";
        InputStream resource = ResourceUtils.getResource(resourcePath);
        if (resource == null) {
            throw new IOException("Database DDL resource not found at: '" + resourcePath + "'");
        }
        return resource;
    }

    private boolean isSchemaExist(Connection con) throws SQLException {
        log.debug("Checking for database schema existence");
        return tableExists(con.getMetaData(), dbProperties.getDbType(), NodesDao.TABLE_NAME);
    }

    private boolean isAnotherDbLockSchemaExist(Connection con, DbType dbType) throws SQLException {
        log.debug("Checking for specific DB lock database schema existence");
        return tableExists(con.getMetaData(), dbType, "distributed_locks");
    }

    private void printConnectionInfo(Connection con, DbType dbType) {
        try {
            DatabaseMetaData meta = con.getMetaData();
            log.info("Database: {} {}. Driver: {} {} Pool: {}", meta.getDatabaseProductName(),
                    meta.getDatabaseProductVersion(),
                    meta.getDriverName(), meta.getDriverVersion(), dbType);
            log.info("Connection URL: {}", meta.getURL());
        } catch (SQLException e) {
            log.warn("Can not retrieve database and driver name / version", e);
        }
    }

    private void registerDataSourceMBean() {
        DataSource dataSource = jdbcHelper.getDataSource();
        if (dataSource instanceof ArtifactoryDataSource) {
            ArtifactoryDataSource artifactoryDS = (ArtifactoryDataSource) dataSource;
            MBeanRegistrationService mbeansService = ContextHelper.get().beanForType(MBeanRegistrationService.class);
            mbeansService.register(new ManagedDataSource(artifactoryDS, jdbcHelper), "Storage", "Data Source");
            artifactoryDS.registerMBeans(mbeansService);
        }
    }

    private boolean checkMySqlMinVersion() {
        log.debug("Checking MySQL version compatibility");
        ResultSet rs = null;
        try {
            rs = jdbcHelper.executeSelect("SELECT VERSION();");
            if (rs.next()) {
                String versionString = rs.getString(1);
                int i = StringUtils.ordinalIndexOf(versionString, ".", 2);
                if (i == -1) {
                    i = versionString.length();
                }
                Double mysqlVersion = Double.valueOf(versionString.substring(0, i));
                if (mysqlVersion >= MYSQL_MIN_VERSION) {
                    return true;
                } else {
                    log.error("Unsupported MySQL version found [" + versionString + "]. " +
                            "Minimum version required is " + MYSQL_MIN_VERSION + ". " +
                            "Please follow the requirements on the wiki page.");
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("Could not determine MySQL version due to an exception", e);
        } finally {
            close(rs);
        }
        log.error("Could not determine MySQL version. Minimum version should be " + MYSQL_MIN_VERSION + " and above.");
        return false;
    }

    private void runEnforceDBPrivilegesConversion() {
        try {
            doWithConnection(jdbcHelper, this::enforceDBPrivileges);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void enforceDBPrivileges(Connection con) {
        File marker = ArtifactoryHome.get().getSkipVerifyPrivilegesMarkerFile();
        if (!marker.exists()) {
            try {
                if (!DBPrivilegesVerifierFactory.createDBPrivilegesVerifier(dbProperties.getDbType())
                        .isSufficientPrivileges(con, "artifactory")) {
                    log.error("Insufficient DB privileges found!. Not starting migration.");
                    throw new RuntimeException("Insufficient DB privileges found!");
                }
            } catch (Exception e) {
                log.error("Error while verifying DB privileges. Not starting migration.");
                throw new RuntimeException("Error while verifying DB privileges", e);
            }
        }
        marker.delete();
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
        runEnforceDBPrivilegesConversion();
        ArtifactoryDBVersion.convert(source.getVersion(), jdbcHelper, dbProperties.getDbType());
        updateDbProperties();
        verifySha256State();
        verifyUniqueRepoPathChecksumState();
    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor, List<DataDiff<?>> configDiff) {
        InternalDbService txme = ContextHelper.get().beanForType(InternalDbService.class);
        txme.verifyMigrations();
    }

    @Override
    public void destroy() {
        jdbcHelper.destroy();
    }

    /**
     * TO BE USED ONLY BY THE SHA256 MIGRATION JOB
     * Tests the db metadata for the sha256 column's (in binaries table) nullable state,
     * and sets the state's flag {@link #sha256Ready} accordingly
     * @return {@link #sha256Ready}
     */
    @Override
    public boolean verifySha256State() {
        if (sha256Ready) {
            //Already verified to be ok, no need to do it again
            return true;
        }
        try {
            doWithConnection(jdbcHelper, this::setSha256State);
        } catch (Exception e) {
            log.warn("Can't determine state of sha256 column in binaries table: {}", e.getMessage());
            log.debug("", e);
        }
        log.debug("Determined SHA256 readiness state to be: {}", sha256Ready);
        return sha256Ready;
    }

    private void setSha256State(Connection conn) throws SQLException {
        DbType dbType = dbProperties.getDbType();
        DatabaseMetaData  metadata = conn.getMetaData();
        try (ResultSet rs = metadata.getColumns(getActiveCatalog(conn, dbType), getActiveSchema(conn, dbType),
                normalizedName("binaries", metadata), normalizedName("sha256", metadata))){
            if (rs.next()) {
                sha256Ready = "NO".equalsIgnoreCase(rs.getString(normalizedName("IS_NULLABLE", metadata)));
            } else {
                log.warn("Can't determine state of sha256 column in binaries table, column not found in db metadata.");
            }
        } catch (Exception e) {
            log.warn("Can't determine state of sha256 column in binaries table: {}", e.getMessage());
            log.debug("", e);
        }
    }

    /**
     * TO BE USED ONLY BY THE REPO_PATH_CHECKSUM MIGRATION JOB
     * Tests the db metadata for the repoPathChecksum column's (in nodes table) unique index state,
     * and sets the state's flag {@link #uniqueRepoPathChecksumReady} accordingly
     * @return {@link #uniqueRepoPathChecksumReady}
     */
    @Override
    public boolean verifyUniqueRepoPathChecksumState() {
        if (uniqueRepoPathChecksumReady) {
            //Already verified to be ok, no need to do it again
            return true;
        }
        try {
            uniqueRepoPathChecksumReady = indexExists(jdbcHelper, "nodes",
                    "repo_path_checksum", "nodes_repo_path_checksum", dbProperties.getDbType());
        } catch (Exception e) {
            log.warn("Can't determine the uniqueness of 'repo_path_checksum' column column in nodes table: {}", e.getMessage());
            log.debug("", e);
        }
        if (uniqueRepoPathChecksumReady) {
            log.debug("Artifactory is running with full repo path checksum support.");
        } else {
            log.debug("Full repo path checksum support is not active yet.");
        }
        log.debug("Determined repoPathChecksum readiness state to be: {}", uniqueRepoPathChecksumReady);
        return uniqueRepoPathChecksumReady;
    }

    @Override
    public boolean isSha256Ready() {
        return sha256Ready;
    }

    @Override
    public boolean isUniqueRepoPathChecksumReady() {
        return uniqueRepoPathChecksumReady;
    }

    @Override
    public void verifyMigrations() {
        verifySha256State();
        verifyUniqueRepoPathChecksumState();
    }
}
