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

package org.artifactory.ui.rest.model.admin.configuration.repository;

import org.artifactory.rest.common.model.RestModel;
import org.artifactory.ui.rest.model.admin.configuration.propertysets.PropertySetNameModel;
import org.artifactory.ui.rest.model.admin.configuration.reverseProxy.ReverseProxyRepoModel;

import java.util.List;

/**
 * @author Dan Feldman
 */
public interface AdvancedRepositoryConfigModel extends RestModel {

    List<PropertySetNameModel> getPropertySets();

    void setPropertySets(List<PropertySetNameModel> propertySets);

    Boolean isBlackedOut();

    void setBlackedOut(Boolean blackedOut);

    Boolean getAllowContentBrowsing();

    void setAllowContentBrowsing(Boolean allowContentBrowsing);

    ReverseProxyRepoModel getReverseProxy();

    void setReverseProxy(ReverseProxyRepoModel reverseProxy);

    String toString();

    DownloadRedirectRepoConfigModel getDownloadRedirectConfig();

    void setDownloadRedirectConfig(DownloadRedirectRepoConfigModel downloadRedirectRepoConfig);
}
