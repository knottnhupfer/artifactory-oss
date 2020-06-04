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

import java.util.Objects;

/**
 * Holds all the version data about Artifactory. version name, and revision from the properties file and
 * ArtifactoryVersion that matches those values.
 *
 * @author Yossi Shaul
 */
public class CompoundVersionDetails {
    private final ArtifactoryVersion version;
    private final String buildNumber;
    private final long timestamp;

    public CompoundVersionDetails(ArtifactoryVersion version, String buildNumber, long timestamp) {
        this.version = version;
        this.buildNumber = buildNumber;
        this.timestamp = timestamp;
    }

    /**
     * @return The closest matched version for the input stream/file
     */
    public ArtifactoryVersion getVersion() {
        return version;
    }

    /**
     * @return The raw version string as read from the input stream/file
     */
    public String getVersionName() {
        return version.getVersion();
    }

    /**
     * @return The raw revision string as read from the input stream/file
     */
    public long getRevision() {
        return version.getRevision();
    }

    /**
     * @return The build number that created this version
     */
    public String getBuildNumber() {
        return buildNumber;
    }

    /**
     * @return Artifactory release timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    public boolean isCurrent() {
        return version.isCurrent();
    }

    /**
     * Outputs in the same format as the artifactory.properties file. used by export, tests etc.
     */
    public String getFileDump() {
        return "artifactory.version=" + version.getVersion() + "\n" +
                "artifactory.revision=" + version.getRevision() + "\n" +
                "artifactory.timestamp=" + timestamp + "\n"+
                "artifactory.buildNumber=" + buildNumber + "\n";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CompoundVersionDetails)) {
            return false;
        }
        CompoundVersionDetails that = (CompoundVersionDetails) o;
        return timestamp == that.timestamp &&
                Objects.equals(version, that.version) &&
                Objects.equals(buildNumber, that.buildNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, buildNumber, timestamp);
    }

    @Override
    public String toString() {
        return "CompoundVersionDetails{" +
                "version=" + version +
                ", buildNumber='" + buildNumber + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
