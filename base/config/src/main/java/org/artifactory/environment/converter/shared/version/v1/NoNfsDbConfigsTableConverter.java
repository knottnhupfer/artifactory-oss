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

package org.artifactory.environment.converter.shared.version.v1;

import org.apache.commons.io.FileUtils;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.environment.converter.shared.ConfigInfo;
import org.artifactory.version.CompoundVersionDetails;
import org.jfrog.common.ResourceUtils;
import org.jfrog.config.db.TemporaryDBChannel;
import org.jfrog.storage.util.DbStatementUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.artifactory.environment.converter.shared.DbConfigsTableConverterHelper.*;
import static org.jfrog.storage.util.DbUtils.getDbTypeNameForSqlResources;

/**
 * @author gidis
 */
public class NoNfsDbConfigsTableConverter extends NoNfsBasicEnvironmentConverter {
    private static final Logger log = LoggerFactory.getLogger(NoNfsDbConfigsTableConverter.class);

    private final static String MAP_FILE_NAME = "configName_to_fileName.map";

    private TemporaryDBChannel dbChannel;
    private File conversionTempDir;

    @Override
    public boolean isInterested(ArtifactoryHome home, CompoundVersionDetails source, CompoundVersionDetails target) {
        return isUpgradeTo5x(source, target);
    }

    @Override
    protected void doConvert(ArtifactoryHome artifactoryHome, File clusterHomeDir) {
        this.conversionTempDir = new File(artifactoryHome.getDataDir(), "db_conversion_temp");
        try {
            deleteBackups(conversionTempDir); // Just in case
            FileUtils.forceMkdir(conversionTempDir);
            dbChannel = setupDbChannel(artifactoryHome);
            if (!shouldRun()) {
                return;
            }
            log.info("Starting configs table v5.0 schema conversion");
            List<ConfigInfo> configs = getExistingConfigsFromDb(dbChannel);
            createFilesystemBackups(configs);
            runDbConversion();
            copyConfigsBackToDB(dbChannel, configs);
            deleteBackups(conversionTempDir);
            log.info("Finished configs table v5.0 schema conversion");
        } catch (Exception e) {
            throw new RuntimeException("Couldn't convert configs table: " + e.getMessage(), e);
        } finally {
            if (dbChannel != null) {
                dbChannel.close();
            }
        }
    }

    @Override
    protected void doAssertConversionPreconditions(ArtifactoryHome artifactoryHome, File clusterHomeDir) {
        // no preconditions
    }

    /**
     * This conversion should run if:
     * 1. config table exists
     * 2. last_modified column does not exist in configs table
     */
    private boolean shouldRun() {
        try (ResultSet resultSet = dbChannel.executeSelect("SELECT * FROM configs")) {
            try {
                log.debug("configs table exists in db, testing for existence of last_modified column");
                resultSet.findColumn("last_modified");
            } catch (SQLException sqe) {
                log.debug("last_modified column does not exist in configs table, conversion will run.");
                return true;
            }
        } catch (Exception e) {
            //Table does not exist, this is a new installation and the table will be created during service conversion
            //Sync events are collected in the special db channel and are released once db is available.
            log.debug("configs table does not exist in db, conversion will not run.");
            return false;
        }
        log.debug("last_modified column already exists in configs table, conversion will not run.");
        return false;
    }

    /**
     * Backups are kept only in the event the db conversion failed, due to the fact that the DB can hold configs that
     * names that conflict with OS-specific pathname restrictions a map file is also kept to map
     * actual_config_name -> temp_filename.
     */
    private void createFilesystemBackups(List<ConfigInfo> configs) throws IOException {
        String[] list = conversionTempDir.list();
        if (conversionTempDir.exists() && list != null && list.length > 0) {
            String tempBackupDirName = conversionTempDir.getAbsolutePath() + "." + System.currentTimeMillis();
            log.warn("Found existing backup dir for config files {}. Renaming it to {}",
                    conversionTempDir.getAbsolutePath(), tempBackupDirName);
            // If we fail on the move let the exception propagate, don't want to overwrite any backups...
            FileUtils.moveDirectory(conversionTempDir, new File(tempBackupDirName));
        }
        File mapFile = new File(conversionTempDir, MAP_FILE_NAME);
        writeBackupsToFilesystem(configs, conversionTempDir, mapFile);
    }

    private void runDbConversion() throws IOException, SQLException {
        String dbTypeName = getDbTypeNameForSqlResources(dbChannel.getDbType());
        String resourcePath = "/conversion/" + dbTypeName + "/" + dbTypeName + "_v500a.sql";
        InputStream resource = ResourceUtils.getResource(resourcePath);
        if (resource == null) {
            throw new IOException("Database DDL resource not found at: '" + resourcePath + "'");
        }
        Connection connection = dbChannel.getConnection();
        DbStatementUtils.executeSqlStream(connection, resource);
    }
}
