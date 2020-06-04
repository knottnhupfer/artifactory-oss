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

package org.artifactory.ui.rest.model.admin.configuration.repository.info;

import org.artifactory.api.rest.restmodel.JsonUtil;
import org.artifactory.rest.common.model.RestModel;

import java.util.List;

/**
 * @author Aviad Shikloshi
 */
public class AvailableRepositories implements RestModel {

    private List<String> availableLocalRepos;
    private List<String> availableRemoteRepos;
    private List<String> availableVirtualRepos;

    public AvailableRepositories() {
    }

    public AvailableRepositories(List<String> availableLocalRepos, List<String> availableRemoteRepos,
            List<String> availableVirtualRepos) {
        this.availableLocalRepos = availableLocalRepos;
        this.availableRemoteRepos = availableRemoteRepos;
        this.availableVirtualRepos = availableVirtualRepos;
    }

    public List<String> getAvailableLocalRepos() {
        return availableLocalRepos;
    }

    public List<String> getAvailableRemoteRepos() {
        return availableRemoteRepos;
    }

    public List<String> getAvailableVirtualRepos() {
        return availableVirtualRepos;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
