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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.debian;

import org.artifactory.addon.debian.DebianInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.BaseArtifactInfo;

import java.util.List;

/**
 * The UI model for Debian and Opkg info tab
 * If a package is invalid and was not indexed we hold the indexFailureReason, otherwise- null
 * A message appears in the Info tab in case this variable is not null
 *
 * @author Yuval Reches
 */
public class DebianArtifactMetadataInfo extends BaseArtifactInfo {

    private DebianInfo debianInfo;
    private List<String> debianDependencies;
    private String indexFailureReason;

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

    public String getIndexFailureReason() {
        return indexFailureReason;
    }

    public void setIndexFailureReason(String indexFailureReason) {
        this.indexFailureReason = indexFailureReason;
    }
}
