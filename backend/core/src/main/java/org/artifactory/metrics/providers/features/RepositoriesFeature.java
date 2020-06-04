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

package org.artifactory.metrics.providers.features;

import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.api.callhome.FeatureGroup;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.descriptor.download.FolderDownloadConfigDescriptor;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.replication.RemoteReplicationDescriptor;
import org.artifactory.descriptor.repo.*;
import org.artifactory.descriptor.repo.distribution.DistributionRepoDescriptor;
import org.artifactory.descriptor.trashcan.TrashcanConfigDescriptor;
import org.artifactory.repo.LocalCacheRepo;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.repo.virtual.VirtualRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * This class represent the repositories feature group of the CallHome feature
 *
 * @author Shay Bagants
 */
@Component
public class RepositoriesFeature implements CallHomeFeature {

    @Autowired
    private CentralConfigService configService;

    @Autowired
    private InternalRepositoryService repoService;

    private static String PACKAGE_TYPE = "package_type";

    @Override
    public FeatureGroup getFeature() {

        FeatureGroup repositoriesFeature = new FeatureGroup("repositories");
        FeatureGroup localRepositoriesFeature = new FeatureGroup("local repositories");
        FeatureGroup remoteRepositoriesFeature = new FeatureGroup("remote repositories");
        FeatureGroup distributionRepositoriesFeature = new FeatureGroup("distribution repositories");

        FeatureGroup trashCanFeature = new FeatureGroup("Trashcan");
        TrashcanConfigDescriptor trashcanConfig = configService.getDescriptor().getTrashcanConfig();
        trashCanFeature.addFeatureAttribute("enabled",
                trashcanConfig.isEnabled());
        trashCanFeature.addFeatureAttribute("retention_period",
                trashcanConfig.getRetentionPeriodDays());

        FeatureGroup folderDownloadFeature = new FeatureGroup("Folder Download");
        FolderDownloadConfigDescriptor folderDownloadConfig = configService.getDescriptor().getFolderDownloadConfig();
        folderDownloadFeature.addFeatureAttribute("enabled", folderDownloadConfig.isEnabled());
        folderDownloadFeature.addFeatureAttribute("enabledForAnonymous", folderDownloadConfig.isEnabledForAnonymous());
        folderDownloadFeature.addFeatureAttribute("max_size", folderDownloadConfig.getMaxDownloadSizeMb());
        folderDownloadFeature.addFeatureAttribute("max_files", folderDownloadConfig.getMaxFiles());
        folderDownloadFeature.addFeatureAttribute("max_parallel_downloads", folderDownloadConfig.getMaxConcurrentRequests());

        List<RealRepoDescriptor> localAndRemoteRepositoriesDescriptors = repoService.getLocalAndRemoteRepoDescriptors();
        List<DistributionRepoDescriptor> distributionRepositoriesDescriptors = repoService
                .getDistributionRepoDescriptors();

        localAndRemoteRepositoriesDescriptors.forEach(rr -> {
            if (rr.isLocal()) {
                addLocalRepoFeatures(localRepositoriesFeature, rr);
            } else {
                addRemoteRepoFeatures(remoteRepositoriesFeature, rr);
            }
        });
        distributionRepositoriesDescriptors.forEach(
                dr -> addDistributionRepoFeatures(distributionRepositoriesFeature, dr)
        );

        long localCount = localAndRemoteRepositoriesDescriptors.stream()
                .filter(RealRepoDescriptor::isLocal)
                .count();
        String numOfRepos = "number_of_repositories";
        localRepositoriesFeature.addFeatureAttribute(numOfRepos, localCount);
        remoteRepositoriesFeature.addFeatureAttribute(numOfRepos,
                localAndRemoteRepositoriesDescriptors.size() - localCount);
        distributionRepositoriesFeature.addFeatureAttribute(numOfRepos,
                distributionRepositoriesDescriptors.size());

        repositoriesFeature.addFeature(localRepositoriesFeature);
        repositoriesFeature.addFeature(remoteRepositoriesFeature);
        repositoriesFeature.addFeature(distributionRepositoriesFeature);

        // virtual repos
        FeatureGroup virtualRepositoriesFeature = new FeatureGroup("virtual repositories");
        List<VirtualRepo> virtualRepositories = repoService.getVirtualRepositories();
        virtualRepositoriesFeature
                .addFeatureAttribute(numOfRepos, virtualRepositories.size());
        addVirtualRepoFeatures(virtualRepositoriesFeature, virtualRepositories);
        repositoriesFeature.addFeature(virtualRepositoriesFeature);

        repositoriesFeature.addFeature(trashCanFeature);
        repositoriesFeature.addFeature(folderDownloadFeature);

        return repositoriesFeature;
    }

    /**
     * Collects virtual repo metadata  {@see RTFACT-8412}
     */
    private void addLocalRepoFeatures(FeatureGroup localRepositoriesFeature,
            final RealRepoDescriptor localRepoDescriptor) {
        // local repos
        FeatureGroup localRepo = createRepoFeatureGroup(localRepoDescriptor);
        LocalReplicationDescriptor localReplication = configService.getDescriptor().getEnabledLocalReplication(localRepoDescriptor.getKey());
        if (localReplication != null && localReplication.isEnabled()) {
            List<LocalReplicationDescriptor> repls = configService.getDescriptor().getMultiLocalReplications(localRepoDescriptor.getKey());
            localRepo.addFeatureAttribute("push_replication", (repls == null || repls.isEmpty() ? "false" : (repls.size() > 1 ? "multi" : "true")));
            localRepo.addFeatureAttribute("event_replication", localReplication.isEnableEventReplication());
            localRepo.addFeatureAttribute("sync_properties", localReplication.isSyncProperties());
            localRepo.addFeatureAttribute("sync_deleted", localReplication.isSyncDeletes());
        } else if (localReplication == null) {
            localRepo.addFeatureAttribute("push_replication", false);
            localRepo.addFeatureAttribute("event_replication", false);
            localRepo.addFeatureAttribute("sync_deleted", false);
        }
        if (isJcrVersion() && RepoType.Docker == localRepoDescriptor.getType()) {
            localRepo.addFeatureAttribute("image_count", countNumberOfManifestInRepo(localRepoDescriptor.getKey()));
        }
        localRepositoriesFeature.addFeature(localRepo);
    }

    /**
     * Collects virtual repo metadata  {@see RTFACT-8412}
     */
    private void addRemoteRepoFeatures(FeatureGroup remoteRepositoriesFeature, final RealRepoDescriptor remoteRepoDescriptor) {
        // remote repos
        FeatureGroup remoteRepo = createRepoFeatureGroup(remoteRepoDescriptor);
        RemoteReplicationDescriptor remoteReplicationDescriptor = configService.getDescriptor().getRemoteReplication(remoteRepoDescriptor.getKey());
        if (remoteReplicationDescriptor != null) {
            remoteRepo.addFeatureAttribute("pull_replication", remoteReplicationDescriptor.isEnabled());
            if (remoteReplicationDescriptor.isEnabled()) {
                remoteRepo.addFeatureAttribute("pull_replication_url", ((RemoteRepoDescriptor) remoteRepoDescriptor).getUrl());
                remoteRepo.addFeatureAttribute("event_replication", remoteReplicationDescriptor.isEnableEventReplication());
            }
        } else {
            remoteRepo.addFeatureAttribute("pull_replication", false);
        }
        if (isJcrVersion() && RepoType.Docker == remoteRepoDescriptor.getType()) {
            remoteRepo.addFeatureAttribute("is_smart_repo", isSmartRepo((HttpRepoDescriptor) remoteRepoDescriptor));
            LocalCacheRepo localCacheRepo = repoService.remoteRepositoryByKey(remoteRepoDescriptor.getKey())
                    .getLocalCacheRepo();
            if (localCacheRepo != null) {
                remoteRepo.addFeatureAttribute("image_count", countNumberOfManifestInRepo(localCacheRepo.getKey()));
            }
        }
        remoteRepositoriesFeature.addFeature(remoteRepo);
    }

    private boolean isSmartRepo(HttpRepoDescriptor remoteRepoDescriptor) {
        return remoteRepoDescriptor.getContentSynchronisation().isEnabled();
    }

    private FeatureGroup createRepoFeatureGroup(RealRepoDescriptor descriptor) {
        FeatureGroup repo = new FeatureGroup(descriptor.getKey());
        repo.addFeatureAttribute(PACKAGE_TYPE, descriptor.getType().name());
        RepoLayout repoLayout = descriptor.getRepoLayout();
        if (repoLayout != null) {
            repo.addFeatureAttribute("repository_layout", repoLayout.getName());
        }
        return repo;
    }

    /**
     * Collects virtual repo metadata  {@see RTFACT-10170}
     */
    private void addDistributionRepoFeatures(FeatureGroup distributionRepositoriesFeature,
            final DistributionRepoDescriptor distributionRepoDescriptor) {
        // distribution repos
        FeatureGroup distRepo = new FeatureGroup(distributionRepoDescriptor.getKey());
        distRepo.addFeatureAttribute("target_repo_license",
                distributionRepoDescriptor.isDefaultNewRepoPremium() ? "premium" : "oss");
        distRepo.addFeatureAttribute("visibility",
                distributionRepoDescriptor.isDefaultNewRepoPrivate() ? "private" : "public");
        distRepo.addFeatureAttribute("distribute_product",
                StringUtils.isNotBlank(distributionRepoDescriptor.getProductName()));
        distributionRepositoriesFeature.addFeature(distRepo);
    }

    private int countNumberOfManifestInRepo(String repoKey) {
        return repoService.countFiles(repoKey, "manifest.json");
    }

    private boolean isJcrVersion() {
        return ContextHelper.get().beanForType(AddonsManager.class).getArtifactoryRunningMode().isJcrOrJcrAol() ;
    }
    /**
     * Collects virtual repo metadata  {@see RTFACT-8412}
     */
    private void addVirtualRepoFeatures(FeatureGroup virtualRepositoriesFeature, List<VirtualRepo> virtualRepositories) {
        virtualRepositories.forEach(vr -> {
            FeatureGroup virtualRepo = new FeatureGroup(vr.getKey());
            virtualRepo.addFeatureAttribute("number_of_included_repositories",
                    vr.getResolvedLocalRepos().size() + vr.getResolvedRemoteRepos().size());
            virtualRepo.addFeatureAttribute(PACKAGE_TYPE, vr.getDescriptor().getType().name());
            if (vr.getDescriptor().getRepoLayout() != null) {
                virtualRepo.addFeatureAttribute("repository_layout", vr.getDescriptor().getRepoLayout().getName());
            }
            if (vr.getDescriptor().getDefaultDeploymentRepo() != null) {
                virtualRepo.addFeatureAttribute("configured_local_deployment",
                        vr.getDescriptor().getDefaultDeploymentRepo().getKey());
            }
            virtualRepositoriesFeature.addFeature(virtualRepo);
        });
    }
}
