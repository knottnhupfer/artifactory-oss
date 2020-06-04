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

package org.artifactory.component;

import org.artifactory.api.component.ComponentDetails;
import org.artifactory.api.properties.PropertiesService;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.md.Properties;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.repo.RepoPath;
import org.jfrog.client.util.PathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.artifactory.mime.DockerNaming.MANIFEST_FILENAME;
import static org.artifactory.mime.NamingUtils.getMimeType;

/**
 * @author Rotem Kfir
 */
@Component("dockerComponentDetailsFetcher")
public class DockerComponentDetailsFetcher implements RepoComponentDetailsFetcher {

    @Autowired
    private PropertiesService propertiesService;

    @Override
    public ComponentDetails calcComponentDetails(RepoPath repoPath) {
        RepoPath manifestRepoPath = repoPath;
        if (!MANIFEST_FILENAME.equals(repoPath.getName())) {
            if (repoPath.getParent() == null) {
                return ComponentDetails.builder().build();
            }
            manifestRepoPath = new RepoPathImpl(repoPath.getParent(), MANIFEST_FILENAME);
        }
        Properties properties = propertiesService.getProperties(manifestRepoPath);
        return ComponentDetails.builder()
                .componentType(RepoType.Docker)
                .name(properties.getFirst("docker.repoName"))
                .version(properties.getFirst("docker.manifest"))
                .extension(PathUtils.getExtension(repoPath.getName()))
                .mimeType(getMimeType(repoPath.getPath()).getType())
                .build();
    }
}
