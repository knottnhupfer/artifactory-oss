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

package org.artifactory.aql.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.aql.AqlService;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.result.AqlEagerResult;
import org.artifactory.aql.result.AqlRestResult;
import org.artifactory.aql.result.rows.AqlBaseFullRowImpl;
import org.artifactory.aql.result.rows.AqlItem;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.artifactory.aql.api.domain.sensitive.AqlApiItem.*;
import static org.artifactory.aql.api.internal.AqlBase.and;

/**
 * General class for aql utilities
 *
 * @author Dan Feldman
 */
public class AqlUtils {
    private static final Logger log = LoggerFactory.getLogger(AqlUtils.class);

    /**
     * Returns a RepoPath from an aql result's path fields
     *
     * @param repo repo key
     * @param path path
     * @param name file name
     */
    public static RepoPath fromAql(String repo, String path, String name) {
        if (StringUtils.equals(path, ".")) {
            return RepoPathFactory.create(repo, name);
        } else {
            return RepoPathFactory.create(repo, path + "/" + name);
        }
    }

    /**
     * Returns the RepoPath that points to this aql result
     *
     * @param row AqlFullRow object that has repo, path and name fields
     */
    public static RepoPath fromAql(AqlItem row) throws IllegalArgumentException {
        if (StringUtils.isBlank(row.getRepo()) || StringUtils.isBlank(row.getPath())
                || StringUtils.isBlank(row.getName())) {
            throw new IllegalArgumentException("Repo, Path, and Name fields must contain values");
        }
        return fromAql(row.getRepo(), row.getPath(), row.getName());
    }

    public static RepoPath fromAql(AqlRestResult.Row row) throws IllegalArgumentException {
        String err = "Repo, Path, and Name fields must contain values";
        if (row == null) {
            throw new IllegalArgumentException(err);
        }
        if (StringUtils.isBlank(row.itemRepo) || StringUtils.isBlank(row.itemPath) ||
                StringUtils.isBlank(row.itemName)) {
            throw new IllegalArgumentException(err);
        }
        return fromAql(row.itemRepo, row.itemPath, row.itemName);
    }

    /**
     * Returns true if the node that the path points to exists (Use with files only!)
     *
     * @param path repo path to check for existence
     */
    public static boolean exists(RepoPath path) {
        AqlSearchablePath aqlPath = new AqlSearchablePath(path);
        AqlApiItem aql = AqlApiItem.create().filter(
                and(
                        AqlApiItem.repo().equal(aqlPath.getRepo()),
                        AqlApiItem.path().equal(aqlPath.getPath()),
                        AqlApiItem.name().equal(aqlPath.getFileName())
                )
        );
        AqlEagerResult<AqlItem> results = ContextHelper.get().beanForType(AqlService.class).executeQueryEager(aql);
        return results != null && results.getResults() != null && results.getResults().size() > 0;
    }

    /**
     * Returns a list of {@link AqlSearchablePath} pointing to all files contained in the current folder
     * as well as all files under all subdirectories of that folder.
     *
     * NOTE: use only with folders!
     *
     * @param path RepoPath of the folder to construct the search paths from
     */
    public static List<AqlSearchablePath> getSearchablePathForCurrentFolderAndSubfolders(RepoPath path) {
        List<AqlSearchablePath> artifactPaths = Lists.newArrayList();
        //Add *.* in filename for AqlSearchablePath creation - path is assumed to be a folder
        RepoPath searchPath = InternalRepoPathFactory.childRepoPath(path, "*.*");
        //All files in the folder containing the file
        AqlSearchablePath allFilesInCurrentFolder = new AqlSearchablePath(searchPath);
        //This will also find files without any extension (i.e. docker, lfs)
        allFilesInCurrentFolder.setFileName("*");
        artifactPaths.add(allFilesInCurrentFolder);
        artifactPaths.add(getSearchablePathForAllFilesInSubfolders(path));
        return artifactPaths;
    }

    /**
     * Returns a searchable path representing all subfolders of current path and all files in them
     * NOTE: use only with folders!
     */
    public static AqlSearchablePath getSearchablePathForAllFilesInSubfolders(RepoPath path) {
        //Add *.* in filename for AqlSearchablePath creation - path is assumed to be a folder
        RepoPath searchPath = InternalRepoPathFactory.childRepoPath(path, "*.*");
        //All files in all subfolders of folder containing the file
        AqlSearchablePath allFilesInSubFolders = new AqlSearchablePath(searchPath);
        if (".".equals(allFilesInSubFolders.getPath())) {  //Special case for root folder
            allFilesInSubFolders.setPath("**");
        } else {
            allFilesInSubFolders.setPath(allFilesInSubFolders.getPath() + "/**");
        }
        allFilesInSubFolders.setFileName("*");
        return allFilesInSubFolders;
    }

    /**
     * Returns an AqlApiItem OR clause containing an AND for each of the searchable paths given
     */
    public static AqlBase.OrClause<AqlApiItem>  getSearchClauseForPaths(List<AqlSearchablePath> aqlSearchablePaths) {
        AqlBase.OrClause<AqlApiItem>  searchClause = AqlBase.or();
        for (AqlSearchablePath path : aqlSearchablePaths) {
            log.debug("Adding path '{}' to artifact search", path.toRepoPath().toString());
            searchClause.append(
                    and(
                            AqlApiItem.repo().equal(path.getRepo()),
                            AqlApiItem.path().matches(path.getPath()),
                            AqlApiItem.name().matches(path.getFileName()),
                            AqlApiItem.depth().greaterEquals(path.getPath().split("/").length)
                    )
            );
        }
        return searchClause;
    }

    public static void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                log.trace("Could not close JDBC result set", e);
            } catch (Exception e) {
                log.trace("Unexpected exception when closing JDBC result set", e);
            }
        }
    }

    /**
     * Maps repo path to all rows returned by the search for it. Also filters out results based on user read permissions.
     */
    public static HashMultimap<RepoPath, AqlBaseFullRowImpl> aggregateResultsByPath(List<AqlBaseFullRowImpl> results) {
        AuthorizationService authService = ContextHelper.get().getAuthorizationService();
        HashMultimap<RepoPath, AqlBaseFullRowImpl> aggregator = HashMultimap.create();
        results.forEach(
                result -> {
                    RepoPath path = AqlUtils.fromAql(result);
                    if (authService.canRead(path)) {
                        aggregator.put(path, result);
                    } else {
                        log.debug("Path '{}' omitted from results due to missing read permissions for user: '{}'",
                                path.toPath(), authService.currentUsername());
                    }
                });
        return aggregator;
    }

    public static List<AqlItem> aggregateRowByPermission(
            List<AqlItem> results) {
        AuthorizationService authService = ContextHelper.get().getAuthorizationService();
        RepositoryService repositoryService = ContextHelper.get().getRepositoryService();
        List<AqlItem> list = Lists.newArrayList();
        results.forEach(
                result -> {
                    RepoPath repoPath = AqlUtils.fromAql(result);
                    if (authService.canRead(repoPath) && !isDistributionRepo(repositoryService, repoPath)) {
                        list.add(result);
                    } else {
                        log.debug("Path '{}' omitted from results due to missing read permissions for user: '{}'",
                                repoPath.toPath(), authService.currentUsername());
                    }
                });
        return list;
    }

    private static boolean isDistributionRepo(RepositoryService repositoryService, RepoPath repoPath) {
        return repositoryService.distributionRepoDescriptorByKey(repoPath.getRepoKey()) != null;
    }

    public static HashMultimap<String, AqlBaseFullRowImpl> aggregateResultsPackage(
            List<AqlBaseFullRowImpl> results) {
        AuthorizationService authService = ContextHelper.get().getAuthorizationService();
        RepositoryService repositoryService = ContextHelper.get().getRepositoryService();
        HashMultimap<String, AqlBaseFullRowImpl> aggregator = HashMultimap.create();
        results.forEach(
                result -> {
                    RepoPath repoPath = RepoPathFactory.create(result.getRepo(), result.getValue());
                    if (authService.canRead(repoPath) && !isDistributionRepo(repositoryService, repoPath)) {
                        aggregator.put(result.getValue(), result);

                    } else {
                        log.debug("Path '{}' omitted from results due to missing read permissions for user: '{}'",
                                repoPath.toPath(), authService.currentUsername());
                    }
                });
        return aggregator;
    }

    /**
     * Constructs a raw query for a recursive search for an artifact under a given path
     *
     * @param pathToSearch - the repoPath under which the artifact will be searched
     * @param artifactName - the name of the artifact that will be searched
     * @return an AqlApiItem query to search for an artifact under a given path
     */
    public static AqlApiItem getRecursiveFindItemQuery(RepoPath pathToSearch, String artifactName) {
        AqlApiItem aql;
        // Both blank paths and a dot (repo root) are accepted. In that case, use a wildcard for the path,
        // and match the filename
        if (pathToSearch.getPath().equals("") || pathToSearch.getPath().equals(".")) {
            aql = AqlApiItem.create().filter(
                    AqlApiItem.and(
                            AqlApiItem.repo().equal(pathToSearch.getRepoKey()),
                            AqlApiItem.path().matches(pathToSearch.getPath() + "*"),
                            name().equal(artifactName)

                    ));
        } else {
            // Otherwise search for an exact path match or any sub-path artifacts
            aql = AqlApiItem.create().filter(
                    AqlApiItem.and(
                            AqlApiItem.repo().equal(pathToSearch.getRepoKey()),
                            name().equal(artifactName),
                            AqlApiItem.or(
                                    AqlApiItem.path().matches(pathToSearch.getPath() + "/*"),
                                    AqlApiItem.path().equal(pathToSearch.getPath())
                            )
                    ));

        }
        return aql;
    }

    public static AqlApiItem getAllDirectChildrenOfParentQuery(RepoPath repoPath) {
        if (repoPath == null) {
            log.debug("Empty repoPath. Unable to construct query");
            return null;
        }
        if (repoPath.getParent() == null) {
            log.debug("Empty Parent. Unable to construct query");
            return null;
        }
        return create()
                .filter(
                        and(
                                repo().equal(repoPath.getRepoKey()),
                                path().equal(repoPath.getParent().getPath()))
                );
    }

    public static Optional<AqlApiItem> getDirectChildrenByModuleAndVersionQuery(@Nonnull RepoPath repoPath,
            ModuleInfo moduleInfo) {
        if (repoPath.getParent() == null) {
            log.debug("Empty Parent. Unable to construct query");
            return Optional.empty();
        }

        AqlBase.AndClause<AqlApiItem> repoPathAndNameCriteria = and(
                repo().equal(repoPath.getRepoKey()),
                path().equal(repoPath.getParent().getPath())
        );
        String baseRevision = moduleInfo.getBaseRevision();
        String module = moduleInfo.getModule();
        if (moduleInfo.isIntegration()) {
            String fileIntegrationRevision = moduleInfo.getFileIntegrationRevision();
            repoPathAndNameCriteria
                    .append(name().matches(module + "-" + baseRevision + "-" + fileIntegrationRevision + "*"));
        } else {
            repoPathAndNameCriteria.append(name().matches(module + "-" + baseRevision + "*"));
        }
        return Optional.of(create().filter(repoPathAndNameCriteria));
    }

    @SafeVarargs
    public static <E> E[] arrayOf(E... elements) {
        return elements;
    }
}
