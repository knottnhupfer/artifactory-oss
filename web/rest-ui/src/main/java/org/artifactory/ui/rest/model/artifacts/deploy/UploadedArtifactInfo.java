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

package org.artifactory.ui.rest.model.artifacts.deploy;

import org.artifactory.rest.common.model.BaseModel;

/**
 * Describes a Response from UI Deploy
 *
 * @author Aviad Shikloshi
 */
public class UploadedArtifactInfo extends BaseModel {

    private Boolean showUrl;
    private String repoKey;
    private String artifactPath;

    public UploadedArtifactInfo() {
    }

    public UploadedArtifactInfo(Boolean showUrl, String repoKey, String artifactPath) {
        this.showUrl = showUrl;
        this.repoKey = repoKey;
        this.artifactPath = artifactPath;
    }

    public Boolean isShowUrl() {
        return showUrl;
    }

    public void setShowUrl(Boolean showUrl) {
        this.showUrl = showUrl;
    }

    public String getRepoKey() {
        return repoKey;
    }

    public void setRepoKey(String repoKey) {
        this.repoKey = repoKey;
    }

    public String getArtifactPath() {
        return artifactPath;
    }

    public void setArtifactPath(String artifactPath) {
        this.artifactPath = artifactPath;
    }
}
