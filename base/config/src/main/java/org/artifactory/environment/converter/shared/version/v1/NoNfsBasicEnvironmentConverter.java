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
import org.artifactory.common.ha.HaNodeProperties;
import org.artifactory.converter.ConverterPreconditionException;
import org.artifactory.environment.converter.BasicEnvironmentConverter;
import org.artifactory.version.ArtifactoryVersionProvider;
import org.artifactory.version.CompoundVersionDetails;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Gidi Shabat
 */
public abstract class NoNfsBasicEnvironmentConverter implements BasicEnvironmentConverter {

    public static File resolveClusterHomeDir(ArtifactoryHome artifactoryHome) {
        File haNodePropertiesFile = artifactoryHome.getArtifactoryHaNodePropertiesFile();
        if (haNodePropertiesFile.exists()) {
            Properties haNodeProperties;
            try {
                haNodeProperties = new Properties();
                haNodeProperties.load(new FileInputStream(haNodePropertiesFile));
            } catch (Exception e) {
                throw new RuntimeException("Failed to load HA node properties from file: " + haNodePropertiesFile.getAbsolutePath());
            }
            String clusterHomePath = (String) haNodeProperties.get("cluster.home");
            if (clusterHomePath == null) {
                // Return null assuming that NoNfs conversion has beans successfully finished or no conversion needed
                return null;
            }
            File clusterHomeDir = new File(clusterHomePath);
            if (!clusterHomeDir.exists()) {
                throw new RuntimeException("Couldn't locate the cluster home dir, expecting it to be in: " +
                        clusterHomeDir.getAbsolutePath());
            }
            return clusterHomeDir;
        }
        return null;
    }

    public static File resolveClusterDataDir(ArtifactoryHome artifactoryHome) {
        File haNodePropertiesFile = artifactoryHome.getArtifactoryHaNodePropertiesFile();
        if (haNodePropertiesFile.exists()) {
            Properties haNodeProperties;
            try {
                haNodeProperties = new Properties();
                haNodeProperties.load(new FileInputStream(haNodePropertiesFile));
            } catch (Exception e) {
                throw new RuntimeException("Failed to load HA node properties from file: "
                        + haNodePropertiesFile.getAbsolutePath());
            }
            String clusterDataDir = (String) haNodeProperties.get(HaNodeProperties.PROP_HA_DATA_DIR);
            if (clusterDataDir != null) {
                return new File(clusterDataDir);
            }
            // Old cluster home property
            String clusterHomePath = (String) haNodeProperties.get("cluster.home");
            if (clusterHomePath == null) {
                // Return null assuming that NoNfs conversion has beans successfully finished or no conversion needed
                return null;
            }
            File clusterHomeDir = new File(clusterHomePath);
            if (!clusterHomeDir.exists()) {
                throw new RuntimeException("Couldn't locate the cluster home dir, expecting it to be in: " +
                        clusterHomeDir.getAbsolutePath());
            }
            return new File(clusterHomeDir, "ha-data");
        }
        return null;
    }

    public static void saveFileOrDirectoryAsBackup(File file) throws IOException {
        if (file.exists()) {
            String backupItemName = file.getName() + BACKUP_FILE_EXT;
            File backupItem = new File(file.getParentFile(), backupItemName);
            if (backupItem.exists()) {
                // Rename backup file so that we would be able to use its name
                String newBackupItemName = findBackupItemName(file);
                File newBackupItem = new File(file.getParentFile(), newBackupItemName);
                if (backupItem.isDirectory()) {
                    FileUtils.moveDirectory(backupItem, newBackupItem);
                } else {
                    FileUtils.moveFile(backupItem, newBackupItem);
                }
            }
            if (file.isDirectory()) {
                FileUtils.moveDirectory(file, backupItem);
            } else {
                FileUtils.moveFile(file, backupItem);
            }
        }
    }

    private static String findBackupItemName(File file) {
        int index = 1;
        String tempBackupItemName = file.getName() + BACKUP_FILE_EXT + "." + index;
        File tempBackupItem = new File(file.getParentFile(), tempBackupItemName);
        while (tempBackupItem.exists()) {
            index++;
            tempBackupItemName = file.getName() + BACKUP_FILE_EXT + "." + index;
            tempBackupItem = new File(file.getParentFile(), tempBackupItemName);
        }
        return tempBackupItemName;
    }

    public static boolean isUpgradeTo5x(CompoundVersionDetails source, CompoundVersionDetails target) {
        return source.getVersion().before(ArtifactoryVersionProvider.v500beta1.get()) && ArtifactoryVersionProvider.v500beta1.get().beforeOrEqual(target.getVersion());
    }

    @Override
    public void convert(ArtifactoryHome artifactoryHome, CompoundVersionDetails source, CompoundVersionDetails target) {
        File clusterHomeDir = resolveClusterHomeDir(artifactoryHome);
        doConvert(artifactoryHome, clusterHomeDir);
    }

    @Override
    public void assertConversionPreconditions(ArtifactoryHome home) throws ConverterPreconditionException {
        doAssertConversionPreconditions(home, resolveClusterHomeDir(home));
    }

    protected abstract void doConvert(ArtifactoryHome artifactoryHome, File clusterHomeDir);

    protected abstract void doAssertConversionPreconditions(ArtifactoryHome artifactoryHome, File clusterHomeDir);

    public static void safeCopyRelativeFile(@Nonnull File clusterHomeDir, File targetFile) {
        File oldFile = getOldHaFile(clusterHomeDir, targetFile);
        safeCopyFile(oldFile, targetFile);
    }

    public static File getOldHaFile(@Nonnull File clusterHomeDir, File targetFile) {
        return new File(clusterHomeDir, "ha-etc/" + targetFile.getName());
    }

    protected static void safeCopyFile(@Nonnull File srcFile, @Nonnull File targetFile) {
        try {
            if (srcFile.exists()) {
                saveFileOrDirectoryAsBackup(targetFile);
                // Make sure it's a new file or the DB system will override it.
                // TODO: [by fsi] May be found a better system here.
                FileUtils.copyFile(srcFile, targetFile, false);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to copy file " + srcFile.getAbsolutePath() + " to "
                    + targetFile.getAbsolutePath(), e);
        }
    }

    void safeMoveDirectory(@Nonnull File clusterHomeDir, File targetDirectory) {
        File oldDir = getOldHaFile(clusterHomeDir, targetDirectory);
        try {
            if (oldDir.exists()) {
                saveFileOrDirectoryAsBackup(targetDirectory);
                if(!targetDirectory.exists()) {
                    FileUtils.forceMkdir(targetDirectory);
                }
                FileUtils.copyDirectory(oldDir, targetDirectory);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to move directory " + oldDir.getAbsolutePath() + " to "
                    + targetDirectory.getAbsolutePath(), e);
        }
    }
}
