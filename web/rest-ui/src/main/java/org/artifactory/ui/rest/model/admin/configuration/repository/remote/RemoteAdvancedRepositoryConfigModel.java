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

import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalAdvancedRepositoryConfigModel;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

import static org.artifactory.repo.config.RepoConfigDefaultValues.*;

/**
 * @author Aviad Shikloshi
 * @author Dan Feldman
 */
@JsonIgnoreProperties("localChecksumPolicy")
public class RemoteAdvancedRepositoryConfigModel extends LocalAdvancedRepositoryConfigModel {

    protected RemoteNetworkRepositoryConfigModel network;
    protected RemoteCacheRepositoryConfigModel cache;
    protected String queryParams;
    protected boolean propagateQueryParams;
    protected List<String> mismatchingMimeTypesOverrideList;


    @JsonIgnore
    protected Boolean hardFail = DEFAULT_HARD_FAIL;
    protected Boolean storeArtifactsLocally = DEFAULT_STORE_ARTIFACTS_LOCALLY;
    protected Boolean synchronizeArtifactProperties = DEFAULT_SYNC_PROPERTIES;
    private Boolean shareConfiguration = DEFAULT_SHARE_CONFIG;
    protected boolean blockMismatchingMimeTypes = DEFAULT_BLOCK_MISMATCHING_MIME_TYPES;
    protected boolean bypassHeadRequests = DEFAULT_BYPASS_HEAD_REQUESTS;

    public RemoteNetworkRepositoryConfigModel getNetwork() {
        return network;
    }

    public void setNetwork(RemoteNetworkRepositoryConfigModel network) {
        this.network = network;
    }

    public RemoteCacheRepositoryConfigModel getCache() {
        return cache;
    }

    public void setCache(RemoteCacheRepositoryConfigModel cache) {
        this.cache = cache;
    }

    public String getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(String queryParams) {
        this.queryParams = queryParams;
    }

    public Boolean getHardFail() {
        return hardFail;
    }

    public void setHardFail(Boolean hardFail) {
        this.hardFail = hardFail;
    }


    public Boolean isStoreArtifactsLocally() {
        return storeArtifactsLocally;
    }

    public void setStoreArtifactsLocally(Boolean storeArtifactsLocally) {
        this.storeArtifactsLocally = storeArtifactsLocally;
    }

    public Boolean getSynchronizeArtifactProperties() {
        return synchronizeArtifactProperties;
    }

    public void setSynchronizeArtifactProperties(Boolean synchronizeArtifactProperties) {
        this.synchronizeArtifactProperties = synchronizeArtifactProperties;
    }

    public Boolean isShareConfiguration() {
        return shareConfiguration;
    }

    public void setShareConfiguration(Boolean shareConfiguration) {
        this.shareConfiguration = shareConfiguration;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }

    public boolean isPropagateQueryParams() {
        return propagateQueryParams;
    }

    public void setPropagateQueryParams(boolean propagateQueryParams) {
        this.propagateQueryParams = propagateQueryParams;
    }

    public List<String> getMismatchingMimeTypesOverrideList() {
        return mismatchingMimeTypesOverrideList;
    }

    public void setMismatchingMimeTypesOverrideList(List<String> mismatchingMimeTypesOverrideList) {
        this.mismatchingMimeTypesOverrideList = mismatchingMimeTypesOverrideList;
    }

    public boolean isBlockMismatchingMimeTypes() {
        return blockMismatchingMimeTypes;
    }

    public void setBlockMismatchingMimeTypes(boolean blockMismatchingMimeTypes) {
        this.blockMismatchingMimeTypes = blockMismatchingMimeTypes;
    }

    public boolean isBypassHeadRequests() {
        return bypassHeadRequests;
    }

    public void setBypassHeadRequests(boolean bypassHeadRequests) {
        this.bypassHeadRequests = bypassHeadRequests;
    }
}
