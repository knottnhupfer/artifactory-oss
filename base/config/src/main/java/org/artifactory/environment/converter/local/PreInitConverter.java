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

package org.artifactory.environment.converter.local;

import org.artifactory.common.ArtifactoryHome;
import org.artifactory.converter.ArtifactoryConverterAdapter;
import org.artifactory.converter.ConverterPreconditionException;
import org.artifactory.environment.converter.local.version.LocalEnvironmentVersion;
import org.artifactory.version.CompoundVersionDetails;

import javax.annotation.Nullable;

/**
 * @author Gidi Shabat
 */
public class PreInitConverter implements ArtifactoryConverterAdapter {

    private ArtifactoryHome artifactoryHome;


    public PreInitConverter(ArtifactoryHome artifactoryHome) {
        this.artifactoryHome = artifactoryHome;
    }

    @Override
    public boolean isInterested(@Nullable CompoundVersionDetails source, CompoundVersionDetails target) {
        return true;
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
    public void assertConversionPrecondition(ArtifactoryHome home, CompoundVersionDetails fromVersion, CompoundVersionDetails toVersion) throws ConverterPreconditionException {
        if (artifactoryHome.getArtifactoryProperties() == null) {
            artifactoryHome.initArtifactorySystemProperties();
        }
        LocalEnvironmentVersion.assertPreConditions(home, fromVersion, toVersion);
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
        try {
            // TODO: [by fsi] Hack here to protect conversion of not init homes
            if (artifactoryHome.getArtifactoryProperties() == null) {
                artifactoryHome.initArtifactorySystemProperties();
            }

            LocalEnvironmentVersion.convert(artifactoryHome, source, target);

        } catch (Exception e) {
            throw new RuntimeException("Failed to execute local environment conversion: " + e.getMessage(), e);
        }
    }
}