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

package org.artifactory.rest.resource.ci;

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.build.BuildAddon;
import org.artifactory.addon.rest.RestAddon;
import org.artifactory.api.bintray.BintrayService;
import org.artifactory.api.bintray.distribution.Distribution;
import org.artifactory.api.bintray.distribution.Distributor;
import org.artifactory.api.bintray.distribution.reporting.DistributionReporter;
import org.artifactory.api.build.BuildRunComparators;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.repo.exception.ItemNotFoundRuntimeException;
import org.artifactory.api.rest.artifact.PromotionResult;
import org.artifactory.api.rest.build.BuildInfo;
import org.artifactory.api.rest.build.Builds;
import org.artifactory.api.rest.build.BuildsByName;
import org.artifactory.api.rest.constant.BintrayRestConstants;
import org.artifactory.api.rest.constant.BuildRestConstants;
import org.artifactory.api.rest.constant.RestConstants;
import org.artifactory.api.search.SearchService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.build.BuildArtifactoryRequestImpl;
import org.artifactory.build.BuildRun;
import org.artifactory.build.InternalBuildService;
import org.artifactory.common.ConstantValues;
import org.artifactory.exception.CancelException;
import org.artifactory.rest.common.exception.RestException;
import org.artifactory.rest.common.model.distribution.DistributionResponseBuilder;
import org.artifactory.rest.common.util.BintrayRestHelper;
import org.artifactory.rest.common.util.RestUtils;
import org.artifactory.rest.common.util.build.BuildResourceHelper;
import org.artifactory.rest.common.util.build.distribution.BuildDistributionHelper;
import org.artifactory.rest.exception.AuthorizationRestException;
import org.artifactory.rest.exception.BadRequestException;
import org.artifactory.rest.exception.NotFoundException;
import org.artifactory.rest.resource.build.BuildsDeletionModel;
import org.artifactory.rest.util.ResponseUtils;
import org.artifactory.util.CollectionUtils;
import org.artifactory.util.DoesNotExistException;
import org.artifactory.webapp.servlet.HttpArtifactoryResponse;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.jfrog.build.api.Build;
import org.jfrog.build.api.BuildRetention;
import org.jfrog.build.api.Module;
import org.jfrog.build.api.dependency.BuildPatternArtifacts;
import org.jfrog.build.api.dependency.BuildPatternArtifactsRequest;
import org.jfrog.build.api.release.BintrayUploadInfoOverride;
import org.jfrog.build.api.release.Promotion;
import org.jfrog.common.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.apache.http.HttpStatus.*;
import static org.artifactory.rest.common.util.BintrayRestHelper.createAggregatedResponse;
import static org.artifactory.rest.common.util.RestUtils.getServletContextUrl;

/**
 * A resource to manage the build actions
 *
 * @author Noam Y. Tenne
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Path(BuildRestConstants.PATH_ROOT)
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
public class BuildResource {
    private static final Logger log = LoggerFactory.getLogger(BuildResource.class);

    public static final String ACCESS_DENIED_MSG = "User is not authorized to access build info";

    @Context
    private HttpServletRequest request;
    @Context
    private HttpServletResponse response;
    @Context
    private ExtendedUriInfo uriInfo;

    private AddonsManager addonsManager;
    private InternalBuildService buildService;
    private AuthorizationService authorizationService;
    private SearchService searchService;
    private BintrayService bintrayService;
    private Distributor distributor;
    private BuildDistributionHelper buildDistributionHelper;

    @Autowired
    public BuildResource(AddonsManager addonsManager, InternalBuildService buildService, AuthorizationService authorizationService,
            SearchService searchService, BintrayService bintrayService, Distributor distributor, BuildDistributionHelper buildDistributionHelper) {
        this.addonsManager = addonsManager;
        this.buildService = buildService;
        this.authorizationService = authorizationService;
        this.searchService = searchService;
        this.bintrayService = bintrayService;
        this.distributor = distributor;
        this.buildDistributionHelper = buildDistributionHelper;
    }

    /**
     * Assemble all, last created, available builds with the last
     * Provides information about all builds the current user has view permission for.
     *
     * @return Builds json object
     */
    @GET
    @Produces({BuildRestConstants.MT_BUILDS, MediaType.APPLICATION_JSON})
    public Builds getAllBuilds() {
        Set<BuildRun> latestBuildsByName = searchService.getLatestBuilds();
        if (!latestBuildsByName.isEmpty()) {
            // Add our builds to the list of build resources
            Builds builds = new Builds();
            builds.slf = RestUtils.getBaseBuildsHref(request);
            latestBuildsByName.stream()
                    .filter(build -> authorizationService
                            .isBuildBasicRead(build.getName(), build.getNumber(), build.getStarted()))
                    .forEach(build -> {
                        String buildHref = RestUtils.getBuildRelativeHref(build.getName());
                        builds.builds.add(new Builds.Build(buildHref, build.getStarted()));
                    });
            if (!builds.builds.isEmpty()) {
                return builds;
            }
        }
        throw new NotFoundException("No builds were found");
    }

    /**
     * Get the build name from the request url and assemble all builds under that name.
     *
     * @return BuildsByName json object
     */
    @GET
    @Path("/{buildName: .+}")
    @Produces({BuildRestConstants.MT_BUILDS_BY_NAME, MediaType.APPLICATION_JSON})
    public BuildsByName getAllSpecificBuilds(@PathParam("buildName") String buildName) {
        Set<BuildRun> buildsByName = buildService.searchBuildsByName(buildName);
        if (!buildsByName.isEmpty()) {
            BuildsByName builds = new BuildsByName();
            builds.slf = RestUtils.getBaseBuildsHref(request) + RestUtils.getBuildRelativeHref(buildName);
            for (BuildRun buildRun : buildsByName) {
                String versionHref = RestUtils.getBuildNumberRelativeHref(buildRun.getNumber());
                builds.buildsNumbers.add(new BuildsByName.Build(versionHref, buildRun.getStarted()));
            }
            return builds;
        }

        throw new NotFoundException(String.format("No build was found for build name: %s", buildName));
    }

    /**
     * Get the build name and number from the request url and send back the exact build for those parameters
     *
     * @return BuildInfo json object
     */
    @GET
    @Path("/{buildName: .+}/{buildNumber: .+}")
    @Produces({BuildRestConstants.MT_BUILD_INFO, BuildRestConstants.MT_BUILDS_DIFF, MediaType.APPLICATION_JSON})
    public Response getBuildInfo(
            @PathParam("buildName") String buildName,
            @PathParam("buildNumber") String buildNumber,
            @QueryParam("started") String buildStarted,
            @QueryParam("diff") String diffNumber) throws IOException {
        Build build = getBuildByName(buildName, buildNumber, buildStarted);
        if (build == null) {
            String msg = String.format("No build was found for build name: %s, build number: %s %s",
                    buildName, buildNumber,
                    StringUtils.isNotBlank(buildStarted) ? ", build started: " + buildStarted : "");
            throw new NotFoundException(msg);
        }
        if (queryParamsContainKey("diff")) {
            if ("null".equals(diffNumber)) {
                throw new BadRequestException("Parameter 'diff' must contain a value, if specified.");
            }
            Build secondBuild = buildService.getLatestBuildByNameAndNumber(buildName, diffNumber);
            if (secondBuild == null) {
                throw new NotFoundException(String.format("No build was found for build name: %s , build number: %s ",
                        buildName, diffNumber));
            }
            BuildRun buildRun = buildService.getBuildRunInternally(build.getName(), build.getNumber(), build.getStarted());
            BuildRun secondBuildRun = buildService.getBuildRunInternally(secondBuild.getName(), secondBuild.getNumber(), secondBuild.getStarted());
            Comparator<BuildRun> comparator = BuildRunComparators.getBuildStartDateComparator();
            if (comparator.compare(buildRun, secondBuildRun) < 0) {
                throw new BadRequestException(
                        "Build number should be greater than the build number to compare against.");
            }
            return prepareBuildDiffResponse(build, secondBuild, request);
        } else {
            return prepareGetBuildResponse(build);
        }
    }

    private Build getBuildByName(String buildName, String buildNumber, String buildStarted) {
        if (StringUtils.isNotBlank(buildStarted)) {
            BuildRun buildRun = buildService.getBuildRun(buildName, buildNumber, buildStarted);
            if (buildRun != null) {
                return buildService.getBuild(buildRun);
            }
        } else {
            return buildService.getLatestBuildByNameAndNumber(buildName, buildNumber);
        }
        return null;
    }

    private Response prepareGetBuildResponse(Build build) throws IOException {
        BuildInfo buildInfo = new BuildInfo();
        buildInfo.slf = RestUtils.getBuildInfoHref(request, build.getName(), build.getNumber());
        buildInfo.buildInfo = build;

        return Response.ok(buildInfo).build();
    }

    private Response prepareBuildDiffResponse(Build firstBuild, Build secondBuild, HttpServletRequest request) {
        RestAddon restAddon = addonsManager.addonByType(RestAddon.class);
        return restAddon.getBuildsDiff(firstBuild, secondBuild, request);
    }

    /**
     * Returns the outputs of build matching the request
     *
     * @param buildPatternArtifactsRequests contains build name and build number or keyword
     * @return build outputs (build dependencies and generated artifacts).
     * The returned array will always be the same size as received, returning nulls on non-found builds.
     */
    @POST
    @Path("/patternArtifacts")
    @Consumes({BuildRestConstants.MT_BUILD_PATTERN_ARTIFACTS_REQUEST, RestConstants.MT_LEGACY_ARTIFACTORY_APP,
            MediaType.APPLICATION_JSON})
    @Produces({BuildRestConstants.MT_BUILD_PATTERN_ARTIFACTS_RESULT, MediaType.APPLICATION_JSON})
    public List<BuildPatternArtifacts> getBuildPatternArtifacts(final List<BuildPatternArtifactsRequest> buildPatternArtifactsRequests) {
        final RestAddon restAddon = addonsManager.addonByType(RestAddon.class);
        final String contextUrl = getServletContextUrl(request);
        return buildPatternArtifactsRequests.stream()
                .map(patternRequest -> restAddon.getBuildPatternArtifacts(patternRequest, contextUrl))
                .collect(Collectors.toList());
    }

    /**
     * Adds the given build information to the DB
     *
     * @param build Build to add
     */
    @PUT
    @Consumes({BuildRestConstants.MT_BUILD_INFO, RestConstants.MT_LEGACY_ARTIFACTORY_APP, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response addBuild(Build build) {
        //If op failed a proper rest-compatible exception is thrown
        buildService.addBuild(build);
        return Response.status(SC_NO_CONTENT).build();
    }

    @POST
    @Path("/retention/{buildName: .+}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response retention(BuildRetention retention, @PathParam("buildName") String buildName, @QueryParam(BuildRestConstants.PARAM_ASYNC) boolean async) {
        Response retentionResponse;
        if (retention == null || retention.getCount() == 0) {
            retentionResponse = Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("Max count retention needs to be a positive number")
                    .build();
        } else {
            boolean forceAsync = ConstantValues.buildRetentionAlwaysAsync.getBoolean();
            retentionResponse = executeRetentionPolicy(buildName, retention, async || forceAsync);
        }
        return retentionResponse;
    }

    private Response executeRetentionPolicy(String buildName, @Nonnull BuildRetention retention, boolean async) {
        if (async) {
            return executeRetentionPolicyAsync(buildName, retention);
        }
        return executeRetentionPolicySync(buildName, retention);
    }

    private Response executeRetentionPolicyAsync(String buildName, BuildRetention retention) {
        addonsManager.addonByType(BuildAddon.class).discardOldBuilds(buildName, retention, true);
        log.info("Retention policy for build '{}' scheduled to run", buildName);
        return Response.status(SC_NO_CONTENT).build();
    }

    private Response executeRetentionPolicySync(String buildName, BuildRetention retention) {
        log.info("Starting execution of Retention Policy for build '{}'", buildName);
        BasicStatusHolder status = addonsManager.addonByType(BuildAddon.class).discardOldBuilds(buildName, retention, false);
        if (status.hasErrors()) {
            int code = status.getMostImportantErrorStatusCode().getStatusCode();
            return Response.status(code)
                    .entity(ResponseUtils
                            .getResponseWithStatusCodeErrorAndErrorMassages(status,
                                    "Errors have occurred while maintaining build retention. " +
                                            "Please review the system logs for further information.", code)).build();
        } else if (status.hasWarnings()) {
            int code = status.getMostImportantWarningsStatusCode().getStatusCode();
            return Response.status(code)
                    .entity(ResponseUtils
                            .getResponseWithStatusCodeErrorAndErrorMassages(status,
                                    "Warnings encountered while performing build retention. " +
                                            "Please review the logs for further information.", code)).build();
        }
        log.info("Retention policy for build '{}' finished successfully", buildName);
        return Response.status(SC_NO_CONTENT).build();
    }

    /**
     * Adds the given module information to an existing build
     *
     * @param buildName   The name of the parent build that should receive the module
     * @param buildNumber The number of the parent build that should receive the module
     * @param modules     Modules to add
     */
    @POST
    @Path("/append/{buildName: .+}/{buildNumber: .+}")
    @Consumes({BuildRestConstants.MT_BUILD_INFO_MODULE, RestConstants.MT_LEGACY_ARTIFACTORY_APP, MediaType.APPLICATION_JSON})
    public void addModule(@PathParam("buildName") String buildName, @PathParam("buildNumber") String buildNumber,
            @QueryParam("started") String buildStarted, List<Module> modules) {
        log.info("Adding module to build '{} #{}'", buildName, buildNumber);

        Build build = getBuildByName(buildName, buildNumber, buildStarted);
        if (build == null) {
            throw new NotFoundException("No builds were found");
        }

        build.getModules().addAll(modules);

        try {
            buildService.updateBuild(build);
        } catch (CancelException e) {
            if (log.isDebugEnabled()) {
                log.debug("An error occurred while adding a module to the build '" + build.getName() + " #" +
                        build.getNumber() + "'.", e);
            }
            throw new RestException(e.getErrorCode(), e.getMessage());
        }
    }

    /**
     * Promotes a build
     *
     * @param buildName   Name of build to promote
     * @param buildNumber Number of build to promote
     * @param promotion   Promotion settings
     * @return Promotion result
     */
    @POST
    @Path("/promote/{buildName: .+}/{buildNumber: .+}")
    @Consumes({BuildRestConstants.MT_PROMOTION_REQUEST, MediaType.APPLICATION_JSON})
    @Produces({BuildRestConstants.MT_PROMOTION_RESULT, MediaType.APPLICATION_JSON})
    public Response promote(
            @PathParam("buildName") String buildName,
            @PathParam("buildNumber") String buildNumber, Promotion promotion) throws IOException {

        if (nonNull(promotion.getMappings()) && (nonNull(promotion.getSourceRepo()) || nonNull(promotion.getTargetRepo()))) {
            throw new BadRequestException("The use of both 'sourceRepo' or 'targetRepo' and 'mappings' fields is forbidden");
        }

        RestAddon restAddon = addonsManager.addonByType(RestAddon.class);
        try {
            if (RestUtils.shouldDecodeParams(request)) {
                buildName = URLDecoder.decode(buildName, "UTF-8");
                buildNumber = URLDecoder.decode(buildNumber, "UTF-8");
            }
            PromotionResult promotionResult = restAddon.promoteBuild(buildName, buildNumber, promotion);
            int status = promotion.isFailFast() && promotionResult.errorsOrWarningHaveOccurred() ? SC_BAD_REQUEST : SC_OK;
            return Response.status(status).entity(promotionResult).build();
        } catch (IllegalArgumentException | ItemNotFoundRuntimeException iae) {
            throw new BadRequestException(iae.getMessage());
        } catch (DoesNotExistException dnee) {
            throw new NotFoundException(dnee.getMessage());
        } catch (ParseException pe) {
            throw new RestException("Unable to parse given build start date: " + pe.getMessage());
        }
    }

    /**
     * Renames structure, content and properties of build info objects
     *
     * @param to Replacement build name
     */
    @POST
    @Path("/rename/{buildName: .+}")
    public String renameBuild(
            @PathParam("buildName") String buildName,
            @QueryParam("to") String to) throws IOException {
        RestAddon restAddon = addonsManager.addonByType(RestAddon.class);
        try {
            String from;
            if (RestUtils.shouldDecodeParams(request)) {
                from = URLDecoder.decode(buildName, "UTF-8");
            } else {
                from = buildName;
            }
            restAddon.renameBuilds(from, to);

            response.setStatus(SC_OK);

            return String.format("Build renaming of '%s' to '%s' was successfully started.\n", from, to);
        } catch (IllegalArgumentException iae) {
            throw new BadRequestException(iae.getMessage());
        } catch (DoesNotExistException dnne) {
            throw new NotFoundException(dnne.getMessage());
        }
    }

    /**
     * Removes the build with the given name and number
     *
     * {@link #response} is changed with the status message of the deletion
     */
    @DELETE
    @Path("/{buildName: .+}")
    public void deleteBuilds(
            @PathParam("buildName") String buildName,
            @QueryParam("artifacts") int artifacts,
            @QueryParam("buildNumbers") StringList buildNumbers,
            @QueryParam("deleteAll") int deleteAll) throws IOException {
        executeDelete(buildName, artifacts, buildNumbers, deleteAll);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/delete")
    public void bulkDeleteBuilds(BuildsDeletionModel build) throws IOException {
        if (build == null || StringUtils.isBlank(build.getBuildName())) {
            log.error("Missing build name");
            throw new BadRequestException("Missing build name");
        }
        if (CollectionUtils.isNullOrEmpty(build.getBuildNumbers()) && !build.isDeleteAll()) {
            log.error("Missing build numbers");
            throw new BadRequestException("Missing build numbers");
        }

        int artifacts = build.isDeleteArtifacts() ? 1 : 0;
        int deleteAll = build.isDeleteAll() ? 1 : 0;
        executeDelete(build.getBuildName(), artifacts, build.getBuildNumbers(), deleteAll);
    }

    private void executeDelete(String buildName, int artifacts, List<String> buildNumbers, int deleteAll) throws IOException {
        RestAddon restAddon = addonsManager.addonByType(RestAddon.class);
        try {
            if (RestUtils.shouldDecodeParams(request)) {
                buildName = URLDecoder.decode(buildName, "UTF-8");
            }
            // Permissions check done in restAddon
            restAddon.deleteBuilds(response, buildName, buildNumbers, artifacts, deleteAll);
        } catch (IllegalArgumentException iae) {
            throw new BadRequestException(iae.getMessage());
        } catch (DoesNotExistException dnne) {
            throw new NotFoundException(dnne.getMessage());
        }
        response.flushBuffer();
    }

    /**
     * Pushes a build to Bintray, expects to find the bintrayBuildInfo.json as one of the build's artifacts
     *
     * @param buildName     Name of build to promote
     * @param buildNumber   Number of build to promote
     * @param gpgPassphrase (optional) the Passphrase to use in conjunction with the key stored in Bintray to
     *                      sign the version
     * @return result of the operation
     */
    @POST
    @Path("/pushToBintray/{buildName: .+}/{buildNumber: .+}")
    @Consumes({BuildRestConstants.MT_BINTRAY_DESCRIPTOR_OVERRIDE, MediaType.APPLICATION_JSON})
    @Produces({BintrayRestConstants.MT_BINTRAY_PUSH_RESPONSE, MediaType.APPLICATION_JSON})
    @Deprecated //use distribute
    public Response pushToBintray(@PathParam("buildName") String buildName,
            @PathParam("buildNumber") String buildNumber,
            @QueryParam("gpgPassphrase") @Nullable String gpgPassphrase,
            @QueryParam("gpgSign") @Nullable Boolean gpgSignOverride,
            @Nullable BintrayUploadInfoOverride override) throws IOException {

        BasicStatusHolder status = new BasicStatusHolder();
        String buildId = buildName + " #" + buildNumber;
        if (!BintrayRestHelper.isPushToBintrayAllowed(status, null)) {
            throw new AuthorizationRestException(status.getLastError().getMessage());
        }
        Build build = getBuild(buildName, buildNumber, status);
        if (!status.isError()) {
            status.merge(bintrayService.pushPromotedBuild(build, gpgPassphrase, gpgSignOverride, override));
        }
        return createAggregatedResponse(status, buildId, false);
    }

    @POST
    @Path("/distribute/{buildName: .+}/{buildNumber: .+}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response distributeBuild(Distribution dist,
            @PathParam("buildName") String buildName,
            @PathParam("buildNumber") String buildNumber) throws IOException {
        //For validation, build search etc. must use status holder because interfaces are used by Bintray service as well
        BasicStatusHolder preOpStatus = new BasicStatusHolder();
        String buildId = buildName + " #" + buildNumber;
        DistributionReporter status = new DistributionReporter(!dist.isDryRun());
        if (!BintrayRestHelper.isPushToBintrayAllowed(preOpStatus, dist.getTargetRepo())) {
            throw new AuthorizationRestException(preOpStatus.getLastError().getMessage());
        }
        Build build = getBuild(buildName, buildNumber, preOpStatus);
        if (build == null) {
            preOpStatus.error("Invalid build {}:{} given.", SC_BAD_REQUEST, log);
        } else {
            buildDistributionHelper.populateBuildPaths(build, dist, status);
        }
        status.merge(preOpStatus);
        String distResponse;
        if (status.isError()) {
            distResponse = DistributionResponseBuilder.writeResponseBody(status, buildId, dist.isAsync(), dist.isDryRun());
        } else {
            status = distributor.distribute(dist);
            distResponse = DistributionResponseBuilder.writeResponseBody(status, buildId, dist.isAsync(), dist.isDryRun());
        }
        return Response.status(DistributionResponseBuilder.getResponseCode(status))
                .entity(distResponse)
                .type(APPLICATION_JSON_TYPE)
                .build();
    }

    private Build getBuild(String buildName, String buildNumber, BasicStatusHolder status) {
        Build build = null;
        try {
            BuildRun buildRun = BuildResourceHelper.validateParamsAndGetBuildInfo(buildName, buildNumber, null, buildService);
            build = buildService.getBuild(buildRun);
        } catch (Exception e) {
            log.debug("", e);
            status.error("Can't get build: " + buildName + ":" + buildNumber + " - " + e.getMessage(), log);
        }
        return build;
    }

    private boolean queryParamsContainKey(String key) {
        MultivaluedMap<String, String> queryParameters = queryParams();
        return queryParameters.containsKey(key);
    }

    private MultivaluedMap<String, String> queryParams() {
        MultivaluedMap<String,  String> map = new MultivaluedHashMap<>();
        final String queryString = request.getQueryString();

        if (!Objects.isNull(queryString)) {
            List<NameValuePair> params = URLEncodedUtils
                    .parse(queryString, Charset.forName("UTF-8"));
            params.stream().forEach(elem -> map.put(elem.getName(), Arrays.asList(elem.getValue())));
        }

        return map;
    }

    @PUT
    @Path("/buildUploadRedirect/{originalServletPath: .+}")
    @Consumes({MediaType.APPLICATION_OCTET_STREAM})
    public void buildUploadRedirect(@PathParam("originalServletPath") String originalServletPath) {
        log.debug("Build upload redirect triggered with original servlet path '{}'", originalServletPath);
        try {
            buildService.handleBuildUploadRedirect(new BuildArtifactoryRequestImpl(request), new HttpArtifactoryResponse(response));
        } catch (IOException ioe) {
            throw new BadRequestException("Failed to deploy build info to path '" + originalServletPath + "' : " + ioe.getMessage());
        }
    }
}
