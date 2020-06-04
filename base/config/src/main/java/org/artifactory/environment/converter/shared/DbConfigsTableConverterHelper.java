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

package org.artifactory.environment.converter.shared;

import com.google.common.collect.Lists;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.config.db.ArtifactoryDbProperties;
import org.jfrog.config.DbChannel;
import org.jfrog.config.DbTimestampHelper;
import org.jfrog.config.db.CommonDbProperties;
import org.jfrog.config.db.TemporaryDBChannel;
import org.jfrog.storage.wrapper.BlobWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Shay Bagants
 * @author Dan Feldman
 */
public class DbConfigsTableConverterHelper {
    private static final Logger log = LoggerFactory.getLogger(DbConfigsTableConverterHelper.class);

    //Illegal path characters - covers all platforms (albeit very restrictive) but we map actual config names so its ok.
    private final static String ILLEGAL_PATH_CHARACTERS = "[^\\w.-]";

    public static void writeBackupsToFilesystem(List<ConfigInfo> configs, File tempDir, File mapFile) throws IOException {
        for (ConfigInfo config : configs) {
            File configFile = new File(tempDir, config.name.replaceAll(ILLEGAL_PATH_CHARACTERS, "_"));
            Files.createFile(configFile.toPath());
            try (FileOutputStream outputStream = new FileOutputStream(configFile)) {
                outputStream.write(config.data);
            } catch (IOException ioe) {
                log.error("Failed to create a filesystem backup for config " + config.name + "from the DB", ioe);
                throw ioe;
            }
            writeBackupMappingFile(mapFile, configFile, config);
        }
    }

    private static void writeBackupMappingFile(File mapFile, File configFile, ConfigInfo config) {
        try (FileWriter mapFileWriter = new FileWriter(mapFile, true)) {
            mapFileWriter.write(config.name + " -> " + configFile.getAbsolutePath() + "\n");
        } catch (Exception e) {
            // Don't stop the conversion for failing to write the map file, put the mapping in the log instead.
            String err = "Can't write db configs temp map file: ";
            log.debug(err, e);
            log.warn(err + "{}. temp file for config {} was created under file {}", e.getMessage(),
                    config.name, configFile.getAbsolutePath());
        }
    }

    public static List<ConfigInfo> getExistingConfigsFromDb(DbChannel dbChannel) throws SQLException, IOException {
        List<ConfigInfo> configs = Lists.newArrayList();
        try (ResultSet resultSet = dbChannel.executeSelect("SELECT * FROM configs")) {
            while (resultSet.next()) {
                String name = resultSet.getString("config_name");
                InputStream dbBlobStream = null;
                InputStream bufferedBinaryStream = null;
                try {
                    dbBlobStream = resultSet.getBinaryStream("data");
                    bufferedBinaryStream = org.apache.commons.io.IOUtils.toBufferedInputStream(dbBlobStream);
                    byte[] data = IOUtils.toByteArray(bufferedBinaryStream);
                    configs.add(new ConfigInfo(name, data));
                } finally {
                    IOUtils.closeQuietly(dbBlobStream);
                    IOUtils.closeQuietly(bufferedBinaryStream);
                }
            }
        }
        return configs;
    }

    public static void copyConfigsBackToDB(DbChannel dbChannel, List<ConfigInfo> configs) {
        String err = "Failed to upload config %s into DB";
        for (ConfigInfo config : configs) {
            int result;
            String configErr = String.format(err, config.name);
            try (ByteArrayInputStream blobStream = new ByteArrayInputStream(config.data)) {
                BlobWrapper blobWrapper = new BlobWrapper(blobStream, config.data.length);
                result = dbChannel.executeUpdate("INSERT INTO configs " +
                        "(config_name, last_modified, data) " +
                        "values(?,?,?)", config.name, DbTimestampHelper.getAdjustedCurrentTimestamp(dbChannel), blobWrapper);
                if (result != 1) {
                    log.error(configErr);
                    throw new RuntimeException(configErr);
                }
            } catch (Exception e) {
                log.error(configErr, e);
                throw new RuntimeException(configErr, e);
            }
        }
    }

    public static TemporaryDBChannel setupDbChannel(ArtifactoryHome artifactoryHome) {
        ArtifactoryDbProperties dbProperties = new ArtifactoryDbProperties(artifactoryHome);
        String password = dbProperties.getPassword();
        return new TemporaryDBChannel(new CommonDbProperties(password, dbProperties.getConnectionUrl(),
                dbProperties.getUsername(), dbProperties.getDbType(), dbProperties.getDriverClass()));
    }

    public static void deleteBackups(File tempDir) {
        log.info("Clearing backup folder at {}", tempDir.getAbsolutePath());
        try {
            FileUtils.deleteDirectory(tempDir);
        } catch (IOException e) {
            log.error("Unable to clean temp backup dir at {} : {}", tempDir.getAbsolutePath(), e.getMessage());
            log.debug(e.getMessage(), e);
        }
    }
}
