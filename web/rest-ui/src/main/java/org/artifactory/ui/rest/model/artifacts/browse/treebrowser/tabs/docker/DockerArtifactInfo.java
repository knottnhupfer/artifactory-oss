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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.docker;

import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.BaseArtifactInfo;

/**
 * @author Chen Keinan
 */
public class DockerArtifactInfo extends BaseArtifactInfo {

    private DockerInfoModel dockerInfo;
    private DockerConfig dockerConfig;

    public DockerInfoModel getDockerInfo() {
        return dockerInfo;
    }

    public void setDockerInfo(DockerInfoModel dockerInfo) {
        this.dockerInfo = dockerInfo;
    }

    public DockerConfig getDockerConfig() {
        return dockerConfig;
    }

    public void setDockerConfig(
            DockerConfig dockerConfig) {
        this.dockerConfig = dockerConfig;
    }
}
