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

package org.artifactory.build;

import com.google.common.collect.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.common.MoveMultiStatusHolder;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.rest.build.ContinueBuildFilter;
import org.artifactory.aql.AqlService;
import org.artifactory.aql.api.domain.sensitive.AqlApiBuild;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.result.AqlEagerResult;
import org.artifactory.aql.result.AqlLazyResult;
import org.artifactory.aql.result.rows.AqlBaseFullRowImpl;
import org.artifactory.aql.result.rows.AqlBuild;
import org.artifactory.aql.util.AqlUtils;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.fs.FileInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.sapi.common.ExportSettings;
import org.artifactory.spring.InternalContextHelper;
import org.artifactory.storage.StorageException;
import org.artifactory.util.CollectionUtils;
import org.jfrog.build.api.Artifact;
import org.jfrog.build.api.*;
import org.jfrog.build.api.Dependency;
import org.jfrog.build.api.Module;
import org.jfrog.common.JsonUtils;
import org.jfrog.common.StreamSupportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.artifactory.aql.AqlConverts.toFileInfo;
import static org.artifactory.build.BuildInfoUtils.parseBuildTime;
import static org.artifactory.build.InternalBuildService.BACKUP_BUILDS_FOLDER;
import static org.artifactory.util.encoding.ArtifactoryBuildRepoPathElementsEncoder.encode;

/**
 * @author Dan Feldman
 */
public abstract class BuildServiceUtils {
    private static final Logger log = LoggerFactory.getLogger(BuildServiceUtils.class);

    public static Set<FileInfo> filterOutNullFileInfos(Iterable<FileInfo> rawInfos) {
        return StreamSupport.stream(rawInfos.spliterator(), false)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public static Iterable<FileInfo> toFileInfoList(Set<ArtifactoryBuildArtifact> artifacts) {
        return artifacts.stream()
                .filter(Objects::nonNull)
                .map(ArtifactoryBuildArtifact::getFileInfo)
                .collect(Collectors.toList());
    }

    public static Set<RepoPath> toRepoPath(Set<FileInfo> artifacts) {
        return Optional.ofNullable(artifacts)
                .orElse(Sets.newHashSet())
                .stream()
                .filter(Objects::nonNull)
                .map(FileInfo::getRepoPath)
                .collect(Collectors.toSet());
    }

    /**
     * Map all build dependencies to checksum, held in a multimap for occurrences of duplicate checksum for different
     * dependencies --> although we cannot be 100% positive which dependency took part in the build with the current
     * BuildInfo implementation.
     */
    static Multimap<String, Dependency> getBuildDependencies(Build build) {
        Multimap<String, Dependency> beansMap = HashMultimap.create();
        List<Module> modules = build.getModules();
        if (modules == null) {
            return beansMap;
        }
        for (Module module : modules) {
            if (module.getDependencies() != null) {
                for (Dependency dependency : module.getDependencies()) {
                    if (dependency.getSha1() != null) {
                        beansMap.put(dependency.getSha1(), dependency);
                    } else {
                        log.warn("Dependency: " + dependency.getId() + " is missing SHA1," + " under build: "
                                + build.getName());
                    }
                }
            }
        }
        return beansMap;
    }

    /**
     * Map all build artifacts to checksum, held in a multimap for occurrences of duplicate checksum for different
     * artifacts so that the search results return all
     */
    static Multimap<String, Artifact> getBuildArtifacts(Build build) {
        //ListMultiMap to hold possible duplicate artifacts coming from BuildInfo
        Multimap<String, Artifact> beansMap = ArrayListMultimap.create();

        List<Module> modules = build.getModules();
        if (modules == null) {
            return beansMap;
        }
        modules.stream()
                .map(Module::getArtifacts)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .forEach(artifact -> {
                    if (artifact.getSha1() != null) {
                        beansMap.put(artifact.getSha1(), artifact);
                    } else {
                        log.warn("Artifact: " + artifact.getName() + " is missing SHA1," + " under build: " + build.getName());
                    }
                });
        return beansMap;
    }

    static void verifyAllArtifactInfosExistInSet(Build build, boolean cleanNullEntries, BasicStatusHolder statusHolder,
            Set<ArtifactoryBuildArtifact> buildArtifactsInfos, VerifierLogLevel logLevel) {
        for (Iterator<ArtifactoryBuildArtifact> iter = buildArtifactsInfos.iterator(); iter.hasNext(); ) {
            ArtifactoryBuildArtifact artifact = iter.next();
            if (artifact.getFileInfo() == null) {
                String errorMsg = "Unable to find artifact '" + artifact.getArtifact().getName() + "' of build '" + build.getName()
                        + "' #" + build.getNumber();
                logToStatus(statusHolder, errorMsg, logLevel);
                if (cleanNullEntries) {
                    iter.remove();
                }
            }
        }
    }

    /**
     * Verifies all dependencies from the build exist in the map and writes appropriate entries to the StatusHolder
     * based on the chosen log level.
     * NOTE: Relies on missing dependency to have a null mapping (as returned by {@link InternalBuildService#getBuildDependenciesFileInfos}
     *
     * @param build                 Build to verify
     * @param statusHolder          StatusHolder that entries will be written into
     * @param buildDependenciesInfo Mapping of Dependencies to FileInfos
     */
    static void verifyAllDependencyInfosExistInMap(Build build, boolean cleanNullEntries, BasicStatusHolder statusHolder,
            Map<Dependency, FileInfo> buildDependenciesInfo, VerifierLogLevel logLevel) {
        List<BuildFileBean> keysToRemove = Lists.newArrayList();
        for (Map.Entry<Dependency, FileInfo> entry : buildDependenciesInfo.entrySet()) {
            if (entry.getValue() == null) {
                String errorMsg = "Unable to find dependency '" + entry.getKey().getId() + "' of build '"
                        + build.getName() + "' #" + build.getNumber();
                keysToRemove.add(entry.getKey());
                logToStatus(statusHolder, errorMsg, logLevel);
            }
        }
        if (cleanNullEntries) {
            keysToRemove.stream()
                    .filter(Objects::nonNull)
                    .forEach(buildDependenciesInfo::remove);
        }
    }

    public static void moveJsonFile(RepoPath sourcePath, RepoPath targetPath, RepositoryService repoService) {
        if (sourcePath.equals(targetPath)) {
            //json is already in proper position
            return;
        }
        Authentication originalAuthentication = SecurityContextHolder.getContext().getAuthentication();
        InternalContextHelper.get().getSecurityService().authenticateAsSystem();
        try {
            MoveMultiStatusHolder statusHolder = repoService.move(sourcePath, targetPath, false, true, true);
            if (statusHolder.isError()) {
                throw new StorageException("Could not move build json from " + sourcePath + " to " + targetPath
                        + ": " + statusHolder.getLastError().getMessage(), statusHolder.getLastError().getException());
            }
        } finally {
            SecurityContextHolder.getContext().setAuthentication(originalAuthentication);
        }
    }

    static boolean isBuildModelInvalid(String buildName, String buildNumber, String buildStarted) {
        return StringUtils.isBlank(buildName) || StringUtils.isBlank(buildNumber)
                || StringUtils.isBlank(buildStarted);
    }

    public static RepoPath getBuildJsonPathInRepo(Build build, String buildRepoKey) {
        return getBuildJsonPathInRepo(build.getName(), build.getNumber(), build.getStarted(), buildRepoKey);
    }

    public static RepoPath getBuildJsonPathInRepo(BuildRun build, String buildRepoKey) {
        return getBuildJsonPathInRepo(build.getName(), build.getNumber(), build.getStarted(), buildRepoKey);
    }

    /**
     * Generates a {@link RepoPath} for a {@link Build} model according to its coordinates in Build Info repo layout.
     * Converts Build start time from ISO8601 date formatted (2018-04-03T12:00:13.862+0300) to millis
     */
    public static RepoPath getBuildJsonPathInRepo(String buildName, String buildNumber, String buildStarted, String repoKey) {
        if (isBuildModelInvalid(buildName, buildNumber, buildStarted)) {
            throw new IllegalArgumentException("Build model must contain build name, number and started timestamp");
        }
        return RepoPathFactory.create(repoKey, createBuildPath(buildName, buildNumber, buildStarted));
    }

    private static String createBuildPath(String buildName, String buildNumber, String buildStarted) {
        return String.join("/", encode(buildName, false),
                encode(buildNumber, true) + "-" + parseBuildTime(buildStarted) + ".json");
    }

    static String getBuildAsJsonString(Build build) {
        return JsonUtils.getInstance().valueToString(build, true);
    }

    public static boolean buildUnderWrongPath(Build build, RepoPath buildJsonPath, String repoKey) {
        RepoPath expectedPath = getBuildJsonPathInRepo(build.getName(), build.getNumber(), build.getStarted(), repoKey);
        return !buildJsonPath.equals(expectedPath);
    }

    /**
     * The 'non-strict' variant of the artifact search is used as a fallback for artifacts that couldn't be matched by
     * the regular property based search.
     * AqlApiBuid.name() and AqlApiBuid.number() are considered 'weak' constraints as the linkage between aql domains is
     * performed by sha1 - so we might end up 'finding' the wrong artifact (meaning the wrong repoPath that has a
     * correct checksum - see todos above)
     */
    @SuppressWarnings("unchecked")
    static Set<ArtifactoryBuildArtifact> matchUnmatchedArtifactsNonStrict(Build build, List<String> sourceRepos,
            List<String> excludedRepos, Multimap<String, Artifact> unmatchedArtifacts, List<String> virtualRepoKeys, AqlService aqlService) {
        AqlBase.AndClause<AqlApiBuild> and = AqlApiBuild.and(
                AqlApiBuild.name().equal(build.getName()),
                AqlApiBuild.number().equal(build.getNumber())
        );
        log.debug("Executing 'non-strict' Artifacts search for build {}:{}", build.getName(), build.getNumber());
        addIncludeExcludeRepos(sourceRepos, excludedRepos, and, false);
        AqlBase nonStrictQuery = AqlApiBuild.create().filter(and);
        nonStrictQuery.include(
                AqlApiBuild.module().artifact().item().sha1Actual(),
                AqlApiBuild.module().artifact().item().md5Actual(),
                AqlApiBuild.module().artifact().item().sha1Original(),
                AqlApiBuild.module().artifact().item().md5Orginal(),
                AqlApiBuild.module().artifact().item().created(),
                AqlApiBuild.module().artifact().item().modifiedBy(),
                AqlApiBuild.module().artifact().item().createdBy(),
                AqlApiBuild.module().artifact().item().updated(),
                AqlApiBuild.module().artifact().item().repo(),
                AqlApiBuild.module().artifact().item().path(),
                AqlApiBuild.module().artifact().item().size(),
                AqlApiBuild.module().artifact().item().name()
                //Ordering by the last updated field, in case of duplicates with the same checksum
                //Since this is match any checksum mode
        ).addSortElement(AqlApiBuild.module().artifact().item().updated()).desc();

        AqlEagerResult<AqlBaseFullRowImpl> aqlResult = aqlService.executeQueryEager(nonStrictQuery);
        log.debug("Search returned {} artifacts", aqlResult.getSize());
        return BuildServiceUtils.matchArtifactsToFileInfos(aqlResult.getResults(), unmatchedArtifacts, virtualRepoKeys);
    }

    @SuppressWarnings("unchecked")
    static void addIncludeExcludeRepos(List<String> sourceRepos, List<String> excludedRepos, AqlBase.AndClause and, boolean isStrict) {
        if (CollectionUtils.notNullOrEmpty(sourceRepos)) {
            log.debug("Search limited to repos: {}", sourceRepos);
            sourceRepos.stream()
                    .filter(StringUtils::isNotBlank)
                    .forEach(sourceRepo -> {
                        if (isStrict) {
                            and.append(AqlApiItem.repo().equal(sourceRepo));
                        } else {
                            and.append(AqlApiBuild.module().artifact().item().repo().equal(sourceRepo));
                        }
                    });
        }
        if (CollectionUtils.notNullOrEmpty(excludedRepos)) {
            log.debug("Search excludes repos: {}", excludedRepos);
            excludedRepos.stream()
                    .filter(StringUtils::isNotBlank)
                    .forEach(excludedRepo -> {
                        if (sourceRepos != null && sourceRepos.contains(excludedRepo)) {
                            log.warn("Cannot exclude results from repo {} that is already in the source repo list",
                                    excludedRepo);
                        } else {
                            if (isStrict) {
                                and.append(AqlApiItem.repo().notEquals(excludedRepo));
                            } else {
                                and.append(AqlApiBuild.module().artifact().item().repo().equal(excludedRepo));
                            }
                        }
                    });
        }
    }

    /**
     * We're making a best effort to guess the relevant dependency according to the id given by the BuildInfo,
     * if indeed more than one dependency is a match for the checksum. if not - the one found is used.
     * Unlike matching the build's artifacts above, a weak match is enough and maybe even recommended here as
     * dependencies might move around or get deleted and we will still find a correct one from another repo.
     */
    static Map<Dependency, FileInfo> matchDependenciesToFileInfos(AqlLazyResult<AqlBuild> aqlLazyResult,
            Multimap<String, Dependency> checksumToDependencyMap, List<String> virtualRepoKeys) {

        Map<Dependency, FileInfo> foundResults = Maps.newHashMap();
        Stream<AqlBuild> aqlBuildStream = aqlLazyResult.asStream(e -> {
            if (e != null) {
                log.error("Error while searching for next build's dependency match {}", e.getMessage());
                log.debug("Error while searching for next build's dependency match. ", e);
            }
        });
        aqlBuildStream.map(r -> (AqlBaseFullRowImpl) r).forEach(result -> {
            //Don't include results from virtual repos
            if (!virtualRepoKeys.contains(result.getRepo())) {
                Collection<Dependency> dependencies = checksumToDependencyMap.get(result.getActualSha1());
                if (!CollectionUtils.isNullOrEmpty(dependencies)) {
                    FileInfo dependencyFileInfo;
                    try {
                        dependencyFileInfo = toFileInfo.apply(result);
                        //Try matching dependencies exactly by id and add them to the results, overwriting previously
                        //found (maybe not exactly matched) dependencies
                        tryExactDependencyMatch(foundResults, result, dependencies, dependencyFileInfo);
                        //Add all dependencies that weren't matched yet to make sure we don't leave false unmatched ones
                        matchAnyDependencyToFileInfo(foundResults, dependencies, dependencyFileInfo);
                    } catch (Exception e) {
                        log.debug("Error creating path from aql result: {} :\n {}", result, e.getMessage());
                        log.debug("", e);
                    }
                }
            }
        });
        return foundResults;
    }

    /**
     * Tries to match dependencies exactly to a path (file info) based on the dependency's name, and adds matched
     * dependencies to the result map.
     */
    private static void tryExactDependencyMatch(Map<Dependency, FileInfo> foundResults, final AqlBaseFullRowImpl result,
            Collection<Dependency> dependencies, FileInfo dependencyFileInfo) {

        Iterable<Dependency> exactMatches = dependencies.stream()
                .filter(Objects::nonNull)
                .filter(dependency -> Objects.nonNull(dependency.getId()))
                .filter(dependency -> dependency.getId().contains(result.getBuildDependencyName()))
                .collect(Collectors.toList());
        for (Dependency exactMatch : exactMatches) {
            log.debug("Exactly matched dependency {} to path {}", exactMatch.getId(), dependencyFileInfo.getRepoPath());
            foundResults.put(exactMatch, dependencyFileInfo);
        }
    }

    /**
     * Adds each dependency in the collection to the result map, if it was not already matched before.
     */
    private static void matchAnyDependencyToFileInfo(Map<Dependency, FileInfo> foundResults,
            Collection<Dependency> dependencies, FileInfo dependencyFileInfo) {
        for (Dependency dependency : dependencies) {
            if (!foundResults.containsKey(dependency)) {
                log.debug("Matched dependency {} to path {}", dependency.getId(), dependencyFileInfo.getRepoPath());
                foundResults.put(dependency, dependencyFileInfo);
            }
        }
    }

    /**
     * Matches FileInfos to build artifacts(created from an aql query's result) by checksum and by artifact name.
     * If indicated by {@param matchAnyChecksum} matches only by checksum if no exact match by name was found
     */
    /* This matching logic is kinda partial as we still can't match exactly sha1 to RepoPath because there's
     * not enough information from the BuildInfo (just sha1 and artifact id). It will not harm promotion \ push to
     * bintray etc. as ALL sha1's are still returned so nothing is skipped, this more of a UI issue that we are
     * currently reluctant to resolve (i.e. by introducing a unique id in the BuildInfo and put it as a property)
     */
    static Set<ArtifactoryBuildArtifact> matchArtifactsToFileInfos(List<AqlBaseFullRowImpl> queryResults,
            Multimap<String, Artifact> checksumToArtifactsMap, List<String> virtualRepoKeys) {
        Set<ArtifactoryBuildArtifact> results = Sets.newHashSet();
        for (final AqlBaseFullRowImpl result : queryResults) {
            //Don't include results from virtual repos
            if (!virtualRepoKeys.contains(result.getRepo())) {
                Collection<Artifact> artifacts = checksumToArtifactsMap.get(result.getActualSha1());
                if (CollectionUtils.notNullOrEmpty(artifacts)) {
                    matchArtifactToFileInfo(results, result, artifacts);
                }
            }
        }
        return results;
    }

    private static void matchArtifactToFileInfo(Set<ArtifactoryBuildArtifact> results, final AqlBaseFullRowImpl result, Collection<Artifact> artifacts) {
        //Try to match exactly by artifact name
        boolean exactMatchFound = tryExactArtifactToFileInfoMatch(results, result, artifacts);
        if (!exactMatchFound) {
            if (log.isDebugEnabled()) {
                log.debug("Exact match for {} not found, trying match by checksum",
                        toFileInfo.apply(result).getRepoPath().toPath());
            }
            //If no match just take the first artifact (it did match the checksum)
            matchAnyArtifactToFileInfo(results, result, artifacts);
        }
    }

    /**
     * Tries to match an artifact to a path based on it's name. If a match is found it is added to the result set
     * and removed from the list
     */
    private static boolean tryExactArtifactToFileInfoMatch(Set<ArtifactoryBuildArtifact> results, final AqlBaseFullRowImpl result, Collection<Artifact> artifacts) {
        Artifact idMatch = artifacts.stream()
                .filter(Objects::nonNull)
                .filter(artifact -> artifact.getName() != null)
                .filter(artifact -> artifact.getName().equals(result.getName()))
                .findFirst()
                .orElse(null);

        if (idMatch != null) {
            results.add(new ArtifactoryBuildArtifact(idMatch, toFileInfo.apply(result)));
            log.debug("Matched artifact {} to path {}", idMatch.getName(), AqlUtils.fromAql(result));
            artifacts.remove(idMatch);
        }
        return idMatch != null;
    }

    private static void matchAnyArtifactToFileInfo(Set<ArtifactoryBuildArtifact> results, AqlBaseFullRowImpl result, Collection<Artifact> artifacts) {
        Iterator<Artifact> artifactsIter = artifacts.iterator();
        Artifact matchedArtifact = artifactsIter.next();
        results.add(new ArtifactoryBuildArtifact(matchedArtifact, toFileInfo.apply(result)));
        log.debug("Matched artifact {} to path {}", matchedArtifact.getName(), AqlUtils.fromAql(result));
        //Remove artifact from list to ensure that we match everything
        artifactsIter.remove();
    }

    /**
     * Makes sure that all the correct build/backup dirs are prepared for backup
     *
     * @param settings          Export settings
     * @param multiStatusHolder Process status holder
     * @param buildsFolder      Builds folder within the backup
     */
    static void prepareBuildsFolder(ExportSettings settings, MutableStatusHolder multiStatusHolder, File buildsFolder) {
        if (buildsFolder.exists()) {
            // Backup previous builds folder if incremental
            if (settings.isIncremental()) {
                File tempBuildBackupDir = new File(settings.getBaseDir(),
                        BACKUP_BUILDS_FOLDER + "." + System.currentTimeMillis());
                try {
                    FileUtils.moveDirectory(buildsFolder, tempBuildBackupDir);
                    FileUtils.forceMkdir(buildsFolder);
                } catch (IOException e) {
                    multiStatusHolder.error(
                            "Failed to create incremental builds temp backup dir: " + tempBuildBackupDir, e, log);
                }
            }
        } else {
            try {
                FileUtils.forceMkdir(buildsFolder);
            } catch (IOException e) {
                multiStatusHolder.error("Failed to create builds backup dir: " + buildsFolder, e, log);
            }
        }
    }

    static <T> List<T> iterateFetchFromDb(Function<ContinueBuildFilter, List<T>> fetchFunction,
            java.util.function.Predicate<T> permissionPredicate, Function<T, BuildId> castingNextContinueBuildFunction,
            ContinueBuildFilter continueBuildFilter) {
        BuildId continueBuild = continueBuildFilter.getContinueBuildId();
        boolean needToFetchFromDB = true;
        List<T> result = new ArrayList<>();
        ContinueBuildFilter nextBuildFilter = new ContinueBuildFilter(continueBuildFilter);
        while (needToFetchFromDB) {
            nextBuildFilter.setLimit(continueBuildFilter.getLimit() * 3);
            nextBuildFilter.setContinueBuildId(continueBuild);
            List<T> itemsFromDb = fetchFunction.apply(nextBuildFilter);
            List<T> currentIterationList = StreamSupportUtils.stream(itemsFromDb)
                    .filter(permissionPredicate)
                    .limit(continueBuildFilter.getLimit() - result.size())
                    .collect(Collectors.toList());
            result.addAll(currentIterationList);
            if (CollectionUtils.notNullOrEmpty(itemsFromDb) && result.size() < continueBuildFilter.getLimit()) {
                continueBuild = castingNextContinueBuildFunction.apply(itemsFromDb.get(itemsFromDb.size() - 1));
                log.debug("Calling database again to fetch builds after '{}'", continueBuild.getName());
            } else {
                needToFetchFromDB = false;
            }
        }
        return result;

    }

    private static void logToStatus(BasicStatusHolder statusHolder, String errorMsg, VerifierLogLevel logLevel) {
        switch (logLevel) {
            case err:
                statusHolder.error(errorMsg, log);
                break;
            case warn:
                statusHolder.warn(errorMsg, log);
                break;
            case debug:
                statusHolder.debug(errorMsg, log);
        }
    }

    public enum VerifierLogLevel {
        err, warn, debug
    }
}