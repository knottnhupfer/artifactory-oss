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

package org.artifactory.ui.rest.model.admin.services.backups;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.descriptor.backup.BackupDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RealRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.distribution.DistributionRepoDescriptor;
import org.artifactory.descriptor.repo.releasebundle.ReleaseBundlesRepoDescriptor;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.model.RestSpecialFields;
import org.artifactory.rest.common.service.IgnoreSpecialFields;
import org.artifactory.rest.common.util.JsonUtil;
import org.codehaus.jackson.map.annotate.JsonFilter;
import org.jfrog.common.config.diff.DiffIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Chen Keinan
 */
@JsonFilter("exclude fields")
@IgnoreSpecialFields(value = {"excludeRepos", "dir", "retentionPeriodHours",
        "excludedRepositories", "sendMailOnError", "excludeBuilds", "incremental","precalculate"})
public class Backup extends BackupDescriptor implements RestModel, RestSpecialFields {

    private List<String> excludeRepos;
    private List<String> includeRepos;
    @DiffIgnore
    private boolean isEdit = false;
    private boolean incremental;
    private boolean precalculate;

    Backup() {
    }

    public Backup(BackupDescriptor backupDescriptor, boolean isEdit) {
        if (backupDescriptor != null) {
            super.setCronExp(backupDescriptor.getCronExp());
            super.setEnabled(backupDescriptor.isEnabled());
            super.setKey(backupDescriptor.getKey());
            super.setExcludeNewRepositories(backupDescriptor.isExcludeNewRepositories());
            populateditFields(backupDescriptor, isEdit);
        }
    }

    /**
     * populate extra fields require for edit
     *
     * @param backupDescriptor - back up descriptor data
     * @param isEdit           - if true , populate fields for edit
     */
    private void populateditFields(BackupDescriptor backupDescriptor, boolean isEdit) {
        if (isEdit) {
            this.isEdit = isEdit;
            super.setRetentionPeriodHours(backupDescriptor.getRetentionPeriodHours());
            super.setCreateArchive(backupDescriptor.isCreateArchive());
            super.setDir(backupDescriptor.getDir());
            super.setSendMailOnError(backupDescriptor.isSendMailOnError());
            super.setCreateArchive(backupDescriptor.isCreateArchive());
            super.setPrecalculate(backupDescriptor.isPrecalculate());
        }
        populateExcludeRepo(backupDescriptor.getExcludedRepositories());
        populateIncludeRepo();
    }

    /**
     * populate exclude Real repo keys
     *
     * @param realRepoDescriptors - real repo descriptors
     */
    private void populateExcludeRepo(List<RealRepoDescriptor> realRepoDescriptors) {
        List<String> excludeRealRepo = new ArrayList<>();
        realRepoDescriptors.forEach(realRepo -> excludeRealRepo.add(realRepo.getKey()));
        excludeRepos = excludeRealRepo;
    }

    /**
     * populate exclude Real repo keys
     */
    private void populateIncludeRepo() {
        List<String> repos = new ArrayList<>();
        CentralConfigService centralConfigService = ContextHelper.get().beanForType(CentralConfigService.class);
        Map<String, LocalRepoDescriptor> localRepoDescriptorMap = centralConfigService.getDescriptor().getLocalRepositoriesMap();
        Map<String, RemoteRepoDescriptor> remoteRepoDescriptorMap = centralConfigService.getDescriptor().getRemoteRepositoriesMap();
        Map<String, DistributionRepoDescriptor> distributionRepositoriesMap = centralConfigService.getDescriptor().getDistributionRepositoriesMap();
        Map<String, ReleaseBundlesRepoDescriptor> releaseBundleRepositoriesMap = centralConfigService.getDescriptor().getReleaseBundlesRepositoriesMap();
        repos.addAll(localRepoDescriptorMap.keySet());
        repos.addAll(remoteRepoDescriptorMap.keySet());
        repos.addAll(distributionRepositoriesMap.keySet());
        repos.addAll(releaseBundleRepositoriesMap.keySet());
        excludeRepos.forEach(repo -> {
            if (localRepoDescriptorMap.get(repo) != null || remoteRepoDescriptorMap.get(repo) != null ||
                    distributionRepositoriesMap.get(repo) != null || releaseBundleRepositoriesMap.get(repo) != null) {
                repos.remove(repo);
            }
        });
        includeRepos = repos;
    }

    public List<String> getIncludeRepos() {
        return includeRepos;
    }

    public void setIncludeRepos(List<String> includeRepos) {
        this.includeRepos = includeRepos;
    }

    /**
     * populate local and remote repositories to map
     *  @param repoKeys                - remote repo key
     * @param realRepoDescriptors     - real repo descriptor list
     * @param localRepoDescriptorMap  - local repo descriptor map
     * @param remoteRepoDescriptorMap - remote repo descriptor map
     * @param distributionRepositoriesMap
     */
    private void populateRealRepoToMap(List<String> repoKeys, List<RealRepoDescriptor> realRepoDescriptors,
            Map<String, LocalRepoDescriptor> localRepoDescriptorMap,
            Map<String, RemoteRepoDescriptor> remoteRepoDescriptorMap,
            Map<String, DistributionRepoDescriptor> distributionRepositoriesMap,
            Map<String, ReleaseBundlesRepoDescriptor> releaseBundleRepoDescriptorMap) {
        repoKeys.forEach(repoKey -> {
            if (localRepoDescriptorMap.get(repoKey) != null) {
                realRepoDescriptors.add(localRepoDescriptorMap.get(repoKey));
            } else if (remoteRepoDescriptorMap.get(repoKey) != null) {
                    realRepoDescriptors.add(remoteRepoDescriptorMap.get(repoKey));
            } else if (distributionRepositoriesMap.get(repoKey) != null) {
                realRepoDescriptors.add(distributionRepositoriesMap.get(repoKey));
            } else if (releaseBundleRepoDescriptorMap.get(repoKey) != null) {
                realRepoDescriptors.add(releaseBundleRepoDescriptorMap.get(repoKey));
            }
        });
    }
    public List<String> getExcludeRepos() {
        return excludeRepos;
    }

    /**
     * get list of repo key and build list of Real repo descriptors
     *
     * @param excludeRepos - list of repos key to exclude
     */
    public void setExcludeRepos(List<String> excludeRepos) {
        this.excludeRepos = excludeRepos;
        List<RealRepoDescriptor> realRepoDescriptors = new ArrayList<>();
        CentralConfigService centralConfigService = ContextHelper.get().beanForType(CentralConfigService.class);
        Map<String, LocalRepoDescriptor> localRepoDescriptorMap = centralConfigService.getDescriptor().getLocalRepositoriesMap();
        Map<String, RemoteRepoDescriptor> remoteRepoDescriptorMap = centralConfigService.getDescriptor().getRemoteRepositoriesMap();
        Map<String, DistributionRepoDescriptor> distributionRepositoriesMap = centralConfigService.getDescriptor().getDistributionRepositoriesMap();
        Map<String, ReleaseBundlesRepoDescriptor> releaseBundleRepoDescriptorMap = centralConfigService.getDescriptor().getReleaseBundlesRepositoriesMap();
        populateRealRepoToMap(excludeRepos, realRepoDescriptors, localRepoDescriptorMap, remoteRepoDescriptorMap,
                distributionRepositoriesMap, releaseBundleRepoDescriptorMap);
        super.setExcludedRepositories(realRepoDescriptors);
    }
    public String toString() {
        return JsonUtil.jsonToStringIgnoreSpecialFields(this);
    }

    @Override
    public boolean ignoreSpecialFields() {
        return !isEdit;
    }
}
