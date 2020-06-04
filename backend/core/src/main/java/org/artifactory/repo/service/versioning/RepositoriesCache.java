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

package org.artifactory.repo.service.versioning;

import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.repo.*;
import org.artifactory.repo.virtual.VirtualRepo;
import org.artifactory.storage.db.security.service.BasicCacheModel;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author gidis
 */
public class RepositoriesCache implements BasicCacheModel {

    private long version;
    public final LocalRepo trashcan;
    public final LocalRepo supportBundlesRepo;
    public final Set<String> allRepoKeysCache;
    public final Map<String, LocalRepo> localRepositoriesMap;
    public final Map<String, RemoteRepo> remoteRepositoriesMap;
    public final Map<String, LocalCacheRepo> localCacheRepositoriesMap;
    public final Map<String, VirtualRepo> virtualRepositoriesMap;
    public final Map<String, DistributionRepo> distributionRepositoriesMap;
    public final Map<String, ReleaseBundlesRepo> releaseBundlesRepoMap;
    public final Map<RepoType, Collection<Repo>> repoTypeRepositoriesMap;
    public final Map<Character, List<String>> reposByFirstCharMap;

    public RepositoriesCache(Map<String, ReleaseBundlesRepo> releaseBundlesRepoMap,
            Map<String, DistributionRepo> distributionRepositoriesMap,
            Map<String, VirtualRepo> virtualRepositoriesMap,
            Map<String, LocalCacheRepo> localCacheRepositoriesMap,
            Map<String, RemoteRepo> remoteRepositoriesMap,
            Map<String, LocalRepo> localRepositoriesMap, LocalRepo trashcan,
            Set<String> allRepoKeysCache, LocalRepo supportBundlesRepo,
            Map<RepoType, Collection<Repo>> repoTypeRepositoriesMap,
            Map<Character, List<String>> reposByFirstCharMap) {
        this.releaseBundlesRepoMap = releaseBundlesRepoMap;
        this.distributionRepositoriesMap = distributionRepositoriesMap;
        this.virtualRepositoriesMap = virtualRepositoriesMap;
        this.localCacheRepositoriesMap = localCacheRepositoriesMap;
        this.remoteRepositoriesMap = remoteRepositoriesMap;
        this.localRepositoriesMap = localRepositoriesMap;
        this.trashcan = trashcan;
        this.allRepoKeysCache = allRepoKeysCache;
        this.supportBundlesRepo = supportBundlesRepo;
        this.repoTypeRepositoriesMap = repoTypeRepositoriesMap;
        this.reposByFirstCharMap = reposByFirstCharMap;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public void destroy() {
        localRepositoriesMap.values().forEach(Repo::close);
        remoteRepositoriesMap.values().forEach(Repo::close);
        localCacheRepositoriesMap.values().forEach(Repo::close);
        virtualRepositoriesMap.values().forEach(Repo::close);
        distributionRepositoriesMap.values().forEach(Repo::close);
    }
}
