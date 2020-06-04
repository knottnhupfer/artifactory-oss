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

package org.artifactory.addon.debian;

import java.util.List;

/**
 * @author Yuval Reches
 */
public class DebianMetadataInfo {

    private DebianInfo debianInfo;
    private List<String> debianDependencies;
    private String failReason;

    DebianMetadataInfo() {

    }

    DebianMetadataInfo(DebianInfo debianInfo, List<String> debianDependencies, String failReason) {
        this.debianInfo = debianInfo;
        this.debianDependencies = debianDependencies;
        this.failReason = failReason;
    }

    public DebianInfo getDebianInfo() {
        return debianInfo;
    }

    public void setDebianInfo(DebianInfo debianInfo) {
        this.debianInfo = debianInfo;
    }

    public List<String> getDebianDependencies() {
        return debianDependencies;
    }

    public void setDebianDependencies(List<String> debianDependencies) {
        this.debianDependencies = debianDependencies;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }
}
