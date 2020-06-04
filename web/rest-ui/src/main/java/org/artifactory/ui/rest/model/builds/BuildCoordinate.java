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

package org.artifactory.ui.rest.model.builds;

/**
 * @author Gidi Shabat
 */
public class BuildCoordinate {
    private String buildName;
    private String buildNumber;
    private long date;

    /**
     * Need the constructor for JSON mapping
     */
    public BuildCoordinate() {
    }

    public BuildCoordinate(String buildName, String buildNumber, long date) {
        this.buildName = buildName;
        this.buildNumber = buildNumber;
        this.date = date;
    }

    public BuildCoordinate(String buildName) {
        this.buildName = buildName;
    }

    public String getBuildName() {
        return buildName;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public long getDate() {
        return date;
    }
}
