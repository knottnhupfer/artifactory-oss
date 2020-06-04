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

package org.artifactory.repo.snapshot;

import com.google.common.collect.TreeMultimap;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.module.ModuleInfoBuilder;
import org.artifactory.api.properties.PropertiesService;
import org.artifactory.api.repo.BaseBrowsableItem;
import org.artifactory.api.repo.BrowsableItemCriteria;
import org.artifactory.api.repo.RepositoryBrowsingService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.repo.exception.ItemNotFoundRuntimeException;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.SnapshotVersionBehavior;
import org.artifactory.fs.ItemInfo;
import org.artifactory.maven.versioning.MavenVersionComparator;
import org.artifactory.md.Properties;
import org.artifactory.mime.MavenNaming;
import org.artifactory.repo.Repo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.repo.StoringRepo;
import org.artifactory.request.InternalRequestContext;
import org.jfrog.client.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Resolves the latest unique snapshot version given a non-unique Maven snapshot artifact request
 * or a request with [RELEASE]/[INTEGRATION] place holders for latest release or latest integration version respectively
 * from local and cache repositories (from remotes only works for maven repos by analyzing the remote maven-metadata).
 *
 * @author Shay Yaakov
 */
public class LocalLatestVersionResolver extends LatestVersionResolver {
    private static final Logger log = LoggerFactory.getLogger(LocalLatestVersionResolver.class);

    private final MavenVersionComparator mavenVersionComparator = new MavenVersionComparator();

    @Override
    protected InternalRequestContext getRequestContext(InternalRequestContext requestContext, Repo repo,
            ModuleInfo originalModuleInfo) {
        if (!(repo.isLocal())) {
            return requestContext;
        }

        String path = requestContext.getResourcePath();
        if (repo.getDescriptor().isMavenRepoLayout() && MavenNaming.isNonUniqueSnapshot(path)) {
            requestContext = getMavenLatestSnapshotRequestContext(requestContext, repo, originalModuleInfo);
        } else {
            boolean searchForReleaseVersion = StringUtils.contains(path, "[RELEASE]");
            boolean searchForIntegrationVersion = StringUtils.contains(path, "[INTEGRATION]");
            if (searchForReleaseVersion || searchForIntegrationVersion) {
                requestContext = getLatestVersionRequestContext(requestContext, (StoringRepo) repo,
                        originalModuleInfo, searchForReleaseVersion);
            }
        }

        return requestContext;
    }

    private InternalRequestContext getLatestVersionRequestContext(InternalRequestContext requestContext,
            StoringRepo repo, ModuleInfo originalModuleInfo, boolean searchForReleaseVersion) {
        VersionsRetriever retriever =
                searchForReleaseVersion ? new ReleaseVersionsRetriever(true) : new SnapshotVersionsRetriever(true);
        ModuleInfo baseRevisionModule = getBaseRevisionModuleInfo(originalModuleInfo);
        TreeMultimap<Calendar, ItemInfo> versionsItems = retriever.collectVersionsItems(repo, baseRevisionModule, true);
        if (versionsItems != null) {
            if (searchForReleaseVersion && !ConstantValues.requestSearchLatestReleaseByDateCreated.getBoolean()) {
                return getRequestContentForReleaseByVersion(versionsItems.values(), repo, requestContext,
                        originalModuleInfo);
            } else {
                return getRequestContextFromMap(versionsItems, repo, requestContext, originalModuleInfo,
                        searchForReleaseVersion);
            }
        } else {
            return requestContext;
        }
    }

    /**
     * Searches for [RELEASE]
     */
    private InternalRequestContext getRequestContentForReleaseByVersion(Collection<ItemInfo> itemInfos,
            StoringRepo repo, InternalRequestContext requestContext, ModuleInfo originalModuleInfo) {
        RepositoryService repositoryService = getRepositoryService();
        List<ModuleInfoWithPath> matchModuleInfo = new ArrayList<>();
        for (ItemInfo item : itemInfos) {
            if (!item.isFolder()) {
                ModuleInfo itemModuleInfo = repositoryService.getItemModuleInfo(item.getRepoPath());
                if (itemModuleInfo.isValid() && areModuleInfosTheSame(originalModuleInfo, itemModuleInfo) &&
                            isPropertiesMatch(item, requestContext.getProperties())) {
                    // Saving the data for path validation
                    String itemPath = item.getRepoPath().getPath();
                    matchModuleInfo.add(new ModuleInfoWithPath(itemPath, itemModuleInfo));
                }
            }
        }
        // Sort the list - latest version first
        matchModuleInfo.sort((o1, o2) -> compareLatest(o2.moduleInfo, o1.moduleInfo));
        return validateRequestContentForRelease(repo, requestContext, matchModuleInfo);
    }

    /**
     * Running over the items that matched the GAV, and validating the path.
     * Return the first item from the list that its path matched.
     * Else, returns the latest item that matches the GAV.
     */
    private InternalRequestContext validateRequestContentForRelease(StoringRepo repo, InternalRequestContext requestContext,
            List<ModuleInfoWithPath> matchModuleInfo) {
        String originalPath = requestContext.getResourcePath();
        for (ModuleInfoWithPath infoWithPath : matchModuleInfo) {
            String baseRevision = infoWithPath.moduleInfo.getBaseRevision();
            // Translate the requested path based on the baseRevision
            String translatedRequestPath = getTranslatedOriginalPath(originalPath, baseRevision, true);
            // Check that the path of the items equals to the requested path
            if (infoWithPath.itemPath.contains(translatedRequestPath)) {
                return translateRepoRequestContext(requestContext, repo, infoWithPath.itemPath);
            }
        }
        // Nothing matched perfectly, returning the latest or the original as fallback
        String fallbackPath = matchModuleInfo.isEmpty() ? originalPath : matchModuleInfo.get(0).itemPath;
        return translateRepoRequestContext(requestContext, repo, fallbackPath);
    }

    /**
     * Gets original path and replaces the [RELEASE] / [INTEGRATION] tags to the relevant numbers
     */
    private String getTranslatedOriginalPath(String originalPath, String replaceWith, boolean isRelease) {
        String translatedRequestPath;
        if (isRelease) {
            translatedRequestPath = originalPath.replace("[RELEASE]", replaceWith);
        } else {
            translatedRequestPath = originalPath.replace("[INTEGRATION]", replaceWith);
        }
        return translatedRequestPath;
    }

    /**
     * Searches by date for [INTEGRATION] / [RELEASE]
     */
    private InternalRequestContext getRequestContextFromMap(TreeMultimap<Calendar, ItemInfo> versionsItems,
            StoringRepo repo, InternalRequestContext requestContext, ModuleInfo originalModuleInfo,
            boolean searchForReleaseVersion) {
        RepositoryService repositoryService = ContextHelper.get().getRepositoryService();
        String firstItemRelPath = null;
        for (Map.Entry<Calendar, ItemInfo> entry : versionsItems.entries()) {
            ItemInfo item = entry.getValue();
            if (!item.isFolder()) {
                ModuleInfo itemModuleInfo = repositoryService.getItemModuleInfo(item.getRepoPath());
                boolean isIntegration = itemModuleInfo.isIntegration();
                boolean matchReleaseSearch = searchForReleaseVersion && !isIntegration;
                boolean matchIntegrationSearch = !searchForReleaseVersion && isIntegration;
                if (itemModuleInfo.isValid() && (matchReleaseSearch || matchIntegrationSearch)) {
                    if (areModuleInfosTheSame(originalModuleInfo, itemModuleInfo) && isPropertiesMatch(item,
                            requestContext.getProperties())) {
                        String itemRelPath = item.getRelPath();
                        // Saving the first path found for fallback
                        firstItemRelPath = firstItemRelPath == null ? itemRelPath : firstItemRelPath;
                        // Getting the original request translated with versions
                        String translatedRequestPath = getTranslatedPathIntegrationOrRelease(requestContext,
                                itemModuleInfo, matchReleaseSearch);
                        // If the item contains the original path, its a match and we return
                        if (itemRelPath.contains(translatedRequestPath)) {
                            return translateRepoRequestContext(requestContext, repo, itemRelPath);
                        }
                    }
                }
            }
        }
        // Fallback: returning the first item that has GAV match, but path doesn't match perfectly
        if (firstItemRelPath != null) {
            return translateRepoRequestContext(requestContext, repo, firstItemRelPath);
        }
        return requestContext;
    }

    /**
     * Gets a request and translating the original path to the relevant path - with [INTEGRATION] or [RELEASE] data
     */
    private String getTranslatedPathIntegrationOrRelease(InternalRequestContext requestContext,
            ModuleInfo itemModuleInfo, boolean matchReleaseSearch) {
        String originalPath = requestContext.getResourcePath();
        String baseRevision = itemModuleInfo.getBaseRevision();
        String fileIntegrationRevision = itemModuleInfo.getFileIntegrationRevision();
        // Determine if the search is for RELEASE or INTEGRATION
        String replaceStr = matchReleaseSearch ? baseRevision : fileIntegrationRevision;
        return getTranslatedOriginalPath(originalPath, replaceStr, matchReleaseSearch);
    }

    private boolean isPropertiesMatch(ItemInfo itemInfo, Properties requestProps) {
        if (requestProps == null || requestProps.isEmpty()) {
            return true;
        }
        Properties nodeProps = ContextHelper.get().beanForType(PropertiesService.class)
                .getProperties(itemInfo);
        Properties.MatchResult result =  nodeProps.matchQuery(requestProps);
        return !Properties.MatchResult.CONFLICT.equals(result);
    }

    private ModuleInfo getBaseRevisionModuleInfo(ModuleInfo deployedModuleInfo) {
        return new ModuleInfoBuilder().organization(deployedModuleInfo.getOrganization()).
                module(deployedModuleInfo.getModule()).baseRevision(deployedModuleInfo.getBaseRevision()).build();
    }

    private InternalRequestContext getMavenLatestSnapshotRequestContext(InternalRequestContext requestContext,
            Repo repo, ModuleInfo originalModuleInfo) {
        LocalRepoDescriptor repoDescriptor = (LocalRepoDescriptor) repo.getDescriptor();
        if (repoDescriptor.getSnapshotVersionBehavior().equals(SnapshotVersionBehavior.NONUNIQUE)) {
            return requestContext;
        }

        String path = requestContext.getResourcePath();
        String parentPath = PathUtils.getParent(path);
        RepoPath parentRepoPath = RepoPathFactory.create(repo.getKey(), parentPath);
        boolean isDeployerBehavior = SnapshotVersionBehavior.DEPLOYER.equals(
                repoDescriptor.getSnapshotVersionBehavior());
        String artifactPath = getLatestArtifactPath(parentRepoPath, originalModuleInfo, isDeployerBehavior,
                requestContext.getProperties());

        return artifactPath != null ? translateRepoRequestContext(requestContext, repo, artifactPath) : requestContext;
    }

    /**
     * Retrieves the path to the latest unique artifact (null if not found)
     *
     * @param parentRepoPath     the parent folder to search within
     * @param originalModuleInfo the user request module info to compare with
     * @param isDeployerBehavior on deployer behaviour compares by last modified, otherwise by version string
     * @param requestProperties  the original request properties (can be null)
     * @return a path to the latest unique artifact (null if not found)
     */
    private String getLatestArtifactPath(RepoPath parentRepoPath, ModuleInfo originalModuleInfo,
            boolean isDeployerBehavior, Properties requestProperties) {
        RepositoryService repositoryService = getRepositoryService();
        ModuleInfo latestModuleInfo = null;
        long latestLastModified = 0;
        String latestArtifactPath = null;
        BrowsableItemCriteria criteria = new BrowsableItemCriteria.Builder(parentRepoPath)
                .requestProperties(requestProperties)
                .includeChecksums(false)
                .includeRemoteResources(false)
                .build();
        RepositoryBrowsingService repositoryBrowsingService = ContextHelper.get().beanForType(
                RepositoryBrowsingService.class);

        List<BaseBrowsableItem> children;
        try {
            children = repositoryBrowsingService.getLocalRepoBrowsableChildren(criteria);
        } catch (ItemNotFoundRuntimeException e) {
            // Simply log the message and return null
            log.debug(e.getMessage());
            return null;
        }

        for (BaseBrowsableItem child : children) {
            if (!child.isFolder()) {
                ModuleInfo itemModuleInfo = repositoryService.getItemModuleInfo(child.getRepoPath());
                if (itemModuleInfo.isValid()) {
                    if (areModuleInfosTheSame(originalModuleInfo, itemModuleInfo)) {
                        if (isDeployerBehavior) {
                            long childLastModified = child.getLastModified();
                            if (childLastModified > latestLastModified) {
                                latestLastModified = childLastModified;
                                latestArtifactPath = child.getRepoPath().getPath();
                            }
                        } else {
                            ModuleInfo resultModuleInfo = getLatestModuleInfo(itemModuleInfo, latestModuleInfo);
                            if (!resultModuleInfo.equals(latestModuleInfo)) {
                                latestModuleInfo = resultModuleInfo;
                                latestArtifactPath = child.getRepoPath().getPath();
                            }
                        }
                    }
                }
            }
        }

        return latestArtifactPath;
    }

    /**
     * Compares 2 given module infos and returns the latest one
     */
    private ModuleInfo getLatestModuleInfo(@Nonnull ModuleInfo moduleInfo1, @Nullable ModuleInfo moduleInfo2) {
        return compareLatest(moduleInfo1, moduleInfo2) >= 0 ? moduleInfo1 : moduleInfo2;
    }

    /**
     * Compares the given module infos
     * @return  1 if moduleInfo1 is later than moduleInfo2.
     *          0 if they are equal.
     *         -1 if moduleInfo1 is earlier than moduleInfo2
     */
    private int compareLatest(@Nonnull ModuleInfo moduleInfo1, @Nullable ModuleInfo moduleInfo2) {
        return compareLatest(moduleInfo1, moduleInfo2, mavenVersionComparator);
    }

    /**
     * Compares the given module infos using mavenVersionComparator
     * @return  1 if moduleInfo1 is later than moduleInfo2.
     *          0 if they are equal.
     *         -1 if moduleInfo1 is earlier than moduleInfo2
     */
    public static int compareLatest(@Nonnull ModuleInfo moduleInfo1, @Nullable ModuleInfo moduleInfo2, MavenVersionComparator mavenVersionComparator) {
        if (moduleInfo2 == null) {
            return 1;
        }
        String version1 = moduleInfo1.getBaseRevision() + "-" + moduleInfo1.getFileIntegrationRevision();
        String version2 = moduleInfo2.getBaseRevision() + "-" + moduleInfo2.getFileIntegrationRevision();

        if (moduleInfo1.getFileIntegrationRevision() == null && moduleInfo2.getFileIntegrationRevision() == null) {
            version1 = moduleInfo1.getBaseRevision();
            version2 = moduleInfo2.getBaseRevision();
        }
        return mavenVersionComparator.compare(version1, version2);
    }

    private boolean areModuleInfosTheSame(ModuleInfo originalModuleInfo, ModuleInfo moduleInfo) {
        boolean releaseCondition = StringUtils.equals(originalModuleInfo.getOrganization(),
                moduleInfo.getOrganization())
                && StringUtils.equals(originalModuleInfo.getModule(), moduleInfo.getModule())
                && StringUtils.equals(originalModuleInfo.getClassifier(), moduleInfo.getClassifier())
                && StringUtils.equals(originalModuleInfo.getExt(), moduleInfo.getExt());

        boolean integrationCondition = releaseCondition
                && StringUtils.equals(originalModuleInfo.getBaseRevision(), moduleInfo.getBaseRevision());

        return originalModuleInfo.isIntegration() ? integrationCondition : releaseCondition;
    }

    @AllArgsConstructor
    private class ModuleInfoWithPath {
        private String itemPath;
        private ModuleInfo moduleInfo;
    }
}
