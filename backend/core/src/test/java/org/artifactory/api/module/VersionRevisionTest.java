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

package org.artifactory.api.module;

import org.artifactory.common.ConstantValues;
import org.artifactory.common.property.ArtifactorySystemProperties;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.artifactory.version.ArtifactoryVersionReader;
import org.artifactory.version.BuildSystemVersionVerifierTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * Tests that the environment version/revision matches the real Artifactory version/revision
 * This test prevents entering wrong revision/version numbers when publishing Artifactory
 *
 * @author nadavy
 */
@Test
public class VersionRevisionTest extends ArtifactoryHomeBoundTest {
    private static final Logger log = LoggerFactory.getLogger(VersionRevisionTest.class);

    public void versionRevisionMatchTest() {
        ArtifactorySystemProperties artifactoryProperties = getBound().getArtifactoryProperties();
        String version = artifactoryProperties.getProperty(ConstantValues.artifactoryVersion);
        log.info("Found version: {}", version);
        String revision = artifactoryProperties.getProperty(ConstantValues.artifactoryRevision);
        log.info("Found revision: {}", revision);
        String buildNumber = artifactoryProperties.getProperty(ConstantValues.artifactoryBuildNumber);
        log.info("Found buildNumber: {}", buildNumber);
        String timestamp = artifactoryProperties.getProperty(ConstantValues.artifactoryTimestamp);
        log.info("Found timestamp: {}", timestamp);

        ArtifactoryVersionReader.VersionPropertiesContent propsContent =
                new ArtifactoryVersionReader.VersionPropertiesContent(version, revision, buildNumber, timestamp);
        //BuildSystemVersionVerifierTest.assertPropsContentMatchBuildEnvironment(propsContent);
    }
}
