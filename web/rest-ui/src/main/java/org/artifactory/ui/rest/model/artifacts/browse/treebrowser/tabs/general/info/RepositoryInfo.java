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

import com.google.common.base.Joiner;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.debian.DebianAddon;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.descriptor.repo.*;
import org.artifactory.descriptor.repo.distribution.DistributionRepoDescriptor;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.request.RequestThreadLocal;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

/**
 * @author Chen Keinan
 */
@JsonPropertyOrder({"name", "repoType", "repositoryPath", "repositoryLayout", "description", "artifactsCount", "bintrayOrg",
        "bintrayProduct", "created", "watchingSince", "lastReplicationStatus", "signingKeyLink"})
public class RepositoryInfo extends BaseInfo {

    private String remoteRepoUrl;
    private String repositoryLayout;
    private String description;
    private String bintrayOrg;
    private String bintrayProduct;
    private String created;
    private String watchingSince;
    private String lastReplicationStatus;
    private String signingKeyLink;
    private int artifactsCount;

    public RepositoryInfo() {
    }

    public RepositoryInfo(RepoDescriptor repoDescriptor, RepoPath repoPath) {
        setRepositoryDescription(repoDescriptor);
        setName(repoPath.getRepoKey());
        setRepositoryPath(repoPath.getRepoKey() + "/");
        setRepositoryLayout(repoDescriptor);
        setRepositoryType(repoDescriptor);

        if (repoDescriptor instanceof LocalRepoDescriptor) {
            setCreatedSinceData(repoPath);
            setLastReplicationStatus(getLastReplicationInfo(repoPath));
            setGpgKeyLink(repoDescriptor);
            setDistributionDetails(repoDescriptor);
        } else if (repoDescriptor instanceof RemoteRepoDescriptor) {
            setRemoteRepositoryUrl(repoDescriptor);
            setIsSmart(repoDescriptor);
        }
    }

    private void setIsSmart(RepoDescriptor repoDescriptor) {
        if (repoDescriptor != null) {
            this.setSmartRepo(((RemoteRepoDescriptor) repoDescriptor).getContentSynchronisation().isEnabled());
        }
    }

    private void setRemoteRepositoryUrl(RepoDescriptor repoDescriptor) {
        if (repoDescriptor != null) {
            this.setRemoteRepoUrl(((RemoteRepoDescriptor) repoDescriptor).getUrl());
        }
    }

    private void setRepositoryDescription(RepoDescriptor repoDescriptor) {
        if (repoDescriptor != null) {
            this.setDescription(repoDescriptor.getDescription());
        }
    }

    private void setRepositoryLayout(RepoDescriptor repoDescriptor) {
        if (repoDescriptor != null && repoDescriptor.getRepoLayout() != null) {
            this.setRepositoryLayout(repoDescriptor.getRepoLayout().getName());
        }
    }

    private void setRepositoryType(RepoDescriptor repoDescriptor) {
        if (repoDescriptor != null && repoDescriptor.getType() != null) {
            this.setRepoType(repoDescriptor.getType().name());
        }
    }

    //Add gpg public key under general info in Debian repository only
    private void setGpgKeyLink(RepoDescriptor repoDescriptor) {
        RepoType repoType = repoDescriptor.getType();
        if (repoType.equals(RepoType.Debian) || repoType.equals(RepoType.Opkg)) {
            AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
            DebianAddon debianAddon = addonsManager.addonByType(DebianAddon.class);
            if (debianAddon.hasPublicKey()) {
                String gpgLink = Joiner.on('/').join(RequestThreadLocal.getBaseUrl(), "api", "gpg", "key/public");
                setSigningKeyLink(gpgLink);
            }
        }
    }

    private void setDistributionDetails(RepoDescriptor repoDescriptor) {
        if (repoDescriptor != null && repoDescriptor instanceof DistributionRepoDescriptor) {
            DistributionRepoDescriptor descriptor = (DistributionRepoDescriptor) repoDescriptor;
            BintrayApplicationConfig bintrayApplication = descriptor.getBintrayApplication();
            if (bintrayApplication != null) {
                this.setBintrayOrg(bintrayApplication.getOrg());
            }
            this.setBintrayProduct(descriptor.getProductName());
        }
    }

    private void setCreatedSinceData(RepoPath repoPath) {
        ArtifactoryContext context = ContextHelper.get();
        ItemInfo itemInfo = context.getRepositoryService().getItemInfo(repoPath);
        setWatchingSince(fetchWatchingSince(repoPath));
        String created = context.getCentralConfig().format(itemInfo.getCreated());
        this.setCreated(created);
    }

    public String getRemoteRepoUrl() {
        return remoteRepoUrl;
    }

    public void setRemoteRepoUrl(String remoteRepoUrl) {
        this.remoteRepoUrl = remoteRepoUrl;
    }

    public String getRepositoryLayout() {
        return repositoryLayout;
    }

    public void setRepositoryLayout(String repositoryLayout) {
        this.repositoryLayout = repositoryLayout;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBintrayOrg() {
        return bintrayOrg;
    }

    public void setBintrayOrg(String bintrayOrg) {
        this.bintrayOrg = bintrayOrg;
    }

    public String getBintrayProduct() {
        return bintrayProduct;
    }

    public void setBintrayProduct(String bintrayProduct) {
        this.bintrayProduct = bintrayProduct;
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

    public String getLastReplicationStatus() {
        return lastReplicationStatus;
    }

    public void setLastReplicationStatus(String lastReplicationStatus) {
        this.lastReplicationStatus = lastReplicationStatus;
    }

    public String getSigningKeyLink() {
        return signingKeyLink;
    }

    public void setSigningKeyLink(String signingKeyLink) {
        this.signingKeyLink = signingKeyLink;
    }

    public int getArtifactsCount() {
        return artifactsCount;
    }

    public void setArtifactsCount(int artifactsCount) {
        this.artifactsCount = artifactsCount;
    }
}
