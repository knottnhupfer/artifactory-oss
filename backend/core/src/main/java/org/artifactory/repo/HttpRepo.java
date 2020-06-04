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

package org.artifactory.repo;

import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.LayoutsCoreAddon;
import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.addon.plugin.PluginsAddon;
import org.artifactory.addon.plugin.RemoteRequestCtx;
import org.artifactory.addon.plugin.download.AfterRemoteDownloadAction;
import org.artifactory.addon.plugin.download.BeforeRemoteDownloadAction;
import org.artifactory.addon.smartrepo.EdgeSmartRepoAddon;
import org.artifactory.addon.webstart.ArtifactWebstartAddon;
import org.artifactory.addon.webstart.KeyStoreNotFoundException;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.ResearchService;
import org.artifactory.api.request.InternalArtifactoryRequest;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.crypto.CryptoHelper;
import org.artifactory.descriptor.delegation.ContentSynchronisation;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.exceptions.IllegalUrlPathException;
import org.artifactory.fs.RepoResource;
import org.artifactory.io.NullResourceStreamHandle;
import org.artifactory.io.RemoteResourceStreamHandle;
import org.artifactory.md.Properties;
import org.artifactory.mime.MavenNaming;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.config.RepoConfigDefaultValues;
import org.artifactory.repo.config.RepoConfigDefaultValues.DefaultUrl;
import org.artifactory.repo.http.CloseableHttpExecute;
import org.artifactory.repo.http.research.HttpRepoResearchRequestResponseInterceptor;
import org.artifactory.repo.remote.browse.*;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.request.*;
import org.artifactory.resource.ResourceStreamHandle;
import org.artifactory.resource.UnfoundRepoResource;
import org.artifactory.rest.exception.ForbiddenException;
import org.artifactory.traffic.TrafficService;
import org.artifactory.util.CollectionUtils;
import org.artifactory.util.HttpClientConfigurator;
import org.artifactory.util.HttpClientUtils;
import org.artifactory.util.HttpUtils;
import org.iostreams.streams.in.BandwidthMonitorInputStream;
import org.jfrog.client.http.CloseableHttpClientDecorator;
import org.jfrog.client.util.KeyStoreProvider;
import org.jfrog.client.util.KeyStoreProviderException;
import org.jfrog.client.util.KeyStoreProviderFactory;
import org.jfrog.client.util.PathUtils;
import org.jfrog.common.StreamSupportUtils;
import org.jfrog.storage.common.StorageUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.security.KeyStore;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Boolean.parseBoolean;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.http.HttpHeaders.ACCEPT_ENCODING;
import static org.artifactory.addon.smartrepo.EdgeSmartRepoAddon.ERR_MSG;
import static org.artifactory.request.ArtifactoryRequest.*;

public class HttpRepo extends RemoteRepoBase<HttpRepoDescriptor> implements CloseableHttpExecute {
    private static final Logger log = LoggerFactory.getLogger(HttpRepo.class);

    private static final String GITHUB_HOST = "github.com";
    private static final String GITHUB_API_HOST = "api.github.com";
    private static final String GITHUB_RAW_HOST = "raw.githubusercontent.com";
    private static final boolean REQUEST_SENT_RETRY_ENABLED = false;
    private static final int RETRY_COUNT = 1;

    @Nullable
    private CloseableHttpClient client;
    private RemoteRepositoryBrowser remoteBrowser;
    private LayoutsCoreAddon layoutsCoreAddon;
    private AddonsManager addonsManager;
    private final ResearchService researchService;
    private boolean handleGzipResponse;

    @GuardedBy("offlineCheckerSync")
    private Thread onlineMonitorThread;
    private Object onlineMonitorSync = new Object();
    private Boolean s3Repository;

    /**
     * This state knows whether this repo's upstream is:
     *  - Another Artifactory instance (true)
     *  - Not another Artifactory instance (false)
     *  - Not checked yet (null);
     *  Any mechanism relying on this state must access it via {@link #isUpstreamArtifactory}
     *  DO NOT MODIFY THIS BOOLEAN OUTSIDE THE SCOPE OF {@link #isUpstreamArtifactory}
     */
    @SuppressWarnings("squid:S3077") //volatile required for proper double-locking, object inner state is atomic.
    private volatile AtomicBoolean artifactoryUpstream = null;
    private final Object artifactoryUpstreamLock = new Object();
    //These are urls the user specified to blacklist from property sync requests.
    private final Set<String> smartBlacklistUrls = new HashSet<>();
    private final Set<String> defaultUrls = new HashSet<>();

    public HttpRepo(HttpRepoDescriptor descriptor, InternalRepositoryService repositoryService, AddonsManager addonsManager,
            ResearchService researchService, boolean globalOfflineMode, RemoteRepo oldRemoteRepo) {
        super(descriptor, repositoryService, globalOfflineMode, oldRemoteRepo);
        this.researchService = researchService;
        this.addonsManager = addonsManager;
        layoutsCoreAddon = addonsManager.addonByType(LayoutsCoreAddon.class);
    }

    @Override
    public void close() {
        if (client instanceof CloseableHttpClientDecorator) {
            ((CloseableHttpClientDecorator)client).onClose();
        }
    }


    @Override
    public void init() {
        super.init();
        // TODO: This flag should be in the remote repo descriptor
        handleGzipResponse = ConstantValues.httpAcceptEncodingGzip.getBoolean();
        if (!isOffline()) {
            this.client = createHttpClient(ConstantValues.httpClientMaxTotalConnections.getInt(), isEnableTokenAuthentication());
        }
        initDefaultUrlsLists();
    }

    void initDefaultUrlsLists() {
        defaultUrls.addAll(RepoConfigDefaultValues.DEFAULT_URLS_LIST);
        //Add http:// endpoint for every https:// one
        defaultUrls.addAll(defaultUrls.stream()
                .filter(url -> url.startsWith("https://"))
                .map(url -> url.replace("https://", "http://"))
                .collect(Collectors.toSet()));
        smartBlacklistUrls.addAll(Arrays.stream(ConstantValues.syncPropertiesBlacklistUrls.getString().split(","))
                .filter(StringUtils::isNotBlank)
                .map(url -> url.replace("\"", ""))
                .collect(Collectors.toSet()));
    }

    private synchronized void initRemoteRepositoryBrowser() {
        if (remoteBrowser != null) {
            return; // already initialized
        }
        HttpExecutor clientExec = this::executeMethod;
        s3Repository = S3RepositoryBrowser.isS3Repository(getUrl(), getHttpClient());
        if (s3Repository) {
            log.debug("Repository {} caches S3 repository", getKey());
            remoteBrowser = new S3RepositoryBrowser(clientExec, this);
        } else {
            remoteBrowser = new HtmlRepositoryBrowser(clientExec);
        }
    }

    @Override
    public void destroy() {
        cleanupResources();
        super.destroy();
        if (client != null) {
            IOUtils.closeQuietly(client);
        }
    }

    @Override
    public void cleanupResources() {
        stopOfflineCheckThread();
    }

    public String getUsername() {
        return getDescriptor().getUsername();
    }

    public String getPassword() {
        return getDescriptor().getPassword();
    }

    public boolean isAllowAnyHostAuth() {
        return getDescriptor().isAllowAnyHostAuth();
    }

    public boolean isEnableTokenAuthentication() {
        return getDescriptor().isEnableTokenAuthentication();
    }

    public boolean isEnableCookieManagement() {
        return getDescriptor().isEnableCookieManagement();
    }

    public int getSocketTimeoutMillis() {
        return getDescriptor().getSocketTimeoutMillis();
    }

    public String getLocalAddress() {
        return getDescriptor().getLocalAddress();
    }

    public ProxyDescriptor getProxy() {
        return getDescriptor().getProxy();
    }

    @Override
    public ResourceStreamHandle conditionalRetrieveResource(String relPath, boolean forceRemoteDownload)
            throws IOException {
        //repo1 does not respect conditional get so the following is irrelevant for now.
        /*
        Date modifiedSince;
        if (modifiedSince != null) {
            //Add the if modified since
            String formattedDate = DateUtil.formatDate(modifiedSince);
            method.setRequestHeader("If-Modified-Since", formattedDate);
        }
        if (statusCode == HttpStatus.SC_NOT_MODIFIED) {
            return new NullResourceStreamHandle();
        }
        */
        //Need to do a conditional get by hand - testing a head result last modified date against the current file
        if (!forceRemoteDownload && isStoreArtifactsLocally()) {
            RepoResource cachedResource = getCachedResource(relPath);
            if (cachedResource.isFound()) {
                if (cachedResource.isExpired()) {
                    //Send HEAD
                    RepoResource resource = retrieveInfo(relPath, false/*The relPath refers to files (.gz)*/, null);
                    if (resource.isFound()) {
                        if (cachedResource.getLastModified() > resource.getLastModified()) {
                            return new NullResourceStreamHandle();
                        }
                    }
                } else {
                    return new NullResourceStreamHandle();
                }
            }
        }
        //Do GET
        return downloadResource(relPath);
    }

    @Override
    public ResourceStreamHandle downloadResource(String relPath) throws IOException {
        return downloadResource(relPath, new NullRequestContext(getRepoPath(relPath)));
    }

    @Override
    public ResourceStreamHandle downloadResource(final String relPath, final RequestContext requestContext)
            throws IOException {
        assert !isOffline() : "Should never be called in offline mode";
        String pathForUrl = convertRequestPathIfNeeded(relPath);
        if (!relPath.equals(pathForUrl)) {
            RepoRequests.logToContext("Remote resource path was translated (%s) due to repository " +
                    "layout differences", pathForUrl);
        }

        Request request = requestContext.getRequest();
        pathForUrl = resolvePathForUrl(pathForUrl, request);

        RepoRequests.logToContext("Appending matrix params to remote request URL");
        pathForUrl += buildRequestMatrixParams(requestContext.getProperties());
        final String fullUrl = appendAndGetUrl(convertRequestPathIfNeeded(pathForUrl), requestContext);
        String encodedUrl = encodeIfNeeded(fullUrl, requestContext);
        String urlWithParams = addQueryParams(encodedUrl, requestContext);

        RepoRequests.logToContext("Using remote request URL - %s", urlWithParams);
        final PluginsAddon pluginAddon = addonsManager.addonByType(PluginsAddon.class);

        final RepoPath repoPath = InternalRepoPathFactory.create(getKey(), pathForUrl);
        final Request requestForPlugins = requestContext.getRequest();
        RepoRequests.logToContext("Executing any BeforeRemoteDownload user plugins that may exist");
        RemoteRequestCtx remoteRequestCtx = new RemoteRequestCtx();
        pluginAddon.execPluginActions(BeforeRemoteDownloadAction.class, remoteRequestCtx, requestForPlugins, repoPath);
        HttpGet method = new HttpGet(urlWithParams);
        HeadersMultiMap headers = whiteListHeaders(requestContext)
                .updateHeaders(remoteRequestCtx.getHeaders());
        notifyInterceptorsOnBeforeRemoteHttpMethodExecution(method, request, headers, RepoPathFactory.create(repoPath.getRepoKey(), relPath));
        if (log.isTraceEnabled()) {
            RepoRequests.logToContext("Executing GET request to %s  {Accept [ %s ]}",
                    urlWithParams, headers.getAcceptHeadersMediaTypeWithoutOptions().collect(Collectors.toSet()));
        } else {
            RepoRequests.logToContext("Executing GET request to %s", urlWithParams);
        }
        final CloseableHttpResponse response = executeMethod(method, headers);

        StatusLine statusLine = response.getStatusLine();
        int statusCode = statusLine.getStatusCode();
        if (statusCode == HttpStatus.SC_NOT_FOUND) {
            //Not found
            IOUtils.closeQuietly(response);
            RepoRequests.logToContext("Received response status %s - throwing exception", statusLine);
            throw new RemoteRequestException("Unable to find " + urlWithParams, statusCode, statusLine.getReasonPhrase());
        }
        if (statusCode != HttpStatus.SC_OK) {
            IOUtils.closeQuietly(response);
            RepoRequests.logToContext("Received response status %s - throwing exception", statusLine);
            throw new RemoteRequestException("Error fetching " + urlWithParams, statusCode, statusLine.getReasonPhrase());
        }
        blockMismatchingMimeTypes(requestContext.getRequest(), response, fullUrl);
        //Found
        long contentLength = HttpUtils.getContentLength(response);
        logDownloading(urlWithParams, contentLength);
        RepoRequests.logToContext("Downloading content");

        final InputStream is = response.getEntity().getContent();
        verifyContentEncoding(response);
        return new TrafficAwareRemoteResourceStreamHandle(response, urlWithParams, requestForPlugins, pluginAddon,
                repoPath, is);
    }

    private String encodeIfNeeded(String fullUrl, RequestContext requestContext) {
        boolean skipEncoding = false;
        if (requestContext != null) {
            String skipEncodingParam = requestContext.getRequest().getParameter(ArtifactoryRequest.PARAM_SKIP_ENCODING);
            skipEncoding = isNotBlank(skipEncodingParam) && Boolean.parseBoolean(skipEncodingParam);
        }

        String url = skipEncoding ? fullUrl : HttpUtils.encodeUrl(fullUrl);
        if (StringUtils.contains(url, "+")) {
            if (s3Repository == null) {
                initRemoteRepositoryBrowser();
            }
            if (s3Repository) {
                url = url.replace("+", "%2B");
            }
        }
        return url;
    }

    /**
     * Collects InternalArtifactoryRequest headers (explicitly allowed for delegation) to the remote server,
     *
     * @see org.artifactory.api.request.InternalArtifactoryRequest#getDelegationAllowedHeaders()
     */
    private HeadersMultiMap whiteListHeaders(RequestContext requestContext) {
        Request request = requestContext.getRequest();
        HeadersMultiMap headers =  new HeadersMultiMap();
        if (request instanceof InternalArtifactoryRequest) {
            ((InternalArtifactoryRequest) request).getDelegationAllowedHeaders().forEach(
                    key->headers.addAll(key, requestContext.getRequest().getHeaderValues(key)));
        }
        return headers;
    }

    private void verifyContentEncoding(HttpResponse response) throws IOException {
        if (!ConstantValues.httpAcceptEncodingGzip.getBoolean() && response.getEntity() != null) {
            Header[] contentEncodings = response.getHeaders(HttpHeaders.CONTENT_ENCODING);
            for (Header contentEncoding : contentEncodings) {
                if ("gzip".equalsIgnoreCase(contentEncoding.getValue())) {
                    throw new IOException("Received gzip encoded stream while gzip compressions is disabled");
                }
            }
        }
    }

    private void logDownloading(String fullUrl, long contentLength) {
        if (NamingUtils.isChecksum(fullUrl)) {
            log.debug("{} downloading {} {} ", this, fullUrl,
                    contentLength >= 0 ? StorageUnit.toReadableString(contentLength) : "Unknown content length");
        } else {
            log.info("{} downloading {} {} ", this, fullUrl,
                    contentLength >= 0 ? StorageUnit.toReadableString(contentLength) : "Unknown content length");
        }
    }

    private void logDownloaded(String fullUrl, int status, BandwidthMonitorInputStream bmis) {
        String statusMsg = status == 200 ? "" : "status='" + (status > 0 ? status : "unknown") + "' ";
        String summary =
                statusMsg + StorageUnit.toReadableString(bmis.getTotalBytesRead()) + " at " +
                        StorageUnit.format(StorageUnit.KB.fromBytes(bmis.getBytesPerSec())) + " KB/sec";
        if (NamingUtils.isChecksum(fullUrl)) {
            log.debug("{} downloaded  {} {}", this, fullUrl, summary);
        } else {
            log.info("{} downloaded  {} {}", this, fullUrl, summary);
        }
    }

    /**
     * Executes an HTTP method using the repository client and returns the http response. This method allows to override
     * some of the default headers, note that the ARTIFACTORY_ORIGINATED, the ORIGIN_ARTIFACTORY and the ACCEPT_ENCODING
     * can't be overridden The caller to this class is responsible to close the response.
     *
     * @param method       Method to execute
     * @param extraHeaders Extra headers to add to the remote server request
     * @return The http response.
     * @throws IOException If the repository is offline or if any error occurs during the execution
     */
    public CloseableHttpResponse executeMethod(HttpRequestBase method, HeadersMultiMap extraHeaders) throws IOException {
        return this.executeMethod(method, null, extraHeaders);
    }

    /**
     * Executes an HTTP method using the repository client and returns the http response. The caller to this class is
     * responsible to close the response.
     *
     * @param method Method to execute
     * @return The http response.
     *
     * @throws IOException If the repository is offline or if any error occurs during the execution
     */
    @Override
    public CloseableHttpResponse executeMethod(HttpRequestBase method) throws IOException {
        return executeMethod(method, null, null);
    }

    /**
     * Executes an HTTP method using the repository client and returns the http response. The caller to this class is
     * responsible to close the response.
     *
     * @param method  Method to execute
     * @param context The request context for execution state
     * @return The http response.
     * @throws IOException If the repository is offline or if any error occurs during the execution
     */
    public CloseableHttpResponse executeMethod(HttpRequestBase method, @Nullable HttpContext context) throws IOException {
        return executeMethod(method, context, null);
    }

    /**
     * Executes an HTTP method using the repository client and returns the http response. This method allows to override
     * some of the default headers, note that the ARTIFACTORY_ORIGINATED, the ORIGIN_ARTIFACTORY and the ACCEPT_ENCODING
     * can't be overridden The caller to this class is responsible to close the response.
     *
     * @param method       Method to execute
     * @param context      The request context for execution state
     * @param extraHeaders Extra headers to add to the remote server request
     * @return The http response.
     * @throws IOException If the repository is offline or if any error occurs during the execution
     */
    private CloseableHttpResponse doExecuteMethod(HttpRequestBase method, @Nullable HttpContext context,
            HeadersMultiMap extraHeaders) throws IOException {
        return doExecuteMethod(method, context, extraHeaders, getHttpClient());
    }

    protected CloseableHttpResponse doExecuteMethod(HttpRequestBase method, @Nullable HttpContext context,
            HeadersMultiMap extraHeaders, CloseableHttpClient client) throws IOException {
        interceptRequestIfNotSmartRepo(getKey());
        addDefaultHeadersAndQueryParams(method, extraHeaders);
        return client.execute(method, context);
    }

    private void interceptRequestIfNotSmartRepo(String repoKey) {
        if (addonsManager.addonByType(EdgeSmartRepoAddon.class).shouldBlockNonSmartRepo(repoKey)) {
            RepoRequests.logToContext("Download denied - (%s)", ERR_MSG);
            throw new ForbiddenException(ERR_MSG);
        }
    }

    /**
     * Executes an HTTP method using the repository client and returns the http response. This method allows to override
     * some of the default headers, note that the ARTIFACTORY_ORIGINATED, the ORIGIN_ARTIFACTORY and the ACCEPT_ENCODING
     * can't be overridden The caller to this class is responsible to close the response.
     *
     * @param method       Method to execute
     * @param context      The request context for execution state
     * @param extraHeaders Extra headers to add to the remote server request
     * @return The http response.
     * @throws IOException If the repository is offline or if any error occurs during the execution
     */
    public final CloseableHttpResponse executeMethod(HttpRequestBase method, @Nullable HttpContext context,
            HeadersMultiMap extraHeaders) throws IOException {
        try {
            return interceptResponse(doExecuteMethod(method, context, extraHeaders));
        } catch (IOException e) {
            putOffline();
            throw e;
        }
    }

    @Override
    public RepoResource retrieveInfo(String path, boolean folder, @Nullable RequestContext requestContext) {
        assert !isOffline() : "Should never be called in offline mode";
        RepoPath repoPath = InternalRepoPathFactory.create(this.getKey(), path, folder);

        String fullUrl = assembleRetrieveInfoUrl(path, requestContext);
        String encodedUrl = encodeIfNeeded(fullUrl, requestContext);
        //TODO: [by yl] Ideally do this in #assembleRetrieveInfoUrl, but encodedUrl full URL brute force encoding
        //breaks pre-encoded query params
        String urlWithParams = addQueryParams(encodedUrl, requestContext);

        HttpRequestBase method;
        String methodType = "HEAD";
        if (shouldReplaceHeadWithGet(requestContext)) {
            log.debug("Param " + PARAM_REPLACE_HEAD_IN_RETRIEVE_INFO_WITH_GET + " found in request" +
                    " context, switching HEAD with GET request");
            methodType = "GET";
            method = new HttpGet(urlWithParams);
        } else {
            method = new HttpHead(urlWithParams);
        }
        RepoRequests.logToContext("Executing %s request to %s", methodType, urlWithParams);
        CloseableHttpResponse response = null;
        try {
            HttpClientContext httpClientContext = new HttpClientContext();
            HeadersMultiMap headers = new HeadersMultiMap();
            // todo - we would like to have the Head use same headers as the actual request.
            //  HeadersMultiMap headers = whiteListHeaders(requestContext);
            Request request = requestContext != null ? requestContext.getRequest() : null;
            notifyInterceptorsOnBeforeRemoteHttpMethodExecution(method, request, headers, repoPath);
            response = executeMethod(method, httpClientContext, headers);
            return handleGetInfoResponse(repoPath, method, fullUrl, response, httpClientContext, requestContext);
        } catch (IOException e) {
            String exceptionMessage = HttpClientUtils.getFilteredErrorMessage(e);
            RepoRequests.logToContext("Failed to execute %s request: %s", methodType, exceptionMessage);
            throw new RuntimeException("Failed retrieving resource from " + urlWithParams + ": " + exceptionMessage, e);
        } finally {
            consumeStream(response);
            IOUtils.closeQuietly(response);
        }
    }

    @Override
    protected RepoResource getInfoFromCache(InternalRequestContext context) {
        return localCacheRepo.getInfo(context);
    }

    /**
     * Synchronize properties can be configured in 2 ways:
     *  - The legacy flag {@link RemoteRepoDescriptor#isSynchronizeProperties()}
     *  - Smart remote property sync {@link RemoteRepoDescriptor#getContentSynchronisation()} and {@link ContentSynchronisation#getProperties()}
     *  Even if one of these (or both) is configured we don't let outgoing property queries execute if they are directed
     *  at known public registries (denoted by {@link DefaultUrl}.
     *  An override over this mechanism is available if the user enters values in {@link ConstantValues#syncPropertiesBlacklistUrls}
     *
     *  NOTE:
     *  Ideally we would like to block property requests for any upstream that is not an Artifactory instance, like we
     *  do in smart remote checks or in {@link EdgeSmartRepoAddon} but these tests rely on the user also giving credentials
     *  to the upstream (since all endpoints are protected in case anonymous is disabled) thus there's no one good
     *  solution for this issue (for instance relying on headers is volatile since upstream my omit them).
     */
    @Override
    public boolean isSynchronizeProperties() {
        HttpRepoDescriptor descriptor = getDescriptor();
        if (!researchService.isRepoConfiguredToSyncProperties(descriptor)) {
            //Property sync (smart remote or not) switched off in config.
            return false;
        }
        String upstreamUrl = descriptor.getUrl();
        //Ideally we'd check the upstream with the smart remote checks but they need auth (or anon access enabled)
        //so we're going with a blacklist for now until a better idea comes up.
        if (!smartBlacklistUrls.isEmpty()) {
            //TODO [by dan]: perhaps this should be a contains() check and not equals..?
            return !smartBlacklistUrls.contains(upstreamUrl);
        }
        //If no urls given in the blacklist override we block property sync if this repo's url is the default one
        //TODO [by dan]: perhaps this should be a contains() check and not equals..?
        if (defaultUrls.contains(upstreamUrl)) {
            if (log.isDebugEnabled()) {
                log.debug("Default url detected for repo '{}', sync properties disabled.", descriptor.getKey());
            }
            return false;
        }
        return true;
    }

    /**
     * The checks made here rely on :
     * - The upstream version and license endpoints are available (so either anonymous is enabled on the upstream or user configured credentials)
     * - The upstream as at least a pro license.
     */
    public boolean isUpstreamArtifactory() {
        if (artifactoryUpstream == null) {
            synchronized (artifactoryUpstreamLock) {
                if (artifactoryUpstream == null) {
                    artifactoryUpstream = new AtomicBoolean(researchService.isSmartRemote(getDescriptor()));
                }
            }
        }
        return artifactoryUpstream.get();
    }

    private boolean shouldReplaceHeadWithGet(@Nullable RequestContext requestContext) {
        boolean replaceHeadWithGet = false;
        if (requestContext != null && requestContext.getRequest() != null) {
            replaceHeadWithGet = parseBoolean(requestContext.getRequest().getParameter(PARAM_REPLACE_HEAD_IN_RETRIEVE_INFO_WITH_GET));
        }
        return replaceHeadWithGet || getDescriptor().isBypassHeadRequests();
    }

    private void consumeStream(CloseableHttpResponse response) {
        double maxSizeToConsume  = StorageUnit.MB.
                toBytes(ConstantValues.remoteDownloadInVainConsumeLimitInMegaBytes.getInt());
        if (response != null && response.getEntity() != null) {
            if (response.getEntity().getContentLength() <= maxSizeToConsume) {
                EntityUtils.consumeQuietly(response.getEntity());
            }
        }
    }

    private String addQueryParams(String fullUrl, @Nullable RequestContext context) {
        if (context == null) {
            return fullUrl;
        }
        if (getDescriptor().isPropagateQueryParams()) {
            RepoRequests.logToContext("Appending query params to remote request URL");
            //TODO: [by YL] support multival params
            Map<String, String[]> params = context.getRequest().getParameters();
            String queryParamsString = params.entrySet().stream()
                    .map(e -> e.getKey() + "=" + (e.getValue() != null ? urlEncode(e.getValue()[0]) : ""))
                    .collect(Collectors.joining("&", "?", ""));
            fullUrl += queryParamsString;
        }
        return fullUrl;
    }

    protected String assembleRetrieveInfoUrl(String path, RequestContext context) {
        String pathForUrl = convertRequestPathIfNeeded(path);
        if (!path.equals(pathForUrl)) {
            RepoRequests.logToContext("Remote resource path was translated (%s) due to repository " +
                    "layout differences", pathForUrl);
        }
        boolean validContext = context != null;
        if (validContext) {
            Request request = context.getRequest();
            pathForUrl = resolvePathForUrl(pathForUrl, request);
        } else {
            validatePathAllowed(pathForUrl);
        }

        String fullUrl = appendAndGetUrl(pathForUrl, context);
        if (validContext) {
            RepoRequests.logToContext("Appending matrix params to remote request URL");
            Properties properties = context.getProperties();
            fullUrl += buildRequestMatrixParams(properties);
        }
        RepoRequests.logToContext("Using remote request URL - %s", fullUrl);
        return fullUrl;
    }

    String resolvePathForUrl(String pathForUrl, Request request) {
        if (request != null) {
            String alternativeRemoteDownloadUrl =
                    request.getParameter(ArtifactoryRequest.PARAM_ALTERNATIVE_REMOTE_DOWNLOAD_URL);
            if (isNotBlank(alternativeRemoteDownloadUrl)) {
                if (isAllowedQueryParamRewrite(request)) {
                    RepoRequests.logToContext("Request contains alternative remote resource path ({}=%s)",
                            ArtifactoryRequest.PARAM_ALTERNATIVE_REMOTE_DOWNLOAD_URL, alternativeRemoteDownloadUrl);
                    return alternativeRemoteDownloadUrl;
                } else {
                    log.warn("Alternative remote download url {} ignored", alternativeRemoteDownloadUrl);
                }
            }
        }
        validatePathAllowed(pathForUrl);
        return pathForUrl;
    }

    /**
     * Notice: for use with HEAD method, no content is expected in the response. Process the remote repository's
     * response and construct a repository resource.
     *
     * @param repoPath       of requested resource
     * @param method         executed {@link org.apache.http.client.methods.HttpHead} from which to process the
     *                       response.
     * @param downloadUrl    Original download url (not encoded, without url params) for mime type verification
     * @param response       The response to the get info request
     */
    protected RepoResource handleGetInfoResponse(RepoPath repoPath, HttpRequestBase method, String downloadUrl,
            CloseableHttpResponse response, @Nullable HttpClientContext context, RequestContext requestContext) {
        int statusCode = response.getStatusLine().getStatusCode();

        if (shouldReturnUnfoundResource(repoPath, response, context, requestContext, statusCode)) {
            return new UnfoundRepoResource(repoPath, response.getStatusLine().getReasonPhrase());
        }

        long contentLength = HttpUtils.getContentLength(response);
        if (contentLength != -1) {
            RepoRequests.logToContext("Found remote resource with content length - %s", contentLength);
        }

        // if status is 204 and length is not 0 then the remote server is doing something wrong
        if (statusCode == HttpStatus.SC_NO_CONTENT && contentLength > 0) {
            // send back unfound resource with 404 status
            RepoRequests.logToContext("Received status {} (message: %s) on remote info request - returning unfound " +
                    "resource", statusCode, response.getStatusLine().getReasonPhrase());
            return new UnfoundRepoResource(repoPath, response.getStatusLine().getReasonPhrase());
        }
        try {
            blockMismatchingMimeTypes(requestContext != null ? requestContext.getRequest() : null, response, downloadUrl);
        } catch (RemoteRequestException rre) {
            log.debug("", rre);
            //Mismatching mime types detected and blocked
            return addRejectedResourceToMissedCache(repoPath, rre.getMessage(), rre.getRemoteReturnCode());
        }
        Set<ChecksumInfo> checksums = HttpUtils.getChecksums(response);
        if (!ConstantValues.saveGetResource.getBoolean()) {
            retrieveChecksumsFromInterceptors(repoPath, response, checksums);
        }

        String originalPath = repoPath.getPath();
        String filename = getFilename(method, originalPath);
        if (isNotBlank(filename)) {
            RepoRequests.logToContext("Found remote resource with filename header - %s", filename);
            if (NamingUtils.isMetadata(originalPath)) {
                String originalPathStrippedOfMetadata = NamingUtils.getMetadataParentPath(originalPath);
                String originalPathWithMetadataNameFromHeader =
                        NamingUtils.getMetadataPath(originalPathStrippedOfMetadata, filename);
                repoPath = InternalRepoPathFactory.create(repoPath.getRepoKey(),
                        originalPathWithMetadataNameFromHeader);
            } else {
                repoPath = InternalRepoPathFactory.create(repoPath.getParent(), filename);
            }
        }

        return createRemoteResourceFromResponse(repoPath, response, checksums);
    }

    private void validatePathAllowed(String path) {
        if (HttpUtils.isValidUrl(path)) {
            log.warn("Download URL contains forbidden redirect to: {}", path);
            throw new IllegalUrlPathException("Download URL contains forbidden redirect to: " + path);
        }
        log.trace("Valid path, doesn't contains URL {}", path);
    }

    private boolean shouldReturnUnfoundResource(RepoPath repoPath, CloseableHttpResponse response,
            @Nullable HttpClientContext context, RequestContext requestContext, int statusCode) {
        if (statusCode == HttpStatus.SC_NOT_FOUND) {
            RepoRequests.logToContext("Received status 404 (message: %s) on remote info request - " +
                    "returning unfound resource", response.getStatusLine().getReasonPhrase());
            return true;
        }

        if (!isDisableFolderRedirectAssertion(requestContext) && isRedirectToFolder(context)) {
            log.error("Remote info request was redirected to a directory. Repo: {} Path: {}", repoPath.getRepoKey(),
                    repoPath.getPath());
            return true;
        }

        // Some servers may return 204 instead of 200
        if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_NO_CONTENT) {
            RepoRequests.logToContext("Received status {} (message: %s) on remote info request - " +
                    "returning unfound resource", statusCode, response.getStatusLine().getReasonPhrase());
            // send back unfound resource with 404 status
            return true;
        }
        return false;
    }

    private boolean isDisableFolderRedirectAssertion(RequestContext context) {
        if (context != null) {
            String disableFolderRedirectAssertion = context.getRequest().getParameter(PARAM_FOLDER_REDIRECT_ASSERTION);
            if (isNotBlank(disableFolderRedirectAssertion) && Boolean.parseBoolean(disableFolderRedirectAssertion)) {
                // Do not perform in case of parameter provided
                RepoRequests.logToContext("Folder redirect assertion is disabled for internal download request");
                return true;
            }
        }
        return false;
    }

    private boolean isRedirectToFolder(HttpClientContext context) {
        if (context != null) {
            // if redirected, check that the last redirect URL doesn't end with '/' (we assume those are directory paths)
            List<URI> redirects = context.getRedirectLocations();
            if (CollectionUtils.notNullOrEmpty(redirects)) {
                URI lastDestination = redirects.get(redirects.size() - 1);
                return lastDestination != null && lastDestination.getPath().endsWith("/");
            }
        }
        return false;
    }

    protected final CloseableHttpClient createHttpClient(int maxTotalConnections , boolean isEnableTokenAuthentication) {
        String password = CryptoHelper.decryptIfNeeded(ArtifactoryHome.get(), getPassword());
        HttpClientConfigurator configurator = new HttpClientConfigurator();
        HttpRepoResearchRequestResponseInterceptor requestResponseInterceptor = new HttpRepoResearchRequestResponseInterceptor(
                getKey());
        configurator.handleGzipResponse(handleGzipResponse)
                .hostFromUrl(getUrl())
                .connectionTimeout(getSocketTimeoutMillis())
                .socketTimeout(getSocketTimeoutMillis())
                .retry(RETRY_COUNT, REQUEST_SENT_RETRY_ENABLED)
                .localAddress(getLocalAddress())
                .authentication(getUsername(), password, isAllowAnyHostAuth(), addAdditionalAuthScopes())
                .redirectStrategy(new JFrogRedirectStrategy())
                .enableCookieManagement(isEnableCookieManagement())
                .addRequestInterceptor(requestResponseInterceptor)
                .addResponseInterceptor(requestResponseInterceptor);
        String certificateToUse = getDescriptor().getClientTlsCertificate();
        if (isNotBlank(certificateToUse)) {
            KeyStoreProvider keyStoreProvider = getKeyStoreProvider();
            if (keyStoreProvider != null) {
                configurator.clientCertKeyStoreProvider(keyStoreProvider);
                configurator.clientCertAlias(ArtifactWebstartAddon.SSL_CERT_ALIAS_PREFIX + certificateToUse);
            }
        }
        return configurator
                .maxTotalConnections(maxTotalConnections)
                .enableTokenAuthentication(isEnableTokenAuthentication, getKey(), null)
                .handleGzipResponse(handleGzipResponse).proxy(getProxy())
                .registerMBean(getKey()).build();
    }

    private List<String> addAdditionalAuthScopes() {
        String url = getUrl();
        if (isNotBlank(url)) {
            try {
                if (GITHUB_HOST.equals(new URL(url).getHost())) {
                    return Lists.newArrayList(GITHUB_API_HOST, GITHUB_RAW_HOST);
                }
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Cannot parse the url " + url, e);
            }
        }
        return Lists.newArrayList();
    }

    private KeyStoreProvider getKeyStoreProvider() {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        ArtifactWebstartAddon webstartAddon = addonsManager.addonByType(ArtifactWebstartAddon.class);
        KeyStore existingKeyStore;
        try {
            existingKeyStore = webstartAddon.getExistingKeyStore();
        } catch (KeyStoreNotFoundException e) {
            return null;
        }
        String keystorePass = webstartAddon.getKeystorePassword();
        if (existingKeyStore != null) {
            try {
                return KeyStoreProviderFactory.getProvider(existingKeyStore, keystorePass);
            } catch (KeyStoreProviderException e) {
                log.warn("Cannot set client certificate on repo. {}", e.getMessage());
                log.debug("Cannot set client certificate on repo.", e);
            }
        }
        return null;
    }

    private RepoResource getCachedResource(String relPath) {
        LocalCacheRepo cache = getLocalCacheRepo();
        final NullRequestContext context = new NullRequestContext(getRepoPath(relPath));
        return requireNonNull(cache).getInfo(context);
    }

    /**
     * Adds default headers and extra headers to the HttpRequest method The extra headers are unique headers that should
     * be added to the remote server request according to special requirement example : user adds extra headers through
     * the User Plugin (BeforeRemoteDownloadAction)
     *
     * @param method       Method to execute
     * @param extraHeaders Extra headers to add to the remote server request
     */
    @SuppressWarnings({"deprecation"})
    private void addDefaultHeadersAndQueryParams(HttpRequestBase method, HeadersMultiMap extraHeaders) {
        //Explicitly force keep alive
        method.setHeader(HttpHeaders.CONNECTION, "Keep-Alive");

        //Add the current requester host id
        Set<String> originatedHeaders = RepoRequests.getOriginatedHeaders();
        for (String originatedHeader : originatedHeaders) {
            method.addHeader(ARTIFACTORY_ORIGINATED, originatedHeader);
        }
        String hostId = ContextHelper.get().beanForType(AddonsManager.class).addonByType(
                HaCommonAddon.class).getHostId();
        // Add the extra headers to the remote request
        if (extraHeaders != null) {
            for (Map.Entry<String, String> entry : extraHeaders.entries()) {
                boolean isReservedKey = ARTIFACTORY_ORIGINATED.equals(entry.getKey()) ||
                        ORIGIN_ARTIFACTORY.equals(entry.getKey()) ||
                        ACCEPT_ENCODING.equals(entry.getKey());
                if (!isReservedKey) {
                    method.addHeader(entry.getKey(), entry.getValue());
                }
            }
        }
        // Add the default artifactory headers, those headers will always override the existing headers if they already exist
        method.addHeader(ARTIFACTORY_ORIGINATED, hostId);

        //For backwards compatibility
        method.setHeader(ORIGIN_ARTIFACTORY, hostId);

        //Set gzip encoding
        if (handleGzipResponse) {
            method.addHeader(ACCEPT_ENCODING, "gzip");
        }

        // Set custom query params
        String queryParams = getDescriptor().getQueryParams();
        if (isNotBlank(queryParams)) {
            String url = method.getURI().toString();
            if (url.contains("?")) {
                url += "&";
            } else {
                url += "?";
            }
            url += HttpUtils.encodeQuery(queryParams);
            method.setURI(URI.create(url));
        }
    }

    private String getFilename(HttpRequestBase method, String originalPath) {
        // Skip filename parsing if we are not dealing with latest maven non-unique snapshot request
        if (!isRequestForLatestMavenSnapshot(originalPath)) {
            return null;
        }

        // Try our custom X-Artifactory-Filename header
        Header filenameHeader = method.getFirstHeader(ArtifactoryRequest.FILE_NAME);
        if (filenameHeader != null) {
            String filenameString = filenameHeader.getValue();
            try {
                return URLDecoder.decode(filenameString, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                log.warn("Unable to decode '{}' header '{}', returning un-encoded value.",
                        ArtifactoryRequest.FILE_NAME, filenameString);
                return filenameString;
            }
        }

        // Didn't find any filename, return null
        return null;
    }

    private boolean isRequestForLatestMavenSnapshot(String originalPath) {
        if (ConstantValues.requestDisableVersionTokens.getBoolean()) {
            return false;
        }

        if (!getDescriptor().isMavenRepoLayout()) {
            return false;
        }

        return MavenNaming.isNonUniqueSnapshot(originalPath);
    }

    @Override
    protected List<RemoteItem> getChildUrls(String dirUrl) throws IOException {
        if (remoteBrowser == null) {
            initRemoteRepositoryBrowser();
        }
        return remoteBrowser.listContent(dirUrl);
    }

    /**
     * Converts the given path to the remote repo's layout if defined
     *
     * @param path Path to convert
     * @return Converted path if required and conversion was successful, given path if not
     */
    public String convertRequestPathIfNeeded(String path) {
        HttpRepoDescriptor descriptor = getDescriptor();
        return layoutsCoreAddon.translateArtifactPath(descriptor.getRepoLayout(), descriptor.getRemoteRepoLayout(),
                path);
    }

    @Override
    protected void putOffline() {
        long assumedOfflinePeriodSecs = getDescriptor().getAssumedOfflinePeriodSecs();
        if (assumedOfflinePeriodSecs <= 0) {
            return;
        }
        // schedule the offline thread to run immediately
        //scheduler.schedule(new OfflineCheckCallable(), 0, TimeUnit.MILLISECONDS);
        synchronized (onlineMonitorSync) {
            if (onlineMonitorThread != null) {
                if (onlineMonitorThread.isAlive()) {
                    return;
                }
                log.debug("Online monitor thread exists but not alive");
            }
            onlineMonitorThread = new Thread(new OnlineMonitorRunnable(), "online-monitor-" + getKey());
            onlineMonitorThread.setDaemon(true);
            log.debug("Online monitor starting {}", onlineMonitorThread.getName());
            onlineMonitorThread.start();
        }
    }

    @Override
    public void resetAssumedOffline() {
        synchronized (onlineMonitorSync) {
            log.info("Resetting assumed offline status");
            stopOfflineCheckThread();
            assumedOffline.getAndSet(false);
        }
    }

    private void stopOfflineCheckThread() {
        synchronized (onlineMonitorSync) {
            if (onlineMonitorThread != null) {
                log.debug("Online monitor stopping {}", onlineMonitorThread.getName());
                onlineMonitorThread.interrupt();
                onlineMonitorThread = null;
            }
        }
    }

    protected CloseableHttpClient getHttpClient() {
        if (client == null) {
            throw new IllegalStateException("Repo is offline. Cannot use the HTTP client.");
        }
        return client;
    }

    private String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("Failed to url encode: " + s, e);
            return s;
        }
    }

    /**
     * If this repo has the 'block mismatching mimetypes' flag lit and the request passed along the expected mime type,
     * verify that the returned mime type matches the expected one.  If no header was passed an attempt is made
     * to match both types by the requested resource's extension instead.
     *
     * If there's a mismatch and the response's mime type is in the repo's block list (either default
     * {@link ConstantValues#blockedMismatchingMimeTypes} in the system properties or the repo's
     * {@link RemoteRepoDescriptor}.mismatchingMimeTypesOverrideList)
     * this method will fail the download with Exception.
     *
     * @param request      Request to fetch remote resource (whether it came internally or from a client)
     * @param response     Response from remote resource to match mime type against
     * @param downloadUrl  For logging
     */
    private void blockMismatchingMimeTypes(@Nullable Request request, CloseableHttpResponse response, String downloadUrl)
            throws RemoteRequestException {
        //Found -> check if received mime type matches the requested one
        if (!shouldVerifyMimeType(response)) {
            return;
        }
        String responseContentType = getMimeTypeFromHeader(response.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue())
                .toLowerCase();
        if(responseContentType.equals("*/*") || responseContentType.equals("application/octet-stream")) {
            log.trace("Response content type is {}: skipping mime type checks", responseContentType);
            return;
        }
        Set<String> acceptableMimeTypes = getAcceptableMimeTypes(request, downloadUrl);
        if (acceptableMimeTypes.contains("*/*") || acceptableMimeTypes.contains("application/octet-stream")) {
            log.trace("Accepts header allows everything, skipping mime type checks");
            return;
        }
        if(shouldBlockMimeType(responseContentType, acceptableMimeTypes)) {
            try {
                response.close();
            } catch (Exception e) {
                log.debug("Failed to release response stream before blocking mime type mismatch: ", e);
            }
            String err = "Error fetching " + downloadUrl + ": Mismatching mime types found in request: "
                    + acceptableMimeTypes + " and response: " + responseContentType + ", and this repo blocks "
                    + responseContentType + " on mismatch";
            RepoRequests.logToContext(err);
            throw new RemoteRequestException(err, HttpStatus.SC_CONFLICT, null);
        }
    }

    /**
     * @param request      the request that goes to the resource
     * @param downloadUrl  actual download url (if it was modified, / translated etc.), without query params
     *
     * @return a unique list of acceptable mime types for this request either by the request's 'Accepts' header
     * or based on the requested file
     */
    private Set<String> getAcceptableMimeTypes(@Nullable Request request, String downloadUrl) {
        return getAcceptableHeaders(request, downloadUrl)
                .flatMap( value -> getUniqueMimeTypeList(value).stream() )
                .collect(Collectors.toSet());
    }

    private Stream<String> getAcceptableHeaders(@Nullable Request request, String downloadUrl) {
        if (request != null) {
            Enumeration<String> acceptHeadersRaw = request.getHeaders(HttpHeaders.ACCEPT);
            if (acceptHeadersRaw.hasMoreElements()) {

                if (log.isTraceEnabled()) {
                    log.trace("Found Accepts header in request: {}", acceptHeadersRaw);
                }
                return StreamSupportUtils.enumerationToStream(acceptHeadersRaw);
            }
        }
        return Stream.of(NamingUtils.getMimeType(downloadUrl).getType());
    }

    /**
     * Mime type mismatch should be verified only if the repo requires it and the response's header is present.
     */
    private boolean shouldVerifyMimeType(CloseableHttpResponse response) {
        return getDescriptor().isBlockMismatchingMimeTypes() && response.containsHeader(HttpHeaders.CONTENT_TYPE);
    }

    /**
     *  Should block if there's a mismatch between the response's Content-Type header and the acceptable mime types list
     *  (which was derived from the request's Accepts header or the requested file's extension) and the response's
     *  returned type is in this repo's block list.
     */
    private boolean shouldBlockMimeType(String responseContentType, Set<String> acceptableMimeTypes) {
        return !acceptableMimeTypes.contains(responseContentType)
                && getRepoMismatchingMimeTypeBlockList().contains(responseContentType);
    }

    private Set<String> getRepoMismatchingMimeTypeBlockList() {
        String mimeTypeBlockList =  getDescriptor().getMismatchingMimeTypesOverrideList();
        String repoKey = getDescriptor().getKey();
        if (isNotBlank(mimeTypeBlockList)) {
            log.trace("Repo {} defines an overriding mismatching mime type block list: {}", repoKey, mimeTypeBlockList);
        } else {
            mimeTypeBlockList = ConstantValues.blockedMismatchingMimeTypes.getString();
            log.trace("Repo {} does not define an overriding mismatching mime type block list, using default: {}",
                    repoKey, mimeTypeBlockList);
        }
        return getUniqueMimeTypeList(mimeTypeBlockList);
    }

    /**
     * Accepts header can have multiple entries https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
     */
    private Set<String> getUniqueMimeTypeList(String acceptsHeader) {
        return Stream.of(acceptsHeader.split(","))                       //if multivalued - values are delimited by ','
                .map(mimeType -> mimeType.replaceFirst(";(?:.*)", ""))   //each value can have options with ';'
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    /**
     * Content-Type header can be 'X/Y; [some encoding info]'
     * @param header    Content-Type header value
     * @return          Just the mime type
     */
    private String getMimeTypeFromHeader(String header) {
        if(header.contains(";")) {
            return header.split(";")[0];
        } else {
            return header;
        }
    }

    private class OnlineMonitorRunnable implements Runnable {
        /**
         * max attempts until reaching the maximum wait time
         */
        private static final int MAX_FAILED_ATTEMPTS = 10;

        /**
         * Failed requests counter
         */
        private int failedAttempts = 0;

        @Override
        public void run() {
            log.debug("Online monitor started for {}", getKey());
            while (true) {
                try {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException();
                    }
                    if (checkOnline()) {
                        if (assumedOffline.get()) {
                            log.info("{} is back online!", getKey());
                        }
                        assumedOffline.getAndSet(false);
                        onlineMonitorThread = null;
                        return;
                    }
                    if (!assumedOffline.get()) {
                        log.info("{} is inaccessible. Setting as offline!", getKey());
                        assumedOffline.getAndSet(true);
                    }
                    long nextOnlineCheckDelay = calculateNextOnlineCheckDelay();
                    nextOnlineCheckMillis = System.currentTimeMillis() + nextOnlineCheckDelay;
                    log.debug("Online monitor sleeping for {} millis", nextOnlineCheckDelay);
                    Thread.sleep(nextOnlineCheckDelay);
                } catch (InterruptedException e) {
                    log.debug("Online monitor interrupted");
                    Thread.interrupted();
                    return;
                }
            }
        }

        private long calculateNextOnlineCheckDelay() {
            long maxFailureCacheSecs = getDescriptor().getAssumedOfflinePeriodSecs();
            long maxFailureCacheMillis = TimeUnit.SECONDS.toMillis(maxFailureCacheSecs);    // always >= 1000

            long nextOnlineCheckDelayMillis;
            failedAttempts++;
            if (failedAttempts < MAX_FAILED_ATTEMPTS) {
                if (maxFailureCacheSecs / MAX_FAILED_ATTEMPTS < 2) {
                    long failurePenaltyMillis = maxFailureCacheMillis / MAX_FAILED_ATTEMPTS;
                    nextOnlineCheckDelayMillis = failedAttempts * failurePenaltyMillis;
                } else {
                    // exponential delay
                    // calculate the base of the exponential equation based on the MAX_FAILED_ATTEMPTS and max offline period
                    // BASE pow MAX_FAILED_ATTEMPTS = MAX_DELAY ==> BASE = MAX_DELAY pow 1/MAX_FAILED_ATTEMPTS
                    double base = Math.pow(maxFailureCacheMillis, 1.0 / (double) MAX_FAILED_ATTEMPTS);
                    nextOnlineCheckDelayMillis = (long) Math.pow(base, failedAttempts);
                    // in any case don't attempt too rapidly
                    nextOnlineCheckDelayMillis = Math.max(100, nextOnlineCheckDelayMillis);
                }
            } else {
                nextOnlineCheckDelayMillis = maxFailureCacheMillis;
            }
            return nextOnlineCheckDelayMillis;
        }

        private boolean checkOnline() {
            // always test with url trailing slash
            String url = PathUtils.addTrailingSlash(getDescriptor().getUrl());
            HttpGet getMethod = new HttpGet(HttpUtils.encodeQuery(url));
            try (CloseableHttpResponse response = getHttpClient().execute(getMethod)) {
                log.debug("Online monitor checking URL: {}", url);
                StatusLine status = response.getStatusLine();
                log.debug("Online monitor http method completed with no exception: {}: {}",
                        status.getStatusCode(), status.getReasonPhrase());

                if (isResourceUnavailable(status)) { //RTFACT-6528
                    log.debug("Considering as offline due to the return code '{}'", status.getStatusCode());
                } else {
                    return true;
                }
            } catch (IllegalStateException | IOException e) {
                // IllegalStateException can be thrown while this tread in race with destroy()
                // i.e. closed client is accesses before interrupt() takes effect
                log.debug("Online monitor http method failed: {}: {}", e.getClass().getName(), e.getMessage());
            }
            return false;
        }
    }

    public class TrafficAwareRemoteResourceStreamHandle extends RemoteResourceStreamHandle {
        private final BandwidthMonitorInputStream bmis;
        private final String remoteIp;
        private CloseableHttpResponse response;
        private String fullUrl;
        private Request requestForPlugins;
        private PluginsAddon pluginAddon;
        private RepoPath repoPath;

        TrafficAwareRemoteResourceStreamHandle(CloseableHttpResponse response, String fullUrl,
                Request requestForPlugins, PluginsAddon pluginAddon, RepoPath repoPath, InputStream is) {
            this.response = response;
            this.fullUrl = fullUrl;
            this.requestForPlugins = requestForPlugins;
            this.pluginAddon = pluginAddon;
            this.repoPath = repoPath;
            this.bmis = new BandwidthMonitorInputStream(is);
            TrafficService trafficService = ContextHelper.get().beanForType(TrafficService.class);
            if (trafficService.isActive()) {
                this.remoteIp = HttpUtils.resolveResponseRemoteAddress(response);
            } else {
                this.remoteIp = StringUtils.EMPTY;
            }
        }

        public String getContentType() {
            return Arrays.stream(response.getHeaders(HttpHeaders.CONTENT_TYPE)).map(Header::getValue).findFirst().orElse("");
        }

        @Override
        public InputStream getInputStream() {
            return bmis;
        }

        @Override
        public long getSize() {
            return -1;
        }

        @Override
        public CloseableHttpResponse getResponse() {
            return response;
        }

        @Override
        public void close() {
            IOUtils.closeQuietly(bmis);
            IOUtils.closeQuietly(response);
            StatusLine statusLine = response.getStatusLine();

            Throwable throwable = getThrowable();
            if (throwable != null) {
                String exceptionMessage = HttpClientUtils.getErrorMessage(throwable);
                log.error("{}: Failed to download '{}'. Received status code {} and caught exception: {}",
                        HttpRepo.this, fullUrl, statusLine != null ? statusLine.getStatusCode() : "unknown",
                        exceptionMessage);
                String downLoadSummary =
                        StorageUnit.toReadableString(bmis.getTotalBytesRead()) + " at " +
                                StorageUnit.format(StorageUnit.KB.fromBytes(bmis.getBytesPerSec())) + " KB/sec";
                log.debug("Failed to download '{}'. Download summary: {}", fullUrl, downLoadSummary, throwable);
                RepoRequests.logToContext("Failed to download: %s", exceptionMessage);
            } else {
                int status = statusLine != null ? statusLine.getStatusCode() : 0;
                logDownloaded(fullUrl, status, bmis);
                RepoRequests.logToContext("Downloaded content");
            }
            RepoRequests.logToContext("Executing any AfterRemoteDownload user plugins that may exist");
            pluginAddon.execPluginActions(AfterRemoteDownloadAction.class, null, requestForPlugins, repoPath);
            RepoRequests.logToContext("Executed all AfterRemoteDownload user plugins");
        }

        String getRemoteIp() {
            return remoteIp;
        }
    }
}
