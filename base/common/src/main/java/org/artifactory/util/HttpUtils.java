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

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.http.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.rest.constant.RestConstants;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.repo.RepoPath;
import org.artifactory.request.ArtifactoryRequest;
import org.artifactory.request.RequestThreadLocal;
import org.artifactory.rest.ErrorResponse;
import org.artifactory.util.encoding.URIUtil;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.jfrog.build.api.Build;
import org.jfrog.client.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import javax.annotation.Nullable;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.*;
import java.util.*;

import static org.artifactory.api.context.ArtifactoryContext.CONTEXT_ID_PROP;

/**
 * @author yoavl
 */
public abstract class HttpUtils {

    public static final String WEBAPP_URL_PATH_PREFIX = "webapp";
    private static final String BROWSE_REPO_URL_PREFIX = "/#/artifacts/browse/tree/General/";

    private static final Logger log = LoggerFactory.getLogger(HttpUtils.class);

    private static String userAgent;

    private HttpUtils() {
        // utility class
    }

    public static String getArtifactoryUserAgent() {
        if (userAgent == null) {
            String artifactoryVersion = ConstantValues.artifactoryVersion.getString();
            if (artifactoryVersion.startsWith("$") || artifactoryVersion.endsWith("SNAPSHOT")) {
                artifactoryVersion = "development";
            }
            userAgent = "Artifactory/" + artifactoryVersion;
        }
        return userAgent;
    }

    /**
     * Reset the cached Artifactory user agent string (required after upgrade)
     */
    public static void resetArtifactoryUserAgent() {
        userAgent = null;
    }

    @SuppressWarnings({"IfMayBeConditional"})
    public static String getRemoteClientAddress(HttpServletRequest request) {
        String remoteAddress;
        //Check if there is a remote address coming from a proxied request
        //(http://httpd.apache.org/docs/2.2/mod/mod_proxy.html#proxypreservehost)
        String header = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotBlank(header)) {
            //Might contain multiple entries - take the first
            remoteAddress = new StringTokenizer(header, ",").nextToken();
        } else {
            //Take it the standard way
            remoteAddress = request.getRemoteAddr();
        }
        return remoteAddress;
    }

    public static String getServletContextUrl(HttpServletRequest httpRequest) {
        return getServletContextUrl(ContextHelper.get(), httpRequest);
    }

    public static String getServletContextUrl(ArtifactoryContext artifactoryContext, HttpServletRequest httpRequest) {

        String jfrogOverrideBaseUrl = httpRequest.getHeader(ArtifactoryRequest.JFROG_OVERRIDE_BASE_URL);
        if (StringUtils.isNotBlank(jfrogOverrideBaseUrl)) {
            return PathUtils.addTrailingSlash(jfrogOverrideBaseUrl) +
                    PathUtils.trimLeadingSlashes(artifactoryContext.getContextPath());
        }

        String artOverrideBaseUrl = httpRequest.getHeader(ArtifactoryRequest.ARTIFACTORY_OVERRIDE_BASE_URL);
        if (StringUtils.isNotBlank(artOverrideBaseUrl)) {
            // original artifactory request url overrides request and base url
            return artOverrideBaseUrl;
        }
        CentralConfigService centralConfigService = artifactoryContext.getCentralConfig();
        String baseUrl = centralConfigService.getDescriptor().getUrlBase();
        if (!StringUtils.isEmpty(baseUrl)) {
            String scheme = httpRequest.getScheme();
            if (baseUrl.startsWith(scheme)) {
                return baseUrl;
            } else {
                int idx = baseUrl.indexOf("://");
                if (idx > 0) {
                    return scheme + "://" + baseUrl.substring(idx + 3);
                } else {
                    return scheme + "://" + baseUrl;
                }
            }
        }
        return getServerUrl(httpRequest) + httpRequest.getContextPath();
    }

    public static String getRestApiUrl(HttpServletRequest request) {
        return getServletContextUrl(request) + "/" + RestConstants.PATH_API;
    }

    private static String getServerUrl(HttpServletRequest httpRequest) {
        int port = httpRequest.getServerPort();
        String scheme = httpRequest.getScheme();
        if (isDefaultPort(scheme, port)) {
            return scheme + "://" + httpRequest.getServerName();
        }
        return scheme + "://" + httpRequest.getServerName() + ":" + port;
    }

    private static boolean isDefaultPort(String scheme, int port) {
        switch (port) {
            case 80:
                return "http".equalsIgnoreCase(scheme);
            case 443:
                return "https".equalsIgnoreCase(scheme);
            default:
                return false;
        }
    }

    public static String getSha1Checksum(ArtifactoryRequest request) {
        String sha1Header = request.getHeader(ArtifactoryRequest.CHECKSUM_SHA1);
        return sha1Header != null ? sha1Header : getChecksum(ChecksumType.sha1, request);
    }

    public static String getSha256Checksum(ArtifactoryRequest request) {
        String sha2Header = request.getHeader(ArtifactoryRequest.CHECKSUM_SHA256);
        return sha2Header != null ? sha2Header : getChecksum(ChecksumType.sha256, request);
    }

    public static String getMd5Checksum(ArtifactoryRequest request) {
        String md5Header = request.getHeader(ArtifactoryRequest.CHECKSUM_MD5);
        return md5Header != null ? md5Header : getChecksum(ChecksumType.md5, request);
    }

    private static String getChecksum(ChecksumType checksumType, ArtifactoryRequest request) {
        String checksumHeader = request.getHeader(ArtifactoryRequest.CHECKSUM);
        if (StringUtils.isNotBlank(checksumHeader) && checksumHeader.length() == checksumType.length()) {
            return checksumHeader;
        }
        return null;
    }

    public static boolean isExpectedContinue(ArtifactoryRequest request) {
        String expectHeader = request.getHeader("Expect");
        if (StringUtils.isBlank(expectHeader)) {
            return false;
        }
        // some clients make the C lowercase even when passed uppercase
        return expectHeader.contains("100-continue") || expectHeader.contains("100-Continue");
    }

    public static String getContextId(ServletContext servletContext) {
        String contextId = Objects.toString(servletContext.getAttribute(CONTEXT_ID_PROP), null);
        if (StringUtils.isBlank(contextId)) {
            contextId = PathUtils.trimLeadingSlashes(servletContext.getContextPath());
        }
        return contextId;
    }

    /**
     * @param status The (http based) response code
     * @return True if the code symbols a successful request cycle (i.e., in the 200-299 range)
     */
    public static boolean isSuccessfulResponseCode(int status) {
        return HttpStatus.SC_OK <= status && status <= 299;
    }

    /**
     * @param status The (http based) response code
     * @return True if the code symbols a successful request cycle (i.e., in the 300-399 range)
     */
    public static boolean isRedirectionResponseCode(int status) {
        return HttpStatus.SC_MULTIPLE_CHOICES <= status && status <= 399;
    }

    /**
     * @param status The (http based) response code
     * @return True if the code symbols a successful request cycle (i.e., in the 200-399 range)
     */
    public static boolean isSuccessOrRedirectResponseCode(int status) {
        return isSuccessfulResponseCode(status) || isRedirectionResponseCode(status);
    }

    /**
     * Calculate a unique id for the VM to support Artifactories with the same ip (e.g. accross NATs)
     */
    public static String getHostId() {
        return ArtifactoryHome.get().getHostId();
    }

    /**
     * @param response The response to get the body from
     * @return Returns the response body input stream or null is there is none.
     */
    @Nullable
    public static InputStream getResponseBody(HttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        return entity == null ? null : entity.getContent();
    }

    public static String encodeQuery(String unescaped) {
        try {
            return URIUtil.encodeQuery(unescaped, "UTF-8");
        } catch (HttpException e) {
            // Nothing to do here, we will return the un-escaped value.
            log.warn("Could not encode path '{}' with UTF-8 charset, returning the un-escaped value.", unescaped);
        }
        return unescaped;
    }

    /**
     * Finds the host part and returns whatever comes after, if not empty. If there is a path to this address, it is
     * always returned with a leading slash.
     * @param address - the full URL to encode
     * @return the address path, or an empty string if the address has no path
     */

     static String getRequestPath(String address) {
        String hostPathDelimiter;
        String path;
        if (!address.contains("://") && !address.startsWith("/")) {
            // In a scheme-less address the first forward slash is the delimiter between the host and the path.
            // Hostnames, IPv4 & IPv6 addresses should never contains a slash.
            hostPathDelimiter = "/";
            path = "/" + StringUtils.substringAfter(address, hostPathDelimiter);
        } else {
            hostPathDelimiter = "://";
            String host = StringUtils.substringBetween(address, hostPathDelimiter, "/");
            if (StringUtils.isBlank(host)) {
                // no host, no path
                log.debug("Address '{}' is invalid, returning empty path", address);
                return "";
            }
            // scheme://host/{path}
            path = StringUtils.substringAfter(address, host);
        }
        if (StringUtils.isBlank(path)) {
            return "";
        }
        return path;
    }

    private static String getAddressHost(String address) {
        String hostPathDelimiter;
        String host;
        if(!address.contains("://")) {
            // In a scheme-less address the first forward slash is the delimiter between the host and the path.
            // Hostnames, IPv4 & IPv6 addresses should never contains a slash.
            hostPathDelimiter = "/";
            host = StringUtils.substringBefore(address, hostPathDelimiter);
        } else {
            hostPathDelimiter = "://";
            host = StringUtils.substringBetween(address, hostPathDelimiter, "/");
        }
        if (StringUtils.isBlank(host)) {
            log.warn("No host found in address: '{}', returning empty string", address);
            return "";
        }
        return host;
    }

    /**
     * Encodes only the path of a given full URL using {@link HttpUtils#getRequestPath(String)}
     * and {@link HttpUtils#encodeQuery(String)}. The scheme, hostname and port are left untouched.
     * Unlike {@link HttpUtils#encodeQuery(String)}, this method is compatible with both IPv6 and IPv4 addresses.
     * @param url - the full URL to encode
     */

    public static String encodeUrl(String url) {
        String requestPath = getRequestPath(url);
        if (requestPath.equals("/") || requestPath.equals("")) {
            // the path is empty, return original input
            return url;
        }
        // Encode only the request path itself, everything else (scheme, host, port, etc) is left untouched
        return StringUtils.substringBefore(url, requestPath) + encodeQuery(requestPath);
    }

    /**
     * @param port the port
     * @return true if the given port is an implicit port (443 or 80)
     */

    public static boolean isImplicitPort(String port) {
        return port.equals("443") || port.equals("80");
    }

    /**
     * Heuristically detect whether the given address can represent an IPv6 address.
     * This method does NOT guarantee that the input represents a valid IPv6 address, it merely checks whether the
     * address contains at least two colons in it, or three if the hostname contains auth information.
     * @param address - the address
     * @return true if the given address could represent an IPv6 address
     */

    public static boolean isIPv6Address(String address) {
        if(getAddressHost(address).contains("@")) {
            // if the hostname itself contains the commercial at sign, this URL has auth info embedded.
            return address.matches(".*:.*:.*:.*");
        }
        return address.matches(".*:.*:.*");
    }

    /**
     * Removes leading/trailing square brackets if needed.
     * @param address - the address
     * @return the address without the leading/trailing square brackets, or the original address.
     */

    public static String trimSquareBracketsIfNeeded(String address) {
        if (address.charAt(0) == '[' && address.charAt(address.length() - 1) == ']') {
            address = address.substring(1, address.length() - 1);
        }
        return address;
    }

    /**
     * Additional to the traditional escaping and encoding string regarded as within the path component of an
     * URI with the default protocol charset it also encodes colon character.
     * Do not encode full url like http://bla/bla, the colon will be converted to %3A
     */
    public static String encodeWithinPath(String unescaped) {
        try {
            unescaped = URIUtil.encodeWithinPath(unescaped);
            unescaped = unescaped.replaceAll(":","%3A");
            return URIUtil.encodeWithinPath(unescaped);
        } catch (HttpException e) {
            // Nothing to do here, we will return the un-escaped value.
            log.warn("Could not encode path '{}' with UTF-8 charset, returning the un-escaped value.", unescaped);
        }
        return unescaped;
    }
    /**
     * Additional to the traditional escaping and decoding string regarded as within the path component of an
     * URI with the default protocol charset it also decodes colon character.
     */
    public static String decodeUri(String encodedUri) {
        try {
            return URIUtil.decode(encodedUri, "UTF-8");
        } catch (HttpException e) {
            // Nothing to do here, we will return the un-escaped value.
            log.warn("Could not decode uri '{}' with UTF-8 charset, returning the encoded value.", encodedUri);
        }
        return encodedUri;
    }

    public static String decodeUrlWithURLDecoder(String url) {
        try {
            return URLDecoder.decode(url, CharEncoding.UTF_8);
        } catch (UnsupportedEncodingException e) {
            log.error("Could not decode uri '{}' with UTF-8 charset, returning the encoded value.", url);
        }
        return url;
    }

    /**
     * encodes to application/x-www-form-urlencoded
     * beware of encoding full url, ex: http://bla/bla.txt -> http%3A%2F%2Fbla%2Fbla.txt
     * test test -> test+test
     * test:test -> test%3Atest
     * do not use when encoding for native browsing, Ivi client expect space char to be encoded as %20 and not `+` when resolving dependencies
     */
    public static String encodeUrlWithURLEncoder(String url) {
        try {
            return URLEncoder.encode(url, CharEncoding.UTF_8);
        } catch (UnsupportedEncodingException e) {
            log.error("Could not encode uri '{}' with UTF-8 charset, returning the original value.", url);
        }
        return url;
    }

    /**
     * Removes the query parameters from the given url
     *
     * @param url URL string with query parameters, e.g. "http://hello/world?lang=java&run=1"
     * @return new string object without the query parameters, e.g. "http://hello/world". If no query elements found the
     * original string is returned.
     */
    public static String stripQuery(String url) {
        int i = url.indexOf("?");
        if (i > -1) {
            return url.substring(0, i);
        } else {
            return url;
        }
    }

    public static String adjustRefererValue(Map<String, String> headersMap, String headerVal) {
        //Append the artifactory user agent to the referer
        if (headerVal == null) {
            //Fallback to host
            headerVal = headersMap.get("HOST");
            if (headerVal == null) {
                //Fallback to unknown
                headerVal = "UNKNOWN";
            }
        }
        if (!headerVal.startsWith("http")) {
            headerVal = "http://" + headerVal;
        }
        try {
            java.net.URL uri = new java.net.URL(headerVal);
            //Only use the uri up to the path part
            headerVal = uri.getProtocol() + "://" + uri.getAuthority();
        } catch (MalformedURLException e) {
            //Nothing
        }
        headerVal += "/" + HttpUtils.getArtifactoryUserAgent();
        return headerVal;
    }

    /**
     * Extracts the content length from the response header, or return -1 if the content-length field was not found.
     *
     * @param response The response
     * @return Content length in bytes or -1 if header not found
     */
    public static long getContentLength(HttpResponse response) {
        Header contentLengthHeader = response.getFirstHeader(HttpHeaders.CONTENT_LENGTH);
        if (contentLengthHeader != null) {
            return extractContentLengthFromHeader(contentLengthHeader.getValue());
        } else {
            return -1;
        }
    }

    public static String getServerAndPortFromContext(String contextUrl) {
        String[] splittedServerContext = contextUrl.split("/");
        if (splittedServerContext.length >= 3) {
            return splittedServerContext[2];
        } else {
            return "";
        }
    }

    public static String getApacheServerAndPortFromContext(String contextUrl) {
        String[] splittedServerContext = contextUrl.split("/");
        if (splittedServerContext.length >= 3) {
            return splittedServerContext[0] + "//" + splittedServerContext[2];
        } else {
            return "";
        }
    }

    /**
     * Return content length as long (required for uploaded files > 2GB).
     * The servlet api can only return this as int.
     *
     * @param request The request
     * @return The content length in bytes or -1 if not found
     */
    public static long getContentLength(HttpServletRequest request) {
        return extractContentLengthFromHeader(request.getHeader(HttpHeaders.CONTENT_LENGTH));
    }

    private static long extractContentLengthFromHeader(String lengthHeader) {
        long contentLength;
        if (lengthHeader != null) {
            try {
                contentLength = Long.parseLong(lengthHeader);
            } catch (NumberFormatException e) {
                log.trace("Bad Content-Length value {}", lengthHeader);
                contentLength = -1;
            }
        } else {
            contentLength = -1;
        }
        return contentLength;
    }

    public static void sendErrorResponse(HttpServletResponse response, int statusCode, String message)
            throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(statusCode);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        ErrorResponse errorResponse = new ErrorResponse(statusCode, message);
        response.getWriter().write(mapper.writeValueAsString(errorResponse));
    }

    public static boolean isAbsolute(String url) {
        try {
            URI uri = new URIBuilder(url).build();
            return uri.isAbsolute();
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public static String getRemoteClientAddress() {
        if (ConstantValues.test.getBoolean()) {
            return "127.0.0.1";
        }
        String remoteClientAddress = RequestThreadLocal.getClientAddress();
        if (remoteClientAddress == null) {
            return "";
        } else {
            return remoteClientAddress;
        }
    }

    public static String resolveResponseRemoteAddress(CloseableHttpResponse response) {
        try {
            Field connHolderField = response.getClass().getDeclaredField("connHolder");
            connHolderField.setAccessible(true);
            Object connHolder = connHolderField.get(response);

            Field managedConnField = connHolder.getClass().getDeclaredField("managedConn");
            managedConnField.setAccessible(true);
            ManagedHttpClientConnection managedConn = (ManagedHttpClientConnection) managedConnField.get(
                    connHolder);
            String hostAddress = managedConn.getSocket().getInetAddress().getHostAddress();
            return hostAddress == null ? StringUtils.EMPTY : hostAddress;
        } catch (Throwable throwable) {
            return StringUtils.EMPTY;
        }
    }

    public static String createBuildInfoLink(Build build) {
        String artifactoryUrl = ContextHelper.get().beanForType(
                CentralConfigService.class).getDescriptor().getServerUrlForEmail();
        if (StringUtils.isBlank(artifactoryUrl)) {
            return build.getName() + ":" + build.getNumber();
        } else {
            try {
                String href = Joiner.on("/").join(
                        artifactoryUrl + HttpUtils.WEBAPP_URL_PATH_PREFIX,
                        "builds",
                        // Do a manual "encoding" of spaces for the build name. This is due to the fact that if the mail
                        // is sent to a Gmail account it will automatically insert '+' for every space, and not its '%20'
                        // hex representation, this will cause a broken link. see more here:
                        // http://www.google.fr/support/forum/p/gmail/thread?tid=53a5c616a0324d96&hl=en
                        build.getName().replace(" ", "%20"),
                        build.getNumber());

                return "<a href=\"" + href + "\"" + " target=\"blank\">" + build.getName() + ":" + build.getNumber()
                        + "</a>";
            } catch (Exception e) {
                return build.getName() + ":" + build.getNumber();
            }
        }
    }

    public static String createLinkToBrowsableArtifact(RepoPath repoPath, String linkLabel) {
        String artifactoryUrl = ContextHelper.get().beanForType(CentralConfigService.class).getDescriptor()
                .getServerUrlForEmail();
        if (StringUtils.isBlank(artifactoryUrl)) {
            return linkLabel;
        } else {
            String url = artifactoryUrl + HttpUtils.WEBAPP_URL_PATH_PREFIX + BROWSE_REPO_URL_PREFIX
                    + HttpUtils.encodeQuery(repoPath.toPath());
            return "<a href=" + url + " target=\"blank\"" + ">" + linkLabel + "</a>";
        }
    }

    /**
     * Extracts session access time if session exist,
     * if session is null, returns System.currentTimeMillis
     *
     * @return session access time
     */
    public static long getSessionAccessTime(HttpServletRequest servletRequest) {
        HttpSession session = servletRequest.getSession(false);
        return session != null ?
                (
                        session.getLastAccessedTime() == 0 ?
                                session.getCreationTime()
                                :
                                session.getLastAccessedTime()
                )
                :
                System.currentTimeMillis();
    }

    /**
     * Checks whether request targeted for changePassword api
     * or if admin triggered "unExpirePasswordForAllUsers" action
     *
     * @return true if request targeted for "changePassword"
     */
    public static boolean isChangePasswordRequest(ServletRequest servletRequest) {
        String uri = ((HttpServletRequest) servletRequest).getRequestURI();
        String invokedMethod = PathUtils.getLastPathElement(uri);
        return "changePassword".equals(invokedMethod) || "unExpirePasswordForAllUsers".equals(invokedMethod);
    }

    public static long getLastModified(HttpResponse response) {
        Header lastModifiedHeader = response.getFirstHeader(HttpHeaders.LAST_MODIFIED);
        if (lastModifiedHeader == null) {
            return -1;
        }
        String lastModifiedString = lastModifiedHeader.getValue();
        Date lastModifiedDate = DateUtils.parseDate(lastModifiedString);
        if (lastModifiedDate != null) {
            return lastModifiedDate.getTime();
        } else {
            log.warn("Unable to parse Last-Modified header : {}", lastModifiedString);
            return System.currentTimeMillis();
        }
    }

    public static String getEtag(HttpResponse response) {
        Header etagHeader = response.getFirstHeader(HttpHeaders.ETAG);
        if (etagHeader == null) {
            return "";
        }
        return etagHeader.getValue();
    }

    public static Set<ChecksumInfo> getChecksums(HttpResponse response) {
        Set<ChecksumInfo> remoteChecksums = Sets.newHashSet();
        addChecksumIfExists(response, ChecksumType.md5, remoteChecksums);
        addChecksumIfExists(response, ChecksumType.sha1, remoteChecksums);
        addChecksumIfExists(response, ChecksumType.sha256, remoteChecksums);
        return remoteChecksums;
    }

    /**
     * Adds the remote checksum by {@param type} if it exists as a header on the {@param response}.
     */
    protected static void addChecksumIfExists(HttpResponse response, ChecksumType type, Set<ChecksumInfo> remoteChecksums) {
        Header checksumHeader = response.getFirstHeader(ArtifactoryRequest.headerForChecksum(type));
        if (checksumHeader != null) {
            remoteChecksums.add(new ChecksumInfo(type, checksumHeader.getValue(), null));
        }
    }

    public static boolean isValidUrl(String url) {
        try {
            new URL(url);
            String urlSeparator = "://";
            return url.contains(urlSeparator);
        } catch (MalformedURLException e) {
            return false;
        }
    }
}
