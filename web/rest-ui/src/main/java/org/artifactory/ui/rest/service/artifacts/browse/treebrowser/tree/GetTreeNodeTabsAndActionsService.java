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

package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tree;

import org.artifactory.addon.AddonsManager;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.actions.TabsAndActions;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.actions.TreeNodeActionsPopulator;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.actions.TreeNodeTabsPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Shay Yaakov
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetTreeNodeTabsAndActionsService implements RestService<TabsAndActions> {

    @Autowired
    private TreeNodeActionsPopulator actionsPopulator;

    @Autowired
    private TreeNodeTabsPopulator tabsPopulator;

    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest<TabsAndActions> request, RestResponse response) {
        TabsAndActions tabsAndActions = request.getImodel();
        boolean edgeLicensed = addonsManager.isEdgeLicensed();
        switch (tabsAndActions.getType()) {
            case "repository":
            case "virtualRemoteRepository":
                actionsPopulator.populateForRepository(tabsAndActions);
                tabsPopulator.populateForRepository(tabsAndActions);
                break;
            case "folder":
            case "virtualRemoteFolder":
                actionsPopulator.populateForFolder(tabsAndActions, edgeLicensed);
                tabsPopulator.populateForFolder(tabsAndActions);
                break;
            case "file":
            case "archive":
            case "virtualRemoteFile":
                actionsPopulator.populateForFile(tabsAndActions, edgeLicensed);
                tabsPopulator.populateForFile(tabsAndActions, edgeLicensed);
                break;
        }
        response.iModel(tabsAndActions);
    }
}