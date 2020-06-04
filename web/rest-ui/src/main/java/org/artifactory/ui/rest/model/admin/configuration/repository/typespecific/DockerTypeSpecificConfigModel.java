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

import com.google.common.collect.Lists;
import org.artifactory.addon.AddonsManager;
import org.artifactory.descriptor.repo.DockerApiVersion;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.rest.common.util.JsonUtil;

import java.util.List;

import static java.util.Optional.ofNullable;
import static org.artifactory.repo.config.RepoConfigDefaultValues.*;

/**
 * @author Dan Feldman
 */
public class DockerTypeSpecificConfigModel implements TypeSpecificConfigModel {
    
    //local
    protected DockerApiVersion dockerApiVersion = DEFAULT_DOCKER_API_VER;
    protected int maxUniqueTags = DEFAULT_MAX_UNIQUE_TAGS;
    private Boolean blockPushingSchema1 = DEFAULT_DOCKER_BLOCK_PUSHING_SCHEMA1;

    //remote
    protected Boolean enableTokenAuthentication = DEFAULT_TOKEN_AUTH;
    protected Boolean listRemoteFolderItems = DEFAULT_LIST_REMOTE_ITEMS_UNSUPPORTED_TYPE;
    private Boolean enableForeignLayersCaching = false;
    private List<String> externalPatterns = Lists.newArrayList("**");

    //virtual
    private Boolean resolveDockerTagsByTimestamp = DEFAULT_DOCKER_VIRTUAL_RESOLVE_TAGS_BY_TIMESTAMP;


    public DockerApiVersion getDockerApiVersion() {
        return dockerApiVersion;
    }

    public void setDockerApiVersion(DockerApiVersion dockerApiVersion) {
        this.dockerApiVersion = dockerApiVersion;
    }

    public int getMaxUniqueTags() {
        return maxUniqueTags;
    }

    public Boolean getBlockPushingSchema1() {
        return blockPushingSchema1;
    }

    public void setBlockPushingSchema1(Boolean blockPushingSchema1) {
        this.blockPushingSchema1 = blockPushingSchema1;
    }

    public void setMaxUniqueTags(int maxUniqueTags) {
        this.maxUniqueTags = maxUniqueTags;
    }

    public Boolean isEnableTokenAuthentication() {
        return enableTokenAuthentication;
    }

    public void setEnableTokenAuthentication(Boolean enableTokenAuthentication) {
        this.enableTokenAuthentication = enableTokenAuthentication;
    }

    public Boolean isListRemoteFolderItems() {
        return listRemoteFolderItems;
    }

    public void setListRemoteFolderItems(Boolean listRemoteFolderItems) {
        this.listRemoteFolderItems = listRemoteFolderItems;
    }

    public Boolean getEnableForeignLayersCaching() {
        return enableForeignLayersCaching;
    }

    public void setEnableForeignLayersCaching(Boolean enableForeignLayersCaching) {
        this.enableForeignLayersCaching = enableForeignLayersCaching;
    }

    public List<String> getExternalPatterns() {
        return externalPatterns;
    }

    public void setExternalPatterns(List<String> externalPatterns) {
        this.externalPatterns = externalPatterns;
    }

    public Boolean getResolveDockerTagsByTimestamp() {
        return resolveDockerTagsByTimestamp;
    }

    public void setResolveDockerTagsByTimestamp(Boolean resolveDockerTagsByTimestamp) {
        this.resolveDockerTagsByTimestamp = resolveDockerTagsByTimestamp;
    }

    @Override
    public void validateLocalTypeSpecific() {
        setDockerApiVersion(ofNullable(getDockerApiVersion()).orElse(DEFAULT_DOCKER_API_VER));
        setMaxUniqueTags(getMaxUniqueTags());
    }

    @Override
    public void validateRemoteTypeSpecific() {
        setEnableTokenAuthentication(ofNullable(isEnableTokenAuthentication()).orElse(DEFAULT_TOKEN_AUTH));
        setListRemoteFolderItems(ofNullable(isListRemoteFolderItems())
                .orElse(DEFAULT_LIST_REMOTE_ITEMS_UNSUPPORTED_TYPE));
    }

    @Override
    public void validateVirtualTypeSpecific(AddonsManager addonsManager) {
        setResolveDockerTagsByTimestamp(ofNullable(getResolveDockerTagsByTimestamp())
                .orElse(DEFAULT_DOCKER_VIRTUAL_RESOLVE_TAGS_BY_TIMESTAMP));
    }

    @Override
    public RepoType getRepoType() {
        return RepoType.Docker;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
