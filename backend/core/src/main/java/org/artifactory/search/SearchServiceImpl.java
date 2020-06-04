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

package org.artifactory.search;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.repo.RepositoryBrowsingService;
import org.artifactory.api.repo.VirtualRepoItem;
import org.artifactory.api.rest.search.common.RestDateFieldName;
import org.artifactory.api.search.ItemSearchResults;
import org.artifactory.api.search.SearchControls;
import org.artifactory.api.search.VersionSearchResults;
import org.artifactory.api.search.archive.ArchiveSearchControls;
import org.artifactory.api.search.archive.ArchiveSearchResult;
import org.artifactory.api.search.artifact.ArtifactSearchControls;
import org.artifactory.api.search.artifact.ArtifactSearchResult;
import org.artifactory.api.search.artifact.ChecksumSearchControls;
import org.artifactory.api.search.deployable.VersionUnitSearchControls;
import org.artifactory.api.search.deployable.VersionUnitSearchResult;
import org.artifactory.api.search.exception.InvalidChecksumException;
import org.artifactory.api.search.gavc.GavcSearchControls;
import org.artifactory.api.search.gavc.GavcSearchResult;
import org.artifactory.api.search.property.PropertySearchControls;
import org.artifactory.api.search.property.PropertySearchResult;
import org.artifactory.api.search.stats.StatsSearchControls;
import org.artifactory.api.search.stats.StatsSearchResult;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.aql.AqlConverts;
import org.artifactory.aql.AqlService;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.model.AqlComparatorEnum;
import org.artifactory.aql.result.AqlEagerResult;
import org.artifactory.aql.result.rows.AqlItem;
import org.artifactory.aql.result.rows.AqlStatistics;
import org.artifactory.aql.util.AqlUtils;
import org.artifactory.build.BuildRun;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.common.ConstantValues;
import org.artifactory.config.CentralConfigKey;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.fs.ItemInfo;
import org.artifactory.mime.MavenNaming;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.*;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.sapi.common.RepositoryRuntimeException;
import org.artifactory.sapi.search.VfsQuery;
import org.artifactory.sapi.search.VfsQueryResult;
import org.artifactory.sapi.search.VfsQueryRow;
import org.artifactory.sapi.search.VfsQueryService;
import org.artifactory.schedule.CachedThreadPoolTaskExecutor;
import org.artifactory.search.archive.ArchiveSearcher;
import org.artifactory.search.archive.ArchiveSearcherAql;
import org.artifactory.search.build.BuildSearcher;
import org.artifactory.search.deployable.VersionUnitSearcher;
import org.artifactory.search.fields.FieldNameConverter;
import org.artifactory.search.gavc.GavcSearcher;
import org.artifactory.search.property.PropertySearcher;
import org.artifactory.search.stats.LastDownloadedItemsSearcher;
import org.artifactory.security.AccessLogger;
import org.artifactory.spring.Reloadable;
import org.artifactory.storage.db.InternalDbService;
import org.artifactory.storage.db.statistics.DownloadedSearcher;
import org.artifactory.storage.spring.StorageContextHelper;
import org.artifactory.util.CollectionUtils;
import org.artifactory.util.GlobalExcludes;
import org.artifactory.util.PathMatcher;
import org.artifactory.version.CompoundVersionDetails;
import org.jfrog.common.config.diff.DataDiff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.artifactory.sapi.search.VfsBoolType.OR;
import static org.artifactory.sapi.search.VfsComparatorType.*;
import static org.artifactory.sapi.search.VfsQueryResultType.FILE;

/**
 * @author Frederic Simon
 * @author Yoav Landman
 */
@Service
@Reloadable(beanClass = InternalSearchService.class,
        initAfter = {InternalRepositoryService.class},
        listenOn = CentralConfigKey.none)
public class SearchServiceImpl implements InternalSearchService {
    private static final Logger log = LoggerFactory.getLogger(SearchServiceImpl.class);
    /**
     * Regexp to test if string contains a valid checksum: valid checksum and\or wildcards ('*' or '?' only).
     */
    private static final Pattern CHECKSUM_VALIDATION_REGEX = Pattern.compile("[a-fA-F0-9\\*\\?]+");
    /**
     * We require at least 12 valid characters a-fA-F0-9 per checksum (other chars can be wildcards)
     */
    private static final int CHECKSUM_NUMBER_OF_REQUIRED_NORMAL_CHARS = 12;

    @Autowired
    private VfsQueryService vfsQueryService;

    @Autowired
    private InternalRepositoryService repoService;

    @Autowired
    private RepositoryBrowsingService repoBrowsingService;

    @Autowired
    private AuthorizationService authService;

    @Autowired
    private CachedThreadPoolTaskExecutor executor;

    @Autowired
    private AqlService aqlService;

    private ArtifactSearcher artifactSearcher;

    // helpers  - setup once after construct.
    private LastDownloadedItemsSearcher lastDownloadedItemsSearcher;
    // stream adapter for getting stats
    private AqlItemsToStatsHelper aqlItemsToStatsHelper;

    /**
     * last downloaded items searcher
     */
    @PostConstruct
    public void setLastDownloadedItemsSearcher() {
        this.lastDownloadedItemsSearcher = new LastDownloadedItemsSearcher(new VisibleAqlItemsSearchHelper(authService, repoService));
    }

    /**
     * createde  a helper for retrieve artifacts stats records.
     */
    @PostConstruct
    private void setAqlItemsToStatsHelper() {
        this.aqlItemsToStatsHelper = new AqlItemsToStatsHelper(new VisibleAqlItemsSearchHelper(authService, repoService));
    }

    @Override
    public ItemSearchResults<ArtifactSearchResult> searchArtifacts(ArtifactSearchControls controls) {
        if (shouldReturnEmptyResults(controls)) {
            return new ItemSearchResults<>(Lists.<ArtifactSearchResult>newArrayList());
        }
        ArtifactSearcher searcher = new ArtifactSearcher();
        return searcher.search(controls);
    }

    private boolean isEnoughValidCharacters(String str) {
        int validCharsCount = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if ((c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F') || (c >= '0' && c <= '9')) {
                validCharsCount++;
            }
            if (validCharsCount == CHECKSUM_NUMBER_OF_REQUIRED_NORMAL_CHARS) {
                return true;
            }
        }
        return false;
    }

    private void validateChecksum(ChecksumSearchControls searchControls) throws InvalidChecksumException {
        if (!searchControls.isEmpty()) {
            EnumMap<ChecksumType, String> checksums = searchControls.getChecksums();
            for (Map.Entry<ChecksumType, String> checksumEntry : checksums.entrySet()) {
                String checksumVal = checksumEntry.getValue();
                if (StringUtils.isNotBlank(checksumVal)) {
                    // Checking first if all characters are valid (a-fA-F0-9*?)
                    if (!CHECKSUM_VALIDATION_REGEX.matcher(checksumVal).matches()) {
                        throw new InvalidChecksumException("Checksum contains invalid characters");
                    }
                    // Checking if we have at least 12chars that are a-fA-F0-9
                    else if (!isEnoughValidCharacters(checksumVal)) {
                        throw new InvalidChecksumException("Not enough valid characters");
                    }
                }
            }
        }
    }

    @Override
    public Set<RepoPath> searchArtifactsByChecksum(ChecksumSearchControls searchControls)
            throws InvalidChecksumException {

        validateChecksum(searchControls);
        Set<RepoPath> results = null;
        if(searchControls.getChecksums().entrySet().iterator().hasNext()) {
            ChecksumType checksumType = searchControls.getChecksums().entrySet().iterator().next().getKey();

            switch (checksumType) {
                case sha256:
                    results = new HashSet<>(searchArtifactsBySha256(searchControls));
                    break;
                case sha1:
                    results = searchArtifactsBySha1(searchControls).stream()
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
                    break;
                case md5:
                    results = searchArtifactsByMd5(searchControls).stream()
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
                    break;
            }
        }
        return results;
    }

    @Override
    public ItemSearchResults<ArtifactSearchResult> getArtifactsByChecksumResults(ChecksumSearchControls searchControls) throws InvalidChecksumException {
        validateChecksum(searchControls);
        List<ArtifactSearchResult> results = searchArtifactsBySha256(searchControls).stream()
                .filter(Objects::nonNull)
                .map(repoService::getItemInfo)
                .map(ArtifactSearchResult::new)
                .collect(Collectors.toList());

        // Search by md5 and sha1
        results.addAll(new ArtifactSearcher().searchArtifactsByChecksum(searchControls).stream()
                .filter(Objects::nonNull)
                .map(ArtifactSearchResult::new)
                .collect(Collectors.toList()));
        return new ItemSearchResults<>(results, results.size());
    }

    private Set<RepoPath> searchArtifactsBySha256(ChecksumSearchControls searchControls) {
        String sha256 = searchControls.getChecksums().get(ChecksumType.sha256);
        if (StringUtils.isBlank(sha256)) {
            return Sets.newHashSet();
        }
        AqlApiItem.AndClause baseQuery = AqlApiItem.and();
        AqlApiItem.OrClause selectedRepos = AqlApiItem.or();
        if (CollectionUtils.notNullOrEmpty(searchControls.getSelectedRepoForSearch())) {
            searchControls.getSelectedRepoForSearch()
                    .forEach(repo -> selectedRepos.append(AqlApiItem.repo().equal(repo)));
        }
        baseQuery.append(selectedRepos);
        if (StorageContextHelper.get().beanForType(InternalDbService.class).isSha256Ready()) {
            baseQuery.append(AqlApiItem.sha2().matches(sha256));
        } else {
            //[sha2]:remove after migration-enforcing major
            baseQuery.append(AqlApiItem.property().property("sha256", AqlComparatorEnum.matches, sha256));
        }
        AqlApiItem query = AqlApiItem.create().filter(baseQuery);
        if (searchControls.isLimitSearchResults()) {
            query.limit(searchControls.getLimit());
        }
        AqlEagerResult<AqlItem> results = aqlService.executeQueryEager(query);
        return results.getResults().stream()
                .map(AqlUtils::fromAql)
                .collect(Collectors.toSet());
    }

    private Set<RepoPath> searchArtifactsBySha1(ChecksumSearchControls searchControls) {
        String sha1 = searchControls.getChecksums().get(ChecksumType.sha1);
        if (StringUtils.isBlank(sha1)) {
            return Sets.newHashSet();
        }
        AqlApiItem.AndClause baseQuery = AqlApiItem.and();
        AqlApiItem.OrClause selectedRepos = AqlApiItem.or();
        if (CollectionUtils.notNullOrEmpty(searchControls.getSelectedRepoForSearch())) {
            searchControls.getSelectedRepoForSearch()
                    .forEach(repo -> selectedRepos.append(AqlApiItem.repo().equal(repo)));
        }
        baseQuery.append(selectedRepos);

        baseQuery.append(AqlApiItem.sha1Actual().matches(sha1));
        AqlApiItem query = AqlApiItem.create().filter(baseQuery);
        if (searchControls.isLimitSearchResults()) {
            query.limit(searchControls.getLimit());
        }
        AqlEagerResult<AqlItem> results = aqlService.executeQueryEager(query);
        return results.getResults().stream()
                .map(AqlUtils::fromAql)
                .collect(Collectors.toSet());
    }

    private Set<RepoPath> searchArtifactsByMd5(ChecksumSearchControls searchControls) {
        String md5 = searchControls.getChecksums().get(ChecksumType.md5);
        if (StringUtils.isBlank(md5)) {
            return Sets.newHashSet();
        }
        AqlApiItem.AndClause baseQuery = AqlApiItem.and();
        AqlApiItem.OrClause selectedRepos = AqlApiItem.or();
        if (CollectionUtils.notNullOrEmpty(searchControls.getSelectedRepoForSearch())) {
            searchControls.getSelectedRepoForSearch()
                    .forEach(repo -> selectedRepos.append(AqlApiItem.repo().equal(repo)));
        }
        baseQuery.append(selectedRepos);

        baseQuery.append(AqlApiItem.md5Actual().matches(md5));
        AqlApiItem query = AqlApiItem.create().filter(baseQuery);
        if (searchControls.isLimitSearchResults()) {
            query.limit(searchControls.getLimit());
        }
        AqlEagerResult<AqlItem> results = aqlService.executeQueryEager(query);
        return results.getResults().stream()
                .map(AqlUtils::fromAql)
                .collect(Collectors.toSet());
    }

    @Override
    public ItemSearchResults<ArchiveSearchResult> searchArchiveContent(ArchiveSearchControls controls) {
        if (shouldReturnEmptyResults(controls)) {
            return new ItemSearchResults<>(Lists.<ArchiveSearchResult>newArrayList());
        }
        ArchiveSearcher searcher = new ArchiveSearcher();
        return searcher.search(controls);
    }

    @Override
    public ItemSearchResults<ArchiveSearchResult> searchArchiveContentAql(ArchiveSearchControls controls) {
        if (shouldReturnEmptyResults(controls)) {
            return new ItemSearchResults<>(Lists.<ArchiveSearchResult>newArrayList());
        }
        ArchiveSearcherAql searcher = new ArchiveSearcherAql();
        return searcher.search(controls);
    }


    @Override
    public Stream<AqlStatistics> getRemoteDownloadedSinceEventsStatistics(@Nonnull RepoPath rootRepoPath, long lastReplicationTime, long limit) {
        return new DownloadedSearcher(aqlService)
                .remoteDownloadedAfter(rootRepoPath, lastReplicationTime, e -> {
                    if (e != null) throw new StatsRetrieveException("Failed", e);
                }, limit);
    }

    @Override
    public Stream<AqlStatistics> getDownloadedSinceEventsStatistics(@Nonnull RepoPath rootRepoPath, long lastReplicationTime, long limit) {
        return new DownloadedSearcher(aqlService)
                .downloadedAfter(rootRepoPath, lastReplicationTime, e -> {
                    if (e != null) throw new StatsRetrieveException("Failed", e);
                }, limit);
    }

    @Override
    public Stream<AqlStatistics> getTimestampStatsEvents(RepoPath repoPath, long timestamp, boolean remote) {
        return new DownloadedSearcher(aqlService)
                .downloadedOnTimestamp(repoPath, timestamp, remote, e -> {
                    if (e != null) throw new StatsRetrieveException("Failed", e);
                });
    }

    /**
     * retrieve a stats stream for each item matching the criteria - perform a query for each sql.
     * The stream should be closed, it Must be either fully consumed or closed (fully consumed would close the underline resource)
     *
     * @param since         time and date which artifacts searched weren't downloaded since. (optional - null if not used)
     * @param createdBefore time and date which artifacts were already created then. (optional - null if not used)
     * @param repositories  scope for repositories searched.
     * @return Stream<StatsSearchResult
     */
    @Override
    public Stream<StatsSearchResult> streamStatsForArtifactsNotDownloadedSince(@Nullable Calendar since,
                                                                               @Nullable Calendar createdBefore,
                                                                               String... repositories) {
        Stream<AqlItem> result = streamAqlItemNotDownloadedSince(since, createdBefore, repositories);
        return aqlItemsToStatsHelper.toStatsSearchResultStream(result);
    }


    static private List<String> toRepoList(String[] repositories) {
        return repositories != null ? Arrays.asList(repositories) : Collections.emptyList();
    }

    @Override
    public Stream<RepoPath> streamArtifactsRepoPathNotDownloadedSince(@Nullable Calendar since,
                                                                      @Nullable Calendar createdBefore,
                                                                      String... repositories) {
        return streamAqlItemNotDownloadedSince(since, createdBefore, repositories).map(AqlConverts.toRepoPath);
    }


    @Override
    public ItemSearchResults<GavcSearchResult> searchGavc(GavcSearchControls controls) {
        if (shouldReturnEmptyResults(controls)) {
            return new ItemSearchResults<>(Lists.<GavcSearchResult>newArrayList());
        }
        GavcSearcher searcher = new GavcSearcher();
        return searcher.search(controls);
    }

    @Override
    public ItemSearchResults<PropertySearchResult> searchProperty(PropertySearchControls controls) {
        if (shouldReturnEmptyResults(controls)) {
            return new ItemSearchResults<>(Lists.<PropertySearchResult>newArrayList());
        }
        PropertySearcher searcher = new PropertySearcher();
        return searcher.search(controls);
    }

    @Override
    public ItemSearchResults<PropertySearchResult> searchPropertyAql(PropertySearchControls controls) {
        if (shouldReturnEmptyResults(controls)) {
            return new ItemSearchResults<>(Lists.<PropertySearchResult>newArrayList());
        }
        PropertySearcher searcher = new PropertySearcher();
        return searcher.search(controls);
    }

    @Override
    public ItemSearchResults<ArtifactSearchResult> searchArtifactsInRange(
            Calendar from,
            Calendar to,
            List<String> reposToSearch,
            RestDateFieldName... dates) {
        if (from == null && to == null) {
            log.warn("Received search artifacts in range with no range!");
            return new ItemSearchResults<>(Collections.<ArtifactSearchResult>emptyList(), 0L);
        }
        if (dates == null || dates.length == 0) {
            log.warn("Received search artifacts in range with no date field!");
            return new ItemSearchResults<>(Collections.<ArtifactSearchResult>emptyList(), 0);
        }

        // all artifactory files that were created or modified after input date
        VfsQuery query = vfsQueryService.createQuery().expectedResult(FILE)
                .setRepoKeys(reposToSearch)
                .name(MavenNaming.MAVEN_METADATA_NAME).comp(NOT_EQUAL);
        // If only one date make it simple
        if (dates.length == 1) {
            addDateRangeFilter(query, from, to, dates[0]);
        } else {
            query.startGroup();
            for (int i = 0; i < dates.length; i++) {
                RestDateFieldName date = dates[i];
                query.startGroup();
                addDateRangeFilter(query, from, to, date);
                // The last one is null end group
                if (i < dates.length - 1) {
                    query.endGroup(OR);
                } else {
                    query.endGroup(null);
                }
            }
            query.endGroup();
        }
        VfsQueryResult queryResult = query.execute(getQueryLimit());
        // There are no limit here the the getCount is really the total amount
        List<ArtifactSearchResult> results = new ArrayList<>((int) queryResult.getCount());
        if (artifactSearcher == null) {
            artifactSearcher = new ArtifactSearcher();
        }
        for (VfsQueryRow vfsQueryRow : queryResult.getAllRows()) {
            ItemInfo item = vfsQueryRow.getItem();
            if (artifactSearcher.isResultAcceptable(item.getRepoPath()) &&
                    isRangeResultValid(item.getRepoPath(), reposToSearch)) {
                results.add(new ArtifactSearchResult(item));
            }
        }
        return new ItemSearchResults<>(results, queryResult.getCount());
    }

    private int getQueryLimit() {
        boolean isLimited = ConstantValues.searchLimitAnonymousUserOnly.getBoolean() ?
                authService.isAnonymous() : !authService.isAdmin();
        return isLimited ? ConstantValues.searchUserQueryLimit.getInt() : Integer.MAX_VALUE;
    }

    private void addDateRangeFilter(VfsQuery query, Calendar from, Calendar to, RestDateFieldName dateField) {
        if (from != null) {
            query.prop(FieldNameConverter.fromRest(dateField).getPropName()).comp(GREATER_THAN).val(from);
        }
        if (to != null) {
            query.prop(FieldNameConverter.fromRest(dateField).getPropName()).comp(LOWER_THAN_EQUAL).val(to);
        }
    }

    @Override
    public VersionSearchResults searchVersionUnits(VersionUnitSearchControls controls) {
        VersionUnitSearcher searcher = new VersionUnitSearcher();
        ItemSearchResults<VersionUnitSearchResult> res = searcher.doSearch(controls);
        return new VersionSearchResults(Sets.newHashSet(res.getResults()), res.getFullResultsCount(),
                res.getFullResultsCount() >= ConstantValues.searchUserQueryLimit.getInt(), false);
    }

    @Override
    public Set<BuildRun> getLatestBuilds() {
        BuildSearcher searcher = new BuildSearcher();
        try {
            return searcher.getLatestBuildsByName();
        } catch (Exception e) {
            throw new RepositoryRuntimeException(e);
        }
    }

    @Override
    public Set<BuildRun> findBuildsByArtifactChecksum(String sha1, String sha2, String md5) {
        BuildSearcher searcher = new BuildSearcher();
        return searcher.findBuildsByArtifactChecksum(sha1, sha2, md5).stream()
                .filter(build -> authService.isBuildBasicRead(build.getName(), build.getNumber(), build.getStarted()))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<BuildRun> findBuildsByDependencyChecksum(String sha1, String sha2, String md5) {
        BuildSearcher searcher = new BuildSearcher();
        return searcher.findBuildsByDependencyChecksum(sha1, sha2, md5).stream()
                .filter(build -> authService.isBuildBasicRead(build.getName(), build.getNumber(), build.getStarted()))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> searchArtifactsByPattern(String pattern) throws ExecutionException, InterruptedException,
            TimeoutException {
        if (StringUtils.isBlank(pattern)) {
            throw new IllegalArgumentException("Unable to search for an empty pattern");
        }

        pattern = pattern.trim();
        if (!pattern.contains(":")) {
            throw new IllegalArgumentException("Pattern must be formatted like [repo-key]:[pattern/to/search/for]");
        }

        if (pattern.contains("**")) {
            throw new IllegalArgumentException("Pattern cannot contain the '**' wildcard");
        }

        String[] patternTokens = StringUtils.split(pattern, ":", 2);
        String repoKey = patternTokens[0];

        Repo repo = repoService.repositoryByKey(repoKey);

        if ((repo == null) || (patternTokens.length == 1) || (StringUtils.isBlank(patternTokens[1]))) {
            return Sets.newHashSet();
        }

        String innerPattern = StringUtils.replace(patternTokens[1], "\\", "/");

        Callable<Set<String>> callable = new Callable<Set<String>>() {

            @Override
            public Set<String> call() throws Exception {
                Set<String> pathsToReturn = Sets.newHashSet();
                List<String> patternFragments = Lists.newArrayList(StringUtils.split(innerPattern, "/"));

                if (repo.isReal()) {
                    String repoKey;
                    if (repo.isLocal() || repo.isCache()) {
                        repoKey = repo.getKey();
                    } else {
                        LocalCacheRepo localCacheRepo = ((RemoteRepoBase) repo).getLocalCacheRepo();
                        if (localCacheRepo != null) {
                            repoKey = localCacheRepo.getKey();
                        } else {
                            repoKey = null;
                        }
                    }
                    if (repoKey != null) {
                        collectLocalRepoItemsRecursively(patternFragments, pathsToReturn,
                                InternalRepoPathFactory.create(repoKey, ""));
                    }
                } else {
                    collectVirtualRepoItemsRecursively(patternFragments, pathsToReturn,
                            InternalRepoPathFactory.create(repo.getKey(), ""));
                }
                return pathsToReturn;
            }
        };

        Future<Set<String>> future = executor.submit(callable);
        return future.get(ConstantValues.searchPatternTimeoutSecs.getLong(), TimeUnit.SECONDS);
    }

    private boolean shouldReturnEmptyResults(SearchControls controls) {
        return checkUnauthorized() || controls.isEmpty();
    }

    private boolean checkUnauthorized() {
        boolean unauthorized =
                !authService.isAuthenticated() || (authService.isAnonymous() && !authService.isAnonAccessEnabled());
        if (unauthorized) {
            AccessLogger.unauthorizedSearch();
        }
        return unauthorized;
    }

    @Override
    public void init() {
    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor, List<DataDiff<?>> configDiff) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
    }

    /**
     * Recursively collect items matching a given pattern from a local repo
     *
     * @param patternFragments Accepted pattern fragments
     * @param pathsToReturn    Result path aggregator
     * @param repoPath         Repo path to search at
     */
    private void collectLocalRepoItemsRecursively(List<String> patternFragments, Set<String> pathsToReturn,
                                                  RepoPath repoPath) {

        org.artifactory.fs.ItemInfo itemInfo = repoService.getItemInfo(repoPath);

        if (!patternFragments.isEmpty()) {

            if (itemInfo.isFolder()) {

                String firstFragment = patternFragments.get(0);
                if (StringUtils.isBlank(firstFragment)) {
                    return;
                }

                for (String childName : repoService.getChildrenNames(repoPath)) {

                    if (patternMatches(firstFragment, childName)) {

                        List<String> fragmentsToPass = Lists.newArrayList(patternFragments);
                        fragmentsToPass.remove(0);
                        collectLocalRepoItemsRecursively(fragmentsToPass, pathsToReturn,
                                InternalRepoPathFactory.create(repoPath, childName));
                    }
                }
            }
        } else if (!itemInfo.isFolder()) {
            pathsToReturn.add(repoPath.getPath());
        }
    }

    /**
     * Recursively collect items matching a given pattern from a virtual repo
     *
     * @param patternFragments Accepted pattern fragments
     * @param pathsToReturn    Result path aggregator
     * @param repoPath         Repo path to search at
     */
    private void collectVirtualRepoItemsRecursively(List<String> patternFragments, Set<String> pathsToReturn,
                                                    RepoPath repoPath) {

        VirtualRepoItem itemInfo = repoBrowsingService.getVirtualRepoItem(repoPath);
        if (itemInfo == null) {
            return;
        }

        if (!patternFragments.isEmpty()) {

            if (itemInfo.isFolder()) {

                String firstFragment = patternFragments.get(0);
                if (StringUtils.isBlank(firstFragment)) {
                    return;
                }
                //TODO: [by tc] should not use the remote children
                for (VirtualRepoItem child : repoBrowsingService.getVirtualRepoItems(repoPath)) {

                    if (patternMatches(firstFragment, child.getName())) {

                        List<String> fragmentsToPass = Lists.newArrayList(patternFragments);
                        fragmentsToPass.remove(0);
                        collectVirtualRepoItemsRecursively(fragmentsToPass, pathsToReturn,
                                InternalRepoPathFactory.create(repoPath, child.getName()));
                    }
                }
            }
        } else if (!itemInfo.isFolder()) {
            pathsToReturn.add(repoPath.getPath());
        }
    }

    /**
     * Checks if the given repo-relative path matches any of the given accepted patterns
     *
     * @param includePattern Accepted pattern
     * @param path           Repo-relative path to check
     * @return True if the path matches any of the patterns
     */
    private boolean patternMatches(String includePattern, String path) {
        return PathMatcher.matches(path, Lists.newArrayList(includePattern), GlobalExcludes.getGlobalExcludes(), true);
    }

    /**
     * Indicates whether the range query result repo path is valid
     *
     * @param repoPath      Repo path of query result
     * @param reposToSearch Lists of repositories to search within
     * @return True if the repo path is valid and comes from a local repo
     */
    private boolean isRangeResultValid(RepoPath repoPath, List<String> reposToSearch) {
        if (repoPath == null) {
            return false;
        }
        if ((reposToSearch != null) && !reposToSearch.isEmpty()) {
            return true;
        }

        LocalRepo localRepo = repoService.localOrCachedRepositoryByKey(repoPath.getRepoKey());
        return (localRepo != null) && (!NamingUtils.isChecksum(repoPath.getPath()));
    }


    /**
     * get all not downloaded visible not checksum Aql items.
     */
    private Stream<AqlItem> streamAqlItemNotDownloadedSince(@Nullable Calendar since, @Nullable Calendar createdBefore,
                                                            String... repositories) {
        StatsSearchControls controls = new StatsSearchControls();
        controls.setLimitSearchResults(false);
        controls.setCreatedBefore(createdBefore);
        controls.setSelectedRepoForSearch(toRepoList(repositories));
        if (since == null) {
            since = Calendar.getInstance();
        }
        controls.setDownloadedSince(since);

        Consumer<Exception> onFinish = (e) -> {
            if (e == null) {
                if (log.isTraceEnabled()) {
                    log.trace("Searched done {}", controls);
                }
            } else {
                log.warn("Failed {}", controls, e);
            }
        };
        return lastDownloadedItemsSearcher.searchAsStream(controls, onFinish);
    }


    @Override
    public Stream<ItemInfo> streamArtifactsItemInfoNotDownloadedSince(@Nullable Calendar since,
                                                                      @Nullable Calendar createdBefore,
                                                                      String... repositories) {
        return streamAqlItemNotDownloadedSince(since, createdBefore, repositories).map(AqlConverts.toItemInfo);
    }

    static class StatsRetrieveException extends RuntimeException {
        public StatsRetrieveException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
