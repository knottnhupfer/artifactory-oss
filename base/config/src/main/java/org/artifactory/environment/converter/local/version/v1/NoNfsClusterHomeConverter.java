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

import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ha.HaNodeProperties;
import org.artifactory.converter.ArtifactoryConverterAdapter;
import org.artifactory.converter.ConverterPreconditionException;
import org.artifactory.version.CompoundVersionDetails;
import org.jfrog.client.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;

import static org.artifactory.environment.converter.shared.version.v1.NoNfsBasicEnvironmentConverter.isUpgradeTo5x;

/**
 * This converter is physically in localEnv/v1 but we need it to run in every node AND after all local and shared
 * (i.e. PRE_INIT and SYNC_HOME) converters have run - because a lot of them need the cluster.home property.
 *
 * @author Gidi Shabat
 */
public class NoNfsClusterHomeConverter implements ArtifactoryConverterAdapter {
    private static final Logger log = LoggerFactory.getLogger(NoNfsClusterHomeConverter.class);

    private final ArtifactoryHome home;

    public NoNfsClusterHomeConverter(ArtifactoryHome home) {
        this.home = home;
    }

    @Override
    public boolean isInterested(@Nullable CompoundVersionDetails source, CompoundVersionDetails target) {
        // ConvertersManagerImpl sends *original home version* (filesystem based) to this method - don't change it!
        return isUpgradeTo5x(source, target);
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
        try {
            File haNodePropertiesFile = home.getArtifactoryHaNodePropertiesFile();
            if (!haNodePropertiesFile.exists()) {
                return;
            }
            HaNodeProperties haNodeProperties = new HaNodeProperties();
            haNodeProperties.load(haNodePropertiesFile);
            String clusterHome = (String) haNodeProperties.getProperties().get("cluster.home");
            if (clusterHome != null) {
                log.info("Starting environment conversion: cluster home dir");
                haNodeProperties.setBackupDir(new File(PathUtils.trimTrailingSlashes(clusterHome) + "/ha-backup").getAbsolutePath());
                haNodeProperties.setClusterDataDir(new File(PathUtils.trimTrailingSlashes(clusterHome) + "/ha-data").getAbsolutePath());
                haNodeProperties.removeProperty("cluster.home");
                haNodeProperties.updateHaPropertiesFile(haNodePropertiesFile);
                log.info("Finished environment conversion: cluster home dir");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute cluster home conversion:", e);
        }
    }

    @Override
    public void revert() {

    }

    @Override
    public void backup() {

    }

    @Override
    public void clean() {

    }

    @Override
    public void assertConversionPrecondition(ArtifactoryHome home,
            CompoundVersionDetails fromVersion, CompoundVersionDetails toVersion) {
        File target = home.getArtifactoryHaNodePropertiesFile();
        if (target.exists() && (!target.canRead() || !target.canWrite())) {
            throw new ConverterPreconditionException("File " + target.getPath() +
                    " doesn't have the permissions required for Artifactory 5 upgrade.");
        }
    }
}
