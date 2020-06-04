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

package org.artifactory.api.request;

import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.apache.http.HttpHeaders;
import org.apache.http.client.utils.DateUtils;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.repo.RepoPath;
import org.artifactory.request.HeadersMultiMap;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.util.*;

/**
 * An internal resource request that is sent by Artifactory itself to the DownloadService asking for a resource.
 *
 * @author Yossi Shaul
 */
public class InternalArtifactoryRequest extends ArtifactoryRequestBase {

    private boolean skipJarIndexing;
    // set this flag to true if Artifactory should mark uploaded artifacts with trusted checksums mark
    private boolean trustServerChecksums;

    private boolean forceDownloadIfNewer;

    private Boolean searchForExistingResourceOnRemoteRequest;

    private Boolean replicationDownloadRequest;

    private Boolean replicationOriginatedDownloadRequest;

    private Boolean disableFolderRedirectAssertion;

    private String alternativeRemoteDownloadUrl;

    private String servletContextUrl = "";

    private Boolean replaceHeadRequestWithGet;

    private boolean skipEncoding;

    /**
     * Use when the internal download request is made in order to work on the file internally.
     * For example: when we download a file and then read it's content, extract it, etc.
     *
     * In that case, if the repository is configured to send HTTP redirect upon download, it will ignore it and download
     * the file anyways.
     */
    private boolean disableRedirect;

    private HeadersMultiMap headers = new HeadersMultiMap();

    private final Set<String> delegationAllowedHeaders;

    private String alternateRemoteSiteUrl = null;

    private boolean skipStatsUpdate = false;

    private String clientAddress = null;

    /**
     * DO NOT USE DIRECTLY!
     *
     * Use factory methods:
     * {@link InternalRequestFactory#createInternalRequestDisableRedirect}
     * {@link InternalRequestFactory#createInternalRequestEnableRedirect}
     *
     * @param disableRedirect You can't know everything.. can you? Read the documentation in the factory.
     */
    public InternalArtifactoryRequest(RepoPath repoPath, boolean disableRedirect) {
        processRepoPath(repoPath);
        setDisableRedirect(disableRedirect);
        delegationAllowedHeaders = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    }

    private void processRepoPath(RepoPath repoPath) {
        String repoKey = processMatrixParamsIfExist(repoPath.getRepoKey());
        String path = processMatrixParamsIfExist(repoPath.getPath());
        setRepoPath(InfoFactoryHolder.get().createRepoPath(repoKey, path));
    }

    @Override
    public long getLastModified() {
        return 0;
    }

    /**
     * @return false - internal requests are sent to download resources
     */
    @Override
    public boolean isHeadOnly() {
        return false;
    }

    @Override
    public String getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    @Override
    public long getIfModifiedSince() {
        Enumeration<String> values = headers.getHeaderValues(HttpHeaders.IF_MODIFIED_SINCE);
        if (!values.hasMoreElements()) {
            return -1L;
        }
        Date date = DateUtils.parseDate(values.nextElement());
        if (date == null) {
            return -1L;
        }
        return date.getTime();
    }

    @Override
    public boolean hasIfModifiedSince() {
        return getIfModifiedSince() != -1L;
    }

    @Override
    public boolean isFromAnotherArtifactory() {
        return false;
    }

    @Override
    public boolean isRecursive() {
        return false;
    }

    /**
     * @return null - the internal request has no input stream
     */
    @Override
    public InputStream getInputStream() {
        return null;
    }

    /**
     * @return 0 - the internal request has no content (only url and headers)
     */
    @Override
    public long getContentLength() {
        return 0;
    }

    /**
     * @deprecated use org.artifactory.api.request.InternalArtifactoryRequest#getHeaderValues(java.lang.String)
     * old code which does not abide to the contract!
     * It ignores the headername parameter in this implementation.
     * @param headerName
     * @return
     */
    @Deprecated
    @Override
    public Enumeration getHeaders(@Nonnull String headerName) {
        // https://www.jfrog.com/jira/browse/RTFACT-17547
        // - It probably should have been  same as getHeadersValues(headerName)
        // maintain the bug for now by:
        Collection<String> values = headers.getHeaders().values();
        return new IteratorEnumeration(values.iterator());
    }

    @Override
    public Enumeration<String> getHeadersKeys() {
        return headers.getHeadersKeys();
    }

    @Override
    public Enumeration<String> getHeaderValues(@Nonnull String headerName) {
        return headers.getHeaderValues(headerName);
    }

    /**
     * setHeader: setting the Http header overriding all previous values that key has.
     * @param key
     * @param value
     */
    public void setHeader(String key, String value) {
        this.headers.setHeader(key, value);
    }

    /**
     * addHeader is addoing the Http header (in addition to all previous values that key has).
     * @param key
     * @param value
     */
    public void addHeader(String key, String value) {
        this.headers.addHeader(key, value);
    }

    /**
     * set http header eligible for remote host delegation, for more details
     *
     * @param key   header name
     * @param value header value
     * @see {@link org.artifactory.api.request.InternalArtifactoryRequest#getDelegationAllowedHeaders()}
     */
    public void setHeaderEligibleForDelegation(String key, String value) {
        delegationAllowedHeaders.add(key);
        setHeader(key, value);
    }

    /**
     * Adds http header eligible for remote host delegation, for more details
     * @see {@link org.artifactory.api.request.InternalArtifactoryRequest#getDelegationAllowedHeaders()}
     *
     * @param key header name
     * @param value header value
     */
    public void addHeaderEligibleForDelegation(String key, String value) {
        delegationAllowedHeaders.add(key);
        addHeader(key, value);
    }

    public void addHeaders(Map<String, String> headers) {
        this.headers.updateHeaders(headers);
    }

    @Override
    public String getUri() {
        return "";
    }

    @Override
    public String getServletContextUrl() {
        return servletContextUrl;
    }

    public void setServletContextUrl(String servletContextUrl) {
        this.servletContextUrl = servletContextUrl;
    }

    public void setSkipJarIndexing(boolean skipJarIndexing) {
        this.skipJarIndexing = skipJarIndexing;
    }

    public void setTrustServerChecksums(boolean trustServerChecksums) {
        this.trustServerChecksums = trustServerChecksums;
    }

    public boolean isSkipEncoding() {
        return skipEncoding;
    }

    public boolean isDisableRedirect() {
        return disableRedirect;
    }

    public boolean isSkipJarIndexing() {
        return skipJarIndexing;
    }

    public boolean isTrustServerChecksums() {
        return trustServerChecksums;
    }

    public boolean isForceDownloadIfNewer() {
        return forceDownloadIfNewer;
    }

    public void setForceDownloadIfNewer(boolean forceDownloadIfNewer) {
        this.forceDownloadIfNewer = forceDownloadIfNewer;
    }

    public void setSearchForExistingResourceOnRemoteRequest(boolean searchForExistingResourceOnRemoteRequest) {
        this.searchForExistingResourceOnRemoteRequest = searchForExistingResourceOnRemoteRequest;
    }

    public void setAlternativeRemoteDownloadUrl(String alternativeRemoteDownloadUrl) {
        this.alternativeRemoteDownloadUrl = alternativeRemoteDownloadUrl;
    }

    public void setReplicationDownloadRequest(Boolean replicationDownloadRequest) {
        this.replicationDownloadRequest = replicationDownloadRequest;
    }

    public void setReplicationOriginatedDownloadRequest(Boolean replicationOriginatedDownloadRequest) {
        this.replicationOriginatedDownloadRequest = replicationOriginatedDownloadRequest;
    }

    public void setDisableFolderRedirectAssertion(Boolean disableFolderRedirectAssertion) {
        this.disableFolderRedirectAssertion = disableFolderRedirectAssertion;
    }

    public void setReplaceHeadRequestWithGet(Boolean replaceHeadRequestWithGet) {
        this.replaceHeadRequestWithGet = replaceHeadRequestWithGet;
    }

    /**
     * Use when the internal download request is made in order to work on the file internally.
     * For example: when we download a file and then read it's content, extract it, etc --> set to TRUE.
     *
     * In that case, if the repository is configured to send HTTP redirect upon download, it will ignore it and download
     * the file anyways.
     */
    public void setDisableRedirect(boolean disableRedirect) {
        this.disableRedirect = disableRedirect;
    }

    public void setAlternateRemoteSiteUrl(String remoteResourceUrl) {
        this.alternateRemoteSiteUrl = remoteResourceUrl;
    }

    public void setSkipEncoding(boolean skipEncoding) {
        this.skipEncoding = skipEncoding;
    }

    @Override
    public void setZipResourcePath(String zipResourcePath) {
        super.setZipResourcePath(zipResourcePath);
    }

    @Override
    public String getParameter(String name) {
        if (PARAM_SKIP_JAR_INDEXING.equals(name)) {
            return String.valueOf(skipJarIndexing);
        }
        if (PARAM_FORCE_DOWNLOAD_IF_NEWER.equals(name)) {
            return String.valueOf(forceDownloadIfNewer);
        }
        if (PARAM_SEARCH_FOR_EXISTING_RESOURCE_ON_REMOTE_REQUEST.equals(name) &&
                searchForExistingResourceOnRemoteRequest != null) {
            return String.valueOf(searchForExistingResourceOnRemoteRequest);
        }
        if (PARAM_ALTERNATIVE_REMOTE_DOWNLOAD_URL.equals(name)) {
            return alternativeRemoteDownloadUrl;
        }
        if (PARAM_REPLICATION_DOWNLOAD_REQUEST.equals(name)) {
            return String.valueOf(replicationDownloadRequest);
        }
        if (PARAM_REPLICATION_ORIGINATED_DOWNLOAD_REQUEST.equals(name)) {
            return String.valueOf(replicationOriginatedDownloadRequest);
        }
        if (PARAM_FOLDER_REDIRECT_ASSERTION.equals(name)) {
            return String.valueOf(disableFolderRedirectAssertion);
        }
        if (PARAM_REPLACE_HEAD_IN_RETRIEVE_INFO_WITH_GET.equals(name)) {
            return String.valueOf(replaceHeadRequestWithGet);
        }
        if (PARAM_SKIP_ENCODING.equals(name)) {
            return String.valueOf(skipEncoding);
        }
        if (PARAM_ALTERNATIVE_REMOTE_SITE_URL.equals(name) &&
                alternateRemoteSiteUrl != null) {
            return String.valueOf(alternateRemoteSiteUrl);
        }
        if (PARAM_FORCE_GET_STREAM.equals(name)) {
            return String.valueOf(disableRedirect);
        }
        return null;
    }

    @Override
    public String[] getParameterValues(String name) {
        String val = getParameter(name);
        if (val != null) {
            return new String[]{val};
        } else {
            return super.getParameterValues(name);
        }
    }

    /**
     * A list of headers that can be delegated to the
     * remote host,
     *
     * This is white-list filtering that used for preventing from
     * unauthorized headers leaking to the external request
     * towards destination server.
     *
     * Service willing to allow certain header being delegated
     * to remote host, should explicitly mark it as eligible
     * for that by registering it in DelegationAllowedHeaders set
     * via {@link org.artifactory.api.request.InternalArtifactoryRequest#setHeaderEligibleForDelegation(String, String)}
     *
     * @return Set<String>
     */
    public Set<String> getDelegationAllowedHeaders() {
        return Collections.unmodifiableSet(delegationAllowedHeaders);
    }

    public boolean isSkipStatsUpdate() {
        return skipStatsUpdate;
    }

    public void setSkipStatsUpdate(boolean skipStatsUpdate) {
        this.skipStatsUpdate = skipStatsUpdate;
    }
}
