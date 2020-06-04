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

package org.artifactory.addon.sso.crowd;

import java.io.Serializable;

/**
 * @author Chen  Keinan
 */
public class CrowdExtGroup implements Serializable {

    private String groupName;

    private String description;

    private boolean existsInArtifactory = false;

    private boolean importIntoArtifactory = false;

    public CrowdExtGroup(String groupName, String description) {
        this.description = description;
        this.groupName = groupName;
    }


    public CrowdExtGroup() {
    }

    public String getDescription() {
        return description;
    }

    public String getGroupName() {
        return groupName;
    }

    public boolean isExistsInArtifactory() {
        return existsInArtifactory;
    }

    public void setExistsInArtifactory(boolean existsInArtifactory) {
        this.existsInArtifactory = existsInArtifactory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CrowdExtGroup group = (CrowdExtGroup) o;

        return groupName.equals(group.groupName);
    }

    public boolean isImportIntoArtifactory() {
        return importIntoArtifactory;
    }

    public void setImportIntoArtifactory(boolean importIntoArtifactory) {
        this.importIntoArtifactory = importIntoArtifactory;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        return groupName.hashCode();
    }
}
