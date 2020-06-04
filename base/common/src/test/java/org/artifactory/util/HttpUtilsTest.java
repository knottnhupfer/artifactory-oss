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

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.request.ArtifactoryRequest;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.artifactory.api.context.ArtifactoryContext.CONTEXT_ID_PROP;
import static org.easymock.EasyMock.*;
import static org.testng.Assert.assertEquals;

/**
 * Tests the helper {@link HttpUtils} class.
 *
 * @author Yossi Shaul
 */
@Test
public class HttpUtilsTest extends ArtifactoryHomeBoundTest {

    @BeforeMethod
    private void setupMock() {
        ArtifactoryContext context = createMock(ArtifactoryContext.class);
        ArtifactoryContextThreadBinder.bind(context);
        CentralConfigService cc = createMock(CentralConfigService.class);
        expect(context.getCentralConfig()).andReturn(cc).anyTimes();
        CentralConfigDescriptor ccd = createMock(CentralConfigDescriptor.class);
        expect(cc.getDescriptor()).andReturn(ccd).anyTimes();
        expect(ccd.getUrlBase()).andReturn(null).anyTimes();
        expect(context.getContextPath()).andReturn("").andReturn("test");
        replay(context, cc, ccd);
    }

    public void encodeUrlWithParameters() {
        String unescaped = "http://domain:8089/context/path?x=a/b/c&b=c";
        String escaped = HttpUtils.encodeQuery(unescaped);
        assertEquals(escaped, unescaped);
    }

    public void encodeUrlWithSpaces() {
        String unescaped = "http://domain:8089/context/file path.txt";
        String escaped = HttpUtils.encodeQuery(unescaped);
        assertEquals(escaped, "http://domain:8089/context/file%20path.txt");
    }

    public void encodeBuildUrl() {
        String unescaped = "http://127.0.0.1:53123/artifactory/api/build/moo :: moo/999";
        String escaped = HttpUtils.encodeQuery(unescaped);
        assertEquals(escaped, "http://127.0.0.1:53123/artifactory/api/build/moo%20::%20moo/999");
    }

    public void getServletContextUrl() {
        String requestUrl = "http://lala.land.com";
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", requestUrl);
        mockRequest.setServerName("lala.land.com");
        String servletContextUrl = HttpUtils.getServletContextUrl(mockRequest);
        assertEquals(servletContextUrl, requestUrl);
    }

    public void getServletContextUrlHttps() {
        String requestUrl = "https://lala.land.com";
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", requestUrl);
        mockRequest.setServerName("lala.land.com");
        mockRequest.setScheme("https");
        mockRequest.setServerPort(443);
        String servletContextUrl = HttpUtils.getServletContextUrl(mockRequest);
        assertEquals(servletContextUrl, requestUrl);
    }

    public void getServletContextUrlWithUrlBase() {
        String requestUrl = "http://lala.land.com";
        setBaseUrl("https://custombaseurl.net");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", requestUrl);
        request.setServerName("lala.land.com");
        String servletContextUrl = HttpUtils.getServletContextUrl(request);
        assertEquals(servletContextUrl, "https://custombaseurl.net");
    }

    public void getServletContextUrlWithBaseUrlAndOverridingHeader() {
        String requestUrl = "http://lala.land.com";
        setBaseUrl("https://custombaseurl.net");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", requestUrl);
        request.setServerName("lala.land.com");
        request.addHeader(ArtifactoryRequest.ARTIFACTORY_OVERRIDE_BASE_URL, "http://originartifactory.net");
        String servletContextUrl = HttpUtils.getServletContextUrl(request);
        assertEquals(servletContextUrl, "http://originartifactory.net");
    }

    public void getServletContextUrlWithJFrogOverridingHeader() {
        String requestUrl = "http://lala.land.com";
        String overrideUrl = "http://mothership.net/";
        setBaseUrl("https://custombaseurl.net");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", requestUrl);
        request.setServerName("lala.land.com");
        request.addHeader(ArtifactoryRequest.JFROG_OVERRIDE_BASE_URL, overrideUrl);
        String servletContextUrl = HttpUtils.getServletContextUrl(request);
        assertEquals(servletContextUrl, overrideUrl);

        // On second call context path is test
        servletContextUrl = HttpUtils.getServletContextUrl(request);
        assertEquals(servletContextUrl, overrideUrl + "test");
    }

    public void getServletContextUrlJfrogAndArtifactoryOverrideHeaders() {
        String requestUrl = "http://lala.land.com";
        String overrideUrl = "http://mothership.net/";
        setBaseUrl("https://custombaseurl.net");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", requestUrl);
        request.setServerName("lala.land.com");
        request.addHeader(ArtifactoryRequest.JFROG_OVERRIDE_BASE_URL, overrideUrl);
        request.addHeader(ArtifactoryRequest.ARTIFACTORY_OVERRIDE_BASE_URL, "http://art.com");
        String servletContextUrl = HttpUtils.getServletContextUrl(request);
        assertEquals(servletContextUrl, overrideUrl);
    }

    public void contextIdExtractedFromContextPath() {
        MockServletContext mockContext = new MockServletContext();
        mockContext.setContextPath("dandan");
        String contextId = HttpUtils.getContextId(mockContext);
        assertEquals(contextId, "dandan");
    }

    public void contextIdExtractedFromServletInitParam() {
        MockServletContext mockContext = new MockServletContext();
        mockContext.setContextPath("not-this");
        mockContext.setAttribute(CONTEXT_ID_PROP, "yossis");
        String contextId = HttpUtils.getContextId(mockContext);
        assertEquals(contextId, "yossis");
    }

    public void encodeFullUrlWithParams() {
        String unescaped = "http://domain:8089/context/path?x=a/b/c&b=c";
        String escaped = HttpUtils.encodeUrl(unescaped);
        assertEquals(escaped, unescaped);
    }

    public void encodeHostnameWithUnderscore() {
        // not a valid dns name but who cares
        String unescaped = "http://_domain:8089/context/path?x=a/b/c&b=c";
        String escaped = HttpUtils.encodeUrl(unescaped);
        assertEquals(escaped, unescaped);
    }

    public void encodeHostnameWithoutPort() {
        // not a valid dns name but who cares
        String unescaped = "http://localhost/context/path?x=a/b/c&b=c";
        String escaped = HttpUtils.encodeUrl(unescaped);
        assertEquals(escaped, unescaped);
    }

    public void encodeNakedUrl() {
        String unescaped = "http://domain:8089/";
        String escaped = HttpUtils.encodeUrl(unescaped);
        assertEquals(escaped, unescaped);
    }

    public void encodeUrlWithMatrixParams() {
        String unescaped = "http://domain:8089/;foo=bar";
        String escaped = HttpUtils.encodeUrl(unescaped);
        assertEquals(escaped, unescaped);
    }

    public void encodeNakedUrlWithMatrixDelimiter() {
        String unescaped = "http://domain:8089/;";
        String escaped = HttpUtils.encodeUrl(unescaped);
        assertEquals(escaped, unescaped);
    }

    public void encodeNakedUrlWithNoTrailingSlash() {
        String unescaped = "http://domain:8089";
        String escaped = HttpUtils.encodeUrl(unescaped);
        assertEquals(escaped, unescaped);
    }

    public void encodeArchiveEntryUrl() {
        String unescaped = "http://domain:8089/foo.zip!bar.ext;";
        String escaped = HttpUtils.encodeUrl(unescaped);
        assertEquals(escaped, unescaped);
    }

    public void encodeNakedUrlWithQueryDelimiter() {
        String unescaped = "http://domain:8089/?";
        String escaped = HttpUtils.encodeUrl(unescaped);
        assertEquals(escaped, unescaped);
    }

    public void encodeFullUrlWithQueryAndMatrixParams() {
        String unescaped = "http://domain:8089/context/path?x=a/b/c&b=c;foo=bar,bar=baz;doodle=moodle";
        String escaped = HttpUtils.encodeUrl(unescaped);
        assertEquals(escaped, unescaped);
    }

    public void encodeFullUrlWithSpecialChars() {
        String unescaped = "http://domain:8089/context/path?x=a/b/c&b=c;foo=bar :: baz,bar+=baz;doodle=#$moodle";
        String escaped = HttpUtils.encodeUrl(unescaped);
        assertEquals(escaped, "http://domain:8089/context/path?x=a/b/c&b=c;foo=bar%20::%20baz,bar+=baz;doodle=%23$moodle");
    }

    public void encodeSchemeLessUrlWithDoubleSlash() {
        String unescaped = "domain:8089//context/path?x=a/b/c&b=c;foo=bar,bar=baz;doodle=moodle";
        String escaped = HttpUtils.encodeUrl(unescaped);
        assertEquals(escaped, unescaped);
    }

    public void encodeFullIPv6Address() {
        String unescaped = "http://[2a05:d014:82c:c112:ecc8:404a:7917:dbc2]:8089/context/path?x=a/b/c&b=c";
        String escaped = HttpUtils.encodeUrl(unescaped);
        assertEquals(escaped, unescaped);
    }

    public void encodeFullIPv6AddressWithFourElements() {
        String unescaped = "http://[2a05::d014:82c:c112]:8089/context/path?x=a/b/c&b=c";
        String escaped = HttpUtils.encodeUrl(unescaped);
        assertEquals(escaped, unescaped);
    }

    public void encodeCompressedIPv6Address() {
        String unescaped = "http://[::1]:8089/context/path?x=a/b/c&b=c";
        String escaped = HttpUtils.encodeUrl(unescaped);
        assertEquals(escaped, unescaped);
    }

    public void encodeIPv6AddressWithZone() {
        String unescaped = "http://[fe80::1ff:fe23:4567:890a%25eth0]/foo";
        String escaped = HttpUtils.encodeUrl(unescaped);
        assertEquals(escaped, unescaped);
    }

    public void encodeAddressWithNoHost() {
        String unescaped = "http://:8080/context/path?x=a/b/c&b=c";
        String escaped = HttpUtils.encodeUrl(unescaped);
        assertEquals(escaped, unescaped);
    }

    public void encodeIPv4Address() {
        String unescaped = "http://127.0.0.1:8080/context/path?x=a/b/c&b=c";
        String escaped = HttpUtils.encodeUrl(unescaped);
        assertEquals(escaped, unescaped);
    }

    public void encodeSshAddress() {
        String unescaped = "ssh://:8080/context/path?x=a/b/c&b=c";
        String escaped = HttpUtils.encodeUrl(unescaped);
        assertEquals(escaped, unescaped);
    }

    public void testIsIPv6AddressTrue() {
        String address = "[::1]:8081";
        boolean isIPv6Address = HttpUtils.isIPv6Address(address);
        Assert.assertTrue(isIPv6Address);
    }

    public void testIsIPv6AddressFalse() {
        String address = "localhost:8081";
        boolean isIPv6Address = HttpUtils.isIPv6Address(address);
        Assert.assertFalse(isIPv6Address);
    }

    public void testIsIPv6AddressFalseWithSquareBrackets() {
        String address = "[localhost:8081]";
        boolean isIPv6Address = HttpUtils.isIPv6Address(address);
        Assert.assertFalse(isIPv6Address);
    }

    public void testIsIPv6AddressFalseWithAuthInfo() {
        String address = "admin:password@localhost:8081";
        boolean isIPv6Address = HttpUtils.isIPv6Address(address);
        Assert.assertFalse(isIPv6Address);
    }

    public void testIsIPv6AddressTrueWithAuthInfo() {
        String address = "admin:password@[::1]:8081";
        boolean isIPv6Address = HttpUtils.isIPv6Address(address);
        Assert.assertTrue(isIPv6Address);
    }

    public void testIsIPv6AddressTrueWithNakedAddress() {
        String address = "[2a05:d014:82c:c112:ecc8:404a:7917:dbc2]";
        boolean isIPv6Address = HttpUtils.isIPv6Address(address);
        Assert.assertTrue(isIPv6Address);
    }

    public void encodeUrlWithBasicAuth() {
        String unescaped = "http://admin:password@domain:8089/context/file path.txt";
        String escaped = HttpUtils.encodeQuery(unescaped);
        assertEquals(escaped, "http://admin:password@domain:8089/context/file%20path.txt");
    }

    public void testGetRequestPathWithHostHasLeadingSlash() {
        String path = HttpUtils.getRequestPath("http://localhost:8080/artifactory/api/system/ping");
        assertEquals(path, "/artifactory/api/system/ping");
    }

    public void testGetRequestPathWithoutHostIsBlank() {
        String path = HttpUtils.getRequestPath("/artifactory/api/system/ping");
        assertEquals(path, "");
    }

    public void testGetRequestPathWithoutHostIsNotBlank() {
        String path = HttpUtils.getRequestPath("localhost/artifactory/api/system/ping");
        assertEquals(path, "/artifactory/api/system/ping");
    }

    public void testTrimEnclosingSquareBrackets() {
        String address = HttpUtils.trimSquareBracketsIfNeeded("[::1]");
        assertEquals(address, "::1");
    }

    public void testTrimEnclosingSquareBracketsNoOp() {
        String address = HttpUtils.trimSquareBracketsIfNeeded("[::1]:8080");
        assertEquals(address, "[::1]:8080");
    }

    private void setBaseUrl(String baseUrl) {
        CentralConfigDescriptor ccd = ContextHelper.get().getCentralConfig().getDescriptor();
        reset(ccd);
        expect(ccd.getUrlBase()).andReturn(baseUrl).anyTimes();
        replay(ccd);
    }

    /**
     * RTFACT-9804 - Allow deployment of files with colon,
     * when using native browsing the href content should be with %3A instead of ':' char
     */
    public void encodeFileNameWithColonChar()
    {
        assertEquals(HttpUtils.encodeWithinPath("file:file"), "file%3Afile");
    }

    /**
     * RTFACT-15708
     * when Ivi resolve dependencies he dont encode special chars,
     * if package name contain '+' the href tag should contain '+' and not escaped char
     */
    public void encodeFileNameWithPlusChar() {
        assertEquals(HttpUtils.encodeWithinPath("file+file"), "file+file");
    }

    public void encodeFileNameWithSpaceChar() {
        assertEquals(HttpUtils.encodeWithinPath("file file"), "file%20file");
    }

    public void decodeFileNameWithColonChar() {
        assertEquals(HttpUtils.decodeUri("file%3Afile"), "file:file");
    }

    public void decodeFileNameWithSpaceChar() {
        assertEquals(HttpUtils.decodeUri("file%20file"), "file file");
    }

    @AfterMethod
    private void shutdownMock() {
        ArtifactoryContextThreadBinder.unbind();
    }
}