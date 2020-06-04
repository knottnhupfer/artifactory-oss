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

package org.artifactory.storage.db.security.service.access;

import com.google.common.util.concurrent.RateLimiter;
import org.apache.http.HttpStatus;
import org.artifactory.common.ConstantValues;
import org.artifactory.event.CacheType;
import org.artifactory.event.InvalidateCacheEvent;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.security.*;
import org.artifactory.security.access.AccessService;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.db.security.service.*;
import org.artifactory.storage.security.service.AclCache;
import org.artifactory.storage.security.service.AclStoreService;
import org.artifactory.util.AlreadyExistsException;
import org.jfrog.access.client.AccessClientException;
import org.jfrog.access.client.AccessClientHttpException;
import org.jfrog.access.client.permission.PermissionsClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.artifactory.security.ArtifactoryResourceType.*;
import static org.artifactory.storage.db.security.service.access.AclMapper.aclToAccessPermission;

/**
 * @author Noam Shemesh
 * @author Omri Ziv
 */
@Service
public class AclStoreServiceImpl implements AclStoreService, ApplicationListener<InvalidateCacheEvent> {

    private VersionCache<AclCacheLoader.AclCacheItem<RepoPermissionTarget>> repoAclsCache;
    private VersionCache<AclCacheLoader.AclCacheItem<BuildPermissionTarget>> buildsAclsCache;
    private VersionCache<AclCacheLoader.AclCacheItem<ReleaseBundlePermissionTarget>> releaseBundlesAclsCache;
    private VersionCache<AllAclCacheLoader.AllAclCacheItem> allAclsCache;
    private AccessService accessService;
    private RateLimiter rateLimiter;
    private long accessPermissionLastUpdated;

    // Repo
    private final Supplier<Collection<Acl<RepoPermissionTarget>>> getRepoAcls = this::getDownstreamAllRepoAcls;
    private static final Function<RepoPermissionTarget, List<String>> getRepoKeysFunction = RepoPermissionTarget::getRepoKeys;

    // Build
    private final Supplier<Collection<Acl<BuildPermissionTarget>>> getBuildAcls = this::getDownstreamAllBuildAcls;
    private static final Function<BuildPermissionTarget, List<String>> getBuildRepoKeysFunction = BuildPermissionTarget::getRepoKeys;

    // Release Bundle
    private final Supplier<Collection<Acl<ReleaseBundlePermissionTarget>>> getReleaseBundleAcls = this::getDownstreamAllReleaseBundlesAcls;
    private static final Function<ReleaseBundlePermissionTarget, List<String>> getReleaseBundleRepoKeysFunction = ReleaseBundlePermissionTarget::getRepoKeys;

    @Autowired
    public void setAccessService(AccessService accessService) {
        this.accessService = accessService;
    }

    @PostConstruct
    public void init() {
        long timeout = ConstantValues.aclDirtyReadsTimeout.getLong();
        boolean asyncReloadCache = ConstantValues.aclVersionCacheAsyncReload.getBoolean();
        AclCacheLoader<RepoPermissionTarget> repoAclCacheLoader = new AclCacheLoader<>(getRepoAcls,
                getRepoKeysFunction);
        AclCacheLoader<BuildPermissionTarget> buildAclCacheLoader = new AclCacheLoader<>(getBuildAcls,
                getBuildRepoKeysFunction);
        AclCacheLoader<ReleaseBundlePermissionTarget> releaseBundleAclCacheLoader = new AclCacheLoader<>(
                getReleaseBundleAcls, getReleaseBundleRepoKeysFunction);
        AllAclCacheLoader allCacheLoader = new AllAclCacheLoader(this::getAllBuildAclsValues,
                this::getAllRepoAclsValues, this::getAllReleaseBundleAclsVaues);
        if (asyncReloadCache) {
            long threadWaitingMillis = ConstantValues.aclVersionCacheAsyncWaitingTimeMillis.getLong();
            long threadWaitingOnErrorMillis = ConstantValues.aclVersionCacheAsyncWaitOnErrorTimeMillis.getLong();
            repoAclsCache = new AsyncVersioningCacheImpl<>(timeout, repoAclCacheLoader, threadWaitingMillis,
                    threadWaitingOnErrorMillis, "repoAclsCache");
            buildsAclsCache = new AsyncVersioningCacheImpl<>(timeout, buildAclCacheLoader, threadWaitingMillis,
                    threadWaitingOnErrorMillis, "buildAclsCache");
            releaseBundlesAclsCache = new AsyncVersioningCacheImpl<>(timeout, releaseBundleAclCacheLoader,
                    threadWaitingMillis, threadWaitingOnErrorMillis, "releaseBundleAclCacheLoader");
            allAclsCache = new AsyncVersioningCacheImpl<>(timeout, allCacheLoader,
                    threadWaitingMillis, threadWaitingOnErrorMillis, "allAclsCache");
        } else {
            repoAclsCache = new VersioningCacheImpl<>(timeout, repoAclCacheLoader);
            buildsAclsCache = new VersioningCacheImpl<>(timeout, buildAclCacheLoader);
            releaseBundlesAclsCache = new VersioningCacheImpl<>(timeout, releaseBundleAclCacheLoader);
            allAclsCache = new VersioningCacheImpl<>(timeout, allCacheLoader);
        }
        rateLimiter = RateLimiter.create(getRefreshTime());
    }

    @Override
    public Collection<RepoAcl> getAllRepoAcls() {
        return getReposAclsMap().values();
    }

    @Override
    public Collection<BuildAcl> getAllBuildAcls() {
        return getBuildsAclsMap().values();
    }

    @Override
    public Collection<ReleaseBundleAcl> getAllReleaseBundleAcls() {
        return getReleaseBundlesAclsMap().values();
    }

    @Override
    public Map<Character, List<PermissionTargetAcls>> getMapPermissionTargetAcls(boolean reversed) {
        invalidCacheIfRequired();
        return reversed ? allAclsCache.get().getReverseAclInfosMap() : allAclsCache.get().getAclInfosMap();
    }

    private Collection<Acl<RepoPermissionTarget>> getDownstreamAllRepoAcls() {
        return accessService.getAccessClient().permissions()
                .findPermissionsByServiceIdAndResourceType(accessService.getArtifactoryServiceId(), REPO.getName())
                .getPermissions()
                .stream()
                .map(AclMapper::toArtifactoryRepoAcl)
                .collect(Collectors.toSet());
    }

    private Collection<Acl<BuildPermissionTarget>> getDownstreamAllBuildAcls() {
        return accessService.getAccessClient().permissions()
                .findPermissionsByServiceIdAndResourceType(accessService.getArtifactoryServiceId(), BUILD.getName())
                .getPermissions()
                .stream()
                .map(AclMapper::toArtifactoryBuildAcl)
                .collect(Collectors.toSet());
    }

    private Collection<Acl<ReleaseBundlePermissionTarget>> getDownstreamAllReleaseBundlesAcls() {
        return accessService.getAccessClient().permissions()
                .findPermissionsByServiceIdAndResourceType(accessService.getArtifactoryServiceId(), RELEASE_BUNDLES.getName())
                .getPermissions()
                .stream()
                .map(AclMapper::toArtifactoryReleaseBundleAcl)
                .collect(Collectors.toSet());
    }

    @Override
    public AclCache<RepoPermissionTarget> getAclCache() {
        invalidCacheIfRequired();
        AclCacheLoader.AclCacheItem<RepoPermissionTarget> aclCacheItem = repoAclsCache.get();
        return new AclCache<>(aclCacheItem.getGroupResultMap(), aclCacheItem.getUserResultMap());
    }

    @Override
    public AclCache<BuildPermissionTarget> getBuildsAclCache() {
        invalidCacheIfRequired();
        AclCacheLoader.AclCacheItem<BuildPermissionTarget> aclCacheItem = buildsAclsCache.get();
        return new AclCache<>(aclCacheItem.getGroupResultMap(), aclCacheItem.getUserResultMap());
    }

    @Override
    public AclCache<ReleaseBundlePermissionTarget> getReleaseBundlesAclCache() {
        invalidCacheIfRequired();
        AclCacheLoader.AclCacheItem<ReleaseBundlePermissionTarget> aclCacheItem = releaseBundlesAclsCache.get();
        return new AclCache<>(aclCacheItem.getGroupResultMap(), aclCacheItem.getUserResultMap());
    }

    private void invalidCacheIfRequired() {
        rateLimiter.setRate(getRefreshTime());

        if (rateLimiter.tryAcquire()) {
            long lastUpdated = accessService.getAccessClient().permissions()
                    .lastUpdatedByServiceId(accessService.getArtifactoryServiceId());
            if (lastUpdated > accessPermissionLastUpdated) {
                invalidateAclCache();
                accessPermissionLastUpdated = lastUpdated;
            }
        }
    }

    private double getRefreshTime() {
        return (double) 1 / ConstantValues.aclMinimumWaitRefreshDataSeconds.getLong();
    }

    @Override
    public RepoAcl getRepoAcl(String permTargetName) {
        return getReposAclsMap().get(permTargetName);
    }

    @Override
    public BuildAcl getBuildAcl(String permTargetName) {
        return getBuildsAclsMap().get(permTargetName);
    }

    @Override
    public ReleaseBundleAcl getReleaseBundleAcl(String permTargetName) {
      return getReleaseBundlesAclsMap().get(permTargetName);
    }

    @Override
    public void createAcl(Acl acl) {
        cudOperation(permClient -> permClient.createPermission(aclToAccessPermission(acl, getServiceId())),
                "Could not create " + getAclDetailsForErrorLog(acl));
    }

    @Override
    public void updateAcl(Acl acl) {
        cudOperation(permClient -> permClient.replacePermission(aclToAccessPermission(acl, getServiceId())),
                "Could not update " + getAclDetailsForErrorLog(acl));
    }

    @Override
    public void deleteAcl(Acl acl) {
        String accessIdentifier = acl.getAccessIdentifier();
        if (accessIdentifier == null) {
            throw new IllegalStateException("Can't delete ACL without unique Access identifier");
        }
        cudOperation(permClient -> permClient.deletePermissionByName(accessIdentifier),
                "Could not delete " + getAclDetailsForErrorLog(acl));
    }

    private String getAclDetailsForErrorLog(Acl acl) {
        return "permission " + acl.getPermissionTarget().getName() + " with identifier " + acl.getAccessIdentifier();
    }

    private void cudOperation(Consumer<PermissionsClient> execute, String possibleErrorMessage) {
        try {
            execute.accept(accessService.getAccessClient().permissions());
        } catch (AccessClientHttpException e) {
            if (HttpStatus.SC_CONFLICT == e.getStatusCode()) {
                throw new AlreadyExistsException(possibleErrorMessage, e);
            }
            throw new StorageException(possibleErrorMessage, e);
        } catch (IllegalArgumentException | AccessClientException e) {
            throw new StorageException(possibleErrorMessage, e);
        } finally {
            invalidateAclCache();
        }
    }

    @Override
    public void removeAllUserAces(String username) {
        removeAllGeneric(username, repoAclsCache, false);
        removeAllGeneric(username, buildsAclsCache, false);
        removeAllGeneric(username, releaseBundlesAclsCache, false);
    }

    @Override
    public void removeAllGroupAces(String groupName) {
        removeAllGeneric(groupName, repoAclsCache, true);
        removeAllGeneric(groupName, buildsAclsCache, true);
        removeAllGeneric(groupName, releaseBundlesAclsCache, true);
    }

    private <T extends PermissionTarget> void removeAllGeneric(String principal,
            VersionCache<AclCacheLoader.AclCacheItem<T>> aclCache, boolean isGroup) {
        Map<String, Set<PrincipalPermission<T>>> entityToAcl = getPricinpalPermission(principal, aclCache, isGroup);
        if (entityToAcl == null) {
            return;
        }
        Map<String, Acl<T>> aclInfoMap = aclCache.get().getAclInfoMap();
        cudOperation(permissionsClient -> entityToAcl
                .values()
                .stream()
                .flatMap(Set::stream)
                .map(principalPermission -> {
                    String permissionName = principalPermission.getPermissionTarget().getName();
                    return aclInfoMap.get(permissionName);
                })
                .map(acl -> removeAceFromAcl(acl, principal, isGroup))
                .forEach(this::updateAcl), "Could not delete ACE for " + (isGroup ? "group" : "user") + principal);
    }

    private <T extends PermissionTarget> Map<String, Set<PrincipalPermission<T>>> getPricinpalPermission(
            String principal, VersionCache<AclCacheLoader.AclCacheItem<T>> aclCache, boolean isGroup) {
        Map<String, Set<PrincipalPermission<T>>> entityToAcl;
        if (isGroup) {
            entityToAcl = aclCache.get().getGroupResultMap().get(principal);
        } else {
            entityToAcl = aclCache.get().getUserResultMap().get(principal);
        }
        return entityToAcl;
    }

    private <T extends PermissionTarget> MutableAcl removeAceFromAcl(Acl<T> acl, String username, boolean isGroup) {
        MutableAcl mutableAcl = copyAclByType(acl);
        mutableAcl.setAces(acl.getAces()
                .stream()
                .filter(ace -> ace.isGroup() != isGroup || (ace.isGroup() == isGroup && !ace.getPrincipal().equals(username)))
                .collect(Collectors.toSet()));
        return mutableAcl;
    }

    private <T extends PermissionTarget> MutableAcl copyAclByType(Acl<T> acl) {
        if (acl instanceof RepoAcl) {
            return InfoFactoryHolder.get().copyRepoAcl((RepoAcl) acl);
        } else if (acl instanceof BuildAcl) {
            return InfoFactoryHolder.get().copyBuildAcl((BuildAcl) acl);
        } else if (acl instanceof ReleaseBundleAcl) {
            return InfoFactoryHolder.get().copyReleaseBundleAcl((ReleaseBundleAcl) acl);
        }
        throw new IllegalStateException("Acl is not of a known type. Must be of type repo/build");
    }

    @Override
    public void deleteAllAcls() {
        cudOperation(permissionsClient -> getAllRepoAcls()
                .forEach(this::deleteAcl), "Could not delete all ACLs");
    }

    @Override
    public String invalidateAclCache() { int cacheVersion = repoAclsCache.promoteVersion();
        int buildsCacheVersion = buildsAclsCache.promoteVersion();
        int releaseBundlesCacheVersion = releaseBundlesAclsCache.promoteVersion();
        int allAclsCacheVersion = allAclsCache.promoteVersion();
        return "Repo cache version: " + cacheVersion + "; Builds cache version: " + buildsCacheVersion
                + "; Release Bundles cache version: " + releaseBundlesCacheVersion + "; All acls cache version"
                + allAclsCacheVersion ;
    }

    @Override
    public void destroy() {
        repoAclsCache.destroy();
        buildsAclsCache.destroy();
        releaseBundlesAclsCache.destroy();
        allAclsCache.destroy();
    }

    private String getServiceId() {
        return accessService.getArtifactoryServiceId().getFormattedName();
    }

    private Map<String, RepoAcl> getReposAclsMap() {
        return getReposAclsMap(false);
    }

    private Map<String, RepoAcl> getReposAclsMap(boolean skipInvalidateCacheIfRequired) {
        if (!skipInvalidateCacheIfRequired) {
            invalidCacheIfRequired();
        }
        Map<String, Acl<RepoPermissionTarget>> aclInfoMap = repoAclsCache.get().getAclInfoMap();
        return aclInfoMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> toRepoAcl(entry.getValue())));
    }

    private RepoAcl toRepoAcl(Acl<RepoPermissionTarget> acl) {
        if (acl instanceof RepoAcl) {
            return (RepoAcl) acl;
        }
        throw new IllegalStateException("Acl is not of type RepoAcl. Must be of type repo");
    }

    private Map<String, BuildAcl> getBuildsAclsMap() {
        return getBuildsAclsMap(false);
    }

    private Map<String, BuildAcl> getBuildsAclsMap(boolean skipInvalidateCacheIfRequired) {
        if (!skipInvalidateCacheIfRequired) {
            invalidCacheIfRequired();
        }
        Map<String, Acl<BuildPermissionTarget>> aclInfoMap = buildsAclsCache.get().getAclInfoMap();
        return aclInfoMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> toBuildAcl(entry.getValue())));
    }

    private BuildAcl toBuildAcl(Acl<BuildPermissionTarget> acl) {
        if (acl instanceof BuildAcl) {
            return (BuildAcl) acl;
        }
        throw new IllegalStateException("Acl is not of type BuildAcl. Must be of type build");
    }

    private Map<String, ReleaseBundleAcl> getReleaseBundlesAclsMap() {
        return getReleaseBundlesAclsMap(false);
    }

    private Map<String, ReleaseBundleAcl> getReleaseBundlesAclsMap(boolean skipInvalidateCacheIfRequired) {
        if (!skipInvalidateCacheIfRequired) {
            invalidCacheIfRequired();
        }
        Map<String, Acl<ReleaseBundlePermissionTarget>> aclInfoMap = releaseBundlesAclsCache.get().getAclInfoMap();
        return aclInfoMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> toReleaseBundleAcl(entry.getValue())));
    }

    private ReleaseBundleAcl toReleaseBundleAcl(Acl<ReleaseBundlePermissionTarget> acl) {
        if (acl instanceof ReleaseBundleAcl) {
            return (ReleaseBundleAcl) acl;
        }
        throw new IllegalStateException("Acl is not of type ReleaseBundleAcl. Must be of type release bundle");
    }

    private Collection<RepoAcl> getAllRepoAclsValues() {
        return getReposAclsMap(true).values();
    }

    private Collection<BuildAcl> getAllBuildAclsValues() {
        return getBuildsAclsMap(true).values();
    }

    private Collection<ReleaseBundleAcl> getAllReleaseBundleAclsVaues() {
        return getReleaseBundlesAclsMap(true).values();
    }

    @Override
    public void onApplicationEvent(InvalidateCacheEvent invalidateCacheEvent) {
        if (CacheType.ACL.equals(invalidateCacheEvent.getCacheType())) {
            invalidateAclCache();
        }
    }
}
