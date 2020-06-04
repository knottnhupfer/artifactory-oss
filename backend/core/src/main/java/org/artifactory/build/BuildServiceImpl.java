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

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.build.BuildAddon;
import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.addon.license.LicensesAddon;
import org.artifactory.addon.plugin.PluginAction;
import org.artifactory.addon.plugin.PluginsAddon;
import org.artifactory.addon.plugin.build.AfterBuildSaveAction;
import org.artifactory.addon.plugin.build.BeforeBuildSaveAction;
import org.artifactory.addon.xray.XrayAddon;
import org.artifactory.api.build.*;
import org.artifactory.api.build.model.BuildGeneralInfo;
import org.artifactory.api.build.model.diff.BuildParams;
import org.artifactory.api.build.request.BuildArtifactoryRequest;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.exception.RepoRejectException;
import org.artifactory.api.request.ArtifactoryResponse;
import org.artifactory.api.request.UploadService;
import org.artifactory.api.rest.artifact.PromotionResult;
import org.artifactory.api.rest.build.ContinueBuildFilter;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.aql.AqlService;
import org.artifactory.aql.api.domain.sensitive.AqlApiBuild;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.model.AqlComparatorEnum;
import org.artifactory.aql.result.AqlEagerResult;
import org.artifactory.aql.result.AqlLazyResult;
import org.artifactory.aql.result.rows.AqlBaseFullRowImpl;
import org.artifactory.aql.result.rows.AqlBuild;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.config.CentralConfigKey;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.repo.RepoBaseDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.factory.xstream.XStreamFactory;
import org.artifactory.fs.FileInfo;
import org.artifactory.io.NullResourceStreamHandle;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.repo.service.deploy.ArtifactoryDeployRequest;
import org.artifactory.repo.service.deploy.ArtifactoryDeployRequestBuilder;
import org.artifactory.request.InternalArtifactoryResponse;
import org.artifactory.resource.ResourceStreamHandle;
import org.artifactory.rest.exception.BadRequestException;
import org.artifactory.rest.exception.ConflictException;
import org.artifactory.rest.exception.ForbiddenException;
import org.artifactory.rest.exception.NotFoundException;
import org.artifactory.sapi.common.ExportSettings;
import org.artifactory.sapi.common.ImportSettings;
import org.artifactory.sapi.interceptor.context.DeleteContext;
import org.artifactory.schedule.TaskBase;
import org.artifactory.schedule.TaskService;
import org.artifactory.schedule.TaskUtils;
import org.artifactory.security.AccessLogger;
import org.artifactory.security.SystemAuthenticationToken;
import org.artifactory.spring.Reloadable;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.db.InternalDbService;
import org.artifactory.storage.db.build.entity.BuildEntity;
import org.artifactory.storage.db.build.service.BuildIdImpl;
import org.artifactory.storage.db.build.service.BuildRunImpl;
import org.artifactory.storage.db.build.service.InternalBuildStoreService;
import org.artifactory.storage.db.util.JdbcHelper;
import org.artifactory.storage.jobs.migration.buildinfo.BuildInfoCalculationFatalException;
import org.artifactory.storage.jobs.migration.buildinfo.BuildInfoCalculationWorkItem;
import org.artifactory.storage.jobs.migration.buildinfo.BuildInfoMigrationJob;
import org.artifactory.storage.jobs.migration.buildinfo.BuildInfoMigrationJobDelegate;
import org.artifactory.ui.rest.service.builds.search.BuildsSearchFilter;
import org.artifactory.util.CollectionUtils;
import org.artifactory.util.date.DateUtils;
import org.artifactory.version.CompoundVersionDetails;
import org.iostreams.streams.in.StringInputStream;
import org.jfrog.build.api.Artifact;
import org.jfrog.build.api.Dependency;
import org.jfrog.build.api.*;
import org.jfrog.build.api.release.Promotion;
import org.jfrog.build.api.release.PromotionStatus;
import org.jfrog.common.JsonUtils;
import org.jfrog.common.config.diff.DataDiff;
import org.jfrog.storage.binstore.common.ChecksumInputStream;
import org.jfrog.storage.binstore.exceptions.BinaryRejectedException;
import org.jfrog.storage.binstore.utils.Checksum;
import org.jfrog.storage.common.ConflictGuard;
import org.jfrog.storage.common.ConflictsGuard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.getLast;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.startsWith;
import static org.apache.commons.lang3.math.NumberUtils.min;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.artifactory.aql.api.internal.AqlBase.and;
import static org.artifactory.build.BuildInfoUtils.formatBuildTime;
import static org.artifactory.build.BuildServiceUtils.*;
import static org.artifactory.storage.db.build.service.BuildStoreServiceImpl.formatDateToString;
import static org.artifactory.storage.jobs.migration.buildinfo.BuildInfoCalculationWorkItem.BUILD_INFO_CALCULATION_KEY_PREFIX;
import static org.jfrog.storage.util.DbUtils.foreignKeyExists;
import static org.jfrog.storage.util.DbUtils.tableExists;

/**
 * Build service main implementation
 *
 * @author Noam Y. Tenne
 */
@Service
@Reloadable(beanClass = InternalBuildService.class, initAfter = {InternalRepositoryService.class})
public class BuildServiceImpl implements InternalBuildService {
    private static final Logger log = LoggerFactory.getLogger(BuildServiceImpl.class);

    private static final String EXPORTABLE_BUILD_VERSION = "v2";
    private static final String BUILD_JSONS_TABLE = "build_jsons";

    //Signifies the state of the v6.6.0 build info migration
    private boolean buildInfoReady = false;
    private String buildInfoRepoKey;

    private Builds builds;
    private AddonsManager addonsManager;
    private AuthorizationService authService;
    private InternalRepositoryService repoService;
    private InternalBuildStoreService buildStoreService;
    private InternalDbService dbService;
    private AqlService aqlService;
    private CentralConfigService configService;
    private UploadService uploadService;
    private TaskService taskService;
    private JdbcHelper jdbcHelper;
    private XStream buildXStream;

    @Autowired
    public void setAddonsManager(AddonsManager addonsManager) {
        this.addonsManager = addonsManager;
    }

    @Autowired
    public void setDbService(InternalDbService dbService) {
        this.dbService = dbService;
    }

    @Autowired
    public void setAqlService(AqlService aqlService) {
        this.aqlService = aqlService;
    }

    @Autowired
    public void setUploadService(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @Autowired
    public void setAuthService(AuthorizationService authService) {
        this.authService = authService;
    }

    @Autowired(required = false)
    public void setBuilds(Builds builds) {
        this.builds = builds;
    }

    @Autowired
    public void setRepoService(InternalRepositoryService repoService) {
        this.repoService = repoService;
    }

    @Autowired
    public void setBuildStoreService(InternalBuildStoreService buildStoreService) {
        this.buildStoreService = buildStoreService;
    }

    @Autowired
    public void setConfigService(CentralConfigService configService) {
        this.configService = configService;
    }

    @Autowired
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Autowired
    public void setJdbcHelper(JdbcHelper jdbcHelper) {
        this.jdbcHelper = jdbcHelper;
    }

    @Override
    public void init() {
        buildXStream = XStreamFactory.create(ImportableExportableBuild.class);
        verifyBuildInfoState();
    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor, List<DataDiff<?>> configDiff) {
        buildXStream = XStreamFactory.create(ImportableExportableBuild.class);
        convertIfNeeded(configDiff);
        verifyBuildInfoState();
        buildInfoRepoKey = null;
    }

    @Override
    public void handleBuildUploadRedirect(BuildArtifactoryRequest request, ArtifactoryResponse response) throws IOException {
        try {
            modifyRequestForProperDeployment(request);
            uploadService.upload(request, response);
        } catch (IOException e) {
            log.error("", e);
            if (!response.isCommitted()) {
                response.sendError(SC_BAD_REQUEST, e.getMessage(), log);
            } else {
                throw new BadRequestException(e.getMessage());
            }
        } catch (RepoRejectException | BinaryRejectedException rej) {
            log.error("Deployment of build info failed: {}", rej.getMessage());
            log.debug("", rej);
            if (!response.isCommitted()) {
                response.sendError(rej.getErrorCode(), rej.getMessage(), log);
            } else {
                throw new ConflictException(rej.getMessage());
            }
        }
    }

    /**
     * This flow is mildly different from the {@link #addBuild} method since we need to preserve the headers that came in
     * with the {@param request} (and other meta-fields that the {@link UploadService} cares about).
     */
    private void modifyRequestForProperDeployment(BuildArtifactoryRequest request) {
        if (isReplicationRequest(request)) {
            String path = request.getRepoPath().getPath();
            // RepoFilter changes the request in case of replication we should handle the original request.
            String originalRequest = StringUtils.substringAfter(path, "build/buildUploadRedirect/");
            RepoPath repoPath = RepoPathFactory.create(originalRequest);
            request.setRepoPath(repoPath);
        } else {
            Build build = getBuildModelFrom(request);
            preAddBuildSteps(build);
            request.replaceContent(JsonUtils.getInstance().valueToByteArray(build));
            RepoPath correctUploadPath = getBuildJsonPathInRepo(build, getBuildInfoRepoKey());
            if (!correctUploadPath.equals(request.getRepoPath())) {
                if (log.isDebugEnabled()) {
                    log.debug("Wrong path given for deployment of build {}:{}, adjusting to path '{}'", build.getName(),
                            build.getNumber(), correctUploadPath.toPath());
                }
                request.setRepoPath(correctUploadPath);
            }
        }
    }

    private boolean isReplicationRequest(BuildArtifactoryRequest request) {
        Enumeration<String> headerValues = request.getHeaderValues("User-Agent");
        String userAgent = headerValues.hasMoreElements() ? headerValues.nextElement() : null;
        // In case the userAgent is Artifactory/ we can assume that the request is valid and not need to do modification
        return isNotBlank(userAgent) && startsWith(userAgent, "Artifactory/");
    }

    @Override
    public void addBuild(Build build) {
        if (log.isDebugEnabled()) {
            log.debug("Adding build '{}'", build);
        }
        //These pre-steps modify the actual build so they must be done here.
        preAddBuildSteps(build);
        InternalArtifactoryResponse artifactoryResponse = new InternalArtifactoryResponse();
        deployBuildJson(build, artifactoryResponse);
        // Checking response here in order to return status to REST API deployment :(
        if (artifactoryResponse.getStatus() == SC_FORBIDDEN) {
            throw new ForbiddenException(artifactoryResponse.getStatusMessage());
        }
        if (artifactoryResponse.getStatus() == SC_BAD_REQUEST) {
            throw new BadRequestException(artifactoryResponse.getStatusMessage());
        }
    }

    private void preAddBuildSteps(Build build) {
        build.setArtifactoryPrincipal(authService.currentUsername());
        buildStoreService.populateMissingChecksums(build);
        aggregatePreviousBuildIssues(build);
        notifyInterceptors(build, BeforeBuildSaveAction.class);
    }

    /**
     * Deploys the {@param build} as a json into the repo, thus triggering the {@link org.artifactory.repo.interceptor.BuildInfoInterceptor})
     * which will in turn trigger {@link InternalBuildService#addBuildInternal} that manages the entire add build flow.
     */
    private void deployBuildJson(Build build, ArtifactoryResponse artifactoryResponse) {
        String buildJson = getBuildAsJsonString(build);
        RepoPath targetPath = getBuildJsonPathInRepo(build, getBuildInfoRepoKey());
        log.debug("Creating build json for '{}:{}' at '{}'", build.getName(), build.getNumber(), targetPath);
        try (StringInputStream buildInStream = new StringInputStream(buildJson, UTF_8)) {
            ArtifactoryDeployRequest deployRequest = createDeployRequest(buildJson.getBytes().length, targetPath,
                    buildInStream);
            uploadService.upload(deployRequest, artifactoryResponse);
        } catch (BinaryRejectedException | RepoRejectException rej) {
            log.debug("", rej);
            throw new ConflictException(rej.getMessage());
        } catch (Exception e) {
            log.error("", e);
            throw new BadRequestException(e.getMessage());
        }
    }

    private ArtifactoryDeployRequest createDeployRequest(long contentLength, RepoPath targetPath, StringInputStream buildAsStream) {
        return new ArtifactoryDeployRequestBuilder(targetPath)
                .inputStream(buildAsStream)
                .contentLength(contentLength)
                .trustServerChecksums(true)
                .build();
    }

    @Override
    public void addBuildInternal(final Build build) {
        String buildName = build.getName();
        String buildNumber = build.getNumber();
        String buildStarted = build.getStarted();
        // must have it here to verify the user has permission to the final destination (if move will be needed)
        if (!authService.canUploadBuild(buildName, buildNumber, buildStarted)) {
            AccessLogger.buildCreateDenied(buildName + ':' + buildNumber);
            throw new ForbiddenException(ACCESS_DENIED_MSG);
        }

        //Shouldn't really happen but users might reach this state via the plugins so why not save them?
        if (exists(build)) {
            String buildString = format("Build %s:%s-%s", build, buildNumber, buildStarted);
            if (isBuildInfoChangedFromDb(build)) {
                throw new ConflictException(buildString + "already exists.");
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(format("Trying to add the same build twice: %s", buildString), new RuntimeException());
                }
                return;
            }
        }
        log.debug("Adding info for build '{}:{}' d{}", buildName, buildNumber, buildStarted);
        buildStoreService.addBuild(build, buildInfoReady);
        log.debug("Added info for build '{}:{}'", buildName, buildNumber);
        AccessLogger.buildCreate(build.getName() + ':' + build.getNumber());
        log.debug("Running License check on build '{}:{}'", buildName, buildNumber);
        addonsManager.addonByType(LicensesAddon.class).performOnBuildArtifacts(build);
        log.debug("Calling Xray build scan (if required) on build '{}:{}'", buildName, buildNumber);
        addonsManager.addonByType(XrayAddon.class).callAddBuildInterceptor(build);
        BuildRetention buildRetention = build.getBuildRetention();
        if (buildRetention != null) {
            log.debug("Executing build retention for build {}", buildName);
            addonsManager.addonByType(BuildAddon.class).discardOldBuilds(build.getName(), buildRetention, false);
        }
        notifyInterceptors(build, AfterBuildSaveAction.class);
    }


    private String calcBuildJsonSha1(Build build) {
        Checksum sha1 = new Checksum(ChecksumType.sha1.alg(), ChecksumType.sha1.length());
        String buildAsJsonString = getBuildAsJsonString(build);
        if (StringUtils.isEmpty(buildAsJsonString)) {
            return "";
        }
        try (InputStream in = IOUtils.toInputStream(buildAsJsonString);
             ChecksumInputStream calcStream = new ChecksumInputStream(in, sha1)) {
            log.debug("Calculating sha1 for build {} {} {}", build.getName(), build.getNumber(), build.getStarted());
            // waste the stream to have it calculate the checksum
            IOUtils.copy(calcStream, new NullOutputStream());
        } catch (Exception e) {
            String msg = "Can't calculate '" + sha1 + "' for build: " + build.getName() + " " + build.getNumber();
            log.error(msg, e);
            return "";
        }
        return sha1.getChecksum();
    }

    private boolean isBuildInfoChangedFromDb(Build build) {
        // In case the migration is done there is no need to do the validation
        if (buildInfoReady) {
            return true;
        }
        Build buildFromDb = buildStoreService.getBuild(new BuildRunImpl(build.getName(), build.getNumber(), build.getStarted()));
        // Build missing equals so we will use the 2 string
        return !calcBuildJsonSha1(build).equals(calcBuildJsonSha1(buildFromDb));
    }

    /**
     * Check if the latest build available has issues, add them all the our newly created build
     * only if the previous build status is not in "Released" status (configurable status from our plugins).
     * This way we collect all previous issues related to the same version which is not released yet.
     *
     * @param newBuild the newly created build to add previous issues to
     */
    private void aggregatePreviousBuildIssues(Build newBuild) {
        Issues newBuildIssues = newBuild.getIssues();
        if (newBuildIssues == null) {
            return;
        }

        if (!newBuildIssues.isAggregateBuildIssues()) {
            return;
        }

        Build latestBuild = getLatestBuildByNameAndStatus(newBuild.getName(), LATEST_BUILD);
        if (latestBuild == null) {
            return;
        }

        // Only aggregate if the previous build does not equal to the user requested status (e.g: "Released")
        // this way we only aggregate the issues related to the current release
        List<PromotionStatus> statuses = latestBuild.getStatuses();
        if (statuses != null) {
            String aggregationBuildStatus = newBuildIssues.getAggregationBuildStatus();
            for (PromotionStatus status : statuses) {
                if (status.getStatus().equalsIgnoreCase(aggregationBuildStatus)) {
                    return;
                }
            }
        }

        // It is important to create new Issue instance so we won't mess up previous ones
        Issues previousIssues = latestBuild.getIssues();
        if (previousIssues != null) {
            Set<Issue> affectedIssues = previousIssues.getAffectedIssues();
            if (affectedIssues != null) {
                for (Issue issue : affectedIssues) {
                    Issue issueToAdd = new Issue(issue.getKey(), issue.getUrl(), issue.getSummary());
                    issueToAdd.setAggregated(true);
                    newBuildIssues.addIssue(issueToAdd);
                }
            }
        }
    }

    @Override
    public BuildRun getBuildRun(String buildName, String buildNumber, String buildStarted) {
        assertBasicReadPermissions(buildName, buildNumber, buildStarted);
        return getBuildRunInternally(buildName, buildNumber, buildStarted);
    }

    @Override
    public BuildRun getBuildRunInternally(String buildName, String buildNumber, String buildStarted) {
        return buildStoreService.getBuildRun(buildName, buildNumber, buildStarted);
    }

    @Override
    public Build getBuild(BuildRun buildRun) {
        assertReadPermissions(buildRun.getName(), buildRun.getNumber(), buildRun.getStarted());
        return getBuildInternally(buildRun);
    }

    @Override
    public Build getBuildInternally(BuildRun buildRun) {
        if (isBuildModelInvalid(buildRun.getName(), buildRun.getNumber(), buildRun.getStarted())) {
            return null;
        }
        return getBuildAccordingToCoordinates(buildRun);
    }

    @Override
    public String getBuildAsJson(BuildRun buildRun) {
        if (isBuildModelInvalid(buildRun.getName(), buildRun.getNumber(), buildRun.getStarted())) {
            return null;
        }
        assertReadPermissions(buildRun.getName(), buildRun.getNumber(), buildRun.getStarted());
        return getBuildAsJsonString(getBuildAccordingToCoordinates(buildRun));
    }

    private Build getBuildAccordingToCoordinates(BuildRun build) {
        if (buildInfoReady) {
            return getBuildModelFromFile(
                    getBuildJsonPathInRepo(build.getName(), build.getNumber(), build.getStarted(),
                            getBuildInfoRepoKey()));
        } else {
            //Don't you love legacy considerations? because I sure do
            return buildStoreService.getBuild(build);
        }
    }

    /**
     * Reads the build json from the repository and converts the stream into {@link Build model}
     *
     * @return Build model
     * null in case of an error
     */
    @Override
    public Build getBuildModelFromFile(RepoPath buildJsonPath) {
        try (ResourceStreamHandle streamHandle = repoService.getResourceStreamHandle(buildJsonPath)) {
            if (streamHandle instanceof NullResourceStreamHandle) {
                throw new IllegalArgumentException("Build info file does not exist: " + buildJsonPath);
            }
            return getBuildAs(streamHandle::getInputStream, in -> JsonUtils.getInstance().readValue(in, Build.class));
        } catch (IOException e) {
            log.error("Unable to retrieve build info json from '{}': {}", buildJsonPath, e.getMessage());
            log.debug("", e);
            throw new IllegalStateException("Could not get JSON string for build " + buildJsonPath, e);
        }
    }

    private Build getBuildModelFrom(BuildArtifactoryRequest request) {
        try (InputStream requestStream = request.getInputStream()) {
            return getBuildAs(() -> requestStream, in -> JsonUtils.getInstance().readValue(in, Build.class));
        } catch (Exception e) {
            String message = request.getRepoPath() + " could not be deployed. " +
                    "Build Info repositories only supports build-info.json files.";
            log.error(message, request.getRepoPath(), e);
            throw new BadRequestException(message);
        }
    }

    private <T> T getBuildAs(Supplier<InputStream> inputSupplier, Function<InputStream, T> whatToDo)
            throws IOException {
        try (InputStream in = inputSupplier.get()) {
            return whatToDo.apply(in);
        }
    }

    @Override
    public void deleteAllBuildsByName(List<String> buildsNames, boolean deleteArtifacts, BasicStatusHolder status) {
        for (String buildName : buildsNames) {
            if (!authService.canDeleteBuild(buildName)) {
                throw new ForbiddenException(String.format(
                        "The user: '%s' is not authorized to delete build info. Delete permission is needed.",
                        authService.currentUsername()));
            }
            // double check for permission, also check whether anonymous
            // can read build info by testing configuration settings
            deleteAllBuildsByName(buildName, deleteArtifacts, status);
        }
    }

    @Override
    public void deleteAllBuildsByName(String buildName, boolean deleteArtifacts, BasicStatusHolder status) {
        searchBuildsByNameInternally(buildName)
                .forEach(build -> deleteBuild(build, deleteArtifacts, status));
    }

    @Override
    public void deleteBuildNumberByRetention(BuildRun buildRun, boolean deleteArtifacts, boolean async,
            BasicStatusHolder status) {
        BuildRetentionWorkItem buildRetentionWorkItem = new BuildRetentionWorkItem(buildRun, deleteArtifacts);
        if (async) {
            getTransactionalMe().deleteBuildAsync(buildRetentionWorkItem);
        } else {
            deleteBuild(buildRun, deleteArtifacts, status);
        }
    }

    @Override
    public void deleteBuild(BuildRun buildRun, boolean deleteArtifacts, BasicStatusHolder status) {
        if (buildRun == null) {
            status.error("Couldn't find requested build", log);
            return;
        }
        String name = buildRun.getName();
        String number = buildRun.getNumber();
        String started = buildRun.getStarted();
        //Its either you're admin (if we're converting, so we're supposed to keep this admin-only) or you have new-mechanism permissions
        assertDeletePermissions(name, number, started);
        if (buildInfoReady) {
            performDeleteBuild(buildRun, deleteArtifacts, status);
        } else {
            deleteBuildDuringMigration(buildRun, deleteArtifacts, status);
        }
        log.debug("Deleting build {} : {} : {}", name, number, started);
    }

    /**
     * 2 flows are possible here - one is legacy-supporting for conversion time, the other is the clean interceptor flow
     */
    private void performDeleteBuild(BuildRun buildRun, boolean deleteArtifacts, BasicStatusHolder status) {
        //deleting artifacts must come first because of foreign keys and the interceptor can't propagate the flag from the request
        if (deleteArtifacts) {
            removeBuildArtifacts(buildRun, status);
        }
        String name = buildRun.getName();
        String number = buildRun.getNumber();
        String started = buildRun.getStarted();
        RepoPath targetBuildJsonPath = getBuildJsonPathInRepo(name, number, started, getBuildInfoRepoKey());
        if (repoService.exists(targetBuildJsonPath)) {
            // Will trigger the build info interceptor, or will throw a runtime error in case of permissions failure
            status.merge(repoService.undeploy(targetBuildJsonPath, false, true));
        } else {
            if (buildInfoReady) {
                throw new NotFoundException(
                        "Build '" + name + ":" + number + "' cannot be found in path " + targetBuildJsonPath);
            } else {
                //Must support the old logic for conversion-quasi-time
                Build build = buildStoreService.getBuild(buildRun);
                deleteBuildInternal(build, status);
            }
        }
    }

    private void deleteBuildDuringMigration(BuildRun buildRun, boolean deleteArtifacts, BasicStatusHolder status) {
        long buildId = buildStoreService.findIdFromBuild(buildRun);
        // Locking the action in order to keep the Build Info migration job safe in case it is currently running
        ConflictGuard lock = getConflictsGuard().getLock(BUILD_INFO_CALCULATION_KEY_PREFIX + buildId);
        try {
            boolean lockAcquired = lock.tryToLock(120, TimeUnit.SECONDS);
            if (lockAcquired) {
                performDeleteBuild(buildRun, deleteArtifacts, status);
            } else {
                log.warn("Could not obtain lock for Build id '{}', cannot delete build", buildId);
            }
        } catch (InterruptedException e) {
            log.error("Failed to acquire lock for Build id: '{}'. {}'", buildId, e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void deleteBuildInternal(Build build, MutableStatusHolder status) {
        String buildName = build.getName();
        String buildNumber = build.getNumber();
        status.debug("Starting to remove build '" + buildName + "' #" + buildNumber, log);
        buildStoreService.deleteBuild(buildName, buildNumber, build.getStarted(), buildInfoReady);
        AccessLogger.buildDelete(buildName);
        status.debug("Finished removing build '" + buildName + "' #" + buildNumber, log);
        triggerXrayBuildDelete(buildName, buildNumber);
    }

    @Override
    public void deleteBuildAsync(BuildRetentionWorkItem buildRetentionWorkItem) {
        log.info("Async delete of build " + buildRetentionWorkItem.getBuildId().getName() + " number: "
                + buildRetentionWorkItem.getBuildId().getNumber());
        BuildRun buildRun = buildRetentionWorkItem.getBuildId();
        boolean deleteArtifacts = buildRetentionWorkItem.isDeleteArtifacts();
        BasicStatusHolder status = new BasicStatusHolder();
        getTransactionalMe().deleteBuild(buildRun, deleteArtifacts, status);
    }

    @Override
    public void removeBuildArtifacts(BuildRun buildRun, BasicStatusHolder status) {
        //Would be a shame to go through all of this just to find out you're missing build delete permissions later
        assertDeletePermissions(buildRun.getName(), buildRun.getNumber(), buildRun.getStarted());
        Build build = getBuildInternally(buildRun);
        String buildName = build.getName();
        String buildNumber = build.getNumber();
        status.debug("Starting to remove the artifacts of build '" + buildName + "' #" + buildNumber, log);
        Set<ArtifactoryBuildArtifact> buildArtifactsInfos = getBuildArtifactsFileInfos(build, false, null, null);
        for (ArtifactoryBuildArtifact artifact : buildArtifactsInfos) {
            if (artifact.getFileInfo() != null) {
                RepoPath repoPath = artifact.getFileInfo().getRepoPath();
                BasicStatusHolder undeployStatus = repoService.undeploy(repoPath, true, true);
                status.merge(undeployStatus);
            }
        }
        status.debug("Finished removing the artifacts of build '" + buildName + "' #" + buildNumber, log);
    }

    @Override
    public Build getLatestBuildByNameAndStatus(String buildName, final String buildStatus) {
        if (StringUtils.isBlank(buildName)) {
            return null;
        }
        if (StringUtils.isBlank(buildStatus)) {
            return null;
        }
        //let's find all builds
        Set<BuildRun> buildsByName = searchBuildsByNameInternally(buildName);
        if (buildsByName == null || buildsByName.isEmpty()) { //no builds - no glory
            return null;
        }
        List<BuildRun> buildRuns = newArrayList(buildsByName);
        buildRuns.sort(BuildRunComparators.getComparatorFor(buildRuns));
        BuildRun latestBuildRun;

        if (buildStatus.equals(LATEST_BUILD)) {
            latestBuildRun = getLast(buildRuns, null);
        } else {
            latestBuildRun = getLast(filter(buildRuns, new Predicate<BuildRun>() {
                @Override
                public boolean apply(BuildRun buildRun) {
                    // Search for the latest build by the given status
                    return buildStatus.equals(buildRun.getReleaseStatus());
                }
            }), null);

        }
        return latestBuildRun == null ? null :
                getBuildInternally(latestBuildRun);
    }

    @Override
    @Nullable
    public Build getLatestBuildByNameAndNumber(String buildName, String buildNumber) {
        Build build = getLatestBuildByNameAndNumberInternally(buildName, buildNumber);
        if (build == null) {
            return null;
        }
        assertReadPermissions(buildName, buildNumber, build.getStarted());
        return build;
    }

    @Override
    @Nullable
    public Build getLatestBuildByNameAndNumberInternally(String buildName, String buildNumber) {
        if (StringUtils.isBlank(buildName) || StringUtils.isBlank(buildNumber)) {
            return null;
        }
        BuildRun latestBuildRun = buildStoreService.getLatestBuildRun(buildName, buildNumber);
        if (latestBuildRun != null) {
            return getBuildInternally(latestBuildRun);
        }
        return null;
    }

    @Override
    public SortedSet<BuildRun> searchBuildsByName(String buildName) {
        return searchBuildsByNameInternally(buildName).stream()
                .filter(build -> authService.isBuildBasicRead(build.getName(), build.getNumber(), build.getStarted()))
                .collect(sortedSetReverseStartDateCollector());
    }

    @Override
    public Set<BuildRun> searchBuildsByNameInternally(String buildName) {
        return buildStoreService.findBuildsByName(buildName);
    }

    @Override
    public List<String> getBuildNames() {
        return getBuildNamesInternally().stream()
                .filter(authService::isBuildBasicRead)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getBuildNamesInternally() {
        return buildStoreService.getAllBuildNames().stream().
                distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public Set<BuildRun> searchBuildsByNameAndNumber(String buildName, String buildNumber) {
        return buildStoreService.findBuildsByNameAndNumber(buildName, buildNumber).stream()
                .filter(build -> authService.isBuildBasicRead(build.getName(), build.getNumber(), build.getStarted()))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isBuildInfoReady() {
        return buildInfoReady;
    }

    @Override
    public Set<BuildRun> searchBuildsByNameAndNumberInternal(String buildName, String buildNumber) {
        return buildStoreService.findBuildsByNameAndNumber(buildName, buildNumber);
    }

    @Override
    public String getBuildInfoRepoKey() {
        return buildInfoRepoKey == null ? extractBuildInfoRepoKeyFromRepos() : buildInfoRepoKey;
    }

    @Override
    public Set<ArtifactoryBuildArtifact> getBuildArtifactsFileInfos(Build build, boolean useFallBack,
            List<String> sourceRepos, List<String> excludedRepos) {
        AqlBase.AndClause and = and();
        log.debug("Executing Artifacts search for build {}:{}", build.getName(), build.getNumber());
        addIncludeExcludeRepos(sourceRepos, excludedRepos, and, true);
        and.append(AqlApiItem.property().property("build.name", AqlComparatorEnum.equals, build.getName()));
        and.append(AqlApiItem.property().property("build.number", AqlComparatorEnum.equals, build.getNumber()));
        AqlBase buildArtifactsQuery = AqlApiItem.create().filter(and);

        AqlEagerResult<AqlBaseFullRowImpl> aqlResult = aqlService.executeQueryEager(buildArtifactsQuery);
        log.debug("Search returned {} artifacts", aqlResult.getSize());
        Multimap<String, Artifact> buildArtifacts = BuildServiceUtils.getBuildArtifacts(build);

        log.debug("This build contains {} artifacts (taken from build info)", buildArtifacts.size());
        List<String> virtualRepoKeys = getVirtualRepoKeys();
        Set<ArtifactoryBuildArtifact> matchedArtifacts = matchArtifactsToFileInfos(aqlResult.getResults(),
                buildArtifacts, virtualRepoKeys);
        log.debug("Matched {} build artifacts to actual paths returned by search", matchedArtifacts.size());

        //buildArtifacts contains all remaining artifacts that weren't matched - match them with the weak search
        //only if indicated and if such remaining unmatched artifacts still exist in the map.
        if (useFallBack && !buildArtifacts.isEmpty()) {
            log.debug("Unmatched artifacts exist and 'use weak match fallback' flag is lit - executing weak match");
            Set<ArtifactoryBuildArtifact> weaklyMatchedArtifacts = matchUnmatchedArtifactsNonStrict(build, sourceRepos,
                    excludedRepos, buildArtifacts, virtualRepoKeys, aqlService);
            log.debug("Weak match has matched {} additional artifacts", weaklyMatchedArtifacts);
            matchedArtifacts.addAll(weaklyMatchedArtifacts);
        }
        //Lastly, populate matchedArtifacts with all remaining unmatched artifacts with null values to help users of
        //this function know if all build artifacts were found.
        log.debug("{} artifacts were not matched to actual paths", buildArtifacts.size());
        matchedArtifacts.addAll(buildArtifacts.values().stream()
                .map(artifact -> new ArtifactoryBuildArtifact(artifact, null))
                .collect(Collectors.toList()));
        return matchedArtifacts;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<Dependency, FileInfo> getBuildDependenciesFileInfos(Build build) {
        AqlBase.AndClause<AqlApiBuild> and = AqlApiBuild.and(
                AqlApiBuild.name().equal(build.getName()),
                AqlApiBuild.number().equal(build.getNumber())
        );
        log.debug("Executing dependencies search for build {}:{}", build.getName(), build.getNumber());

        AqlBase buildDependenciesQuery = AqlApiBuild.create().filter(and);
        buildDependenciesQuery.include(
                AqlApiBuild.module().dependecy().name(),
                AqlApiBuild.module().dependecy().item().sha1Actual(),
                AqlApiBuild.module().dependecy().item().md5Actual(),
                AqlApiBuild.module().dependecy().item().sha1Original(),
                AqlApiBuild.module().dependecy().item().md5Orginal(),
                AqlApiBuild.module().dependecy().item().created(),
                AqlApiBuild.module().dependecy().item().modifiedBy(),
                AqlApiBuild.module().dependecy().item().createdBy(),
                AqlApiBuild.module().dependecy().item().updated(),
                AqlApiBuild.module().dependecy().item().repo(),
                AqlApiBuild.module().dependecy().item().path(),
                AqlApiBuild.module().dependecy().item().name(),
                AqlApiBuild.module().dependecy().item().size()
                //Ordering by the last updated field, in case of duplicates with the same checksum.
        ).addSortElement(AqlApiBuild.module().dependecy().item().updated()).asc();
        Map<Dependency, FileInfo> matchedDependencies;
        Multimap<String, Dependency> buildDependencies;
        try (AqlLazyResult<AqlBuild> aqlLazyResult = aqlService.executeQueryLazy(buildDependenciesQuery)) {
            buildDependencies = BuildServiceUtils.getBuildDependencies(build);
            log.debug("This build contains {} dependencies (taken from build info)", buildDependencies.size());
            matchedDependencies = matchDependenciesToFileInfos(aqlLazyResult, buildDependencies, getVirtualRepoKeys());
        } catch (Exception e) {
            log.error("Failed to close dependencies lazy query, {}", e.getMessage());
            log.debug("Failed to close dependencies lazy query", e);
            throw new BuildDependenciesCloseException("Failed to close dependencies lazy query", e);
        }

        log.debug("Matched {} build dependencies to actual paths returned by search", matchedDependencies.size());
        //Lastly, populate matchedDependencies with all remaining unmatched dependencies with null values to help users
        //of this function know if all build artifacts were found.
        log.debug("{} dependencies were not matched to actual paths", buildDependencies.size());
        for (Dependency dependency : buildDependencies.values()) {
            if (!matchedDependencies.containsKey(dependency)) {
                matchedDependencies.put(dependency, null);
            }
        }
        return matchedDependencies;
    }

    @Override
    public void exportTo(ExportSettings settings) {
        MutableStatusHolder multiStatusHolder = settings.getStatusHolder();
        multiStatusHolder.debug("Starting build info export", log);

        File buildsFolder = new File(settings.getBaseDir(), BUILDS_EXPORT_DIR);
        prepareBuildsFolder(settings, multiStatusHolder, buildsFolder);
        if (multiStatusHolder.isError()) {
            return;
        }

        try {
            long exportedBuildCount = 1;
            List<String> buildNames = buildStoreService.getAllBuildNames();
            for (String buildName : buildNames) {
                Set<BuildRun> buildsByName = buildStoreService.findBuildsByName(buildName);
                for (BuildRun buildRun : buildsByName) {
                    String buildNumber = buildRun.getNumber();
                    try {
                        exportBuild(settings, buildRun, exportedBuildCount, buildsFolder);
                        exportedBuildCount++;
                    } catch (Exception e) {
                        String errorMessage = format("Failed to export build info: %s:%s", buildName, buildNumber);
                        if (settings.isFailFast()) {
                            throw new Exception(errorMessage, e);
                        }
                        multiStatusHolder.error(errorMessage, e, log);
                    }
                }
            }
        } catch (Exception e) {
            multiStatusHolder.error("Error occurred during build info export.", e, log);
        }

        if (settings.isIncremental() && !multiStatusHolder.isError()) {
            try {
                log.debug("Cleaning previous builds backup folder.");

                File[] backupDirsToRemove = settings.getBaseDir().listFiles(
                        (dir, name) -> name.startsWith(BACKUP_BUILDS_FOLDER));
                if (backupDirsToRemove != null) {
                    for (File backupDirToRemove : backupDirsToRemove) {
                        log.debug("Cleaning previous build backup folder: {}", backupDirToRemove.getAbsolutePath());
                        FileUtils.forceDelete(backupDirToRemove);
                    }
                }
            } catch (IOException e) {
                multiStatusHolder.error("Failed to clean previous builds backup folder.", e, log);
            }
        }

        multiStatusHolder.debug("Finished build info export", log);
    }

    /**
     * Do not use this. Use {@link #getBuildInfoRepoKey()}
     */
    private String extractBuildInfoRepoKeyFromRepos() {
        buildInfoRepoKey = configService.getDescriptor().getLocalRepositoriesMap().entrySet()
                .stream()
                .filter(repo -> RepoType.BuildInfo.equals(repo.getValue().getType()))
                .map(Map.Entry::getKey).findFirst()
                .orElseThrow(() -> new IllegalStateException("Build Info repository not found."));
        return buildInfoRepoKey;
    }

    /**
     * Removes the entire Build Info repo files, while avoiding the storage interceptors
     */
    private void deleteBuildRepo() {
        RepoPath targetBuild = RepoPathFactory.create(getBuildInfoRepoKey());
        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        try {
            SecurityContextHolder.getContext().setAuthentication(new SystemAuthenticationToken());
            BasicStatusHolder statusHolder = repoService
                    .undeploy(targetBuild, true, new DeleteContext(targetBuild).avoidBuildDeleteInterceptor());
            if (statusHolder.isError()) {
                throw new StorageException(
                        "Could not delete build json from " + targetBuild + ": "
                                + statusHolder.getLastError().getMessage(), statusHolder.getLastError().getException());
            }
        } finally {
            // Restore previous permissions
            SecurityContextHolder.getContext().setAuthentication(currentAuth);
        }
    }

    @Override
    public void importFrom(ImportSettings settings) {
        final MutableStatusHolder status = settings.getStatusHolder();
        status.status("Starting build info import", log);

        log.info("Resetting the Build Info repo");
        buildInfoRepoKey = null;

        dbService.invokeInTransaction("BuildImport-deleteAllBuilds", () -> {
            try {
                // First we should undeploy all builds in the builds repo
                deleteBuildRepo();
                // This might can be removed, we can keep it
                buildStoreService.deleteAllBuilds(buildInfoReady);
            } catch (Exception e) {
                status.error("Failed to delete builds root node", e, log);
            }
            return null;
        });

        File buildsFolder = new File(settings.getBaseDir(), BUILDS_EXPORT_DIR);
        String buildsFolderPath = buildsFolder.getPath();
        if (!buildsFolder.exists()) {
            status.debug("'" + buildsFolderPath + "' folder is either non-existent or not a " +
                    "directory. old-style build info import was not performed", log);
            return;
        }

        IOFileFilter buildExportFileFilter = new AbstractFileFilter() {
            @Override
            public boolean accept(File file) {
                String fileName = file.getName();
                return fileName.startsWith("build") && fileName.endsWith(".xml");
            }
        };

        Collection<File> buildExportFiles =
                FileUtils.listFiles(buildsFolder, buildExportFileFilter, DirectoryFileFilter.DIRECTORY);

        if (buildExportFiles.isEmpty()) {
            status.status("'" + buildsFolderPath + "' folder does not contain build export files. " +
                    "Build info import was not performed", log);
            return;
        }

        importBuildFiles(settings, buildExportFiles);
        status.warn("Old-style import builds witnessed at location '"
                + buildsFolder.getParent() + File.separator + buildsFolder.getName()
                + "'.  If a build-info repository content export exists as well in this import it will be disregarded " +
                "for backwards compatibility.", log);
        status.warn("The correct way to import and export builds is to enable content export and let " +
                "Artifactory handle the build info repo import/export", log);
        settings.setExcludeBuildInfoRepo(true);
        status.status("Finished build info import", log);
    }

    private void importBuildFiles(ImportSettings settings, Collection<File> buildExportFiles) {
        for (File buildExportFile : buildExportFiles) {
            try (FileInputStream inputStream = new FileInputStream(buildExportFile)) {
                ImportableExportableBuild importableBuild = (ImportableExportableBuild) buildXStream
                        .fromXML(inputStream);
                // import each build in a separate transaction
                getTransactionalMe().importBuild(settings, importableBuild);
            } catch (Exception e) {
                settings.getStatusHolder().error("Error occurred during build info import", e, log);
                if (settings.isFailFast()) {
                    break;
                }
            }
        }
    }

    @Override
    public void importBuild(ImportSettings settings, ImportableExportableBuild importableBuild) {
        String buildName = importableBuild.getBuildName();
        MutableStatusHolder multiStatusHolder = settings.getStatusHolder();
        String buildNumber = importableBuild.getBuildNumber();
        String buildStarted = importableBuild.getBuildStarted();
        try {
            multiStatusHolder
                    .debug(format("Beginning import of build: %s:%s:%s", buildName, buildNumber, buildStarted), log);
            Build build = JsonUtils.getInstance().readValue(importableBuild.getJson(), Build.class);
            addBuild(build);
        } catch (Exception e) {
            String msg = "Could not import build " + buildName + ":" + buildNumber + ":" + buildStarted;
            // Print stack trace in debug
            log.debug(msg, e);
            multiStatusHolder.error(msg, e, log);
        }
        multiStatusHolder
                .debug(format("Finished import of build: %s:%s:%s", buildName, buildNumber, buildStarted), log);
    }

    @Override
    public PromotionResult promoteBuild(BuildRun buildRun, Promotion promotion) {
        // Assertion is done here for efficiency
        assertUploadPermissions(buildRun.getName(), buildRun.getNumber(), buildRun.getStarted());
        BuildPromotionHelper buildPromotionHelper = new BuildPromotionHelper();
        return buildPromotionHelper.promoteBuild(buildRun, promotion);
    }

    @Override
    public void renameBuilds(String from, String to) {
        Set<BuildRun> buildsToRename = searchBuildsByNameInternally(from);
        if (buildsToRename.isEmpty()) {
            log.error("Could not find builds by the name '{}'. No builds were renamed.", from);
            return;
        }
        //Although the caller ran a permission check (to properly report back before async) we do it again since
        // between async dispatch and execution stuff might change
        Set<BuildRun> allowedBuildsToRename = buildsToRename.stream()
                .filter(build -> authService.canReadBuild(build.getName(), build.getNumber(), build.getStarted()))
                .filter(build -> authService.canDeleteBuild(build.getName(), build.getNumber(), build.getStarted()))
                .filter(build -> authService.canUploadBuild(build.getName(), build.getNumber(), build.getStarted()))
                .collect(Collectors.toSet());
        if (allowedBuildsToRename.size() != buildsToRename.size() && log.isErrorEnabled()) {
            log.error("The user: '{}' lacks the required permissions to rename build '{}'", authService.currentUsername(), from);
        }
        for (BuildRun buildToRename : buildsToRename) {
            try {
                getTransactionalMe().renameBuild(buildToRename, to);
                log.info("Renamed build number '{}' that started at '{}' from '{}' to '{}'.",
                        buildToRename.getNumber(), buildToRename.getStarted(), buildToRename.getName(), to);
            } catch (Exception e) {
                log.error("Failed to rename build: '{}' #{} that started at {}: {}", buildToRename.getName(),
                        buildToRename.getNumber(), buildToRename.getStarted(), e.getMessage());
                log.error("", e);
            }
        }
    }

    @Override
    public void renameBuild(BuildRun buildRun, String to) {
        Build buildJson = getBuildInternally(buildRun);
        if (buildJson == null) {
            throw new NotFoundException("Cannot rename non existent build " + buildRun);
        }
        //shouldRename also modifies the buildJson object's build name attribute
        if (shouldRename(buildRun, to, buildJson)) {
            // delete according to the build-info state, renaming of build will do migration
            deleteBuild(buildRun, false, new BasicStatusHolder());
            addBuild(buildJson);
        }
    }

    /**
     * This piece of legacy code supposedly deals with a case where the db row representing the build (in builds table)
     * and the json representing the build (either in the repo or in the build_jsosns table) have a mismatch in their name.
     * I dunno how or why this would happen but I guess its here for a reason (lost to oblivion) so i'm keeping it.
     */
    private boolean shouldRename(BuildRun buildRun, String to, Build buildJson) {
        boolean changed = false;
        if (!StringUtils.equals(buildJson.getName(), to)) {
            buildJson.setName(to);
            changed = true;
        }
        if (!StringUtils.equals(buildRun.getName(), to)) {
            changed = true;
        }
        if (!changed) {
            log.info("Build {} already named {} nothing to do!", buildRun, to);
        }
        return changed;
    }

    @Override
    public void updateBuild(@Nonnull Build build) {
        String name = build.getName();
        String number = build.getNumber();
        String started = build.getStarted();

        // Checking permissions here, as cannot check inside async later
        assertDeletePermissions(name, number, started);
        assertUploadPermissions(name, number, started);
        //Reaching update implies the same build (name, number and started) already exists
        log.info("Updating build {} Number {} that started at {}", name, number, started);
        getTransactionalMe().addBuild(build);
        log.info("Update of build {} Number {} that started at {} completed successfully", name, number, started);
    }

    @Override
    public void addPromotionStatus(Build build, PromotionStatus promotion) {
        String name = build.getName();
        String number = build.getNumber();
        String started = build.getStarted();
        build.addStatus(promotion);
        // Assert MUST BE KEPT HERE, as we perform delete before upload.
        // If we don't assert and in case there is no upload permission we will be left with no build at all.
        assertUploadPermissions(name, number, started);
        // In the state before build migration is finished: update the DB with the updated build json (as well as
        // writing the build.json to the build-info repository
        if (!buildInfoReady) {
            buildStoreService.addPromotionStatus(build, promotion, authService.currentUsername(), buildInfoReady);
        }
        log.debug("Overwriting the .json content of '{}:{}' with new build model including promotion", name, number);
        deletePreviousBuildAsSystem(build);
        addBuild(build);
    }

    /**
     * Performs delete for the build json by the system user (in order to allow promotion even for users without
     * delete permission)
     *
     * Delete is performed in a separate transaction:
     * We need to manually perform the delete BEFORE the deploy,
     * otherwise it can cause deploy-->delete instead of delete-->deploy.
     * This is because {@link SqlStorageSession} currently doesn't hold the order of events.
     *
     * @param build Build to be deleted
     */
    private void deletePreviousBuildAsSystem(Build build) {
        BasicStatusHolder status = new BasicStatusHolder();
        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        try {
            SecurityContextHolder.getContext().setAuthentication(new SystemAuthenticationToken());
            BuildRun buildRun = getBuildRunInternally(build.getName(), build.getNumber(), build.getStarted());
            if (!buildInfoReady) {
                log.debug("Invoking Build info deletion.");
                deleteBuild(buildRun, false, status);
            } else {
                log.debug("Invoking Build info deletion in new transaction in case.");
                dbService.invokeInTransaction(
                        "deleteBuildInPromotionBySystem" + build.getName() + build.getNumber() + build.getStarted(),
                        () -> {
                            getTransactionalMe().deleteBuild(buildRun, false, status);
                            return null;
                        });
            }
        } finally {
            // Restore previous permissions
            SecurityContextHolder.getContext().setAuthentication(currentAuth);
        }
        if (status.hasErrors() || status.hasWarnings()) {
            log.warn("Couldn't delete build as part of promotion: {}.", status.getLastError().getMessage());
        }
    }

    @Nullable
    @Override
    public List<PublishedModule> getPublishedModules(String buildNumber, String buildStarted, String buildName) {
        assertBasicReadPermissions(buildName, buildNumber, formatBuildTime(buildStarted));
        return buildStoreService.getPublishedModules(buildNumber, buildStarted);
    }

    @Nullable
    @Override
    public List<ModuleArtifact> getModuleArtifact(String buildName, String buildNumber, String moduleId,
            String buildStarted) {
        assertBasicReadPermissions(buildName, buildNumber, formatBuildTime(buildStarted));
        return buildStoreService.getModuleArtifact(buildName, buildNumber, moduleId, buildStarted);
    }

    @Nullable
    @Override
    public List<ModuleDependency> getModuleDependency(String buildNumber, String moduleId, String buildStarted,
            String buildName) {
        assertBasicReadPermissions(buildName, buildNumber, formatBuildTime(buildStarted));
        return buildStoreService.getModuleDependency(buildNumber, moduleId, buildStarted);
    }

    private void exportBuild(ExportSettings settings, BuildRun buildRun,
            long exportedBuildCount, File buildsFolder) throws Exception {
        MutableStatusHolder multiStatusHolder = settings.getStatusHolder();

        String buildName = buildRun.getName();
        String buildNumber = buildRun.getNumber();
        String buildStarted = buildRun.getStarted();
        multiStatusHolder.debug(
                format("Beginning export of build: %s:%s:%s", buildName, buildNumber, buildStarted), log);

        ImportableExportableBuild exportedBuild = getExportableBuild(buildRun);

        File buildFile = new File(buildsFolder, "build" + exportedBuildCount + ".xml");
        exportBuildToFile(exportedBuild, buildFile);

        multiStatusHolder.debug(
                format("Finished export of build: %s:%s:%s", buildName, buildNumber, buildStarted), log);
    }

    @Override
    public ImportableExportableBuild getExportableBuild(BuildRun buildRun) {
        assertReadPermissions(buildRun.getName(), buildRun.getNumber(), buildRun.getStarted());
        BuildEntity buildEntity = buildStoreService.getBuildEntity(buildRun);
        ImportableExportableBuild exportedBuild = new ImportableExportableBuild();
        exportedBuild.setVersion(EXPORTABLE_BUILD_VERSION);
        exportedBuild.setBuildName(buildEntity.getBuildName());
        exportedBuild.setBuildNumber(buildEntity.getBuildNumber());
        exportedBuild.setBuildStarted(formatDateToString(buildEntity.getBuildDate()));

        String jsonString = getBuildAsJson(buildRun);
        exportedBuild.setJson(jsonString);

        exportedBuild.setCreated(buildEntity.getCreated());
        exportedBuild.setLastModified(buildEntity.getModified());
        exportedBuild.setCreatedBy(buildEntity.getCreatedBy());
        exportedBuild.setLastModifiedBy(buildEntity.getModifiedBy());
        return exportedBuild;
    }

    private void exportBuildToFile(ImportableExportableBuild exportedBuild, File buildFile) throws Exception {
        try (FileOutputStream buildFileOutputStream = new FileOutputStream(buildFile)) {
            buildXStream.toXML(exportedBuild, buildFileOutputStream);
        }
    }

    @Override
    public List<ModuleArtifact> getModuleArtifactsForDiffWithPaging(BuildParams buildParams) {
        assertBasicReadPermissions(buildParams.getBuildName(), buildParams.getCurrBuildNum(),
                formatBuildTime(buildParams.getCurrBuildDate()));
        return buildStoreService.getModuleArtifactsForDiffWithPaging(buildParams);
    }

    @Override
    public List<ModuleDependency> getModuleDependencyForDiffWithPaging(BuildParams buildParams) {
        assertBasicReadPermissions(buildParams.getBuildName(), buildParams.getCurrBuildNum(),
                formatBuildTime(buildParams.getCurrBuildDate()));
        return buildStoreService.getModuleDependencyForDiffWithPaging(buildParams);
    }

    @Override
    public List<GeneralBuild> getPrevBuildsList(String buildName, String buildDate) {
        return buildStoreService.getPrevBuildsList(buildName, buildDate).stream()
                .filter(generalBuild -> authService
                        .isBuildBasicRead(generalBuild.getBuildName(), generalBuild.getBuildNumber(),
                                formatBuildTime(generalBuild.getBuildDate())))
                .collect(Collectors.toList());
    }

    @Override
    public List<BuildProps> getBuildPropsData(BuildParams buildParams) {
        assertReadPermissions(buildParams.getBuildName(), buildParams.getCurrBuildNum(),
                formatBuildTime(buildParams.getCurrBuildDate()));
        return buildStoreService.getBuildPropsData(buildParams);
    }

    @Override
    public List<BuildId> getLatestBuildIDsPaging(ContinueBuildFilter buildFilter) {
        return BuildServiceUtils.iterateFetchFromDb(buildStoreService::getLatestBuildIDsPaging,
                build -> authService.isBuildBasicRead(build.getName(), build.getNumber(), build.getStarted()),
                buildId -> buildId, buildFilter);
    }

    @Override
    public List<BuildId> getBuildIDsByName(String buildName, String fromDate, String toDate, long limit, String orderBy, String direction) {
        List<BuildId> builds = buildStoreService.getLatestBuildIDsByName(buildName, fromDate, toDate, orderBy, direction);
        long resultsLimit = min(limit, ConstantValues.searchUserQueryLimit.getInt());

        if (authService.hasBasicReadPermissionForAllBuilds()) {
            return builds.stream()
                    .limit(resultsLimit)
                    .collect(Collectors.toList());
        }

        return builds.stream()
                .filter(build -> authService.canReadBuild(build.getName(), build.getNumber(), build.getStarted()))
                .limit(resultsLimit)
                .collect(Collectors.toList());
    }

    @Override
    public List<GeneralBuild> getBuildVersions(BuildsSearchFilter filter) {

        List<GeneralBuild> builds = Lists.newArrayList();
        if(authService.isAdmin()){
            filter.setLimit(filter.getLimit());
            builds = buildStoreService.getBuildVersions(filter);
        }
        else {
            while (builds.size() < filter.getLimit()){
                List<GeneralBuild> buildVersionsBatch = buildStoreService.getBuildVersions(filter);
                List<GeneralBuild> filteredBuildVersionsByPermissions = filterVersionByPermissions(filter,buildVersionsBatch);
                builds.addAll(filteredBuildVersionsByPermissions);
                if(buildVersionsBatch.size() < filter.getDaoLimit()){
                    break;
                }
            }
        }
        return builds.stream().limit(filter.getLimit()).collect(Collectors.toList());
    }

    private List<GeneralBuild> filterVersionByPermissions(BuildsSearchFilter filter,
            List<GeneralBuild> buildVersionsBatch) {
        return buildVersionsBatch.stream()
                .limit(filter.getLimit())
                .filter(build -> authService.canReadBuild(build.getBuildName(), build.getBuildNumber()))
                .collect(Collectors.toList());
    }

    @Override
    public List<GeneralBuild> getBuildForName(String buildName, ContinueBuildFilter buildFilter) {
        return iterateFetchFromDb(continuePageBuildFilter ->
                        buildStoreService.getBuildForName(buildName, continuePageBuildFilter),
                build -> authService
                        .isBuildBasicRead(build.getBuildName(), build.getBuildNumber(), formatBuildTime(build.getBuildDate())),
                generalBuild -> new BuildIdImpl(generalBuild.getBuildId(), generalBuild.getBuildName(),
                        generalBuild.getBuildNumber(), generalBuild.getBuildDate()),
                buildFilter);
    }

    @Override
    public List<FileInfo> collectBuildArtifacts(Build build, @Nullable List<String> sourceRepos,
            @Nullable List<String> excludedRepos, @Nullable BasicStatusHolder status) {
        status = status == null ? new BasicStatusHolder() : status;
        if (build == null) {
            status.error("Invalid build given.", SC_BAD_REQUEST, log);
            return Lists.newArrayList();
        }
        log.info("Collecting Build artifacts for build {}:{}", build.getName(), build.getNumber());
        Set<ArtifactoryBuildArtifact> infos = getBuildArtifactsFileInfos(build, false, sourceRepos, excludedRepos);
        BuildServiceUtils.verifyAllArtifactInfosExistInSet(build, true, status, infos,
                BuildServiceUtils.VerifierLogLevel.warn);
        return Lists.newArrayList(BuildServiceUtils.toFileInfoList(infos));
    }

    @Override
    public void assertBasicReadPermissions(String buildName, String buildNumber, String buildStarted) {
        if (!authService.isBuildBasicRead(buildName, buildNumber, buildStarted)) {
            throw new ForbiddenException(String.format(
                    "The user: '%s' is not authorized to access build info. 'Global Basic Read' Flag turned on or read permission is needed.",
                    authService.currentUsername()));
        }
    }

    @Override
    public void assertReadPermissions(String buildName, String buildNumber, String buildStarted) {
        if (!authService.canReadBuild(buildName, buildNumber, buildStarted)) {
            throw new ForbiddenException(
                    String.format("The user: '%s' is not authorized to access build info. Read permission is needed.",
                            authService.currentUsername()));
        }
    }

    @Override
    public void assertDeletePermissions(String buildName, String buildNumber, String buildStarted) {
        if (!authService.canDeleteBuild(buildName, buildNumber, buildStarted)) {
            throw new ForbiddenException(
                    String.format("The user: '%s' is not authorized to delete build info. Delete permission is needed.",
                            authService.currentUsername()));
        }
    }

    @Override
    public void assertUploadPermissions(String buildName, String buildNumber, String buildStarted) {
        if (!authService.canUploadBuild(buildName, buildNumber, buildStarted)) {
            throw new ForbiddenException(
                    String.format("The user: '%s' is not authorized to upload build info. Upload permission is needed.",
                            authService.currentUsername()));
        }
    }

    @Override
    public BuildGeneralInfo getBuildGeneralInfo(String buildName, String buildNumber, String buildStarted) {
        Build build = getBuildNonStrict(buildName, buildNumber, buildStarted);
        if (build == null) {
            log.warn("Build {}:{}-{} not found.", buildName, buildNumber, buildStarted);
            return null;
        }
        String buildAgent = (build.getBuildAgent() == null) ? null : build.getBuildAgent().toString();
        String agent = (build.getAgent() == null) ? null : build.getAgent().toString();
        String foundBuildName = build.getName();
        String foundBuildNumber = build.getNumber();
        String foundBuildStarted = build.getStarted();
        return BuildGeneralInfo.builder()
                .buildName(foundBuildName)
                .lastBuildTime(foundBuildStarted)
                .agent(agent)
                .buildAgent(buildAgent)
                .artifactoryPrincipal(build.getArtifactoryPrincipal())
                .principal(build.getPrincipal())
                .duration(DateUtils.getDuration(build.getDurationMillis()))
                .buildNumber(buildNumber)
                .time(DateUtils.parseBuildDate(build.getStarted()))
                .url(build.getUrl())
                .isBuildFullView(authService.canReadBuild(foundBuildName, foundBuildNumber, foundBuildStarted))
                .canManage(authService.canManageBuild(foundBuildName, foundBuildNumber, foundBuildStarted))
                .canDelete(authService.canDeleteBuild(foundBuildName, foundBuildNumber, foundBuildStarted))
                .build();
    }

    /**
     * Non strict since if {@param buildStarted} is not supplied we just get the latest build (some UI functions use
     * this)
     *
     * THIS METHOD ONLY CHECKS FOR BASIC-READ PERMISSIONS since the data returned by the caller requires just that.
     */
    private Build getBuildNonStrict(String buildName, String buildNumber, @Nullable String buildStarted) {
        Build build = null;
        if (isNotBlank(buildStarted)) {
            BuildRun buildRun = getBuildRunInternally(buildName, buildNumber, buildStarted);
            if (buildRun != null) {
                build = getBuildInternally(buildRun);
            }
        } else {
            // Take the latest build of the specified number
            build = getLatestBuildByNameAndNumberInternally(buildName, buildNumber);
        }
        if (build != null) {
            assertBasicReadPermissions(build.getName(), build.getNumber(), build.getStarted());
        }
        return build;
    }

    private void triggerXrayBuildDelete(String buildName, String buildNumber) {
        // Getting the addon for delete event. Won't send anything in case no licensed installed (coreImpl)
        XrayAddon xrayAddon = addonsManager.addonByType(XrayAddon.class);
        // Sending a delete event
        xrayAddon.callDeleteBuildInterceptor(buildName, buildNumber);
    }

    private void notifyInterceptors(Build build, Class<? extends PluginAction> interceptor) {
        addonsManager.addonByType(PluginsAddon.class)
                .execPluginActions(interceptor, builds, new DetailedBuildRunImpl(build));
    }

    private boolean exists(Build build) {
        return buildStoreService.exists(build);
    }

    /**
     * Returns an internal instance of the service
     */
    private InternalBuildService getTransactionalMe() {
        return ContextHelper.get().beanForType(InternalBuildService.class);
    }

    private List<String> getVirtualRepoKeys() {
        return repoService.getVirtualRepoDescriptors().stream()
                .map(RepoBaseDescriptor::getKey)
                .collect(Collectors.toList());
    }

    private Collector<BuildRun, ?, TreeSet<BuildRun>> sortedSetReverseStartDateCollector() {
        return Collectors.toCollection(() -> new TreeSet<>(
                Collections.reverseOrder(BuildRunComparators.getBuildStartDateComparator())));
    }

    //** ----- Build Info migration logic ----- **\\

    @Override
    public void onContextCreated() {
        if (!buildInfoReady) {
            startBuildInfoMigrationJob();
        }
    }

    private void startBuildInfoMigrationJob() {
        TaskBase buildInfoJob = TaskUtils.createManualTask(BuildInfoMigrationJob.class, 0L);
        List<TaskBase> activeTasks = taskService
                .getActiveTasks(task -> task.getType().equals(BuildInfoMigrationJob.class));
        if (CollectionUtils.isNullOrEmpty(activeTasks)) {
            taskService.startTask(buildInfoJob, false);
        }
    }

    @Override
    public void migrateBuildInfo(BuildInfoCalculationWorkItem workItem) {
        BuildInfoMigrationJobDelegate delegate = workItem.getDelegate();
        long buildId = workItem.getBuildId();
        ConflictGuard lock = getConflictsGuard().getLock(workItem.getUniqueKey());
        try {
            boolean lockAcquired = lock.tryToLock(10, TimeUnit.SECONDS);
            if (lockAcquired) {
                delegate.incrementCurrentBatchCount();
                // The actual work
                delegate.migrateBuildJsonToRepo(buildId);
                delegate.incrementTotalDone();
            } else {
                throw new BuildInfoCalculationFatalException("Could not obtain lock for Build " + buildId, null);
            }
        } catch (InterruptedException e) {
            delegate.log().error("Failed to acquire lock for Build id '{}. {}'", buildId, e.getMessage());
            delegate.handleExceptionDuringMigration(buildId, e);
        } catch (BuildInfoCalculationFatalException e) {
            delegate.handleExceptionDuringMigration(buildId, e);
            delegate.log().error("Failed to migrate Build Info of Build id '" + buildId + "'", e);
        } finally {
            delegate.markTaskAsFinished(Long.toString(buildId));
            lock.unlock();
        }
    }

    /**
     * Since this service must listen on any reload (to verify the build-info state) a check for whether the reload
     * was called for backup changes is made here.
     */
    private void convertIfNeeded(List<DataDiff<?>> configDiff) {
        boolean shouldConvert = configDiff.stream()
                .filter(Objects::nonNull)
                .anyMatch(diff -> diff.getFieldName().contains(CentralConfigKey.backups.getKey()));
        if (shouldConvert) {
            this.convert(null, null);
        }
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
        if (ArtifactoryHome.get().getCreateBackupExcludedBuildNames().exists()) {
            log.debug("Found marker file, executing conversion");
            ArtifactoryContext context = ContextHelper.get();
            CentralConfigService centralConfigService = context.beanForType(CentralConfigService.class);
            SecurityService securityService = context.beanForType(SecurityService.class);
            new BuildBackupConverter(centralConfigService, repoService, securityService).convert();
        }
    }

    /**
     * Protects the realm of Builds by locking any change to a Build json.
     * Key is BuildId.
     */
    private ConflictsGuard<Object> getConflictsGuard() {
        return addonsManager.addonByType(HaCommonAddon.class).getConflictsGuard("builds");
    }

    /**
     * TO BE USED ONLY BY THE BUILD INFO MIGRATION JOB
     * tests the old build_jsosn table (which is not created since 6.6-m001), if it exists, for the presence of the
     * build_jsons_builds_fk foreign key.
     * If either the table or the key doesn't exist then the state is considered new/ready.
     */
    @Override
    public boolean verifyBuildInfoState() {
        if (buildInfoReady) {
            //Already verified to be ok, no need to do it again
            return true;
        }
        try {
            buildInfoReady = !foreignKeyExists(jdbcHelper, dbService.getDatabaseType(), BUILD_JSONS_TABLE, "build_jsons_builds_fk");
        } catch (Exception e) {
            log.error("Cannot verify existence of state-dependant foreign key 'build_jsons_builds_fk', state will " +
                    "be determined based on existence of 'build_jsons' table", e);
            try {
                // This will trigger the migration again if the table is still there (meaning as in all upgrading instances)
                // but worst case it will run its course and won't do any writes.
                buildInfoReady = !tableExists(jdbcHelper, dbService.getDatabaseType(), BUILD_JSONS_TABLE);
            } catch (Exception wtf) {
                log.error("Cannot verify existence of table 'build_jsons', setting state to ready. If required, v6.6.0 build info repo migration will not run.", wtf);
                buildInfoReady = true;
            }
        }
        return buildInfoReady;
    }

    private static class BuildDependenciesCloseException extends RuntimeException {
        BuildDependenciesCloseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
