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

package org.artifactory.rest.common.service.admin.xray;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.xray.XrayAddon;
import org.artifactory.addon.xray.XrayRepo;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.exception.BadRequestException;
import org.artifactory.rest.exception.ForbiddenWebAppException;
import org.artifactory.security.AuthenticationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Get list of repositories to index, mark these repositories for 'xrayIndex' and remove the old 'xrayIndex' if any.
 *
 * @author Shay Bagants
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdateXrayIndexRepos implements RestService {

    private static final Logger log = LoggerFactory.getLogger(UpdateXrayIndexRepos.class);

    @Autowired
    RepositoryService repoService;

    @Autowired
    AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        XrayAddon xrayAddon = addonsManager.addonByType(XrayAddon.class);
        if (xrayAddon.isXrayEnabled()) {
            String remoteAddress = AuthenticationHelper.getRemoteAddress(AuthenticationHelper.getAuthentication());
            log.info("Adding repos to Xray index, request received from instance at: {}", remoteAddress);
            List<XrayRepo> repos = request.getModels();
            log.debug("Requested repos: {}", repos.stream().map(XrayRepo::getName).collect(Collectors.toList()));
            asserNoVirtualOrDistRepos(repos);
            xrayAddon.updateXraySelectedIndexedRepos(repos);
            return;
        }
        throw new ForbiddenWebAppException("Failed to update repositories. Xray configuration is not enabled.");
    }

    private void asserNoVirtualOrDistRepos(List<XrayRepo> repos) {
        repos.forEach(repo -> {
            RepoDescriptor descriptor = repoService.repoDescriptorByKey(repo.getName());
            if (descriptor == null) {
                throw new BadRequestException("Request could not be completed. Repository key:'" + repo.getName() + "' does not exists.");
            }
            if (!descriptor.isReal() || descriptor.getType().equals(RepoType.Distribution) ||
                    (descriptor instanceof RemoteRepoDescriptor && !((RemoteRepoDescriptor) descriptor).isStoreArtifactsLocally())) {
                throw new BadRequestException("Request could not be completed. Repository key:'" + repo.getName() + "' is not supported for xray indexing.");
            }
        });
    }
}
