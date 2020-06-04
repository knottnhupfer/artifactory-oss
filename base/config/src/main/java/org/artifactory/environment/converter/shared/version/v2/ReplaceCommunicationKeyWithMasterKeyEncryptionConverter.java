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

package org.artifactory.environment.converter.shared.version.v2;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.converter.ConverterPreconditionException;
import org.artifactory.environment.converter.BasicEnvironmentConverter;
import org.artifactory.environment.converter.shared.ConfigInfo;
import org.artifactory.environment.converter.shared.DbConfigsTableConverterHelper;
import org.artifactory.version.ArtifactoryVersionProvider;
import org.artifactory.version.CompoundVersionDetails;
import org.jfrog.config.db.TemporaryDBChannel;
import org.jfrog.security.crypto.EncryptionWrapper;
import org.jfrog.security.crypto.EncryptionWrapperFactory;
import org.jfrog.security.crypto.result.DecryptionStringResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.artifactory.common.ArtifactoryHome.ARTIFACTORY_CONFIG_FILE;
import static org.artifactory.environment.converter.shared.DbConfigsTableConverterHelper.*;
import static org.jfrog.storage.util.DbUtils.columnExists;

/**
 * Re-encrypts all config blobs in db configs table with the new master.key (available here since Access
 * already generated it and we're past the home init phases).
 *
 * @author Shay Bagants
 */
public class ReplaceCommunicationKeyWithMasterKeyEncryptionConverter implements BasicEnvironmentConverter {
    private static final Logger log = LoggerFactory.getLogger(ReplaceCommunicationKeyWithMasterKeyEncryptionConverter.class);

    private final static String MAP_FILE_NAME = "configName_to_fileName.map";

    private TemporaryDBChannel dbChannel;
    private File conversionTempDir;
    private ArtifactoryHome home;

    @Override
    public boolean isInterested(ArtifactoryHome home, CompoundVersionDetails source, CompoundVersionDetails target) {
        return source.getVersion().before(ArtifactoryVersionProvider.v570m001.get()) && ArtifactoryVersionProvider.v570m001.get().beforeOrEqual(target.getVersion());
    }

    @Override
    public void convert(ArtifactoryHome artifactoryHome, CompoundVersionDetails source, CompoundVersionDetails target) {
        doConvert(artifactoryHome);
    }

    @Override
    public void assertConversionPreconditions(ArtifactoryHome home) throws ConverterPreconditionException {
        //Test comm.key exists, readable and comm.key.old path is writable
        File commKey = home.getCommunicationKeyFile();
        File backup = getBackupFile(commKey);

        assertFileReadPermission(commKey);
        assertTargetFilePermissions(backup);
    }

    private void doConvert(ArtifactoryHome artifactoryHome) {
        this.home = artifactoryHome;
        this.conversionTempDir = new File(home.getDataDir(), "db_conversion_temp");
        try {
            deleteBackups(conversionTempDir); // Just in case
            FileUtils.forceMkdir(conversionTempDir);
            dbChannel = setupDbChannel(home);
            log.debug("Checking if conversion should run");
            if (!shouldRun()) {
                return;
            }
            log.info("Starting configs table data conversion");
            List<ConfigInfo> configs = getExistingConfigsFromDb(dbChannel);
            decryptBackupAndReset(configs);
            configs = reEncryptDataIfNeeded(configs);
            copyConfigsBackToDB(dbChannel, configs);
            moveCommunicationKeyAside();
            deleteBackups(conversionTempDir);
            log.info("Finished configs table v5.5.2 schema conversion");
        } catch (Exception e) {
            throw new RuntimeException("Couldn't convert configs encryption: " + e.getMessage(), e);
        } finally {
            if (dbChannel != null) {
                dbChannel.close();
            }
        }
    }

    /**
     * Backups are saved un-encrypted, just in case.
     * Here we decrypt, make the backup and attempt to clear the configs table (because we can't re-insert already
     * existing primary keys).
     * if the clear of db fails, we also attempt to re-insert the config.xml which is the only entity in the configs
     * table that's not reflected in the filesystem.
     */
    private void decryptBackupAndReset(List<ConfigInfo> configs) throws IOException, SQLException {
        List<ConfigInfo> decryptedConfigs = decryptConfigs(configs);
        createFilesystemBackups(decryptedConfigs);
        try {
            resetConfigsTable();
        } catch (SQLException sql) {
            //Upon failure we want to make sure the config descriptor is re-inserted into the db since everything else 
            //gets re-synced from the filesystem.
            log.error("Failed to clear configs table: ", sql);
            log.debug("Attempting to re-insert artifactory.config.xml into db");
            ConfigInfo configXmlInfo = decryptedConfigs.stream()
                    .filter(configInfo -> configInfo.name.equalsIgnoreCase(ARTIFACTORY_CONFIG_FILE))
                    .findFirst()
                    .orElse(null);
            if (configXmlInfo == null) {
                //I find it hard to believe this can happen, better safe then sorry though.
                log.warn("artifactory.config.xml was not retrieved from configs table, cannot re-insert");
                throw sql;
            }
            try {
                copyConfigsBackToDB(dbChannel, Lists.newArrayList(configXmlInfo));
            } catch (Exception e) {
                log.error("", e);
                throw sql;
            }
        }
    }

    private List<ConfigInfo> decryptConfigs(List<ConfigInfo> configs) {
        EncryptionWrapper communicationKeyWrapper = getCommunicationKeyWrapper();
        return configs.stream()
                .map(config -> {
                    log.debug("Checking if {} need to be decrypted", config.name);
                    //TODO [by shayb]: temporary fix. For the long run, every file in the config table should be encoded!
                    if (config.name.equals("keystore") || config.name.equals("access.creds")) {
                        return new ConfigInfo(config.name, config.data);
                    } else {
                        String resultedText;
                        String configDataString = new String(config.data, StandardCharsets.UTF_8);
                        if (communicationKeyWrapper.isEncodedByMe(configDataString)) {
                            try {
                                DecryptionStringResult result = communicationKeyWrapper.decryptIfNeeded(configDataString);
                                resultedText = result.getDecryptedData();
                            } catch (Exception e) {
                                // this might be encrypted with the arti.key, so it start with AM, but the communication key decryption will fail. This should not be decrypted in that case
                                if (config.name.endsWith("access.creds")) {
                                    return new ConfigInfo(config.name, config.data);
                                } else {
                                    throw e;
                                }
                            }
                        } else {
                            resultedText = configDataString;
                        }
                        return new ConfigInfo(config.name, resultedText.getBytes());
                    }
                }).collect(Collectors.toList());
    }

    /**
     * Deletes all rows from configs table, before re-inserting with new encryption
     */
    private void resetConfigsTable() throws SQLException {
        dbChannel.executeUpdate("DELETE from configs WHERE 1=1");
    }

    private void moveCommunicationKeyAside() {
        File communicationKeyFile = home.getCommunicationKeyFile();
        File backup = getBackupFile(communicationKeyFile);
        try {
            Files.move(communicationKeyFile.toPath(), backup.toPath());
        } catch (IOException e) {
            log.error("Could not move Communication key file. {}", e.getMessage());
            log.debug("Could not move Communication key file. ", e);
        }
    }

    private List<ConfigInfo> reEncryptDataIfNeeded(List<ConfigInfo> configs) {
        //Need to do the decrypt again to know what was encrypted (so we don't encrypt the non-encrypted configs)
        EncryptionWrapper communicationKeyWrapper = getCommunicationKeyWrapper();
        EncryptionWrapper masterEncryptionWrapper = home.getMasterEncryptionWrapper();
        return configs.stream()
                .map(config -> {
                    log.debug("Checking if {} need to be decrypted", config.name);
                    //TODO [by shayb]: temporary fix. For the long run, every file in the config table should be encoded!
                    if (config.name.equals("keystore")) {
                        return new ConfigInfo(config.name, config.data);
                    } else {
                        String resultedText;
                        String configDataString = new String(config.data, StandardCharsets.UTF_8);
                        if (communicationKeyWrapper.isEncodedByMe(configDataString)) {
                            String toEncrypt;
                            try {
                                DecryptionStringResult result = communicationKeyWrapper.decryptIfNeeded(configDataString);
                                toEncrypt = result.getDecryptedData();
                            } catch (Exception e) {
                                // access creds might be encrypted with the artifactory.key, so the communicationKey.isAlreadyEnvrypted will return true (because both arti and communication key encrypted data starts with "AM"), but on the decrypt method, it will fail because the communication key is the wrong key
                                if (config.name.endsWith("access.creds")) {
                                    toEncrypt = configDataString;
                                } else {
                                    throw e;
                                }
                            }
                            resultedText = masterEncryptionWrapper.encryptIfNeeded(toEncrypt);

                        } else {
                            resultedText = configDataString;
                        }
                        return new ConfigInfo(config.name, resultedText.getBytes());
                    }
                }).collect(Collectors.toList());
    }

    /**
     * Backups are kept only in the event the db conversion failed, due to the fact that the DB can hold configs that
     * names that conflict with OS-specific pathname restrictions a map file is also kept to map
     * actual_config_name -> temp_filename.
     */
    private void createFilesystemBackups(List<ConfigInfo> configs) throws IOException {
        if (conversionTempDir.exists() && !isEmpty(conversionTempDir.list())) {
            String tempBackupDirName = conversionTempDir.getAbsolutePath() + "." + System.currentTimeMillis();
            log.warn("Found existing backup dir for config files {}. Renaming it to {}",
                    conversionTempDir.getAbsolutePath(), tempBackupDirName);
            // If we fail on the move let the exception propagate, don't want to overwrite any backups...
            FileUtils.moveDirectory(conversionTempDir, new File(tempBackupDirName));
        }
        File mapFile = new File(conversionTempDir, MAP_FILE_NAME);
        DbConfigsTableConverterHelper.writeBackupsToFilesystem(configs, conversionTempDir, mapFile);
    }

    private EncryptionWrapper getCommunicationKeyWrapper() {
        try {
            //shouldRun() already verified existence of key file
            File communicationKey = home.getCommunicationKeyFile();
            return EncryptionWrapperFactory.createArtifactoryKeyWrapper(
                    communicationKey,
                    communicationKey.getParentFile(),
                    1,
                    pathname -> false); //no fallback for the comm.key
        } catch (Exception e) {
            String err = "Failed to resolve communication key.";
            log.error(err, e);
            throw new RuntimeException(err, e);
        }
    }

    private boolean shouldRun() {
        boolean colExists;
        boolean commKeyExists = home.getCommunicationKeyFile() != null && home.getCommunicationKeyFile().exists();
        try {
            colExists = columnExists(dbChannel.getConnection().getMetaData(), dbChannel.getDbType(), "configs", "last_modified");
        } catch (SQLException e) {
            String err = "Failed to check db schema for compatibility, cannot replace database encryption: ";
            log.error(err, e);
            throw new RuntimeException(err, e);
        }
        if (!commKeyExists) {
            log.debug("Conversion will not run: communication.key file not found in home location.");
        }
        if (!colExists) {
            log.debug("Conversion will not run: last_modified column does not exist in configs table.");
        }
        return commKeyExists && colExists;
    }

    private File getBackupFile(File communicationKeyFile) {
        return new File(communicationKeyFile.getAbsolutePath() + ".old");
    }
}
