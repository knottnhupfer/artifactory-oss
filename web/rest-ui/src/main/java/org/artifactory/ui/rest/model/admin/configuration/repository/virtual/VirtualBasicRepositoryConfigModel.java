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

package org.artifactory.ui.rest.model.admin.configuration.repository.virtual;

import org.artifactory.api.rest.restmodel.JsonUtil;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalBasicRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.xray.XrayRepoConfigModel;

import java.util.List;

/**
 * @author Dan Feldman
 * @author Aviad Shikloshi
 */
public class VirtualBasicRepositoryConfigModel extends LocalBasicRepositoryConfigModel {

    private List<VirtualSelectedRepository> selectedRepositories;
    private List<VirtualSelectedRepository> resolvedRepositories;
    private String defaultDeploymentRepo;

    @Override
    public XrayRepoConfigModel getXrayConfig() {
        return null;
    }

    @Override
    public void setXrayConfig(XrayRepoConfigModel xrayConfig) {

    }

    public List<VirtualSelectedRepository> getSelectedRepositories() {
        return selectedRepositories;
    }

    public void setSelectedRepositories(
            List<VirtualSelectedRepository> selectedRepositories) {
        this.selectedRepositories = selectedRepositories;
    }

    public List<VirtualSelectedRepository> getResolvedRepositories() {
        return resolvedRepositories;
    }

    public void setResolvedRepositories(
            List<VirtualSelectedRepository> resolvedRepositories) {
        this.resolvedRepositories = resolvedRepositories;
    }

    public String getDefaultDeploymentRepo() {
        return defaultDeploymentRepo;
    }

    public void setDefaultDeploymentRepo(String defaultDeploymentRepo) {
        this.defaultDeploymentRepo = defaultDeploymentRepo;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
