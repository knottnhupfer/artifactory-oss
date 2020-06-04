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

package org.artifactory.logging.converter;

import org.apache.commons.io.FileUtils;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.property.FatalConversionException;
import org.artifactory.converter.ArtifactoryConverterAdapter;
import org.artifactory.converter.ConverterPreconditionException;
import org.artifactory.logging.version.LoggingVersion;
import org.artifactory.version.CompoundVersionDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.artifactory.environment.converter.BasicEnvironmentConverter.BACKUP_FILE_EXT;

/**
 * @author Gidi Shabat
 */
public class LoggingConverter implements ArtifactoryConverterAdapter {
    private static final Logger log = LoggerFactory.getLogger(LoggingConverter.class);

    private File path;
    private File loggingFile;
    private File loggingBackupFile;

    public LoggingConverter(File path) {
        this.path = path;
        loggingFile = new File(path, ArtifactoryHome.LOGBACK_CONFIG_FILE_NAME);
        loggingBackupFile = new File(path, ArtifactoryHome.LOGBACK_CONFIG_FILE_NAME + BACKUP_FILE_EXT);
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
        try {
            // Perform the logback conversion here because if we do it after configuration is loaded, we must wait 'till
            // the changes are detected by the watchdog (possibly missing out on important log messages)
            //Might be first run, protect
            if (path.exists()) {
                LoggingVersion.convert(source.getVersion(), path);
            }
        } catch (FatalConversionException e) {
            //When a fatal conversion happens fail the context loading
            log.error("Conversion failed with fatal status.\n" + "You should analyze the error and retry launching " +
                    "Artifactory. Error is: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            //When conversion fails - report and continue - don't fail
            log.error("Failed to execute logging conversion.", e);
        }
    }

    @Override
    public boolean isInterested(CompoundVersionDetails source, CompoundVersionDetails target) {
        return source != null && !source.isCurrent();
    }

    @Override
    public void backup() {
        try {
            if (loggingBackupFile.exists()) {
                FileUtils.forceDelete(loggingBackupFile);
            }
            if (loggingFile.exists()) {
                FileUtils.copyFile(loggingFile, loggingBackupFile);
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to save backup file '" + loggingBackupFile.getAbsolutePath() + "' from the login file: '"
                            + loggingFile.getAbsolutePath() + "'", e);
        }
    }

    @Override
    public void clean() {
        File loginBackupFile = new File(path, ArtifactoryHome.LOGBACK_CONFIG_FILE_NAME + BACKUP_FILE_EXT);
        try {
            if (loginBackupFile.exists()) {
                FileUtils.forceDelete(loginBackupFile);
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to clean backup file '" + loginBackupFile.getAbsolutePath()
                            + "' after successful conversion'", e);
        }
    }

    @Override
    public void assertConversionPrecondition(ArtifactoryHome home, CompoundVersionDetails fromVersion, CompoundVersionDetails toVersion) throws ConverterPreconditionException {
        assertTargetFilePermissions(loggingFile);
        assertTargetFilePermissions(loggingBackupFile);
    }

    private void assertTargetFilePermissions(File targetFile) {
        if (targetFile.exists() && !targetFile.canWrite()) {
            throw new ConverterPreconditionException("File " + targetFile.getName() +
                    " doesn't have the permissions required for Artifactory 5 upgrade.");
        }
    }

    @Override
    public void revert() {
        try {
            if (loggingBackupFile.exists()) {
                if (loggingFile.exists()) {
                    FileUtils.forceDelete(loggingFile);
                }
                FileUtils.moveFile(loggingBackupFile, loggingFile);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to revert conversion", e);
        }
    }
}

