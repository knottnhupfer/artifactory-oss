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

package org.artifactory.request;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.request.ArtifactoryResponse;
import org.artifactory.api.rest.constant.ArtifactRestConstants;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.RealRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.fs.RepoResource;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.request.range.RangeAwareContext;
import org.artifactory.resource.RepoResourceInfo;
import org.artifactory.resource.ResourceStreamHandle;
import org.artifactory.resource.ZipEntryResource;
import org.artifactory.security.AccessLogger;
import org.artifactory.traffic.TrafficService;
import org.artifactory.traffic.entry.DownloadEntry;
import org.artifactory.util.HttpUtils;
import org.jfrog.access.common.ServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.artifactory.api.rest.constant.ArtifactRestConstants.MT_ITEM_PROPERTIES;
import static org.artifactory.request.range.ResponseWithRangeSupportHelper.createRangeAwareContext;

/**
 * @author yoavl
 */
public final class RequestResponseHelper {
    private static final Logger log = LoggerFactory.getLogger(RequestResponseHelper.class);

    private TrafficService trafficService;
    private AuthorizationService authorizationService;

    public RequestResponseHelper(TrafficService service, AuthorizationService authorizationService) {
        trafficService = service;
        this.authorizationService = authorizationService;
    }

    private static void addDebugLog(long actualLength, long resLenght, RepoPath repoPath) {
        if (log.isDebugEnabled()) {
            log.debug("Sending back body response for '{}'. Original resource size: {}, actual size: {}.",
                    repoPath, resLenght, actualLength);
        }
    }

    public void sendBodyResponse(ArtifactoryResponse response, RepoResource res, ResourceStreamHandle handle,
            InternalRequestContext requestContext) throws IOException {
        // Try to get range headers
        String rangesString = tryToGetRangeHeaderInsensitive(HttpHeaders.RANGE, requestContext.getRequest());
        String ifRangesString = tryToGetRangeHeaderInsensitive(HttpHeaders.IF_RANGE, requestContext.getRequest());

        String originalMimeType = res.getMimeType();
        String artifactPath = res instanceof ZipEntryResource ? ((ZipEntryResource) res).getEntryPath() : res.getResponseRepoPath().getPath();
        //modify the returned Content-Type header if the request coming from a browser
        String modifiedMimeType = getModifiedMimeType(artifactPath,
                res.getResponseRepoPath().getRepoKey(), requestContext.getRequest());
        String mimeType = modifiedMimeType != null ? modifiedMimeType : originalMimeType;
        InputStream inputStream = handle.getInputStream();
        // Get Actual file actualLength (Note that it might not be equal to the res.getSize)
        long actualLength = handle.getSize();
        AccessLogger.downloaded(res.getRepoPath());
        addDebugLog(actualLength, res.getSize(), res.getRepoPath());
        // Ensure valid content length
        actualLength = actualLength > 0 ? actualLength : res.getSize();
        // Get artifact last modified date
        long lastModified = res.getLastModified();
        // Get artifact sha1
        String sha1 = res.getInfo().getSha1();
        // Create range aware data for the response
        RangeAwareContext context = createRangeAwareContext(inputStream, actualLength, rangesString, ifRangesString,
                mimeType, lastModified, sha1);
        // If request range not satisfiable update response status end return
        if (context.getStatus() == HttpStatus.SC_REQUESTED_RANGE_NOT_SATISFIABLE) {
            response.setHeader(HttpHeaders.CONTENT_RANGE, context.getContentRange());
            response.setStatus(context.getStatus());
            return;
        }
        // Update response with repo resource info
        updateResponseFromRepoResource(response, res);
        // update content length with range aware content length
        response.setContentLength(context.getContentLength());
        // update content type with range aware content type
        response.setContentType(context.getContentType());
        // update headers with range aware headers
        if (context.getContentRange() != null) {
            response.setHeader(HttpHeaders.CONTENT_RANGE, context.getContentRange());
        }
        // Set response status
        if (context.getStatus() > 0) {
            response.setStatus(context.getStatus());
        }
        // Get range aware input stream
        inputStream = context.getInputStream();
        // Get current time for logs
        long start = System.currentTimeMillis();
        // Send range aware input stream
        response.sendStream(inputStream);
        fireDownloadTrafficEvent(requestContext, response, res.getRepoPath(), context.getContentLength(), start);
    }

    public String getModifiedMimeType(String filePath, String repoKey, Request request) {
        boolean browserRequest = BrowserRequestHelper.isBrowserRequest(request);
        if (browserRequest && !contentBrowsingDisabled(repoKey)) {
            return BrowserRequestHelper.getMimeType(filePath);
        }
        return null;
    }

    private String tryToGetRangeHeaderInsensitive(String name, Request request) {
        String header = request.getHeader(name);
        if (StringUtils.isBlank(header)) {
            header = request.getHeader(name.toLowerCase());
        }
        return header;
    }

    public void sendBodyResponse(ArtifactoryResponse response, RepoPath repoPath, String content, Request request,
            boolean expirable) throws IOException {
        // Make sure that the response is not empty
        if (content == null) {
            RuntimeException exception = new RuntimeException("Cannot send null response");
            response.sendInternalError(exception, log);
            throw exception;
        }
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        try (InputStream is = new ByteArrayInputStream(bytes)) {
            String path = repoPath.getPath();

            String originalMimeType = NamingUtils.getMimeTypeByPathAsString(path);
            //modify the returned Content-Type header if the request coming from a browser
            String modifiedMimeType = getModifiedMimeType(repoPath.getPath(), repoPath.getRepoKey(), request);
            String mimeType = modifiedMimeType != null ? modifiedMimeType : originalMimeType;
            response.setContentType(mimeType);
            int bodySize = bytes.length;
            response.setLastModified(System.currentTimeMillis());
            AccessLogger.downloaded(repoPath);
            // Try to get range header
            String rangesString = tryToGetRangeHeaderInsensitive(HttpHeaders.RANGE, request);
            // Create range aware data for the response
            // If-Range is not supported
            RangeAwareContext context = createRangeAwareContext(is, bodySize, rangesString, null, mimeType, -1, null);
            // If request range not satisfiable update response status end return
            if (context.getStatus() == HttpStatus.SC_REQUESTED_RANGE_NOT_SATISFIABLE) {
                response.setHeader(HttpHeaders.CONTENT_RANGE, context.getContentRange());
                response.setStatus(context.getStatus());
                return;
            }
            // update content length with range aware content length
            response.setContentLength(context.getContentLength());
            // update content type with range aware content type
            response.setContentType(context.getContentType());
            // update headers with range aware headers
            if (context.getContentRange() != null) {
                response.setHeader(HttpHeaders.CONTENT_RANGE, context.getContentRange());
            }
            // Get range aware input stream
            InputStream inputStream = context.getInputStream();
            // Set response status
            if (context.getStatus() > 0) {
                response.setStatus(context.getStatus());
            }
            if (expirable) {
                noCache(response);
            }
            // Get current time for logs
            long start = System.currentTimeMillis();
            response.sendStream(inputStream);
            fireDownloadTrafficEvent(request, response, repoPath, context.getContentLength(), start);
        }
    }

    private void fireDownloadTrafficEvent(Request request, ArtifactoryResponse response, RepoPath repoPath, long size,
            long start) {
        if (!(response instanceof InternalArtifactoryResponse)) {
            String remoteAddress = HttpUtils.getRemoteClientAddress();
            String userAgent = "";
            if (isXrayUser(authorizationService.currentUsername())) {
                userAgent = getUserAgentIfXrayUser(request, authorizationService.currentUsername());
                if (trafficService.isActive() && StringUtils.isEmpty(userAgent)) {
                    log.warn("Xray user without user agent {} {} {} {}", repoPath.getId(), size, System.currentTimeMillis() - start, remoteAddress);
                }
            }
            DownloadEntry downloadEntry = new DownloadEntry(
                    repoPath.getId(), size, System.currentTimeMillis() - start, remoteAddress, userAgent);
            trafficService.handleTrafficEntry(downloadEntry);
        }
    }

    private void fireDownloadTrafficEvent(InternalRequestContext requestContext, ArtifactoryResponse response, RepoPath repoPath, long size,
            long start) {
        if (!(response instanceof InternalArtifactoryResponse)) {
            String remoteAddress = HttpUtils.getRemoteClientAddress();
            String userAgent = "";
            if (isXrayUser(authorizationService.currentUsername())) {
                userAgent = getUserAgentIfXrayUser(requestContext);
                if (trafficService.isActive() && StringUtils.isEmpty(userAgent)) {
                    log.warn("Xray user without user agent {} {} {} {}", repoPath.getId(), size, System.currentTimeMillis() - start, remoteAddress);
                    userAgent = "Xray/NA";
                }
            }
            DownloadEntry downloadEntry = new DownloadEntry(
                    repoPath.getId(), size, System.currentTimeMillis() - start, remoteAddress, userAgent);
            trafficService.handleTrafficEntry(downloadEntry);
        }
    }

    public void sendHeadResponse(ArtifactoryResponse response, RepoResource res) {
        sendHeadResponse(response, res, null);
    }

    public void sendHeadResponse(ArtifactoryResponse response, RepoResource res, ResourceStreamHandle handle) {
        log.debug("{}: Sending HEAD meta-information", res.getRepoPath());
        if (!isContentLengthSet(response)) {
            long length = (handle != null && handle.getSize() > 0) ? handle.getSize() : res.getSize();
            response.setContentLength(length);
        }
        updateResponseFromRepoResource(response, res);
        response.setContentType(res.getMimeType());
        response.sendSuccess();
    }

    public void sendNotModifiedResponse(ArtifactoryResponse response, RepoResource res) {
        log.debug("{}: Sending NOT-MODIFIED response", res.toString());
        response.setStatus(HttpStatus.SC_NOT_MODIFIED);
        sendEmptyResponse(response, res);
    }

    public void sendEmptyResponse(ArtifactoryResponse response, RepoResource res) {
        log.debug("{}: Sending EMPTY response", res.toString());
        response.setContentLength(0);
        updateResponseFromRepoResource(response, res);
        response.setContentType(res.getMimeType());
        response.flush();
    }

    public void updateResponseForProperties(ArtifactoryResponse response, RepoResource res,
            String content, MediaType mediaType, InternalRequestContext requestContext) throws IOException {
        RepoPath propsDownloadRepoPath;
        String contentType;
        if (mediaType.equals(MediaType.APPLICATION_XML)) {
            propsDownloadRepoPath = RepoPathFactory.create(res.getRepoPath().getRepoKey(),
                    res.getRepoPath().getPath() + "?" + ArtifactRestConstants.PROPERTIES_XML_PARAM);
            contentType = mediaType.getType();
        } else if (mediaType.equals(MediaType.APPLICATION_JSON)) {
            propsDownloadRepoPath = RepoPathFactory.create(res.getRepoPath().getRepoKey(),
                    res.getRepoPath().getPath() + "?" + ArtifactRestConstants.PROPERTIES_PARAM);
            contentType = MT_ITEM_PROPERTIES;
        } else {
            response.sendError(HttpStatus.SC_BAD_REQUEST, "Media Type " + mediaType + " not supported!", log);
            return;
        }
        // props generated xml and json always browsable
        setBasicHeaders(response, res, false);
        noCache(response);
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        try (InputStream is = new ByteArrayInputStream(bytes)) {
            int bodySize = bytes.length;
            AccessLogger.downloaded(propsDownloadRepoPath);
            // Try to get range header
            String rangesString = tryToGetRangeHeaderInsensitive(HttpHeaders.RANGE, requestContext.getRequest());
            String ifRangesString = tryToGetRangeHeaderInsensitive(HttpHeaders.IF_RANGE, requestContext.getRequest());
            // Get artifact last modified date
            long lastModified = res.getLastModified();
            // Get artifact sha1
            String sha1 = res.getInfo().getSha1();
            // Create range aware data for the response
            RangeAwareContext context = createRangeAwareContext(is, bodySize, rangesString, ifRangesString, contentType,
                    lastModified, sha1);
            // If request range not satisfiable update response status end return
            if (context.getStatus() == HttpStatus.SC_REQUESTED_RANGE_NOT_SATISFIABLE) {
                response.setHeader(HttpHeaders.CONTENT_RANGE, context.getContentRange());
                response.setStatus(context.getStatus());
                return;
            }
            // update content length with range aware content length
            response.setContentLength(context.getContentLength());
            // update content type with range aware content type
            response.setContentType(context.getContentType());
            // update headers with range aware headers
            if (context.getContentRange() != null) {
                response.setHeader(HttpHeaders.CONTENT_RANGE, context.getContentRange());
            }
            // Set response status
            if (context.getStatus() > 0) {
                response.setStatus(context.getStatus());
            }
            // Get range aware input stream
            InputStream inputStream = context.getInputStream();
            // Get current time for logs
            long start = System.currentTimeMillis();
            // Send stream
            response.sendStream(inputStream);
            // Fire Download traffic event
            fireDownloadTrafficEvent(requestContext, response, propsDownloadRepoPath, context.getContentLength(), start);
        }
    }

    private void updateResponseFromRepoResource(ArtifactoryResponse response, RepoResource res) {
        setBasicHeaders(response, res, contentBrowsingDisabled(res.getResponseRepoPath().getRepoKey()));
        if (res.isExpirable()) {
            noCache(response);
        }
    }

    private boolean isContentLengthSet(ArtifactoryResponse response) {
        return response.getContentLength() != -1;
    }

    private void setBasicHeaders(ArtifactoryResponse response, RepoResource res, boolean contentBrowsingDisabled) {
        response.setLastModified(res.getLastModified());
        RepoResourceInfo info = res.getInfo();
        // set the sha1 as the eTag and the sha1 header
        String sha1 = info.getSha1();
        response.setEtag(sha1);
        response.setSha1(sha1);
        response.setSha2(info.getSha2());
        response.setMd5(info.getMd5());
        response.setRangeSupport("bytes");
        if (response instanceof ArtifactoryResponseBase) {
            String fileName = info.getName();
            if (!isNotZipResource(res)) {
                // The filename is the zip entry inside the zip
                ZipEntryResource zipEntryResource = (ZipEntryResource) res;
                fileName = zipEntryResource.getEntryPath();
            }
            ((ArtifactoryResponseBase) response).setFilename(fileName);

            // content disposition is not set only for archive resources when archived browsing is enabled
            if (contentBrowsingDisabled) {
                ((ArtifactoryResponseBase) response).setContentDispositionAttachment(fileName);
            }
        }
    }

    private void noCache(ArtifactoryResponse response) {
        response.setHeader("Cache-Control", "no-store");
    }

    private boolean isNotZipResource(RepoResource res) {
        return !(res instanceof ZipEntryResource);
    }

    private boolean contentBrowsingDisabled(String repoKey) {
        boolean result = true;
        RepositoryService repositoryService = ContextHelper.get().getRepositoryService();
        RepoDescriptor repoDescriptor = repositoryService.repoDescriptorByKey(repoKey);
        if (repoDescriptor != null) {
            if (repoDescriptor instanceof RealRepoDescriptor) {
                result = !((RealRepoDescriptor) repoDescriptor).isArchiveBrowsingEnabled();
            }
        }
        // We return true by default if we couldn't get the flag from the descriptor
        return result;
    }

    private String getUserAgentIfXrayUser(InternalRequestContext requestContext) {
        boolean isXrayUser = isXrayUser(authorizationService.currentUsername());
        if (!isXrayUser) {
            return "";
        }

        if (requestContext instanceof NullRequestContext) {
            if (trafficService.isActive()) {
                log.info("Xray user with NullRequestContext");
            }
            return "";
        }

        Request request = requestContext.getRequest();
        if (request == null) {
            return "";
        }

        String userAgent = request.getHeader("User-Agent");
        if (trafficService.isActive() && StringUtils.isEmpty(userAgent)) {
            log.info("Xray user without User-Agent");
        }

        return userAgent;
    }

    public static String getUserAgentIfXrayUser(Request request, String userName) {
        boolean isXrayUser = isXrayUser(userName);
        String userAgent = request.getHeader("User-Agent");
        if (isXrayUser && StringUtils.isEmpty(userAgent)) {
            log.info("Xray user without User-Agent");
        }
        return isXrayUser ? userAgent : "";
    }

    public static boolean isXrayUser(String userName) {
        return StringUtils.isNotBlank(userName) &&
                ("xray".equalsIgnoreCase(userName) ||
                        StringUtils.startsWithIgnoreCase(userName, "token:" + ServiceType.XRAY));
    }
}