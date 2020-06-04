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
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.repo.RepoPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.artifactory.api.module.ModuleInfoUtils.moduleInfoFromArtifactPath;
import static org.artifactory.api.module.ModuleInfoUtils.moduleInfoFromDescriptorPath;
import static org.artifactory.mime.NamingUtils.getMimeType;

/**
 * @author Gal Ben Ami
 */
@Component("mavenComponentDetailsFetcher")
public class MavenComponentDetailsFetcher implements RepoComponentDetailsFetcher {

    @Autowired
    private RepositoryService repositoryService;

    @Override
    public ComponentDetails calcComponentDetails(RepoPath repoPath) {
        String path = repoPath.getPath();
        ModuleInfo moduleInfo;
        RepoDescriptor repo = repositoryService.repoDescriptorByKey(repoPath.getRepoKey());
        if (repo != null) {
            RepoLayout repoLayout = repo.getRepoLayout();
            moduleInfo = moduleInfoFromDescriptorPath(path, repoLayout);
            if (!moduleInfo.isValid()) {
                moduleInfo = moduleInfoFromArtifactPath(path, repoLayout);
            }
        } else {
            moduleInfo = new ModuleInfo();
        }

        String componentName = moduleInfo.getOrganization() == null ? null : moduleInfo.getOrganization() + ":" + moduleInfo.getModule();
        return ComponentDetails.builder()
                .componentType(RepoType.Maven)
                .name(componentName)
                .version(moduleInfo.getBaseRevision())
                .extension(moduleInfo.getExt())
                .mimeType(getMimeType(path).getType())
                .build();
    }
}
