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

import org.apache.http.conn.util.InetAddressUtils;
import org.artifactory.descriptor.repo.ReverseProxyDescriptor;
import org.artifactory.descriptor.repo.ReverseProxyMethod;
import org.artifactory.descriptor.repo.WebServerType;
import org.artifactory.webapp.servlet.redirection.RedirectionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.artifactory.util.ConfReverseProxyHelper.getReverseProxyDescriptor;

/**
 * @author saffih
 */
public class DockerInternalRewrite implements RedirectionHandler {

    private static final Logger log = LoggerFactory.getLogger(DockerInternalRewrite.class);

    private final static String V2 = "/v2/";

    @Override
    public boolean shouldRedirect(ServletRequest request) {
        // TODO [NS] No need to run full flow to understand if we can redirect
        return DockerInternalRewrite.getInternalRewrite((HttpServletRequest) request) != null;
    }

    @Override
    public void redirect(ServletRequest request, ServletResponse response) {
        try {
            String newURI = DockerInternalRewrite.getInternalRewrite((HttpServletRequest) request);
            if (newURI != null) {
                request.getRequestDispatcher(newURI).forward(request, response);
            }
        } catch (Exception e) {
            log.debug("Could not redirect to docker repository", e);
            log.error("Could not redirect to docker repository");
        }
    }

    static String getLastSubdomain(String host) {
        int domainIdx = host.indexOf('.');
        if (domainIdx < 0) {
            return null;
        }
        int tldIdx = host.indexOf('.', domainIdx + 1);
        if (tldIdx > 0) {
            int forthIdx = host.indexOf('.', tldIdx + 1);
            if (forthIdx > 0) { // Either 4th level subdomain or IPv4.
                if (InetAddressUtils.isIPv4Address(host)) {
                    return null;
                }
            }
        }

        return host.substring(0, domainIdx);
    }

    /**
     * Do internal rewrite of url into Docker v2 repositories
     * /v2/{repo}/ ==> /api/docker/{repo}/v2/
     * and for domain XXXX.*.*.* /v2/ ==> /api/docker/XXXX/v2/
     * The domain rewrite is controled rewrite.config
     */
    private static String getInternalRewrite(HttpServletRequest request) {
        String relPath = getServletRelativePath(request);
        if (relPath == null) {
            return null;
        }
        String serverName = request.getServerName();
        ReverseProxyMethod dockerReverseProxyMethod = ConfReverseProxyHelper.getReverseProxyMethod();
        return getInternalRewrite(
                serverName,
                relPath,
                dockerReverseProxyMethod);
    }

    private static String getServletRelativePath(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String artPrefix = request.getContextPath();
        if (!requestURI.startsWith(artPrefix)) {
            log.error("Request uri {} does not start with the request context {} !!! ", requestURI, artPrefix);
            return null;
        }
        return requestURI.substring(artPrefix.length());
    }

    /**
     * Calculate rewritten url based on the servername, relative path and rewrite method (provided by a getter )
     */
    static String getInternalRewrite(String serverName, String relPath, ReverseProxyMethod chosenMethod) {
        if (!relPath.startsWith(V2)) {
            return null;
        }
        // domain case ?
        String dockerPath = "/api/docker";
        String domainRepo = getLastSubdomain(serverName);
        boolean useSubDomain = (domainRepo != null);
        boolean fallbackSingleRegistry = true;
        // by default we would like domain based - unless specified otherwith.
        if (chosenMethod != null) {
            // subdomain was not specified
            if (!chosenMethod.equals(ReverseProxyMethod.SUBDOMAIN)) {
                useSubDomain = false;
            }
            // repopath was not specified
            if (!chosenMethod.equals(ReverseProxyMethod.REPOPATHPREFIX)) {
                fallbackSingleRegistry = false;
            }
        }

        if (useSubDomain) {
            return dockerPath + "/" + domainRepo + relPath;
        }
        if (fallbackSingleRegistry) {
            int repoIndexStart = V2.length();
            int repoIndexEnd = relPath.indexOf("/", V2.length());
            if (repoIndexEnd == -1) {
                return dockerPath + V2;
            }
            String repoKey = relPath.substring(repoIndexStart, repoIndexEnd);
            return dockerPath + "/" + repoKey + V2 + relPath.substring(repoIndexEnd + 1);
        }

        // a method specified without internal rewrite.  we do not change the url. I.E. PORT Based
        return null;
    }

    public static URI rewriteBack(String repoKey, URI uri, Set<Map.Entry<String, List<String>>> headers) {
        ReverseProxyDescriptor currentReverseProxy = getReverseProxyDescriptor();
        if (currentReverseProxy == null) {
            // maintain old behaviour - until we support first use etc.
            return uri;
        }
        WebServerType proxy = ConfReverseProxyHelper.getReverseProxyType();
        return rewriteBack(repoKey, uri, proxy, headers);
    }


    static URI rewriteBack(String repoKey, URI uri, WebServerType proxy, Set<Map.Entry<String, List<String>>> headers) {
        ReverseProxyMethod reverseProxyMethod = ConfReverseProxyHelper.getReverseProxyMethod();
        return rewriteBack(repoKey, uri, proxy, reverseProxyMethod, headers);
    }

    static URI rewriteBack(String repoKey, URI uri, WebServerType proxy, ReverseProxyMethod reverseProxyMethod,
            Set<Map.Entry<String, List<String>>> headers) {
        String path = uri.getPath();
        // insert repo key into path.
        if (ReverseProxyMethod.REPOPATHPREFIX.equals(reverseProxyMethod)) {
            if (path.startsWith(V2)) {
                path = path.substring(V2.length());
            } else if (path.startsWith("v2/")) {
                path = path.substring("v2/".length());
            }
            path = "/v2/" + repoKey + "/" + path;
        }
        // unless specified otherwise
        String scheme = "https";

        // Our default snippet provides this header with value of scheme (if the value was not already set)
        String key = "x-forwarded-proto";
        String schemToUse = getSingleHeaderValue(headers, key);
        if (schemToUse == null) {
            // This code would make NginX http settings fail and Https with Tomcat fail as well.
            // Only in DIRECT mode either keep the original
            if (WebServerType.DIRECT.equals(proxy)) {
                scheme = "http";
                log.debug(" No  X-Forwarded-Proto using http schema");
            } else {
                // the default docker impl without the header was https
                log.debug(" No  X-Forwarded-Proto using https schema ");
                // should be changed to http with Nginx
                // scheme = "http";
            }
        } else {
            scheme = schemToUse;
        }

        // support port header
        String port = null;
        if (uri.getPort() > 0) {
            port = String.valueOf(uri.getPort());
        }


        // return UriBuilder.fromPath(path).scheme(scheme).host(uri.getHost()).port(port).build()
        try {
            URI constructedUri;
            if (port == null) {
                constructedUri = new URI(scheme, uri.getHost(), path, null);
            } else {
                constructedUri = new URI(scheme, null, uri.getHost(), Integer.valueOf(port), path, null, null);
            }
            // existing code does not rewrites the base url in the docker repo handler.
            // it was not included. baseUrlOverrideKey = "x-artifactory-override-base-url";
            if (log.isTraceEnabled()) {
                if (!constructedUri.equals(uri)) {
                    log.trace("rewriteBack: {} to {} ", uri, constructedUri);
                }
            }
            return constructedUri;
        } catch (URISyntaxException e) {
            log.warn("can't create URI ", e);
            return uri;
        }
    }

    /**
     * get single value header
     */
    private static String getSingleHeaderValue(Set<Map.Entry<String, List<String>>> headers, String key) {
        String res = null;

        List<String> valueToUse = headers.stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(key))
                .map(entry -> entry.getValue().get(0))
                .collect(Collectors.toList());
        if (!valueToUse.isEmpty()) {
            res = valueToUse.get(0);
            if (valueToUse.size() > 1) {
                log.warn("multiple value header for {} : {} ", key, valueToUse);
            }
        }
        return res;
    }
}
