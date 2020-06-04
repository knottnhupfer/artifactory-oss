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

package org.artifactory.version;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.artifactory.common.ConstantValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

import static org.artifactory.version.ArtifactoryVersion.getCurrent;
import static org.artifactory.version.ArtifactoryVersion.isCurrentVersion;


/**
 * Returns ArtifactoryVersion object from a properties stream/file.
 *
 * @author Yossi Shaul
 */
public class ArtifactoryVersionReader {
    private static final Logger log = LoggerFactory.getLogger(ArtifactoryVersionReader.class);

    // First release of Artifactory
    static final long UNFILTERED_TIMESTAMP = 1167040800000L;

    public static class VersionPropertiesContent {
        String versionString;
        String revisionString;
        String buildNumberString;
        String timestampString;

        VersionPropertiesContent() {
        }

        public VersionPropertiesContent(String version, String revision, String buildNumber, String timestamp) {
            this.versionString = version;
            this.revisionString = revision;
            this.buildNumberString = buildNumber;
            this.timestampString = timestamp;
        }
    }

    public static CompoundVersionDetails readFromFileAndFindVersion(File propertiesFile) {
        if (propertiesFile == null) {
            throw new IllegalArgumentException("Null properties file is not allowed");
        }
        try {
            return readAndFindVersion(new FileInputStream(propertiesFile), propertiesFile.getAbsolutePath());
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Properties file " + propertiesFile.getName() + " doesn't exist");
        }
    }

    static VersionPropertiesContent readPropsContent(InputStream inputStream, String sourceName) {
        VersionPropertiesContent res = new VersionPropertiesContent();
        if (inputStream == null) {
            throw new IllegalArgumentException("Artifactory properties input stream cannot be null");
        }
        Properties props = new Properties();
        try {
            props.load(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read version details from '" + sourceName + "'", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        String versionPropName = ConstantValues.artifactoryVersion.getPropertyName();
        res.versionString = props.getProperty(versionPropName);
        if (StringUtils.isBlank(res.versionString)) {
            throw new IllegalArgumentException("Version source '" + sourceName +
                    "' does not have the mandatory property '" + versionPropName + "'");
        }
        res.revisionString = props.getProperty(ConstantValues.artifactoryRevision.getPropertyName());
        res.timestampString = props.getProperty(ConstantValues.artifactoryTimestamp.getPropertyName());
        res.buildNumberString = props.getProperty(ConstantValues.artifactoryBuildNumber.getPropertyName());

        return res;
    }

    public static CompoundVersionDetails readAndFindVersion(InputStream inputStream, String sourceName) {
        return getCompoundVersionDetails(readPropsContent(inputStream, sourceName));
    }

    public static CompoundVersionDetails getCompoundVersionDetails(String version, String revision,
            String timestamp) {
        return getCompoundVersionDetails(new VersionPropertiesContent(version, revision, "UNDEFINED", timestamp));
    }

    private static CompoundVersionDetails getCompoundVersionDetails(VersionPropertiesContent versionProps) {
        long revision = Integer.MAX_VALUE;
        if (StringUtils.isNumeric(versionProps.revisionString)) {
            revision = Long.parseLong(versionProps.revisionString);
        }
        ArtifactoryVersion version;
        if (isCurrentVersion(versionProps.versionString, versionProps.revisionString)) {
            version = getCurrent();
        } else {
            version = ArtifactoryVersionProvider.get(versionProps.versionString, revision);
        }
        long timestamp;
        try {
            if ("${timestamp}".equals(versionProps.timestampString) || "LOCAL".equals(versionProps.timestampString)) {
                // In dev mode
                timestamp = UNFILTERED_TIMESTAMP;
            } else {
                timestamp = Long.parseLong(versionProps.timestampString);
            }
        } catch (Exception e) {
            log.warn("Could not parse timestamp value {}", versionProps.timestampString);
            timestamp = 0;
        }


        return new CompoundVersionDetails(version,
                versionProps.buildNumberString, timestamp);
    }
}
