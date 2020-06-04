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

import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.rest.common.exception.RepoConfigException;
import org.artifactory.rest.common.util.JsonUtil;

import static java.util.Optional.ofNullable;
import static org.artifactory.repo.config.RepoConfigDefaultValues.DEFAULT_COMPOSER_REGISTRY;

/**
 * @author Shay Bagants
 */
public class ComposerTypeSpecificConfigModel extends VcsTypeSpecificConfigModel {

    //remote
    private String registryUrl = DEFAULT_COMPOSER_REGISTRY;

    public String getRegistryUrl() {
        return registryUrl;
    }

    public void setRegistryUrl(String registryUrl) {
        this.registryUrl = registryUrl;
    }

    @Override
    public void validateRemoteTypeSpecific() throws RepoConfigException {
        setRegistryUrl(ofNullable(getRegistryUrl()).orElse(DEFAULT_COMPOSER_REGISTRY));
        super.validateRemoteTypeSpecific();
    }

    @Override
    public RepoType getRepoType() {
        return RepoType.Composer;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
