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

package org.artifactory.repo.remote.browse;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.repo.http.GuaveCacheConnectionManagersHolder;
import org.artifactory.repo.http.IdleConnectionMonitorService;
import org.artifactory.repo.http.IdleConnectionMonitorServiceImpl;
import org.artifactory.spring.InternalArtifactoryContext;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.artifactory.test.ArtifactoryHomeStub;
import org.artifactory.test.mock.SimpleMockServer;
import org.artifactory.util.HttpClientConfigurator;
import org.artifactory.util.StringInputStream;
import org.easymock.EasyMock;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Unit tests for {@link S3RepositoryBrowser}. Some tests are disabled because they require internet connection. They
 * should be executed by developers for debugging only.
 *
 * @author Yossi Shaul
 */
@Test
public class S3RepositoryBrowserTest extends ArtifactoryHomeBoundTest {
    private static final Logger log = LoggerFactory.getLogger(S3RepositoryBrowserTest.class);

    static {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        lc.getLogger(S3RepositoryBrowserTest.class).setLevel(Level.INFO);
    }

    private SimpleMockServer server;
    private CloseableHttpClient client;
    private S3RepositoryBrowser s3Browser;

    public void detectRootLocal() throws IOException {
        String rootUrl = s3Browser.detectRootUrl(server.getBaseUrl() + testResourcesPath + "s3-nosuchkey.xml");
        assertEquals(rootUrl, server.getBaseUrl(), "Wrong root url detected");
    }

    @BeforeMethod
    public void beforeTestMethod() {
        bindArtifactoryHome();

        getBound().setProperty(ConstantValues.idleConnectionMonitorInterval, "10");
        getBound().setProperty(ConstantValues.disableIdleConnectionMonitoring, "false");
        IdleConnectionMonitorService idleConnectionMonitorService = new IdleConnectionMonitorServiceImpl(new GuaveCacheConnectionManagersHolder());

        ArtifactoryContext contextMock = EasyMock.createMock(InternalArtifactoryContext.class);
        EasyMock.expect(contextMock.beanForType(IdleConnectionMonitorService.class))
                .andReturn(idleConnectionMonitorService).anyTimes();
        ArtifactoryContextThreadBinder.bind(contextMock);
        EasyMock.replay(contextMock);

        server = new SimpleMockServer(new S3Handler());
        server.start();
        ArtifactoryHomeStub props = new ArtifactoryHomeStub();
        props.setProperty(ConstantValues.artifactoryVersion, "TEST");
        ArtifactoryHome.bind(props);

        client = new HttpClientConfigurator().connectionTimeout(30000).socketTimeout(2000).noRetry().build();

        HttpExecutor httpExecutor = method -> {
            log.info("Executing " + method.getURI() + " on dummy S3 repo!");
            return client.execute(method);
        };
        s3Browser = new S3RepositoryBrowser(httpExecutor);
    }

    @AfterMethod
    public void shutdown() throws IOException {
        if (client != null) {
            client.close();
        }
        server.stop();
        ArtifactoryContextThreadBinder.unbind();
        ArtifactoryHome.unbind();
    }

    public void listRemoteItems() throws IOException {
        ReflectionTestUtils.setField(s3Browser, "rootUrl", server.getBaseUrl());
        List<RemoteItem> result = s3Browser.listContent(
                server.getBaseUrl() + testResourcesPath + "s3-prefix-org.springframework.xml");
        assertEquals(result.size(), 35, "Unexpected content count");

        RemoteItem directoryItem = result.get(0);
        assertEquals(directoryItem.getUrl(), server.getBaseUrl() + "maven/bundles/release/org/springframework/batch/");
        assertTrue(directoryItem.isDirectory());
        assertEquals(directoryItem.getSize(), 0, "No size for directories");
        assertEquals(directoryItem.getLastModified(), 0);

        // files after folders
        assertEquals(result.get(33).getUrl(),
                server.getBaseUrl() + "maven/bundles/release/org/springframework/roo_$folder$");
        assertFalse(result.get(33).isDirectory());
        RemoteItem fileItem = result.get(34);
        assertEquals(fileItem.getUrl(), server.getBaseUrl() + "maven/bundles/release/org/springframework/my.txt");
        assertFalse(fileItem.isDirectory());
        assertEquals(fileItem.getSize(), 23);
        assertEquals(fileItem.getLastModified(), 1305442594000L);
    }

    public void listRemoteItemsFolderAndFilesWithSameName() throws IOException {
        // some s3 repositories (e.g., terracotta http://repo.terracotta.org/?delimiter=/&prefix=maven2/) has files and
        // folders with the same name (for instance file named 'org' and directory named 'org/' under the same directory)
        // in such a case we prefer the directory and don't return the file
        ReflectionTestUtils.setField(s3Browser, "rootUrl", server.getBaseUrl());
        List<RemoteItem> result = s3Browser.listContent(server.getBaseUrl() + testResourcesPath + "s3-terracotta.xml");
        assertEquals(result.size(), 6, "Unexpected content count");
    }

    @Test(enabled = false)
    public void isS3RepositoryAWS() {
        boolean s3Repository = S3RepositoryBrowser.isS3Repository(
                "https://lang-python.s3.amazonaws.com/", client);
        assertTrue(s3Repository, "Expected true for S3 repository");
    }

    @Test(enabled = false)
    public void isS3RepositoryGS() {
        boolean s3Repository = S3RepositoryBrowser.isS3Repository(
                "https://storage.googleapis.com/golang/ ", client);
        assertTrue(s3Repository, "Expected true for S3 repository");
    }


    @Test(enabled = false)
    public void isS3RepositorySpring() {
        boolean s3Repository = S3RepositoryBrowser.isS3Repository(
                "http://repository.springsource.com/maven/bundles/release", client);
        assertTrue(s3Repository, "Expected true for S3 repository");
    }

    @Test(enabled = false)
    public void isS3RepositoryTerracotta() {
        boolean s3Repository = S3RepositoryBrowser.isS3Repository("http://repo.terracotta.org/", client);
        assertTrue(s3Repository, "Expected true for S3 repository");
    }

    @Test(enabled = false)
    public void isS3RepositoryNonS3() {
        boolean s3Repository = S3RepositoryBrowser.isS3Repository("http://repo.jfrog.org/artifactory/repo/org", client);
        assertFalse(s3Repository, "Expected false for S3 repository");
    }

    @Test(enabled = false)
    public void detectRootUsingRoot() throws IOException {
        String rootUrl = s3Browser.detectRootUrl("http://repository.springsource.com/");
        assertEquals(rootUrl, "http://repository.springsource.com/");
    }

    @Test(enabled = false)
    public void detectRootUsingRootNoSlash() throws IOException {
        String rootUrl = s3Browser.detectRootUrl("http://repository.springsource.com");
        assertEquals(rootUrl, "http://repository.springsource.com/");
    }

    @Test(enabled = false)
    public void detectRootTerracottaUsingRoot() throws IOException {
        String rootUrl = s3Browser.detectRootUrl("http://repo.terracotta.org/");
        assertEquals(rootUrl, "http://repo.terracotta.org/");
    }

    @Test(enabled = false)
    public void detectRootTerracottaNoSlash() throws IOException {
        String rootUrl = s3Browser.detectRootUrl("http://repo.terracotta.org/maven2");
        assertEquals(rootUrl, "http://repo.terracotta.org/");
    }

    @Test(enabled = false)
    public void detectRootTerracottaExistingSubFolder() throws IOException {
        String rootUrl = s3Browser.detectRootUrl("http://repo.terracotta.org/maven2/org/");
        assertEquals(rootUrl, "http://repo.terracotta.org/");
    }

    @Test(enabled = false)
    public void detectRootTerracottaExistingSubFolderNoSlash() throws IOException {
        String rootUrl = s3Browser.detectRootUrl("http://repo.terracotta.org/maven2/org");
        assertEquals(rootUrl, "http://repo.terracotta.org/");
    }

    @Test(enabled = false)
    public void detectRootTerracottaNonExistingSubFolder() throws IOException {
        String rootUrl = s3Browser.detectRootUrl("http://repo.terracotta.org/maven2/blabla/");
        assertEquals(rootUrl, "http://repo.terracotta.org/");
    }

    @Test(enabled = false)
    public void detectRootTerracottaNonExistingSubFolderNoSlash() throws IOException {
        String rootUrl = s3Browser.detectRootUrl("http://repo.terracotta.org/maven2/blabla");
        assertEquals(rootUrl, "http://repo.terracotta.org/");
    }

    @Test(enabled = false)
    public void detectRoot() throws IOException {
        String rootUrl = s3Browser.detectRootUrl("http://repo.terracotta.org/org/test");
        assertEquals(rootUrl, "http://repo.terracotta.org/");
    }

    private class S3Handler extends AbstractHandler {
        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
                throws IOException {
            String path = target;
            // compose the resource path based on the prefix
            String prefix = request.getParameter("prefix");
            if (StringUtils.isNotBlank(prefix)) {
                path += prefix;
            }
            InputStream resource = SimpleMockServer.class.getResourceAsStream(path);
            if (resource == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                // assume the root is the base http, so use the target path as the Key
                String key = StringUtils.removeStart(target, "/");
                String responseBody = "<Error>" +
                        "<Code>NoSuchKey</Code>" +
                        "<Message>The specified key does not exist.</Message>" +
                        "<Key>" + key + "</Key>" +
                        "<RequestId>E69C77E280506925</RequestId>" +
                        "<HostId>AfNsMbcTZ0kw77y06lcsMEb6oxENUr80Xa0yR2P3kRs9fajMBHr0i1NYNRtjd8mV</HostId>" +
                        "</Error>";
                resource = new StringInputStream(responseBody);
            }
            IOUtils.copy(resource, response.getOutputStream());
            baseRequest.setHandled(true);
        }
    }

    private static final String testResourcesPath = "org/artifactory/repo/remote/browse/s3/";

}
