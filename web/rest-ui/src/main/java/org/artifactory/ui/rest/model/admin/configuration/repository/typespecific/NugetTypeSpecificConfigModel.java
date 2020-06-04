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
import org.artifactory.rest.common.util.JsonUtil;

import static java.util.Optional.ofNullable;
import static org.artifactory.repo.config.RepoConfigDefaultValues.*;

/**
 * @author Dan Feldman
 */
public class NugetTypeSpecificConfigModel implements TypeSpecificConfigModel {

    protected Boolean forceNugetAuthentication = DEFAULT_FORCE_NUGET_AUTH;

    //local
    protected Integer maxUniqueSnapshots = DEFAULT_MAX_UNIQUE_SNAPSHOTS;

    //remote
    protected String feedContextPath = DEFAULT_NUGET_FEED_PATH;
    protected String downloadContextPath = DEFAULT_NUGET_DOWNLOAD_PATH;
    protected Boolean listRemoteFolderItems = DEFAULT_LIST_REMOTE_ITEMS_UNSUPPORTED_TYPE;
    protected String v3FeedUrl = DEFAULT_NUGET_V3_URL;

    public String getFeedContextPath() {
        return feedContextPath;
    }

    public void setFeedContextPath(String feedContextPath) {
        this.feedContextPath = feedContextPath;
    }

    public String getDownloadContextPath() {
        return downloadContextPath;
    }

    public void setDownloadContextPath(String downloadContextPath) {
        this.downloadContextPath = downloadContextPath;
    }

    public Integer getMaxUniqueSnapshots() {
        return maxUniqueSnapshots;
    }

    public void setMaxUniqueSnapshots(Integer maxUniqueSnapshots) {
        this.maxUniqueSnapshots = maxUniqueSnapshots;
    }

    public Boolean isListRemoteFolderItems() {
        return listRemoteFolderItems;
    }

    public void setListRemoteFolderItems(Boolean listRemoteFolderItems) {
        this.listRemoteFolderItems = listRemoteFolderItems;
    }

    public String getV3FeedUrl() {
        return v3FeedUrl;
    }

    public void setV3FeedUrl(String v3FeedUrl) {
        this.v3FeedUrl = v3FeedUrl;
    }

    public Boolean isForceNugetAuthentication() {
        return forceNugetAuthentication;
    }

    public void setForceNugetAuthentication(Boolean forceNugetAuthentication) {
        this.forceNugetAuthentication = forceNugetAuthentication;
    }

    @Override
    public void validateSharedTypeSpecific() {
        setForceNugetAuthentication(ofNullable(isForceNugetAuthentication()).orElse(DEFAULT_FORCE_NUGET_AUTH));
    }

    @Override
    public void validateLocalTypeSpecific() {
        setMaxUniqueSnapshots(ofNullable(getMaxUniqueSnapshots()).orElse(DEFAULT_MAX_UNIQUE_SNAPSHOTS));
    }

    @Override
    public void validateRemoteTypeSpecific() {
        setDownloadContextPath(ofNullable(getDownloadContextPath()).orElse(DEFAULT_NUGET_DOWNLOAD_PATH));
        setFeedContextPath(ofNullable(getFeedContextPath()).orElse(DEFAULT_NUGET_FEED_PATH));
        setListRemoteFolderItems(ofNullable(isListRemoteFolderItems())
                .orElse(DEFAULT_LIST_REMOTE_ITEMS_UNSUPPORTED_TYPE));
        setV3FeedUrl(ofNullable(getV3FeedUrl()).orElse(DEFAULT_NUGET_V3_URL));
    }

    @Override
    public RepoType getRepoType() {
        return RepoType.NuGet;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
