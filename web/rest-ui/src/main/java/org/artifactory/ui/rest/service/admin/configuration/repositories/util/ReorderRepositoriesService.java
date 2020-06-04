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

package org.artifactory.ui.rest.service.admin.configuration.repositories.util;

import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.descriptor.repo.distribution.DistributionRepoDescriptor;
import org.artifactory.descriptor.repo.releasebundle.ReleaseBundlesRepoDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.artifactory.repo.RepoDetailsType.*;

/**
 * Reorders the list of repositories based on a list sent by the UI
 *
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ReorderRepositoriesService implements RestService<List<String>> {
    private static final Logger log = LoggerFactory.getLogger(ReorderRepositoriesService.class);

    @Autowired
    private CentralConfigService configService;

    @Override
    @SuppressWarnings("unchecked") //yeah this code is disgusting, no time for something else
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String repoType = request.getPathParamByKey("repoType");
        log.debug("Processing reorder repos request for type {}", repoType);
        if (StringUtils.isBlank(repoType)) {
            response.error("The type of repositories to reorder must be specified").responseCode(SC_BAD_REQUEST);
        }
        List<String> newOrderRepoKeyList = request.getModels();
        if (CollectionUtils.isNullOrEmpty(newOrderRepoKeyList)) {
            response.error("No list to reorder by was sent.").responseCode(SC_BAD_REQUEST);
            return;
        }
        Map<String, ? extends RepoDescriptor> currentRepoMap;
        MutableCentralConfigDescriptor mutableDescriptor = configService.getMutableDescriptor();
        switch (repoType) {
            case LOCAL_REPO:
                currentRepoMap = mutableDescriptor.getLocalRepositoriesMap();
                LinkedHashMap<String, LocalRepoDescriptor> localMap = (LinkedHashMap<String, LocalRepoDescriptor>)
                        getNewOrderRepoMap(response, repoType, newOrderRepoKeyList, currentRepoMap);
                if (localMap.size() > 0) {
                    mutableDescriptor.setLocalRepositoriesMap(localMap);
                    configService.saveEditedDescriptorAndReload(mutableDescriptor);
                }
                break;
            case REMOTE_REPO:
                currentRepoMap = mutableDescriptor.getRemoteRepositoriesMap();
                LinkedHashMap<String, RemoteRepoDescriptor> remoteMap = (LinkedHashMap<String, RemoteRepoDescriptor>)
                        getNewOrderRepoMap(response, repoType, newOrderRepoKeyList, currentRepoMap);
                if (remoteMap.size() > 0) {
                    mutableDescriptor.setRemoteRepositoriesMap(remoteMap);
                    configService.saveEditedDescriptorAndReload(mutableDescriptor);
                }
                break;
            case VIRTUAL_REPO:
                currentRepoMap = mutableDescriptor.getVirtualRepositoriesMap();
                LinkedHashMap<String, VirtualRepoDescriptor> virtualMap = (LinkedHashMap<String, VirtualRepoDescriptor>)
                        getNewOrderRepoMap(response, repoType, newOrderRepoKeyList, currentRepoMap);
                if (virtualMap.size() > 0) {
                    mutableDescriptor.setVirtualRepositoriesMap(virtualMap);
                    configService.saveEditedDescriptorAndReload(mutableDescriptor);
                }
                break;
            case DISTRIBUTION_REPO:
                currentRepoMap = mutableDescriptor.getDistributionRepositoriesMap();
                LinkedHashMap<String, DistributionRepoDescriptor> distributionMap = (LinkedHashMap<String, DistributionRepoDescriptor>)
                        getNewOrderRepoMap(response, repoType, newOrderRepoKeyList, currentRepoMap);
                if (distributionMap.size() > 0) {
                    mutableDescriptor.setDistributionRepositoriesMap(distributionMap);
                    configService.saveEditedDescriptorAndReload(mutableDescriptor);
                }
                break;
            case RELEASE_BUNDLE_REPO:
                currentRepoMap = mutableDescriptor.getReleaseBundlesRepositoriesMap();
                LinkedHashMap<String, ReleaseBundlesRepoDescriptor> bundlesMap = getNewOrderRepoMap(response, repoType, newOrderRepoKeyList, currentRepoMap);
                if (!bundlesMap.isEmpty()) {
                    mutableDescriptor.setReleaseBundlesRepositoriesMap(bundlesMap);
                    configService.saveEditedDescriptorAndReload(mutableDescriptor);
                }
                break;
            default:
                response.error("Invalid repository type given: " + repoType).responseCode(SC_BAD_REQUEST);
        }
    }

    private LinkedHashMap<String, ? extends RepoDescriptor> getNewOrderRepoMap(RestResponse<? extends Object> response,
            String repoType,
            List<String> newOrderRepoKeyList, Map<String, ? extends RepoDescriptor> currentRepoMap) {
        LinkedHashMap<String, RepoDescriptor> newRepoMap = Maps.newLinkedHashMap();
        if (isLegalReorder(response, repoType, newOrderRepoKeyList, currentRepoMap)) {
            for (String repoKey : newOrderRepoKeyList) {
                newRepoMap.put(repoKey, currentRepoMap.get(repoKey));
            }
            log.info("Reordering {} repositories list.", repoType);
        }
        return newRepoMap;
    }

    private boolean isLegalReorder(RestResponse response, String repoType, List<String> newOrderRepoKeyList,
            Map<String, ? extends RepoDescriptor> currentRepoMap) {
        Set<String> oldOrderRepoKeys = currentRepoMap.keySet();
        if (oldOrderRepoKeys.size() != newOrderRepoKeyList.size()) {
            log.debug("Current {} repo map size: {}", repoType, oldOrderRepoKeys.size());
            log.debug("New Order list size : {}", newOrderRepoKeyList.size());
            response.error("The size of the list to order by does not match the size of the current repo list, " +
                    "aborting.").responseCode(SC_BAD_REQUEST);
            return false;
        } else if (!newOrderRepoKeyList.containsAll(oldOrderRepoKeys)) {
            response.error("The new order list is missing \\ has excess repositories that are currently saved." +
                    repoType).responseCode(SC_BAD_REQUEST);
            return false;
        }
        return true;
    }
}