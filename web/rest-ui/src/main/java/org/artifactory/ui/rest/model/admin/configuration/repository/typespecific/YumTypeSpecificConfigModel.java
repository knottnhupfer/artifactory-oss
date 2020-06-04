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
import org.artifactory.rest.common.util.JsonUtil;

import static java.util.Optional.ofNullable;
import static org.artifactory.repo.config.RepoConfigDefaultValues.*;

/**
 * @author Dan Feldman
 */
public class YumTypeSpecificConfigModel implements TypeSpecificConfigModel {

    //local
    private Integer metadataFolderDepth = DEFAULT_YUM_METADATA_DEPTH;
    private String groupFileNames = DEFAULT_YUM_GROUPFILE_NAME;
    private Boolean autoCalculateYumMetadata = DEFAULT_YUM_AUTO_CALCULATE;
    private Boolean enableFileListsIndexing = DEFAULT_ENABLE_FILELIST_INDEXING;
    protected Boolean listRemoteFolderItems = DEFAULT_LIST_REMOTE_ITEMS_SUPPORTED_TYPE;

    //virtual
    private Long virtualRetrievalCachePeriodSecs = DEFAULT_VIRTUAL_RETRIEVAL_CACHE_PERIOD;

    public Integer getMetadataFolderDepth() {
        return metadataFolderDepth;
    }

    public void setMetadataFolderDepth(Integer metadataFolderDepth) {
        this.metadataFolderDepth = metadataFolderDepth;
    }

    public String getGroupFileNames() {
        return groupFileNames;
    }

    public void setGroupFileNames(String groupFileNames) {
        this.groupFileNames = groupFileNames;
    }

    public Boolean isAutoCalculateYumMetadata() {
        return autoCalculateYumMetadata;
    }

    public void setAutoCalculateYumMetadata(Boolean autoCalculateYumMetadata) {
        this.autoCalculateYumMetadata = autoCalculateYumMetadata;
    }

    public Boolean isEnableFileListsIndexing() {
        return enableFileListsIndexing;
    }

    public void setEnableFileListsIndexing(Boolean enableFileListsIndexing) {
        this.enableFileListsIndexing = enableFileListsIndexing;
    }

    public Boolean isListRemoteFolderItems() {
        return listRemoteFolderItems;
    }

    public void setListRemoteFolderItems(Boolean listRemoteFolderItems) {
        this.listRemoteFolderItems = listRemoteFolderItems;
    }

    public Long getVirtualRetrievalCachePeriodSecs() {
        return virtualRetrievalCachePeriodSecs;
    }

    public void setVirtualRetrievalCachePeriodSecs(Long virtualRetrievalCachePeriodSecs) {
        this.virtualRetrievalCachePeriodSecs = virtualRetrievalCachePeriodSecs;
    }

    @Override
    public void validateLocalTypeSpecific() {
        setGroupFileNames(ofNullable(getGroupFileNames()).orElse(DEFAULT_YUM_GROUPFILE_NAME));
        setMetadataFolderDepth(ofNullable(getMetadataFolderDepth()).orElse(DEFAULT_YUM_METADATA_DEPTH));
        setAutoCalculateYumMetadata(ofNullable(isAutoCalculateYumMetadata()).orElse(DEFAULT_YUM_AUTO_CALCULATE));
        setEnableFileListsIndexing(ofNullable(isEnableFileListsIndexing()).orElse(DEFAULT_ENABLE_FILELIST_INDEXING));
    }

    @Override
    public void validateRemoteTypeSpecific() {
        setListRemoteFolderItems(ofNullable(isListRemoteFolderItems())
                .orElse(DEFAULT_LIST_REMOTE_ITEMS_SUPPORTED_TYPE));
    }

    @Override
    public void validateVirtualTypeSpecific(AddonsManager addonsManager) {
        setVirtualRetrievalCachePeriodSecs(ofNullable(getVirtualRetrievalCachePeriodSecs())
                .orElse(DEFAULT_VIRTUAL_RETRIEVAL_CACHE_PERIOD));
    }

    @Override
    public RepoType getRepoType() {
        return RepoType.YUM;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
