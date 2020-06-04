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

package org.artifactory.ui.rest.model.admin.configuration.repository.typespecific;

import org.artifactory.addon.AddonsManager;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.descriptor.repo.VcsGitConfiguration;
import org.artifactory.rest.common.exception.RepoConfigException;
import org.artifactory.rest.common.util.JsonUtil;

import static java.util.Optional.ofNullable;
import static org.artifactory.repo.config.RepoConfigDefaultValues.DEFAULT_PODS_SPECS_REPO;
import static org.artifactory.repo.config.RepoConfigDefaultValues.DEFAULT_VCS_GIT_CONFIG;

/**
 * @author Dan Feldman
 */
public class CocoaPodsTypeSpecificConfigModel extends VcsTypeSpecificConfigModel {

    //remote
    private String specsRepoUrl = DEFAULT_PODS_SPECS_REPO;
    private VcsGitConfiguration specsRepoProvider = DEFAULT_VCS_GIT_CONFIG;

    public String getSpecsRepoUrl() {
        return specsRepoUrl;
    }

    public void setSpecsRepoUrl(String specsRepoUrl) {
        this.specsRepoUrl = specsRepoUrl;
    }

    public VcsGitConfiguration getSpecsRepoProvider() {
        return specsRepoProvider;
    }

    public void setSpecsRepoProvider(VcsGitConfiguration specsRepoProvider) {
        this.specsRepoProvider = specsRepoProvider;
    }

    @Override
    public void validateRemoteTypeSpecific() throws RepoConfigException {
        setSpecsRepoUrl(ofNullable(getSpecsRepoUrl()).orElse(DEFAULT_PODS_SPECS_REPO));
        setSpecsRepoProvider(ofNullable(getSpecsRepoProvider()).orElse(DEFAULT_VCS_GIT_CONFIG));
        super.validateRemoteTypeSpecific();
    }

    @Override
    public void validateVirtualTypeSpecific(AddonsManager addonsManager) throws RepoConfigException {
        throwUnsupportedVirtualRepoType();
    }

    @Override
    public RepoType getRepoType() {
        return RepoType.CocoaPods;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
