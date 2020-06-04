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

package org.artifactory.storage.db.build.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.build.*;
import org.artifactory.api.build.model.diff.BuildParams;
import org.artifactory.api.rest.build.ContinueBuildFilter;
import org.artifactory.aql.AqlService;
import org.artifactory.aql.api.domain.sensitive.AqlApiBuild;
import org.artifactory.aql.api.internal.AqlApiDynamicFieldsDomains;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.result.rows.AqlBuild;
import org.artifactory.build.BuildId;
import org.artifactory.build.BuildInfoUtils;
import org.artifactory.build.BuildRun;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.common.ConstantValues;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.binstore.service.BinaryInfo;
import org.artifactory.storage.binstore.service.BinaryService;
import org.artifactory.storage.build.service.BuildSearchCriteria;
import org.artifactory.storage.db.InternalDbService;
import org.artifactory.storage.db.build.dao.BuildArtifactsDao;
import org.artifactory.storage.db.build.dao.BuildDependenciesDao;
import org.artifactory.storage.db.build.dao.BuildModulesDao;
import org.artifactory.storage.db.build.dao.BuildsDao;
import org.artifactory.storage.db.build.entity.*;
import org.artifactory.storage.db.util.blob.BlobWrapperFactory;
import org.artifactory.storage.jobs.migration.buildinfo.BuildMigrationUtils;
import org.artifactory.ui.rest.service.builds.search.BuildsSearchFilter;
import org.artifactory.util.CollectionUtils;
import org.jfrog.build.api.Module;
import org.jfrog.build.api.*;
import org.jfrog.build.api.release.PromotionStatus;
import org.jfrog.common.JsonUtils;
import org.jfrog.common.StreamSupportUtils;
import org.jfrog.storage.wrapper.BlobWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.artifactory.aql.api.domain.sensitive.AqlApiBuild.*;
import static org.artifactory.build.BuildInfoUtils.parseBuildTime;

/**
 * Date: 11/14/12
 * Time: 12:42 PM
 *
 * @author freds
 */
@Service
public class BuildStoreServiceImpl implements InternalBuildStoreService {
    private static final Logger log = LoggerFactory.getLogger(BuildStoreServiceImpl.class);

    @Autowired
    private InternalDbService dbService;

    @Autowired
    private BinaryService binaryService;

    @Autowired
    private BuildsDao buildsDao;

    @Autowired
    private BuildModulesDao buildModulesDao;

    @Autowired
    private BuildArtifactsDao buildArtifactsDao;

    @Autowired
    private BuildDependenciesDao buildDependenciesDao;

    @Autowired
    private BlobWrapperFactory blobsFactory;

    @Autowired
    private AqlService aqlService;

    public static Date parseStringToDate(String dateString) {
        return new Date(parseBuildTime(dateString));
    }

    public static String formatDateToString(long buildStarted) {
        return BuildInfoUtils.formatBuildTime(buildStarted);
    }

    @Override
    public void addBuild(Build build, boolean stateReady) {
        try {
            String buildStarted = build.getStarted();
            Date parsedDate = parseStringToDate(buildStarted);

            // TODO: [by fsi] we are loosing the timezone information written in the JSON
            // Generates a big inconsistency between DB entry and JSON data
            BuildEntity dbBuild = new BuildEntity(dbService.nextId(), build.getName(), build.getNumber(),
                    parsedDate.getTime(),
                    build.getUrl(), System.currentTimeMillis(), build.getArtifactoryPrincipal(),
                    0L, null);
            long buildId = dbBuild.getBuildId();
            dbBuild.setProperties(createProperties(buildId, build));
            dbBuild.setPromotions(createPromotions(buildId, build));
            BlobWrapper buildJson = getBuildJsonWrapper(build, stateReady);
            buildsDao.createBuild(dbBuild, buildJson);
            insertModules(buildId, build);
        } catch (SQLException e) {
            throw new StorageException("Could not insert build " + build, e);
        }
    }

    private ArrayList<BuildPromotionStatus> createPromotions(long buildId, Build build) {
        List<PromotionStatus> statuses = build.getStatuses();
        ArrayList<BuildPromotionStatus> buildPromotions;
        if (statuses != null && !statuses.isEmpty()) {
            buildPromotions = new ArrayList<>(statuses.size());
            for (PromotionStatus status : statuses) {
                buildPromotions.add(convertPromotionStatus(buildId, status));
            }
        } else {
            buildPromotions = new ArrayList<>(1);
        }
        return buildPromotions;
    }

    private BuildPromotionStatus convertPromotionStatus(long buildId, PromotionStatus status) {
        return new BuildPromotionStatus(buildId,
                status.getTimestampDate().getTime(),
                status.getUser(),
                status.getStatus(),
                status.getRepository(),
                status.getComment(),
                status.getCiUser());
    }

    private Set<BuildProperty> createProperties(long buildId, Build build) {
        Properties properties = build.getProperties();
        Set<BuildProperty> buildProperties;
        if (properties != null && !properties.isEmpty()) {
            buildProperties = new HashSet<>(properties.size());
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                buildProperties.add(
                        new BuildProperty(dbService.nextId(), buildId, entry.getKey().toString(),
                                entry.getValue().toString())
                );
            }
        } else {
            buildProperties = new HashSet<>(1);
        }
        return buildProperties;
    }

    private void insertModules(long buildId, Build build) throws SQLException {
        List<Module> modules = build.getModules();
        if (modules == null || modules.isEmpty()) {
            // Nothing to do here
            return;
        }
        for (Module module : modules) {
            BuildModule dbModule = new BuildModule(dbService.nextId(), buildId, module.getId());
            Properties properties = module.getProperties();
            Set<ModuleProperty> moduleProperties;
            if (properties != null && !properties.isEmpty()) {
                moduleProperties = Sets.newHashSetWithExpectedSize(properties.size());
                for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                    moduleProperties.add(
                            new ModuleProperty(dbService.nextId(), dbModule.getModuleId(),
                                    entry.getKey().toString(),
                                    entry.getValue().toString())
                    );
                }
            } else {
                moduleProperties = Sets.newHashSetWithExpectedSize(1);
            }
            dbModule.setProperties(moduleProperties);
            buildModulesDao.createBuildModule(dbModule);

            List<Artifact> artifacts = module.getArtifacts();
            List<BuildArtifact> dbArtifacts;
            if (artifacts != null && !artifacts.isEmpty()) {
                dbArtifacts = Lists.newArrayListWithExpectedSize(artifacts.size());
                for (Artifact artifact : artifacts) {
                    // Artifact properties are not inserted in DB
                    dbArtifacts.add(new BuildArtifact(dbService.nextId(), dbModule.getModuleId(),
                            artifact.getName(), artifact.getType(), artifact.getSha1(), artifact.getMd5(), artifact.getSha256()));
                }
            } else {
                dbArtifacts = Lists.newArrayListWithExpectedSize(1);
            }
            buildArtifactsDao.createBuildArtifacts(dbArtifacts);

            List<Dependency> dependencies = module.getDependencies();
            List<BuildDependency> dbDependencies;
            if (dependencies != null && !dependencies.isEmpty()) {
                dbDependencies = Lists.newArrayListWithExpectedSize(dependencies.size());
                for (Dependency dependency : dependencies) {
                    // Dependency properties are not inserted in DB
                    dbDependencies.add(new BuildDependency(dbService.nextId(), dbModule.getModuleId(),
                            dependency.getId(), dependency.getScopes(), dependency.getType(),
                            dependency.getSha1(), dependency.getMd5(), dependency.getSha256()));
                }
            } else {
                dbDependencies = Lists.newArrayListWithExpectedSize(1);
            }
            buildDependenciesDao.createBuildDependencies(dbDependencies);
        }
    }

    @Override
    public void populateMissingChecksums(Build build) {
        List<Module> modules = build.getModules();
        if (modules != null && !modules.isEmpty()) {
            for (Module module : modules) {
                handleBeanPopulation(module.getArtifacts());
                handleBeanPopulation(module.getDependencies());
            }
        }
    }

    @Override
    public BuildRun getBuildRun(String buildName, String buildNumber, String buildStarted) {
        try {
            Date parsedDate = parseStringToDate(buildStarted);
            BuildEntity entity = buildsDao.findBuild(buildName, buildNumber, parsedDate.getTime());
            if (entity != null) {
                return getBuildRun(entity);
            }
        } catch (SQLException e) {
            throw new StorageException("Could not execute find build for build" +
                    " name='" + buildName + "' number='" + buildNumber + "' start='" + buildStarted + "'", e);
        }
        return null;
    }

    @Override
    public void addPromotionStatus(Build build, PromotionStatus promotion, String currentUser, boolean stateReady) {
        if (log.isDebugEnabled()) {
            log.debug("Adding build promotion status for build '{}'", build);
        }
        BuildRun buildRun = getBuildRun(build.getName(), build.getNumber(), build.getStarted());
        if (buildRun == null) {
            throw new StorageException("Could not add promotion " + promotion + " to non existent build " + build);
        }
        try {
            long buildId = findIdFromBuildRun(buildRun);
            BlobWrapper buildJson = getBuildJsonWrapper(build, stateReady);
            buildsDao.addPromotionStatus(buildId, convertPromotionStatus(buildId, promotion),
                    buildJson, currentUser, System.currentTimeMillis());
        } catch (SQLException e) {
            throw new StorageException("Could not add promotion " + promotion + " for build " + buildRun, e);
        }
    }

    @Override
    public boolean exists(Build build) {
        String name = build.getName();
        String number = build.getNumber();
        String started = build.getStarted();
        try {
            return buildsDao.findBuildId(name, number, parseBuildTime(started)) > 0;
        } catch (SQLException e) {
            throw new StorageException(format("Error encountered while verifying build existence for build %s:%s-%s", name, number, started), e);
        }
    }

    @Override
    public Build getBuild(BuildRun buildRun) {
        try {
            boolean shouldFixProperties = ConstantValues.buildInfoMigrationFixProperties.getBoolean();
            long buildId = findIdFromBuildRun(buildRun);
            if (buildId > 0L) {
                return shouldFixProperties ? JsonUtils.getInstance()
                        .readValue(BuildMigrationUtils.fixBuildProperties(buildsDao.getJsonBuild(buildId, String.class)), Build.class) :
                        buildsDao.getJsonBuild(buildId, Build.class);
            }
        } catch (SQLException e) {
            throw new StorageException("Could not execute get build JSON for build " + buildRun, e);
        }
        return null;
    }

    @Override
    public BuildRun getLatestBuildRun(String buildName, String buildNumber) {
        long buildDate;
        String fullBuildDate = "not found";
        try {
            buildDate = buildsDao.findLatestBuildDate(buildName, buildNumber);
            if (buildDate > 0L) {
                fullBuildDate = formatDateToString(buildDate);
                return new BuildRunImpl(buildName, buildNumber, fullBuildDate);
            }
        } catch (SQLException e) {
            throw new StorageException("Could not find build JSON for latest build" +
                    " name='" + buildName + "' number='" + buildNumber + "' latest date found='" + fullBuildDate + "'",
                    e
            );
        }
        return null;
    }

    @Override
    public BuildEntity getBuildEntity(BuildRun buildRun) {
        try {
            BuildEntity buildEntity = buildsDao.findBuild(buildRun.getName(), buildRun.getNumber(), buildRun.getStartedDate().getTime());
            if (buildEntity == null) {
                throw new StorageException("Cannot create exportable build of non existent build " + buildRun);
            }
            return buildEntity;
        } catch (SQLException e) {
            throw new StorageException("Could not create exportable build object for " + buildRun, e);
        }
    }

    @Override
    public void deleteBuild(String buildName, boolean stateReady) {
        try {
            List<Long> buildIds = buildsDao.findBuildIds(buildName);
            deleteBuilds(buildIds, stateReady);
            log.debug("Deleted build {}", buildName);
        } catch (SQLException e) {
            throw new StorageException("Could not delete all builds with name '" + buildName + "'", e);
        }
    }

    private void deleteBuilds(Collection<Long> buildIds, boolean stateReady) throws SQLException {
        for (Long buildId : buildIds) {
            List<Long> moduleIds = buildModulesDao.findModuleIdsForBuild(buildId);
            if (!moduleIds.isEmpty()) {
                buildArtifactsDao.deleteBuildArtifacts(moduleIds);
                buildDependenciesDao.deleteBuildDependencies(moduleIds);
            }
            buildModulesDao.deleteBuildModules(buildId);
            buildsDao.deleteBuild(buildId, !stateReady);
        }
    }

    @Override
    public void deleteBuild(String buildName, String buildNumber, String buildStarted, boolean stateReady) {
        BuildRunImpl buildRun = new BuildRunImpl(buildName, buildNumber, buildStarted);
        log.debug("Deleting Build " + buildRun);
        try {
            long buildId = findIdFromBuildRun(buildRun);
            if (buildId > 0L) {
                deleteBuilds(ImmutableList.of(buildId), stateReady);
            } else {
                log.info("Build " + buildRun + " already deleted!");
            }
        } catch (SQLException e) {
            throw new StorageException("Could not delete build " + buildRun, e);
        }
    }

    @Override
    public void deleteAllBuilds(boolean stateReady) {
        try {
            buildArtifactsDao.deleteAllBuildArtifacts();
            buildDependenciesDao.deleteAllBuildDependencies();
            buildModulesDao.deleteAllBuildModules();
            buildsDao.deleteAllBuilds(!stateReady);
        } catch (SQLException e) {
            throw new StorageException("Could not delete all builds", e);
        }
    }

    @Override
    public Set<BuildRun> findBuildsForChecksum(BuildSearchCriteria criteria, ChecksumType type, String checksum) {
        if (!type.isValid(checksum)) {
            log.info("Looking for invalid checksum " + type.name() + " '" + checksum + "'");
        }
        try {
            Set<BuildRun> results = Sets.newHashSet();
            if (criteria.searchInDependencies()) {
                Collection<BuildEntity> buildEntities = buildsDao.findBuildsForDependencyChecksum(type, checksum);
                for (BuildEntity buildEntity : buildEntities) {
                    results.add(getBuildRun(buildEntity));
                }
            }

            if (criteria.searchInArtifacts()) {
                Collection<BuildEntity> buildEntities = buildsDao.findBuildsForArtifactChecksum(type, checksum);
                for (BuildEntity buildEntity : buildEntities) {
                    results.add(getBuildRun(buildEntity));
                }
            }

            return results;
        } catch (SQLException e) {
            throw new StorageException("Could not find builds for " + type.name() + " '" + checksum + "'", e);
        }
    }

    @Override
    public List<PublishedModule> getPublishedModules(String buildNumber, String date) {
        List<PublishedModule> buildModule = null;
        try {
             buildModule = buildsDao.getBuildModule(buildNumber, date);
        }catch (Exception e){
            log.error(e.toString());
        }
        return buildModule;
    }

    @Override
    public List<ModuleArtifact> getModuleArtifact(String buildName, String buildNumber, String moduleId, String date) {
        List<ModuleArtifact> moduleArtifactList = null;
        try {
            moduleArtifactList = buildsDao.getModuleArtifact(buildName, buildNumber, date, moduleId);
        } catch (Exception e) {
            log.error(e.toString());
        }
        return moduleArtifactList;
    }

    @Override
    public List<ModuleDependency> getModuleDependency(String buildNumber, String moduleId, String date) {
        List<ModuleDependency> moduleArtifactList = null;
        try {
            moduleArtifactList = buildsDao.getModuleDependency(buildNumber, date, moduleId);
        } catch (Exception e) {
            log.error(e.toString());
        }
        return moduleArtifactList;
    }

    @Override
    public List<ModuleArtifact> getModuleArtifactsForDiffWithPaging(BuildParams buildParams) {
        return buildsDao.getModuleArtifactsForDiffWithPaging(buildParams);
    }

    public List<GeneralBuild> getPrevBuildsList(String buildName, String buildDate) {
        return buildsDao.getPrevBuildsList(buildName, buildDate);
    }

    @Override
    public List<BuildProps> getBuildPropsData(BuildParams buildParams) {
        List<BuildProps> buildPropsList = buildsDao.getBuildPropsList(buildParams);
        return distinctBuildProps(buildPropsList);
    }

    public static List<BuildProps> distinctBuildProps(List<BuildProps> buildPropertyList) {
        List<BuildPropertyAsKeyValue> distinct = buildPropertyList.stream()
                .map(BuildPropertyAsKeyValue::new).distinct().collect(Collectors.toList());

        return distinct.stream().map(it -> new BuildProps(it.propKey, it.propValue, null)).collect(Collectors.toList());
    }

    @Override
    public List<ModuleDependency> getModuleDependencyForDiffWithPaging(BuildParams buildParams) {
        return buildsDao.getModuleDependencyForDiffWithPaging(buildParams);
    }

    @Override
    public Set<BuildRun> getLatestBuildsByName() {
        try {
            List<String> allBuildNames = buildsDao.getAllBuildNames();
            LinkedHashSet<BuildRun> results = new LinkedHashSet<>(allBuildNames.size());
            for (String buildName : allBuildNames) {
                BuildEntity buildEntity = buildsDao.getLatestBuild(buildName);
                if (buildEntity != null) {
                    results.add(getBuildRun(buildEntity));
                }
            }
            return results;
        } catch (SQLException e) {
            throw new StorageException("Could not list all builds by name and latest build date", e);
        }
    }

    @Override
    public List<BuildId> getLatestBuildIDsPaging(ContinueBuildFilter continueBuildFilter) {
        try {
            List<BuildIdEntity> allBuildNames = buildsDao.getLatestBuildIds(continueBuildFilter);
            return StreamSupportUtils.stream(allBuildNames)
                    .map(this::buildIDFromBuildIDEntity)
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new StorageException("Could not list all builds by name and latest build date", e);
        }
    }

    @Override
    public List<BuildId> getLatestBuildIDsByName(String buildName, String fromDate, String toDate, String orderBy, String direction) {
        try {
            List<BuildIdEntity> builds = buildsDao.getLatestBuildIds(buildName, fromDate, toDate, orderBy, direction);
            return builds.stream()
                    .map(this::buildIDFromBuildIDEntity)
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new StorageException("Could not list all builds by name and latest build date", e);
        }
    }

    @Override
    public List<GeneralBuild> getBuildVersions(BuildsSearchFilter filter) {
        List<AqlBuild> allBuildNames = getAqlBuildVersions(filter);

        return allBuildNames.stream()
                .map(this::createGeneralInfo)
                .collect(Collectors.toList());
    }

    private GeneralBuild createGeneralInfo(AqlBuild build) {
        return new GeneralBuild(build.getBuildName(), build.getBuildNumber(), build.getBuildCreated().getTime(),
                        build.getBuildUrl(), build.getBuildPromotionStatus());
    }


    public List<AqlBuild> getAqlBuildVersions(BuildsSearchFilter filter) {
        AqlBase.CriteriaClause<AqlApiBuild> nameEquals = null;
        AqlBase.CriteriaClause<AqlApiBuild> before = null;
        AqlBase.CriteriaClause<AqlApiBuild> after = null;

        AqlBase.OrClause<AqlApiBuild> numbersOrClause = null;

        if (StringUtils.isNotBlank(filter.getName())) {
            nameEquals = name().equal(filter.getName());
        }
        if (filter.getAfter() != 0) {
            after = created().greaterEquals(filter.getAfter());
        }
        if (filter.getBefore() != 0) {
           before = created().lessEquals(filter.getBefore());
        }
        if(filter.getNumbers() != null) {
            numbersOrClause = AqlBase.or();
            for (String number : filter.getNumbers()) {
                numbersOrClause.append(number().equal(number));
            }
        }

        AqlApiBuild aql = AqlApiBuild.create().include(AqlApiBuild.number(),AqlApiBuild.url(),AqlApiBuild.created(),AqlApiBuild.buildPromotions().status())
                .filter(AqlBase.and(
                        nameEquals,
                        after,
                        before,
                        numbersOrClause
                ))
                .addSortElement(sortResultsBy(filter))
                .desc();


        if (filter.getDirection().equals("asc")) {
            aql.asc();
        }

        return aqlService.executeQueryEager(aql).getResults();
    }

    private static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiBuild> sortResultsBy(
            BuildsSearchFilter filter) {

        switch (filter.getOrderBy()) {
            case "date_created":
                return AqlApiBuild.created();
            case "number":
                return AqlApiBuild.number();
            default:
                return AqlApiBuild.created();
        }
    }

    private BuildIdImpl buildIDFromBuildIDEntity(BuildIdEntity buildIDEntity) {
        return new BuildIdImpl(buildIDEntity.getBuildId(), buildIDEntity.getBuildName(),
                                buildIDEntity.getBuildNumber(), buildIDEntity.getBuildDate());
    }

    @Override
    public Set<BuildRun> findBuildsByName(String buildName) {
        Set<BuildRun> results = Sets.newHashSet();
        try {
            List<Long> buildIds = buildsDao.findBuildIds(buildName);
            for (Long buildId : buildIds) {
                getBuildRun(buildId).ifPresent(results::add);
            }
        } catch (SQLException e) {
            throw new StorageException("Could not search for builds with name='" + buildName + "'", e);
        }
        return results;
    }

    @Override
    public Set<BuildRun> findBuildsByNameAndNumber(String buildName, String buildNumber) {
        Set<BuildRun> results = Sets.newHashSet();
        try {
            List<Long> buildIds = buildsDao.findBuildIds(buildName, buildNumber);
            for (Long buildId : buildIds) {
                getBuildRun(buildId).ifPresent(results::add);
            }
        } catch (SQLException e) {
            throw new StorageException(
                    "Could not search for builds with name='" + buildName + "' and number='" + buildNumber + "'", e);
        }
        return results;
    }

    @Override
    public List<String> getAllBuildNames() {
        try {
            return buildsDao.getAllBuildNames();
        } catch (SQLException e) {
            throw new StorageException("Could not retrieve the list of build names", e);
        }
    }

    private long findIdFromBuildRun(BuildRun buildRun) throws SQLException {
        long buildId = 0L;
        if (buildRun instanceof BuildRunImpl) {
            buildId = ((BuildRunImpl) buildRun).getBuildId();
        }
        if (buildId <= 0L) {
            buildId = buildsDao.findBuildId(buildRun.getName(), buildRun.getNumber(),
                    buildRun.getStartedDate().getTime());
        }
        return buildId;
    }

    @Override
    public long findIdFromBuild(BuildRun build) {
        try {
            return buildsDao.findBuildId(build.getName(), build.getNumber(), parseBuildTime(build.getStarted()));
        } catch (SQLException e) {
            throw new StorageException("Could not get build id by name, number and started date", e);
        }
    }

    @Nonnull
    private Optional<BuildRun> getBuildRun(Long buildId) throws SQLException {
        BuildEntity buildEntity = buildsDao.getBuild(buildId);
        if(buildEntity == null){
            return Optional.empty();
        }
        return Optional.of(getBuildRun(buildEntity));
    }

    @Override
    public List<GeneralBuild> getBuildForName(String buildName, ContinueBuildFilter continueBuildFilter) {
        try {
            return buildsDao.getBuildForName(buildName, continueBuildFilter);
        } catch (SQLException e) {
            throw new StorageException(String.format("Could not get builds for name '%s'.", buildName), e);
        }
    }

    private BuildRun getBuildRun(BuildEntity buildEntity) {
        String releaseStatus = null;
        if (!buildEntity.getPromotions().isEmpty()) {
            releaseStatus = buildEntity.getPromotions().last().getStatus();
        }
        return new BuildRunImpl(buildEntity.getBuildId(), buildEntity.getBuildName(), buildEntity.getBuildNumber(),
                formatDateToString(buildEntity.getBuildDate()), buildEntity.getCiUrl(), releaseStatus);
    }

    @Nullable
    private BlobWrapper getBuildJsonWrapper(Build build, boolean stateReady) {
        // Fully migrated to build-info repo, no need to save the build json in db
        BlobWrapper jsonObject = null;
        if (!stateReady) {
            // Legacy code for an instance with upgrade migration not yet complete
            jsonObject = blobsFactory.createJsonObjectWrapper(build);
        }
        return jsonObject;
    }

    /**
     * Locates and fills in missing checksums of a build file bean
     *
     * @param buildFiles List of build files to populate
     */
    private void handleBeanPopulation(List<? extends BuildFileBean> buildFiles) {
        if (CollectionUtils.isNullOrEmpty(buildFiles)) {
            return;
        }
        Set<String> checksums = getMissingChecksums(buildFiles);

        Set<BinaryInfo> binaryInfos = binaryService.findBinaries(checksums);
        Map<String, BinaryInfo> sha1ToInfo = Maps.newHashMap();
        Map<String, BinaryInfo> sha2ToInfo = Maps.newHashMap();
        Map<String, BinaryInfo> md5ToInfo = Maps.newHashMap();

        binaryInfos.forEach(binaryInfo -> {
            sha1ToInfo.put(binaryInfo.getSha1(), binaryInfo);
            sha2ToInfo.put(binaryInfo.getSha2(), binaryInfo);
            md5ToInfo.put(binaryInfo.getMd5(), binaryInfo);
        });
        populateMissingChecksums(buildFiles, sha1ToInfo, sha2ToInfo, md5ToInfo);
    }

    /**
     * Collects one available checksum for each {@link BuildFileBean} in {@param buildFiles} that has one or more
     * missing checksums from its build info.
     * If all checksums are missing from the build info or if the bean has all, it is skipped.
     */
    private Set<String> getMissingChecksums(List<? extends BuildFileBean> buildFiles) {
        Set<String> checksums = Sets.newHashSet();
        for (BuildFileBean buildFile : buildFiles) {
            boolean sha1Exists = StringUtils.isNotBlank(buildFile.getSha1());
            boolean sha2Exists = StringUtils.isNotBlank(buildFile.getSha256());
            boolean md5Exists = StringUtils.isNotBlank(buildFile.getMd5());

            //If the bean has all or none of the checksums, no point in looking for this bean's other checksums
            if ((sha1Exists && sha2Exists && md5Exists) || ((!sha1Exists && !sha2Exists && !md5Exists))) {
                continue;
            }
            //This bean is missing one or more checksums - need to search for the others based on the first one it has.
            if (sha1Exists) {
                checksums.add(buildFile.getSha1());
            }
            else if (sha2Exists) {
                checksums.add(buildFile.getSha256());
            }
            else {
                checksums.add(buildFile.getMd5());
            }
        }
        return checksums;
    }

    private void populateMissingChecksums(List<? extends BuildFileBean> buildFiles, Map<String, BinaryInfo> sha1ToInfo,
            Map<String, BinaryInfo> sha2ToInfo, Map<String, BinaryInfo> md5ToInfo) {

        for (BuildFileBean buildFile : buildFiles) {
            boolean sha1Exists = StringUtils.isNotBlank(buildFile.getSha1());
            boolean sha2Exists = StringUtils.isNotBlank(buildFile.getSha256());
            boolean md5Exists = StringUtils.isNotBlank(buildFile.getMd5());

            //If the bean has all or none of the checksums, skip it
            if ((sha1Exists && sha2Exists && md5Exists) || ((!sha1Exists && !sha2Exists && !md5Exists))) {
                continue;
            }

            if (sha1Exists) {
                BinaryInfo info = sha1ToInfo.get(buildFile.getSha1());
                if (info == null) {
                    continue;
                }
                if (!sha2Exists) {
                    setNewChecksum(ChecksumType.sha256, info.getSha2(), buildFile::setSha256);
                }
                if (!md5Exists) {
                    setNewChecksum(ChecksumType.md5, info.getMd5(), buildFile::setMd5);
                }
            } else if (sha2Exists) {
                BinaryInfo info = sha2ToInfo.get(buildFile.getSha256());
                if (info == null) {
                    continue;
                }
                //there's no sha1 here for sure
                setNewChecksum(ChecksumType.sha1, info.getSha1(), buildFile::setSha1);
                if (!md5Exists) {
                    setNewChecksum(ChecksumType.md5, info.getMd5(), buildFile::setMd5);
                }
            } else { //md5
                BinaryInfo info = md5ToInfo.get(buildFile.getMd5());
                if (info == null) {
                    continue;
                }
                //there's no sha1 and no sha2 here for sure
                setNewChecksum(ChecksumType.sha256, info.getSha2(), buildFile::setSha256);
                setNewChecksum(ChecksumType.sha1, info.getSha1(), buildFile::setSha1);
            }
        }
    }

    private void setNewChecksum(ChecksumType checksumType, String newChecksum, Consumer<String> checksumSetter) {
        if (checksumType.isValid(newChecksum)) {
            checksumSetter.accept(newChecksum);
        }
    }

}
