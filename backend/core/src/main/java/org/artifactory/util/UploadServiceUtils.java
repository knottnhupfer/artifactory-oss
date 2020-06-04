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

package org.artifactory.util;

import com.google.common.collect.Sets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.repo.exception.RepoRejectException;
import org.artifactory.api.request.ArtifactoryResponse;
import org.artifactory.api.request.InternalArtifactoryRequest;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.SnapshotVersionBehavior;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.exception.ValidationException;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.MutableFileInfo;
import org.artifactory.fs.RepoResource;
import org.artifactory.io.checksum.ChecksumUtils;
import org.artifactory.io.checksum.policy.ChecksumPolicy;
import org.artifactory.io.checksum.policy.LocalRepoChecksumPolicy;
import org.artifactory.md.Properties;
import org.artifactory.model.xstream.fs.PropertiesImpl;
import org.artifactory.properties.validation.PropertyNameValidator;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.SaveResourceContext;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.repo.snapshot.MavenSnapshotVersionAdapter;
import org.artifactory.repo.snapshot.MavenSnapshotVersionAdapterContext;
import org.artifactory.request.ArtifactoryRequest;
import org.artifactory.resource.MutableRepoResourceInfo;
import org.artifactory.security.AccessLogger;
import org.artifactory.traffic.TrafficService;
import org.artifactory.traffic.entry.UploadEntry;
import org.artifactory.webapp.servlet.DelayedHttpResponse;
import org.artifactory.webapp.servlet.HttpArtifactoryResponse;
import org.jfrog.client.util.PathUtils;
import org.jfrog.storage.binstore.common.ReaderTrackingInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import static org.apache.http.HttpStatus.SC_CONFLICT;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.artifactory.descriptor.repo.LocalRepoChecksumPolicyType.SERVER;
import static org.artifactory.descriptor.repo.SnapshotVersionBehavior.DEPLOYER;
import static org.artifactory.util.HttpUtils.*;

/**
 * Man UploadService, you need to lose weight!
 *
 * @author Dan Feldman
 */
public class UploadServiceUtils {
    private static final Logger log = LoggerFactory.getLogger(UploadServiceUtils.class);

    public static boolean responseWasIntercepted(ArtifactoryResponse response) {
        return response.isError();
    }

    public static boolean isRequestedRepoKeyInvalid(ArtifactoryRequest request) {
        return StringUtils.isBlank(request.getRepoKey());
    }

    public static boolean isTargetRepositoryInvalid(LocalRepo targetRepository) {
        return targetRepository == null;
    }

    public static void consumeRequestBody(ArtifactoryRequest request) throws IOException {
        IOUtils.copy(request.getInputStream(), new NullOutputStream());
    }

    public static boolean processOriginatedExternally(ArtifactoryResponse response) {
        //Must check the type of the response instead of the request since the HTTP request object isn't accessible here
        return response instanceof HttpArtifactoryResponse;
    }

    public static void commitResponseIfDelayed(ArtifactoryResponse response) throws IOException {
        if (response instanceof DelayedHttpResponse) {
            ((DelayedHttpResponse) response).commitResponseCode();
        }
    }

    public static boolean rejectionSignifiesRequiredAuthorization(RepoRejectException rejectionException, AuthorizationService authService) {
        return (rejectionException.getErrorCode() == HttpStatus.SC_FORBIDDEN) && authService.isAnonymous();
    }


    public static boolean isAbnormalChecksumContentLength(long length) {
        return length > 1024;
    }

    public static boolean isAbnormalPropertiesContentLength(long length) {
        int maxLength = ConstantValues.replicationPropertiesMaxLength.getInt();
        return length > maxLength;
    }

    public static boolean isAbnormalStatisticsContentLength(long length) {
        int maxLength = ConstantValues.replicationStatisticsMaxLength.getInt();
        return length > maxLength;
    }

    public static void consumeContentAndRespondWithSuccess(ArtifactoryRequest request, ArtifactoryResponse response) throws IOException {
        consumeRequestBody(request);
        response.sendSuccess();
    }

    public static void sendInvalidUploadedChecksumResponse(ArtifactoryRequest request, ArtifactoryResponse response,
            LocalRepo targetRepo, RepoPath repoPath, String errorMessage) throws IOException {
        ChecksumPolicy checksumPolicy = targetRepo.getChecksumPolicy();
        if (checksumPolicy instanceof LocalRepoChecksumPolicy &&
                ((LocalRepoChecksumPolicy) checksumPolicy).getPolicyType().equals(SERVER)) {
            log.debug(errorMessage);
            sendUploadedChecksumResponse(request, response, repoPath);
        } else {
            response.sendError(SC_CONFLICT, errorMessage, log);
        }
    }

    public static void fireUploadTrafficEvent(RepoResource resource, long remoteUploadStartTime, TrafficService trafficService) {
        if (remoteUploadStartTime > 0) {
            String remoteAddress = getRemoteClientAddress();
            // fire upload event only if the resource is really uploaded from the remote client
            UploadEntry uploadEntry = new UploadEntry(resource.getRepoPath().getId(),
                    resource.getSize(), System.currentTimeMillis() - remoteUploadStartTime, remoteAddress);
            trafficService.handleTrafficEntry(uploadEntry);
        }
    }

    public static LocalRepo getTargetRepository(ArtifactoryRequest request, InternalRepositoryService repoService) {
        String repoKey = request.getRepoKey();
        VirtualRepoDescriptor virtualRepoDescriptor = repoService.virtualRepoDescriptorByKey(repoKey);
        if (virtualRepoDescriptor == null) {
            return repoService.localRepositoryByKey(repoKey);
        }

        LocalRepoDescriptor defaultDeploymentRepo = virtualRepoDescriptor.getDefaultDeploymentRepo();
        return defaultDeploymentRepo != null ? repoService.localRepositoryByKey(defaultDeploymentRepo.getKey()) : null;
    }

    public static Properties populateAndValidateItemPropertiesFromRequest(ArtifactoryRequest request, InputStream inputStream,
            RepoPath repoPath, AuthorizationService authService) throws RepoRejectException {
        Properties properties = null;
        //Adding sha256 property to artifacts to not break backward compatibility - needs to be deprecated though
        if (isChecksumDeploy(request)) {
            String sha256Checksum = getSha256Checksum(request);
            // Make sure that we found the file using checksum deploy
            if (StringUtils.isNotBlank(sha256Checksum) && inputStream != null &&
                    inputStream instanceof ReaderTrackingInputStream) {
                properties = (Properties) InfoFactoryHolder.get().createProperties();
                properties.put("sha256", sha256Checksum);
            }
        }
        if (request.getProperties() != null && !request.getProperties().isEmpty() && validateCanAnnotate(repoPath, authService)) {
            validateProperties(request.getProperties());
            if (properties == null) {
                properties = new PropertiesImpl();
            }
            properties.putAll(request.getProperties());
        }

        return properties;
    }

    public static boolean validateCanAnnotate(RepoPath repoPath, AuthorizationService authService) {
        if (!authService.canAnnotate(repoPath)) {
            log.warn("The user: '{}' is not permitted to annotate '{}' on '{}'.", authService.currentUsername(), repoPath.getPath(), repoPath.getRepoKey());
            AccessLogger.annotateDenied(repoPath);
            return false;
        }
        return true;
    }

    public static void validateProperties(@Nonnull Properties properties) throws RepoRejectException {
        for (String propertyKey : properties.keySet()) {
            try {
                PropertyNameValidator.validate(propertyKey);
            } catch (ValidationException e) {
                throw new RepoRejectException("Property key: " + propertyKey + " is invalid due to " + e.getMessage(),
                        HttpStatus.SC_BAD_REQUEST);
            }
        }
    }

    public static boolean isVirtualRepoKey(String repoKey, RepositoryService repoService) {
        return repoService.virtualRepoDescriptorByKey(repoKey) != null;
    }

    public static boolean isMavenRepo(LocalRepo repo) {
        return repo.getDescriptor().isMavenRepoLayout();
    }

    public static boolean isChecksumDeploy(ArtifactoryRequest request) {
        return Boolean.parseBoolean(request.getHeader(ArtifactoryRequest.CHECKSUM_DEPLOY));
    }

    public static boolean isCheckBinaryExistenceInFilestore(ArtifactoryRequest request) {
        return Boolean.parseBoolean(request.getHeader(ArtifactoryRequest.CHECK_BINARY_EXISTENCE_IN_FILESTORE));
    }

    public static boolean isDeployArchiveBundle(ArtifactoryRequest request) {
        return Boolean.parseBoolean(request.getHeader(ArtifactoryRequest.EXPLODE_ARCHIVE))
                || Boolean.parseBoolean(request.getHeader(ArtifactoryRequest.EXPLODE_ARCHIVE_ATOMIC));
    }

    public static boolean isRepoSnapshotPolicyNotDeployer(LocalRepo repo) {
        SnapshotVersionBehavior mavenSnapshotVersionBehavior = repo.getMavenSnapshotVersionBehavior();
        return !mavenSnapshotVersionBehavior.equals(DEPLOYER);
    }

    public static  RepoPath adjustAndGetChecksumTargetRepoPath(ArtifactoryRequest request, LocalRepo repo) {
        String checksumTargetFile = request.getPath();
        if (isMavenRepo(repo)) {
            checksumTargetFile = adjustMavenSnapshotPath(repo, request);
        }
        return repo.getRepoPath(PathUtils.stripExtension(checksumTargetFile));
    }

    public static  String getChecksumContentAsString(ArtifactoryRequest request) throws IOException {
        try (InputStream inputStream = request.getInputStream()) {
            return ChecksumUtils.checksumStringFromStream(inputStream);
        }
    }

    public static  boolean isChecksumValidAccordingToPolicy(String checksum, ChecksumInfo checksumInfo) {
        return checksum.equalsIgnoreCase(checksumInfo.getActual());
    }

    public static void consumeContentAndRespondAccepted(ArtifactoryRequest request, ArtifactoryResponse response) throws IOException {
        log.trace("Skipping deployment of maven metadata file {}", request.getPath());
        consumeRequestBody(request);
        response.setStatus(HttpStatus.SC_ACCEPTED);
    }

    public static void sendUploadedChecksumResponse(ArtifactoryRequest request, ArtifactoryResponse response, RepoPath targetFileRepoPath) {
        response.setHeader("Location", buildArtifactUrl(request, targetFileRepoPath));
        response.setStatus(SC_CREATED);
        response.sendSuccess();
    }

    public static String buildArtifactUrl(ArtifactoryRequest request, RepoPath repoPath) {
        return request.getServletContextUrl() + "/" + repoPath.getRepoKey() + "/" + repoPath.getPath();
    }

    public static void setItemLastModifiedInfoFromHeaders(ArtifactoryRequest request, RepoResource res) {
        String lastModifiedString = request.getHeader(ArtifactoryRequest.LAST_MODIFIED);
        if (StringUtils.isNotBlank(lastModifiedString)) {
            long lastModified = Long.parseLong(lastModifiedString);
            if (lastModified > 0) {
                ((MutableRepoResourceInfo) res.getInfo()).setLastModified(lastModified);
            }
        }
    }

    public static void setItemCreatedInfoFromHeaders(ArtifactoryRequest request,
            SaveResourceContext.Builder contextBuilder) {
        String createdString = request.getHeader(ArtifactoryRequest.CREATED);
        if (StringUtils.isNotBlank(createdString)) {
            long created = Long.parseLong(createdString);
            if (created > 0) {
                contextBuilder.created(created);
            }
        }
    }

    public static void setItemCreatedByInfoFromHeaders(ArtifactoryRequest request,
            SaveResourceContext.Builder contextBuilder) {
        String createBy = request.getHeader(ArtifactoryRequest.CREATED_BY);
        if (StringUtils.isNotBlank(createBy)) {
            contextBuilder.createdBy(createBy);
        }
    }

    public static void setItemModifiedInfoFromHeaders(ArtifactoryRequest request,
            SaveResourceContext.Builder contextBuilder) {
        String modifiedBy = request.getHeader(ArtifactoryRequest.MODIFIED_BY);
        if (StringUtils.isNotBlank(modifiedBy)) {
            contextBuilder.modifiedBy(modifiedBy);
        }
    }

    public static void setFileInfoChecksums(ArtifactoryRequest request, MutableFileInfo fileInfo,
            boolean checksumDeploy) {
        if (checksumDeploy || (request instanceof InternalArtifactoryRequest &&
                ((InternalArtifactoryRequest) request).isTrustServerChecksums())) {
            fileInfo.createTrustedChecksums();
            return;
        }
        // set checksums if attached to the request headers
        String sha1 = getSha1Checksum(request);
        String sha2 = getSha256Checksum(request);
        String md5 = HttpUtils.getMd5Checksum(request);
        Set<ChecksumInfo> checksums = Sets.newHashSet();
        if (StringUtils.isNotBlank(sha1)) {
            log.debug("Found sha1 '{}' for file '{}", sha1, fileInfo.getRepoPath());
            checksums.add(new ChecksumInfo(ChecksumType.sha1, sha1, null));
        }
        if (StringUtils.isNotBlank(sha2)) {
            log.debug("Found sha256 '{}' for file '{}", sha2, fileInfo.getRepoPath());
            checksums.add(new ChecksumInfo(ChecksumType.sha256, sha2, null));
        }
        if (StringUtils.isNotBlank(md5)) {
            log.debug("Found md5 '{}' for file '{}", md5, fileInfo.getRepoPath());
            checksums.add(new ChecksumInfo(ChecksumType.md5, md5, null));
        }
        if (!checksums.isEmpty()) {
            fileInfo.setChecksums(checksums);
        }
    }

    public static String adjustMavenSnapshotPath(LocalRepo repo, ArtifactoryRequest request) {
        String path = request.getPath();
        ModuleInfo itemModuleInfo = repo.getItemModuleInfo(path);
        MavenSnapshotVersionAdapter adapter = repo.getMavenSnapshotVersionAdapter();
        MavenSnapshotVersionAdapterContext context = new MavenSnapshotVersionAdapterContext(
                repo.getRepoPath(path), itemModuleInfo);

        Properties properties = request.getProperties();
        if (properties != null) {
            String timestamp = properties.getFirst("build.timestamp");
            if (StringUtils.isNotBlank(timestamp)) {
                context.setTimestamp(timestamp);
            }
        }
        String adjustedPath = adapter.adaptSnapshotPath(context);
        if (!adjustedPath.equals(path)) {
            log.debug("Snapshot file path '{}' adjusted to: '{}'", path, adjustedPath);
        }
        return adjustedPath;
    }

    public static void populateItemInfoFromHeaders(ArtifactoryRequest request, RepoResource res, SaveResourceContext.Builder contextBuilder, AuthorizationService authService) {
        if (authService.isAdmin()) {
            setItemLastModifiedInfoFromHeaders(request, res);
            setItemCreatedInfoFromHeaders(request, contextBuilder);
            setItemCreatedByInfoFromHeaders(request, contextBuilder);
            setItemModifiedInfoFromHeaders(request, contextBuilder);
        }
    }
}
