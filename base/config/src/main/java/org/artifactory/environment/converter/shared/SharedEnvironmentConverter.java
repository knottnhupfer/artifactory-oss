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

import org.artifactory.common.ArtifactoryHome;
import org.artifactory.converter.ArtifactoryConverterAdapter;
import org.artifactory.environment.converter.shared.version.SharedEnvironmentVersion;
import org.artifactory.version.CompoundVersionDetails;

/**
 * @author Dan Feldman
 */
public class SharedEnvironmentConverter implements ArtifactoryConverterAdapter {

    private ArtifactoryHome artifactoryHome;

    public SharedEnvironmentConverter(ArtifactoryHome artifactoryHome) {
        this.artifactoryHome = artifactoryHome;
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
        try {
            SharedEnvironmentVersion.getCurrent().convert(artifactoryHome, source, target);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute shared environment conversion: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isInterested(CompoundVersionDetails source, CompoundVersionDetails target) {
        return source != null && !source.isCurrent();
    }

    @Override
    public void backup() {

    }

    @Override
    public void clean() {
    }

    @Override
    public void assertConversionPrecondition(ArtifactoryHome home, CompoundVersionDetails fromVersion,
            CompoundVersionDetails toVersion) {
        SharedEnvironmentVersion.getCurrent().assertPreConditions(home, fromVersion, toVersion);
    }

    @Override
    public void revert() {
    }
}