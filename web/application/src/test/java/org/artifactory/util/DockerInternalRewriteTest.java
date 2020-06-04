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

import org.artifactory.descriptor.repo.ReverseProxyMethod;
import org.artifactory.descriptor.repo.WebServerType;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.artifactory.util.DockerInternalRewrite.rewriteBack;
import static org.testng.Assert.assertEquals;

/**
 * @author saffih
 */
public class DockerInternalRewriteTest {
    @Test
    public void testGetLastSubdomain() throws Exception {
        Assert.assertEquals(DockerInternalRewrite.getLastSubdomain("registry.artifactory.jfrog.com"), "registry");
        assertEquals(DockerInternalRewrite.getLastSubdomain("artifactory.jfrog.com"), "artifactory");
        assertEquals(DockerInternalRewrite.getLastSubdomain("jfrog.com"), "jfrog");
        assertEquals(DockerInternalRewrite.getLastSubdomain("com"), null);
        assertEquals(DockerInternalRewrite.getLastSubdomain("127.0.0.1"), null);
        assertEquals(DockerInternalRewrite.getLastSubdomain("muchtoomuch.toomany.host.sub.second.top.root"), "muchtoomuch");
        assertEquals(DockerInternalRewrite.getLastSubdomain("toomany.host.sub.second.top.root"), "toomany");
        assertEquals(DockerInternalRewrite.getLastSubdomain("host.sub.second.top.root"), "host");
        assertEquals(DockerInternalRewrite.getLastSubdomain("host.sub.second.בעברית.root"), "host");
        assertEquals(DockerInternalRewrite.getLastSubdomain("sub.second.top.root"), "sub");
        assertEquals(DockerInternalRewrite.getLastSubdomain("second.top.root"), "second");
        assertEquals(DockerInternalRewrite.getLastSubdomain("top.root"), "top");
        assertEquals(DockerInternalRewrite.getLastSubdomain("root"), null);

    }

    @Test
    public void testOldDockerClientIssue() throws Exception {
        assertEquals(DockerInternalRewrite.getInternalRewrite(
                "registry.artifactory.jfrog.com",
                "/v2/ubuntu",
                null),
                "/api/docker/registry/v2/ubuntu");

        assertEquals(DockerInternalRewrite.getInternalRewrite(
                "localhost",
                "/v2/registry/ubuntu",
                null),
                "/api/docker/registry/v2/ubuntu");
    }

    @Test
    void testRepoPathPrefixMethod() {
        ReverseProxyMethod method = ReverseProxyMethod.REPOPATHPREFIX;
        assertEquals(DockerInternalRewrite.getInternalRewrite(
                "registry.artifactory.jfrog.com",
                "/api/docker/registry/v2/ubuntu",
                method),
                null);

        assertEquals(DockerInternalRewrite.getInternalRewrite(
                "localhost",
                "/v2/registry/ubuntu",
                method),
                "/api/docker/registry/v2/ubuntu");

        assertEquals(DockerInternalRewrite.getInternalRewrite(
                "localhost",
                "/v2/registry//ubuntu",
                method),
                "/api/docker/registry/v2//ubuntu");

        // how should we behave ?
        //assertEquals(DockerInternalRewrite.getInternalRewrite(
        //        "localhost",
        //        "/v2//registry/ubuntu",
        //        method),
        //        "/api/docker//v2/registry/ubuntu");


    }

    @Test void testSubdomainRewrite() {
        ReverseProxyMethod method = ReverseProxyMethod.SUBDOMAIN;
        assertEquals(DockerInternalRewrite.getInternalRewrite(
                "registry.artifactory.jfrog.com",
                "/v2/ubuntu",
                method),
                "/api/docker/registry/v2/ubuntu");

        assertEquals(DockerInternalRewrite.getInternalRewrite(
                "localhost",
                "/v2/registry/ubuntu",
                method),
                null);

        assertEquals(DockerInternalRewrite.getInternalRewrite(
                "registry.artifactory.jfrog.com",
                "/v2//// no matter what ////",
                method),
                "/api/docker/registry/v2//// no matter what ////");
    }

    @Test void testRejectedRewrite() {
        ReverseProxyMethod method = ReverseProxyMethod.SUBDOMAIN;
        assertEquals(DockerInternalRewrite.getInternalRewrite(
                "registry.artifactory.jfrog.com",
                "//v2/ubuntu",
                method),
                null);


        assertEquals(DockerInternalRewrite.getInternalRewrite(
                "registry.artifactory.jfrog.com",
                "/v2",
                method),
                null);

        assertEquals(DockerInternalRewrite.getInternalRewrite(
                "registry.artifactory.jfrog.com",
                "/v",
                method),
                null);

        assertEquals(DockerInternalRewrite.getInternalRewrite(
                "registry.artifactory.jfrog.com",
                "/v",
                method),
                null);
        assertEquals(DockerInternalRewrite.getInternalRewrite(
                "registry.artifactory.jfrog.com",
                "/",
                method),
                null);
        assertEquals(DockerInternalRewrite.getInternalRewrite(
                "registry.artifactory.jfrog.com",
                "",
                method),
                null);
        assertEquals(DockerInternalRewrite.getInternalRewrite(
                "registry.artifactory.jfrog.com",
                " a%@r4ljeklhfakjlsh123dsjajdas a  jfdkjfkjadfsחלחלגגדדג ",
                method),
                null);
        assertEquals(DockerInternalRewrite.getInternalRewrite(
                "registry.artifactory.jfrog.com",
                "/ v2 /v2/",
                method),
                null);

    }

    // explicit contruct.
    @Test void backRewriteCheckNginxSubDomain() throws URISyntaxException {

        URI uri = new URI("https", null,"me.art.com", 8081, "/v2/part1/image", null, null);
        URI uriHttp = new URI("http", null,"me.art.com", 8081, "/v2/part1/image", null, null);
        {
            String scheme = "https";
            HashMap<String, List<String>> m = schemeHeaders(scheme);
            URI ret = rewriteBack("key", uri, WebServerType.NGINX, ReverseProxyMethod.SUBDOMAIN, m.entrySet());
            assertEquals(uri, ret);
        }
        {
            String scheme = "http";
            HashMap<String, List<String>> m = schemeHeaders(scheme);
            URI ret = rewriteBack("key", uri, WebServerType.NGINX, ReverseProxyMethod.SUBDOMAIN, m.entrySet());
            assertEquals(scheme, ret.getScheme());
            assertEquals(uriHttp, ret);
        }
        // same as prev behaviour
        {
            HashMap<String, List<String>> m = headers(new HashMap<>());
            URI ret = rewriteBack("key", uri, WebServerType.NGINX, ReverseProxyMethod.SUBDOMAIN, m.entrySet());
            assertEquals(uri, ret);
        }
    }


    /**
     * Docker 1.12  (Docker version 1.12.0, build 8eab29e) failed
     * when we used the nginx port header which overridided the HOST header port
     *
     * @throws URISyntaxException
     */
    @Test void backRewriteCheckNginxSubDomainDocker112Issue() throws URISyntaxException {

        URI uri = new URI("https","me.art.com", "/v2/part1/image", null);
        URI uriHttp = new URI("http","me.art.com", "/v2/part1/image", null);
        {
            String scheme = "https";
            String port = "443";
            HashMap<String, List<String>> m = headersOf(scheme, port);
            URI ret = rewriteBack("key", uri, WebServerType.NGINX, ReverseProxyMethod.SUBDOMAIN, m.entrySet());
            assertEquals(uri, ret);
        }
        {
            String scheme = "http";
            String port = "80";
            HashMap<String, List<String>> m = headersOf(scheme, port);
            URI ret = rewriteBack("key", uri, WebServerType.NGINX, ReverseProxyMethod.SUBDOMAIN, m.entrySet());
            assertEquals(scheme, ret.getScheme());
            assertEquals(uriHttp, ret);
        }


        // same as prev behaviour
        {
            HashMap<String, List<String>> m = headers(new HashMap<>());
            URI ret = rewriteBack("key", uri, WebServerType.NGINX, ReverseProxyMethod.SUBDOMAIN, m.entrySet());
            assertEquals(uri, ret);
        }
    }

    private HashMap<String, List<String>> headersOf(String scheme, String port) {
        return headers(new HashMap<String, String>() {{
                    put("X-ForWarded-Proto", scheme);
                    put("X-ForWarded-Port", port);
                    put("x-artifactory-override-base-url", "https://me.art.com/aritfactory");
                }});
    }


    @Test void backRewriteCheckNginxPortShouldNotChange() throws URISyntaxException {
        URI uri = new URI("https", null,"me.art.com", 5555, "/v2/part1/image", null, null);
        URI uriHttp = new URI("http", null,"me.art.com", 5555, "/v2/part1/image", null, null);
        {
            String scheme = "https";
            HashMap<String, List<String>> m = schemeHeaders(scheme);
            URI ret = rewriteBack("key", uri, WebServerType.NGINX, ReverseProxyMethod.PORTPERREPO, m.entrySet());
            assertEquals(uri, ret);
        }
        {
            String scheme = "http";
            HashMap<String, List<String>> m = schemeHeaders(scheme);
            URI ret = rewriteBack("key", uri, WebServerType.NGINX, ReverseProxyMethod.PORTPERREPO, m.entrySet());
            assertEquals(scheme, ret.getScheme());
            assertEquals(uriHttp, ret);
        }
        {
            HashMap<String, List<String>> m = headers(new HashMap<>());
            URI ret = rewriteBack("key", uri, WebServerType.NGINX, ReverseProxyMethod.PORTPERREPO, m.entrySet());
            assertEquals(uri, ret);
        }

    }

    @Test void backRewriteCheckNginxPath() throws URISyntaxException {
        URI uri = new URI("https", null,"me.art.com", 8081, "/v2/part1/image", null, null);
        URI uriHttp = new URI("http", null,"me.art.com", 8081, "/v2/part1/image", null, null);
        {
            String scheme = "https";
            HashMap<String, List<String>> m = schemeHeaders(scheme);
            URI ret = rewriteBack("key", uri, WebServerType.NGINX, ReverseProxyMethod.PORTPERREPO, m.entrySet());
            assertEquals(uri, ret);
        }
        {
            String scheme = "http";
            HashMap<String, List<String>> m = schemeHeaders(scheme);
            URI ret = rewriteBack("key", uri, WebServerType.NGINX, ReverseProxyMethod.PORTPERREPO, m.entrySet());
            assertEquals(scheme, ret.getScheme());
        }
        {
            HashMap<String, List<String>> m = headers(new HashMap<>());
            URI ret = rewriteBack("key", uri, WebServerType.NGINX, ReverseProxyMethod.PORTPERREPO, m.entrySet());
            assertEquals(uri, ret);
        }
    }

    @Test void backRewriteCheckTomcatSubDomain() throws URISyntaxException {

        String repoKey = "key";
        URI uri = new URI("https", null,"me.art.com", 8081, "/v2/part1/image", null, null);
        URI uriHttp = new URI("http", null,"me.art.com", 8081, "/v2/part1/image", null, null);
        {
            String scheme = "https";
            HashMap<String, List<String>> m = schemeHeaders(scheme);
            URI ret = rewriteBack(repoKey, uri, WebServerType.DIRECT, ReverseProxyMethod.SUBDOMAIN, m.entrySet());
            assertEquals(uri, ret);
        }
        {
            String scheme = "http";
            HashMap<String, List<String>> m = schemeHeaders(scheme);
            URI ret = rewriteBack(repoKey, uri, WebServerType.DIRECT, ReverseProxyMethod.SUBDOMAIN, m.entrySet());
            assertEquals(scheme, ret.getScheme());
            assertEquals(uriHttp, ret);
        }
        {   // NO proxy - use http
            HashMap<String, List<String>> m = headers(new HashMap<>());
            URI ret = rewriteBack(repoKey, uri, WebServerType.DIRECT, ReverseProxyMethod.SUBDOMAIN, m.entrySet());
            assertEquals(uriHttp, ret);
        }
    }


    @Test void backRewriteCheckTomcatPath() throws URISyntaxException {
        String repoKey = "key";
        URI uri = new URI("https", null,"me.art.com", 8081, "/v2/part1/image", null, null);
        URI uriHttp = new URI("http", null,"me.art.com", 8081, "/v2/part1/image", null, null);
        URI uriGoal = new URI("https", null,"me.art.com", 8081, "/v2/"+repoKey+"/part1/image", null, null);
        URI uriHttpGoal = new URI("http", null,"me.art.com", 8081, "/v2/"+repoKey+"/part1/image", null, null);
        {
            String scheme = "https";
            HashMap<String, List<String>> m = schemeHeaders(scheme);
            URI ret = rewriteBack(repoKey, uri, WebServerType.DIRECT, ReverseProxyMethod.REPOPATHPREFIX, m.entrySet());
            Assert.assertEquals(uriGoal, ret);
        }
        {
            String scheme = "http";
            HashMap<String, List<String>> m = schemeHeaders(scheme);
            URI ret = rewriteBack(repoKey, uri, WebServerType.DIRECT, ReverseProxyMethod.REPOPATHPREFIX, m.entrySet());
            assertEquals(scheme, ret.getScheme());
            assertEquals(uriHttpGoal, ret);
        }
        {   // NO proxy - use http
            HashMap<String, List<String>> m = headers(new HashMap<>());
            URI ret = rewriteBack(repoKey, uri, WebServerType.DIRECT, ReverseProxyMethod.REPOPATHPREFIX, m.entrySet());
            assertEquals(uriHttpGoal, ret);
        }
    }


// "test/blobs/uploads/ea25b53d-738a-4c05-af66-b204680da4a0"

    private HashMap<String, List<String>> schemeHeaders(String scheme) {

        HashMap<String, String> m = new HashMap<String, String>();
        m.put("X-ForWarded-Proto", scheme);
        return headers(m);
    }
    private HashMap<String, List<String>> headers(Map<String, String> m) {
        HashMap<String, List<String>> res = new HashMap<>();
        m.entrySet().stream().forEach(it->{res.put(it.getKey(), Collections.singletonList(it.getValue()));});
        return res;
    }

}