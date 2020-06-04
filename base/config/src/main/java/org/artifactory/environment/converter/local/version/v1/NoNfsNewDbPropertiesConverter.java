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

package org.artifactory.environment.converter.local.version.v1;

import com.google.common.collect.Lists;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.config.db.ArtifactoryDbProperties;
import org.artifactory.converter.ConverterPreconditionException;
import org.artifactory.environment.converter.BasicEnvironmentConverter;
import org.artifactory.version.CompoundVersionDetails;
import org.jfrog.common.logging.BootstrapLogger;

import java.io.File;
import java.util.List;

import static org.artifactory.environment.converter.shared.version.v1.NoNfsBasicEnvironmentConverter.isUpgradeTo5x;
import static org.artifactory.environment.converter.shared.version.v1.NoNfsBasicEnvironmentConverter.resolveClusterHomeDir;
import static org.jfrog.config.wrappers.ConfigurationManagerAdapter.normalizedFilesystemPath;

/**
 * This converter is responsible for converting the storage.properties file to the new db.properties file where
 * applicable.
 * It runs only in cases where the db.properties file does not already exist locally and when there's a
 * storage.properties file to work with (locally or in the cluster nfs location if it's available)
 *
 * @author Gidi Shabat
 */
public class NoNfsNewDbPropertiesConverter implements BasicEnvironmentConverter {

    private List<File> convertedFiles = Lists.newArrayList();

    @Override
    public void convert(ArtifactoryHome home, CompoundVersionDetails source, CompoundVersionDetails target) {
        getConvertedFiles(home);
        if (!convertedFiles.isEmpty()) {
            convertPropertiesFile(home, home.getDBPropertiesFile(), convertedFiles.get(0));
        }
    }

    @Override
    public void assertConversionPreconditions(ArtifactoryHome home) throws ConverterPreconditionException {
        getConvertedFiles(home);
        convertedFiles.forEach(this::assertFilePermissions);
        assertTargetFilePermissions(home.getDBPropertiesFile());
    }

    private void getConvertedFiles(ArtifactoryHome home) {
        if (convertedFiles.isEmpty()) {
            File targetDbPropertiesFile = home.getDBPropertiesFile();
            if (!targetDbPropertiesFile.exists()) {
                File clusterHomeDir = resolveClusterHomeDir(home);
                File clusterStorageProperties = new File(clusterHomeDir, normalizedFilesystemPath("ha-etc", "storage.properties"));
                if (clusterHomeDir != null && clusterStorageProperties.exists()) {
                    convertedFiles.add(clusterStorageProperties);
                } else {
                    File localStorageProperties = new File(home.getEtcDir(), "storage.properties");
                    if (localStorageProperties.exists()) {
                        convertedFiles.add(localStorageProperties);
                    }
                }
            }
        }
    }

    private void convertPropertiesFile(ArtifactoryHome home, File targetDbPropertiesFile, File storagePropertiesFile) {
        BootstrapLogger.info("Starting local environment conversion for db.properties");
        ArtifactoryDbProperties dbProperties = new ArtifactoryDbProperties(home, storagePropertiesFile);
        // Now that we have well configured DbProperty, we can save it in home/etc/db.properties file
        try {
            dbProperties.updateDbPropertiesFile(targetDbPropertiesFile);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert db.properties file.", e);
        }
        BootstrapLogger.info("Finished local environment conversion for db.properties");
    }

    @Override
    public boolean isInterested(ArtifactoryHome home, CompoundVersionDetails source, CompoundVersionDetails target) {
        return isUpgradeTo5x(source, target);
    }
}