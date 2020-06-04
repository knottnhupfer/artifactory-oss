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

package org.artifactory.ui.rest.service.admin.security.crowdsso;

import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.sso.crowd.CrowdAddon;
import org.artifactory.addon.sso.crowd.CrowdExtGroup;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.factory.InfoFactory;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.GroupInfo;
import org.artifactory.security.MutableGroupInfo;
import org.artifactory.ui.rest.model.admin.security.crowdsso.CrowdGroupModel;
import org.artifactory.ui.rest.model.admin.security.crowdsso.CrowdGroupsModel;
import org.artifactory.ui.rest.model.admin.security.crowdsso.CrowdIntegration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RefreshCrowdGroupsService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(RefreshCrowdGroupsService.class);
    @Autowired
    CentralConfigService centralConfigService;

    @Autowired
    UserGroupService userGroupService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String userName = request.getPathParamByKey("name");
        // fetch crowd groups
        CrowdGroupsModel crowdGroupsModels = fetchCrowdGroups(request, response, userName);
        if (crowdGroupsModels != null) {
            if(crowdGroupsModels.getCrowdGroupModels().isEmpty()){
                if (StringUtils.isBlank(userName)){
                    response.warn("No group found");
                }else{
                    response.warn("No group found for filter: " + userName);
                }
            }
            // update response with model
            response.iModel(crowdGroupsModels);
        }
    }

    /**
     * fetch crowd groups
     *
     * @param artifactoryRequest  - encapsulate data related to request
     * @param artifactoryResponse - encapsulate data require for response
     * @param userName            - user name
     */
    private CrowdGroupsModel fetchCrowdGroups(ArtifactoryRestRequest artifactoryRequest,
            RestResponse artifactoryResponse,
            String userName) {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        CrowdAddon ssoAddon = addonsManager.addonByType(CrowdAddon.class);
        Set<CrowdExtGroup> groups;
        try {
            CrowdIntegration crowdIntegration   = (CrowdIntegration) artifactoryRequest.getImodel();
            groups = ssoAddon.findCrowdExtGroups(userName, crowdIntegration);
        } catch (Exception e) {
            artifactoryResponse.error(e.getMessage());
            return null;
        }
        updateGroupExistInArtifactory(groups);
        List<CrowdGroupModel> list = new ArrayList<>();
        groups.forEach(group -> list.add(new CrowdGroupModel(group)));
        CrowdGroupsModel crowdGroupsModel = new CrowdGroupsModel();
        crowdGroupsModel.getCrowdGroupModels().addAll(list);
        return crowdGroupsModel;
    }


    /**
     * update exist in artifactory flag data
     *
     * @param groups - crowd group
     */
    private void updateGroupExistInArtifactory(Set<CrowdExtGroup> groups) {
        if (!groups.isEmpty()) {
            InfoFactory factory = InfoFactoryHolder.get();
            Map<String, GroupInfo> artifactoryCrowdGroups = userGroupService.getAllGroupsByGroupNames(groups.stream()
                    .map(CrowdExtGroup::getGroupName)
                    .collect(Collectors.toList()));
            for (CrowdExtGroup group : groups) {
                MutableGroupInfo groupToFind = factory.createGroup(group.getGroupName());
                if (artifactoryCrowdGroups.values().contains(groupToFind)) {
                    group.setExistsInArtifactory(true);
                }
            }
        }
    }
}
