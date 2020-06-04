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

package org.artifactory.config;

import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.ha.HaNodeProperties;
import org.artifactory.storage.db.version.converter.DbSqlConverterUtil;
import org.jfrog.config.ConfigurationManager;
import org.jfrog.config.DbChannel;
import org.jfrog.config.LogChannel;
import org.jfrog.config.db.TemporaryDBChannel;
import org.jfrog.security.crypto.EncryptionWrapper;
import org.jfrog.security.masterkey.status.MasterKeyStatus;
import org.jfrog.storage.util.DbUtils;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import static org.jfrog.security.masterkey.status.MasterKeyStatus.MasterKeyStatusEnum.on;

/**
 * @author Shay Bagants
 */
public class MasterKeyBootstrapUtil {
    private static final String MASTER_KEY_TABLE_NAME = "master_key_status";
    private static final String MASTER_KEY_TABLE_CONVERSION = "v570a";
    private static final String TABLE_CREATION_ERR = "Failed to ensure db table '" + MASTER_KEY_TABLE_NAME + "' exists: ";
    private static final int TABLE_CREATION_TRIES = 10;

    private ArtifactoryHome artifactoryHome;
    private ConfigurationManager configurationManager;

    public MasterKeyBootstrapUtil(ArtifactoryHome artifactoryHome, ConfigurationManager configurationManager) {
        this.artifactoryHome = artifactoryHome;
        this.configurationManager = configurationManager;
    }

    public void handleMasterKey() {
        EncryptionWrapper masterKey = getMasterKey();
        String nodeId = getNodeId();
        MasterKeyStatus masterKeyStatus = new MasterKeyStatus(masterKey.getFingerprint(), on, nodeId, 0);
        validateOrInsertKeyInformation(masterKeyStatus, 1);
    }

    private String getNodeId() {
        HaNodeProperties haNodeProperties = artifactoryHome.getHaNodeProperties();
        String nodeId;
        if (haNodeProperties != null) {
            nodeId = haNodeProperties.getServerId();
        } else {
            nodeId = HaCommonAddon.ARTIFACTORY_PRO;
        }
        return nodeId;
    }

    /**
     * Validate the local key fingerprint against the DB. If another key exists, throw exception, if no entry exists,
     * insert the key data into the DB. If the same key exists, continue without doing anything
     */
    private void validateOrInsertKeyInformation(MasterKeyStatus masterKeyStatus, int retries) {
        tryToCreateTable();
        boolean keyInDb = isKeyInDb(masterKeyStatus.getKid());
        boolean success = false;
        if (!keyInDb) {
            try {
                int rows = dbChannel().executeUpdate("INSERT INTO " + MASTER_KEY_TABLE_NAME  +
                                " (is_unique_key, status, set_by_node_id, kid, expires) " +
                                "VALUES (?, ?, ?, ?, ?)",
                        1, masterKeyStatus.getStatus().name(), masterKeyStatus.getSetByNodeId(),
                        masterKeyStatus.getKid(), masterKeyStatus.getExpires());
                if (rows > 0) {
                    success = true;
                }
            } catch (SQLException e) {
                log().debug("Could not update master key information. " + e.getMessage());
                log().debug("Could not update master key information.", e);
            }
        }
        if (!keyInDb && !success) {
            if (retries > 0) {
                retries = retries - 1;
                validateOrInsertKeyInformation(masterKeyStatus, retries);
            } else {
                throw new RuntimeException("Could not validate master key data against the DB");
            }
        }
    }

    private void tryToCreateTable() {
        //Should be impossible to reach this point without the temp channel. If you did, well... blame yourself
        if (!(dbChannel() instanceof TemporaryDBChannel)) {
            log().debug("Expected " + TemporaryDBChannel.class.getName() + " db channel but got " + dbChannel().getClass().getName());
            throw new RuntimeException("Got unexpected db connection while starting up!");
        }
        try {
            createTableWithRetry(TABLE_CREATION_TRIES);
        } catch (Exception e) {
            log().error(TABLE_CREATION_ERR, e);
            throw new RuntimeException(TABLE_CREATION_ERR + e.getMessage(), e);
        }
    }

    private void createTableWithRetry(int retries) throws SQLException {
        //Connection is assigned as a field on the temp db channel, no extra op is made to get it.
        TemporaryDBChannel temporaryDBChannel = (TemporaryDBChannel) dbChannel();
        Connection conn = temporaryDBChannel.getConnection();
        if (!DbUtils.tableExists(conn.getMetaData(), temporaryDBChannel.getDbType(), MASTER_KEY_TABLE_NAME)) {
            try {
                DbSqlConverterUtil.convert(conn, dbChannel().getDbType(), MASTER_KEY_TABLE_CONVERSION);
            } catch (Exception e) {
                if (retries > 0) {
                    sleepSecond(3);
                    log().warn("Failed to create master.key file: " + e.getMessage());
                    log().debug("", e);
                    createTableWithRetry(retries - 1);
                } else {
                    throw new RuntimeException(TABLE_CREATION_ERR + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Check if a key with the specified fingerprint is registered in the DB for this instance/cluster. If it is, return
     * true, if no key exist, return false, if another fingerprint exists, throw RuntimeException and block Artiactory
     * from start.
     *
     * @param existingKeyFingerprint The fingerprint to search in the DB
     * @return true if the fingerprint exists, false if no key exists
     * @throws IllegalStateException if a key with another fingerprint exists in the DB
     */
    private boolean isKeyInDb(String existingKeyFingerprint) {
        boolean keyInDb = false;
        try (ResultSet resultSet = dbChannel().executeSelect("SELECT kid FROM " + MASTER_KEY_TABLE_NAME)) {
            while (resultSet.next()) {
                keyInDb = true;
                String dbKeyFingerprint = resultSet.getString("kid");
                if (!existingKeyFingerprint.equals(dbKeyFingerprint)) {
                    String schemaName = resultSet.getMetaData().getSchemaName(1);
                    String errorInfo = "Using DB schema: "+ schemaName +
                            ", db.properties file location: " + artifactoryHome.getDBPropertiesFile() +
                            ", Connection Url: " + artifactoryHome.getDBProperties().getConnectionUrl() + ", DB Master key" +
                            " fingerprint: " + dbKeyFingerprint + ", Local master key fingerprint: " + existingKeyFingerprint;
                    log().error("Master key checksum mismatch. Aborting initialization. " + errorInfo);

                    throw new IllegalStateException("Master key checksum mismatch");
                }
            }
        } catch (SQLException e) {
            log().error("An error occurred while validating master key against the DB. " + e.getMessage());
            log().debug("", e);
            throw new RuntimeException("An error occurred while validating master key against the DB", e);
        }
        return keyInDb;
    }

    /**
     * Wait 30 seconds for the master key file to be available at the Artifactory home or take it as a parameter
     */
    private EncryptionWrapper getMasterKey() {
        File masterKey = artifactoryHome.getMasterKeyFile();
        long timeoutMillis = ConstantValues.masterKeyWaitingTimeout.getLong();
        long startTime = System.currentTimeMillis();
        long now = startTime;
        boolean success = false;
        while (now - startTime < timeoutMillis) {
            try {
                log().debug("Checking if Master key file exists...");
                success = masterKey.exists() || StringUtils.isNotBlank(System.getProperty("jfrog.master.key"));
                if (success) {
                    log().info("Master key file found.");
                    break;
                }
            } catch (Exception e) {
                log().debug("Could not find key. " + e.toString());
                log().debug("", e);
            }
            log().info("master.key file currently missing - waiting for Access to create it." +
                    " Reattempting to check master.key file existence in 1 second.");
            sleepSecond(1);
            now = System.currentTimeMillis();
        }
        if (success) {
            return artifactoryHome.getMasterEncryptionWrapper();
        }

        log().error("master.key file is missing - timed out while waiting for master.key after "
                + TimeUnit.MILLISECONDS.toSeconds(timeoutMillis) + " seconds. Please provide it manually\n" +
                "Please check you are using the appropriate Access version, and that Access is up and running");
        throw new RuntimeException("master.key file is missing - timed out while waiting for master.key after "
                + TimeUnit.MILLISECONDS.toSeconds(timeoutMillis) + " seconds. Please provide it manually\n" +
                "Please check you are using the appropriate Access version, and that Access is up and running"
        );
    }

    private void sleepSecond(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            log().warn("Sleep interrupted while waiting for master.key");
            log().debug("", e);
        }
    }

    private LogChannel log() {
        return configurationManager.getLogChannel();
    }

    private DbChannel dbChannel() {
        return configurationManager.getDBChannel();
    }
}
