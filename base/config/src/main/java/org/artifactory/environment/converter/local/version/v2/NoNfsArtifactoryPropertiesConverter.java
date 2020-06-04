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

package org.artifactory.environment.converter.local.version.v2;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.converter.ConverterPreconditionException;
import org.artifactory.environment.converter.BasicEnvironmentConverter;
import org.artifactory.version.ArtifactoryVersionProvider;
import org.artifactory.version.CompoundVersionDetails;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.artifactory.common.ArtifactoryHome.ARTIFACTORY_PROPERTIES_FILE;
import static org.artifactory.environment.converter.shared.version.v1.NoNfsBasicEnvironmentConverter.resolveClusterDataDir;
import static org.artifactory.environment.converter.shared.version.v1.NoNfsBasicEnvironmentConverter.saveFileOrDirectoryAsBackup;

/**
 * Rename the old artifactory.properties files (local and shared) to be backup files.
 * On each node copy artifactory.properties from data folder to local etc folder.
 *
 * @author Rotem Kfir
 */
public class NoNfsArtifactoryPropertiesConverter implements BasicEnvironmentConverter {

    private List<File> convertedFiles = Lists.newArrayList();

    @Override
    public boolean isInterested(ArtifactoryHome home, CompoundVersionDetails source, CompoundVersionDetails target) {
        return source.getVersion().beforeOrEqual(ArtifactoryVersionProvider.v530.get());
    }

    @Override
    public void convert(ArtifactoryHome artifactoryHome, CompoundVersionDetails source, CompoundVersionDetails target) {
        // Rename artifactory.properties in local and shared data dir to .back
        File clusterDataDir = resolveClusterDataDir(artifactoryHome);
        if (clusterDataDir != null) {
            File oldFile = new File(clusterDataDir, ARTIFACTORY_PROPERTIES_FILE);
            renameFileToBackup(oldFile);
        }

        File localDataDir = new File(artifactoryHome.getHomeDir(), "data");
        if (!localDataDir.equals(clusterDataDir)) {
            renameFileToBackup(new File(localDataDir, ARTIFACTORY_PROPERTIES_FILE));
        }

        // Copy the file from the data dir to local etc dir
        File dataDir = clusterDataDir != null ? clusterDataDir : localDataDir;
        File propBackup = new File(dataDir, ARTIFACTORY_PROPERTIES_FILE + BACKUP_FILE_EXT);
        copyFile(propBackup, artifactoryHome.getArtifactoryPropertiesFile());
    }

    @Override
    public void assertConversionPreconditions(ArtifactoryHome home) throws ConverterPreconditionException {
        getConvertedFiles(home);
        convertedFiles.forEach(this::assertFilePermissions);
        assertTargetFilePermissions(home.getArtifactoryPropertiesFile());
    }

    private void getConvertedFiles(ArtifactoryHome home) {
        if (convertedFiles.isEmpty()) {
            File clusterDataDir = resolveClusterDataDir(home);
            if (clusterDataDir != null) {
                convertedFiles.add(new File(clusterDataDir, ARTIFACTORY_PROPERTIES_FILE));
            }
            File localDataDir = new File(home.getHomeDir(), "data");
            if (!localDataDir.equals(clusterDataDir)) {
                convertedFiles.add(new File(localDataDir, ARTIFACTORY_PROPERTIES_FILE));
            }
        }
    }


    private void renameFileToBackup(@Nonnull File file) {
        try {
            saveFileOrDirectoryAsBackup(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to backup file " + file.getAbsolutePath(), e);
        }
    }

    private void copyFile(@Nonnull File srcFile, @Nonnull File targetFile) {
        try {
            if (srcFile.exists()) {
                FileUtils.copyFile(srcFile, targetFile, false);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to copy file " + srcFile.getAbsolutePath() + " to "
                    + targetFile.getAbsolutePath(), e);
        }
    }
}
