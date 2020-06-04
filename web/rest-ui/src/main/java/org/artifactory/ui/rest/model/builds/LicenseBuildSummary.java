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
 * @author Chen Keinan
 */
public class LicenseBuildSummary {

    private Integer approved;
    private Integer notFound;
    private Integer neutral;
    private Integer notApproved;
    private Integer unknown;

    public LicenseBuildSummary() {
    }

    public LicenseBuildSummary(int unknown, int approved, int neutral, int notApproved, int notFound) {
        this.unknown = unknown;
        this.approved = approved;
        this.neutral = neutral;
        this.notApproved = notApproved;
        this.notFound = notFound;
    }

    public Integer getApproved() {
        return approved;
    }

    public void setApproved(Integer approved) {
        this.approved = approved;
    }

    public Integer getNotFound() {
        return notFound;
    }

    public void setNotFound(Integer notFound) {
        this.notFound = notFound;
    }

    public Integer getNeutral() {
        return neutral;
    }

    public void setNeutral(Integer neutral) {
        this.neutral = neutral;
    }

    public Integer getNotApproved() {
        return notApproved;
    }

    public void setNotApproved(Integer notApproved) {
        this.notApproved = notApproved;
    }

    public Integer getUnknown() {
        return unknown;
    }

    public void setUnknown(Integer unknown) {
        this.unknown = unknown;
    }
}
