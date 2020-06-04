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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.info;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryBrowsingService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.repo.VirtualRepoItem;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.repo.BintrayApplicationConfig;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.descriptor.repo.distribution.DistributionRepoDescriptor;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.RepoPath;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.jfrog.client.util.PathUtils;

/**
 * @author Chen Keinan
 */
@JsonPropertyOrder(
    {
        "name", "repositoryPath", "bintrayUrl",
        "deployedBy", "artifactsCount", "created", "watchingSince", "lastReplicationStatus"
    }
)
public class FolderInfo extends BaseInfo {

    private String deployedBy;
    private String created;
    private String watchingSince;
    private int artifactsCount;

    public FolderInfo() {
    }

    public FolderInfo(RepoPath repoPath, boolean isLocal) {
        if (isLocal) {
            populateFolderInfo(repoPath);
        } else {
            populateVirtualRemoteFolderInfo(repoPath);
        }
    }

    private void populateFolderInfo(RepoPath repoPath) {
        RepositoryService repoService = ContextHelper.get().beanForType(RepositoryService.class);
        CentralConfigService centralConfigService = ContextHelper.get().beanForType(CentralConfigService.class);

        // set name
        this.setName(repoPath.getName());
        // set repository path
        this.setRepositoryPath(repoPath.getRepoKey() + "/" + repoPath.getPath() + "/");
        ItemInfo itemInfo = repoService.getItemInfo(repoPath);
        // set watching since
        setWatchingSince(fetchWatchingSince(repoPath));
        // set created
        setCreated(centralConfigService, itemInfo);
        // set deployed by
        this.setDeployedBy(itemInfo.getModifiedBy());
        setBintrayUrl(fetchBintrayUrl(repoService, repoPath));
    }

    private void populateVirtualRemoteFolderInfo(RepoPath repoPath) {
        // set name
        this.setName(repoPath.getName());
        // set repository path
        this.setRepositoryPath(repoPath.getRepoKey() + "/" + repoPath.getPath() + "/");

        addVirtualItemDetails(repoPath);
    }

    public String getDeployedBy() {
        return deployedBy;
    }

    public void setDeployedBy(String deployedBy) {
        this.deployedBy = deployedBy;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getWatchingSince() {
        return watchingSince;
    }

    public void setWatchingSince(String watchingSince) {
        this.watchingSince = watchingSince;
    }

    public int getArtifactsCount() {
        return artifactsCount;
    }

    public void setArtifactsCount(int artifactsCount) {
        this.artifactsCount = artifactsCount;
    }

    private String fetchBintrayUrl(RepositoryService repoService, RepoPath repoPath) {
        LocalRepoDescriptor repoDescriptor = repoService.localCachedOrDistributionRepoDescriptorByKey(repoPath.getRepoKey());
        if (repoDescriptor != null && repoDescriptor instanceof DistributionRepoDescriptor) {
            DistributionRepoDescriptor descriptor = (DistributionRepoDescriptor) repoDescriptor;
            BintrayApplicationConfig bintrayApplication = descriptor.getBintrayApplication();
            if (bintrayApplication != null) {
                String path = repoPath.getPath();
                boolean isFileOrFolder = PathUtils.getPathElements(path).length > 3;
                if (!isFileOrFolder) {
                    return ConstantValues.bintrayUrl.getString() + "/" + bintrayApplication.getOrg() + "/" + path;
                }
            }
        }
        return null;
    }

    private void addVirtualItemDetails(RepoPath repoPath) {
        ArtifactoryContext context = ContextHelper.get();
        RepositoryService repoService = context.getRepositoryService();
        VirtualRepoDescriptor virtual = repoService.virtualRepoDescriptorByKey(repoPath.getRepoKey());
        if (virtual != null) {
            RepositoryBrowsingService browsingService = context.beanForType(RepositoryBrowsingService.class);
            VirtualRepoItem item = browsingService.getVirtualRepoItem(repoPath);
            if (item != null && item.getItemInfo() != null) {
                CentralConfigService centralConfig = context.getCentralConfig();
                this.setCreated(centralConfig.format(item.getItemInfo().getCreated()));
            }
        }
    }

    private void setCreated(CentralConfigService centralConfigService, ItemInfo itemInfo) {
        String created = centralConfigService.format(itemInfo.getCreated()) + " " + DurationFormatUtils.formatDuration(
                System.currentTimeMillis() - itemInfo.getCreated(), "(d'd' H'h' m'm' s's' ago)");
        this.setCreated(created);
    }
}
