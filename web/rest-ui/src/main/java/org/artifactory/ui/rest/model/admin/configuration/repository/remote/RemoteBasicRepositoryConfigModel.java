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

package org.artifactory.ui.rest.model.admin.configuration.repository.remote;

import org.artifactory.descriptor.delegation.ContentSynchronisation;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalBasicRepositoryConfigModel;

import static org.artifactory.repo.config.RepoConfigDefaultValues.DEFAULT_DELEGATION_CONTEXT;
import static org.artifactory.repo.config.RepoConfigDefaultValues.DEFAULT_OFFLINE;

/**
 * @author Aviad Shikloshi
 * @author Dan Feldman
 */
public class RemoteBasicRepositoryConfigModel extends LocalBasicRepositoryConfigModel {

    protected String url;
    private String remoteLayoutMapping;
    protected Boolean offline = DEFAULT_OFFLINE;
    private ContentSynchronisation contentSynchronisation = DEFAULT_DELEGATION_CONTEXT;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRemoteLayoutMapping() {
        return remoteLayoutMapping;
    }

    public void setRemoteLayoutMapping(String remoteLayoutMapping) {
        this.remoteLayoutMapping = remoteLayoutMapping;
    }

    public Boolean isOffline() {
        return offline;
    }

    public void setOffline(Boolean offline) {
        this.offline = offline;
    }

    public ContentSynchronisation getContentSynchronisation() {
        return contentSynchronisation;
    }

    public void setContentSynchronisation(ContentSynchronisation contentSynchronisation) {
        this.contentSynchronisation = contentSynchronisation;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
