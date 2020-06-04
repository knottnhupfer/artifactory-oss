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

package org.artifactory.ui.rest.model.admin.configuration.repository.local;

import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.admin.configuration.propertysets.PropertySetNameModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.AdvancedRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.DownloadRedirectRepoConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.reverseProxy.ReverseProxyRepoModel;

import java.util.List;

import static org.artifactory.repo.config.RepoConfigDefaultValues.DEFAULT_ALLOW_CONTENT_BROWSING;
import static org.artifactory.repo.config.RepoConfigDefaultValues.DEFAULT_BLACKED_OUT;

/**
 * @author Dan Feldman
 * @author Aviad Shikloshi
 */
public class LocalAdvancedRepositoryConfigModel implements AdvancedRepositoryConfigModel {

    protected List<PropertySetNameModel> propertySets;
    protected Boolean blackedOut = DEFAULT_BLACKED_OUT;
    private Boolean allowContentBrowsing = DEFAULT_ALLOW_CONTENT_BROWSING;
    private ReverseProxyRepoModel reverseProxy;
    private DownloadRedirectRepoConfigModel downloadRedirectRepoConfig;

    @Override
    public List<PropertySetNameModel> getPropertySets() {
        return propertySets;
    }

    @Override
    public void setPropertySets(List<PropertySetNameModel> propertySets) {
        this.propertySets = propertySets;
    }

    @Override
    public Boolean isBlackedOut() {
        return blackedOut;
    }

    @Override
    public void setBlackedOut(Boolean blackedOut) {
        this.blackedOut = blackedOut;
    }

    @Override
    public Boolean getAllowContentBrowsing() {
        return allowContentBrowsing;
    }

    @Override
    public void setAllowContentBrowsing(Boolean allowContentBrowsing) {
        this.allowContentBrowsing = allowContentBrowsing;
    }

    @Override
    public ReverseProxyRepoModel getReverseProxy() {
        return reverseProxy;
    }

    @Override
    public void setReverseProxy(ReverseProxyRepoModel reverseProxy) {
        this.reverseProxy = reverseProxy;
    }

    @Override
    public DownloadRedirectRepoConfigModel getDownloadRedirectConfig() {
        return downloadRedirectRepoConfig;
    }

    @Override
    public void setDownloadRedirectConfig(DownloadRedirectRepoConfigModel downloadRedirectRepoConfig) {
        this.downloadRedirectRepoConfig = downloadRedirectRepoConfig;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
