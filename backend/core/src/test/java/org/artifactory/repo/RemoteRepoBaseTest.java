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

import org.apache.commons.compress.utils.Sets;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.message.BasicHeader;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.HaAddon;
import org.artifactory.addon.LayoutsCoreAddon;
import org.artifactory.addon.xray.XrayAddon;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.api.repo.ResearchService;
import org.artifactory.api.request.InternalArtifactoryRequest;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.checksum.ChecksumsInfo;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.StatusHolder;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.fs.RepoResource;
import org.artifactory.io.checksum.ChecksumUtils;
import org.artifactory.md.Properties;
import org.artifactory.model.xstream.fs.PropertiesImpl;
import org.artifactory.repo.db.DbCacheRepo;
import org.artifactory.repo.db.DbStoringRepoMixin;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.repo.service.deploy.UIBuildArtifactoryRequest;
import org.artifactory.request.InternalRequestContext;
import org.artifactory.request.Request;
import org.artifactory.resource.FileResource;
import org.artifactory.resource.RemoteRepoResource;
import org.artifactory.resource.UnfoundRepoResource;
import org.artifactory.resource.UnfoundRepoResourceReason.Reason;
import org.artifactory.spring.InternalArtifactoryContext;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.artifactory.util.RepoLayoutUtils;
import org.artifactory.webapp.servlet.HttpArtifactoryRequest;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import static org.artifactory.request.ArtifactoryRequest.*;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * This class tests the behaviour of the allowsDownloadMethod and checks that it returns the proper results on different
 * scenarios
 *
 * @author Noam Tenne
 */
@Test
public class RemoteRepoBaseTest extends ArtifactoryHomeBoundTest {

    @Mock private AuthorizationService authService;
    @Mock private InternalRepositoryService internalRepoService;
    @Mock private ResearchService researchService;
    @Mock private InternalArtifactoryContext context;
    @Mock private AddonsManager addonsManager;
    @Mock private LayoutsCoreAddon layoutsCoreAddon;
    @Mock private HaAddon haAddon;
    @Mock private XrayAddon xrayAddon;
    @Mock private InternalRequestContext requestContext;
    @Mock private Request request;
    @Mock private CloseableHttpResponse response;
    @Mock private HttpRepo.TrafficAwareRemoteResourceStreamHandle trafficAwareRemoteResourceStreamHandle;

    private HttpRepoDescriptor httpRepoDescriptor = new HttpRepoDescriptor();
    private HttpRepo httpRepo;
    private RepoPath repoPath = InternalRepoPathFactory.create("remote-repo-cache", "test/test/1.0/test-1.0.jar");
    private String timeStr = "Tue, 17 Feb 2015 13:15:35 GMT";
    private ChecksumsInfo checksumsInfo;

    @BeforeClass
    public void setupClass() {
        MockitoAnnotations.initMocks(this);
        createOriginalChecksum();
    }

    @BeforeMethod
    public void setup() {
        when(context.getAuthorizationService()).thenReturn(authService);
        when(context.beanForType(AddonsManager.class)).thenReturn(addonsManager);
        when(addonsManager.addonByType(LayoutsCoreAddon.class)).thenReturn(layoutsCoreAddon);
        when(addonsManager.addonByType(HaAddon.class)).thenReturn(haAddon);
        when(addonsManager.addonByType(XrayAddon.class)).thenReturn(xrayAddon);
        when(requestContext.getRequest()).thenReturn(request);
        when(request.getRepoPath()).thenReturn(repoPath);
        ArtifactoryContextThreadBinder.bind(context);
        httpRepoDescriptor.setRepoLayout(RepoLayoutUtils.MAVEN_2_DEFAULT);
        httpRepoDescriptor.setUrl("http://someUrl");
        httpRepo = new HttpRepo(httpRepoDescriptor, internalRepoService, addonsManager, researchService,false, null);
    }

    private void createOriginalChecksum() {
        String sha1 = "f8237d8959e03355010bb85cc3dc46a46fb31110";
        String sha256 = "0f8eb4b72b6e0c9e88b388eb967b49e067ef1004bf07bffc22c3acb13b43580a";
        String md5 = "a35fe7f7fe8217b4369a0af4244d1fca";

        ChecksumInfo sha1ChecksumInfo = new ChecksumInfo(ChecksumType.sha1, sha1, null);
        ChecksumInfo sha256ChecksumInfo = new ChecksumInfo(ChecksumType.sha256, sha256, null);
        ChecksumInfo md5ChecksumInfo = new ChecksumInfo(ChecksumType.md5, md5, null);
        checksumsInfo = new ChecksumsInfo();
        checksumsInfo.addChecksumInfo(sha1ChecksumInfo);
        checksumsInfo.addChecksumInfo(sha256ChecksumInfo);
        checksumsInfo.addChecksumInfo(md5ChecksumInfo);
        when(trafficAwareRemoteResourceStreamHandle.getResponse())
                .thenReturn(response);
        when(response.getFirstHeader(HttpHeaders.LAST_MODIFIED))
                .thenReturn(new BasicHeader(HttpHeaders.LAST_MODIFIED, "Tue, 17 Feb 2015 13:15:35 GMT"));
        when(response.getFirstHeader(CHECKSUM_SHA1))
                .thenReturn(new BasicHeader(CHECKSUM_SHA1, sha1));
        when(response.getFirstHeader(CHECKSUM_SHA256))
                .thenReturn(new BasicHeader(CHECKSUM_SHA256, sha256));
        when(response.getFirstHeader(CHECKSUM_MD5))
                .thenReturn(new BasicHeader(CHECKSUM_MD5, md5));
        when(response.getFirstHeader(HttpHeaders.CONTENT_LENGTH))
                .thenReturn(new BasicHeader(HttpHeaders.CONTENT_LENGTH, "4"));
        when(response.getFirstHeader(HttpHeaders.ETAG))
                .thenReturn(new BasicHeader(HttpHeaders.ETAG, "etag-1"));

    }

    @AfterMethod
    public void tearDown() {
        ArtifactoryContextThreadBinder.unbind();
    }

    /**
     * Try to download with ordinary path
     */
    public void testDownloadWithCachePrefix() {
        expectCanRead(true);
        expectXrayBlocked();
        createCacheRepo();
        StatusHolder statusHolder = tryAllowsDownload(repoPath);
        Assert.assertFalse(statusHolder.isError());
        verifyCanRead();
    }

    /**
     * Try to download while supplying a path with no "-cache" prefix
     */
    public void testDownloadWithNoCachePrefix() {
        RepoPath repoPathNoPrefix = InternalRepoPathFactory.create("remote-repo", "test/test/1.0/test-1.0.jar");
        expectCanRead(true);
        createCacheRepo();
        StatusHolder statusHolder = tryAllowsDownload(repoPathNoPrefix);
        Assert.assertFalse(statusHolder.isError());
        verifyCanRead();
    }

    /**
     * Try to download when Anonymous mode is enabled
     */
    public void testDownloadWithAnonymousEnabledAndNoReadPermissions() {
        expectCanRead(false);       // don't give read access
        expectXrayBlocked();
        createCacheRepo();   // and enable anon
        StatusHolder statusHolder = tryAllowsDownload(repoPath);
        Assert.assertTrue(statusHolder.isError());
        verifyCanRead();
    }

    private void verifyCanRead() {
        verify(authService).canRead(repoPath);
        reset(authService);
    }

    /**
     * Try to download when the repo is blacked out
     */
    public void testDownloadWithBlackedOut() {
        httpRepoDescriptor.setBlackedOut(true);
        createCacheRepo();
        StatusHolder statusHolder = tryAllowsDownload(repoPath);
        Assert.assertTrue(statusHolder.isError());
        httpRepoDescriptor.setBlackedOut(false);
    }

    /**
     * Try to download when the repo does not serve releases
     */
    public void testDownloadWithNoReleases() {
        httpRepoDescriptor.setHandleReleases(false);
        createCacheRepo();
        StatusHolder statusHolder = tryAllowsDownload(repoPath);
        Assert.assertTrue(statusHolder.isError());
        httpRepoDescriptor.setHandleReleases(true);
    }

    /**
     * Try to download when requested resource is excluded
     */
    public void testDownloadWithExcludes() {
        httpRepoDescriptor.setExcludesPattern("test/test/1.0/**");
        createCacheRepo();
        StatusHolder statusHolder = tryAllowsDownload(repoPath);
        Assert.assertTrue(statusHolder.isError());
        httpRepoDescriptor.setExcludesPattern("");
    }

    /**
     * Verify reading of valid checksum file to test the {@link ChecksumUtils#checksumStringFromStream(InputStream)}
     * method
     */
    public void testReadValidChecksum() throws Exception {
        invokeReadChecksum("valid", "dasfasdf4r234234q32asdfadfasasdfasdf");
    }

    /**
     * Verify reading of a checksum file containing comments to test the {@link ChecksumUtils#checksumStringFromStream(InputStream)}
     * method
     */
    public void testReadCommentChecksum() throws Exception {
        invokeReadChecksum("comment", "asdfaeaef435345435asdf");
    }

    /**
     * Verify reading of a checksum file containing file description to test the {@link
     * ChecksumUtils#checksumStringFromStream(InputStream)} method
     */
    public void testReadDescChecksum() throws Exception {
        invokeReadChecksum("desc", "dasfasdf4r234234q32asdfadfasdf");
    }

    /**
     * Verify reading of an empty checksum file to test the {@link ChecksumUtils#checksumStringFromStream(InputStream)}
     * method
     */
    public void testReadEmptyChecksum() throws Exception {
        invokeReadChecksum("empty", "");
    }

    public void returnCachedResourceResourceIsFound() {
        FileResource cachedResource = mock(FileResource.class);
        when(cachedResource.isFound()).thenReturn(true);
        assertThat(httpRepo.shouldReturnCachedResource(requestContext, cachedResource)).isTrue();
    }

    public void returnCachedResourceWhenPropMismatch() {
        UnfoundRepoResource cachedResource = mock(UnfoundRepoResource.class);
        when(cachedResource.getReason()).thenReturn(Reason.PROPERTY_MISMATCH);
        assertThat(httpRepo.shouldReturnCachedResource(requestContext, cachedResource)).isTrue();
    }

    public void returnCachedResourceWhenRemoteReplicationOriginated() {
        RepoResource cachedResource = mock(RepoResource.class);
        when(requestContext.getRequest()).thenReturn(request);
        when(request.getParameter(PARAM_REPLICATION_ORIGINATED_DOWNLOAD_REQUEST)).thenReturn("true");
        assertThat(httpRepo.shouldReturnCachedResource(requestContext, cachedResource)).isTrue();

        when(request.getParameter(PARAM_REPLICATION_ORIGINATED_DOWNLOAD_REQUEST)).thenReturn(null);
        assertThat(httpRepo.shouldReturnCachedResource(requestContext, cachedResource)).isFalse();
    }

    public void testCreateRemoteResource() {
        boolean isSavedResource = ConstantValues.saveGetResource.getBoolean();
        ArtifactoryHome.get().getArtifactoryProperties().setProperty(ConstantValues.saveGetResource.getPropertyName(), Boolean.toString(true));
        long timeMillis = DateUtils.parseDate(timeStr, new String[]{ "E, d MMM yyyy HH:mm:ss Z" }).getTime();
        HttpRepo httpRepo1 = spy(httpRepo);
        RemoteRepoResource repoResource = httpRepo1.createRemoteResourceFromResponse(repoPath, response, checksumsInfo.getChecksums());
        assertTrue(checksumsInfo.isIdentical(repoResource.getInfo().getChecksumsInfo()));
        assertEquals(timeMillis, repoResource.getLastModified());
        assertEquals(4, repoResource.getSize());
        assertEquals(repoPath, repoResource.getRepoPath());
        assertEquals("etag-1", repoResource.getEtag());
        ArtifactoryHome.get().getArtifactoryProperties().setProperty(ConstantValues.saveGetResource.getPropertyName(), Boolean.toString(isSavedResource));
    }

    public void testReconstructResourceFromGetResponseIfNeeded() {
        Set<ChecksumInfo> remoteChecksums = Sets.newHashSet();
        boolean isSavedResource = ConstantValues.saveGetResource.getBoolean();
        ArtifactoryHome.get().getArtifactoryProperties().setProperty(ConstantValues.saveGetResource.getPropertyName(), Boolean.toString(true));

        RemoteRepoResource repoResource = new RemoteRepoResource(repoPath, 123, "etag", 50, remoteChecksums, null);
        long timeMillis = DateUtils.parseDate(timeStr, new String[]{ "E, d MMM yyyy HH:mm:ss Z" }).getTime();

        httpRepo.reconstructResourceFromGetResponseIfNeeded(repoResource, trafficAwareRemoteResourceStreamHandle,
                requestContext.getRequest().getRepoPath());

        assertTrue(checksumsInfo.isIdentical(repoResource.getInfo().getChecksumsInfo()));
        assertEquals(timeMillis, repoResource.getLastModified());
        assertEquals(4, repoResource.getSize());
        assertEquals(repoPath, repoResource.getRepoPath());
        Properties properties = new PropertiesImpl();
        properties.put(DbStoringRepoMixin.ETAG_PROP_KEY, repoResource.getEtag());
        properties = httpRepo.updateEtagInProperties(repoResource, properties);
        assertEquals("etag-1", properties.getFirst("artifactory.internal.etag"));
        ArtifactoryHome.get().getArtifactoryProperties().setProperty(ConstantValues.saveGetResource.getPropertyName(), Boolean.toString(isSavedResource));

    }

    public void testReconstructResourceFromGetResponseNotNeeded() {
        Set<ChecksumInfo> remoteChecksums = Sets.newHashSet();
        boolean isSavedResource = ConstantValues.saveGetResource.getBoolean();
        ArtifactoryHome.get().getArtifactoryProperties().setProperty(ConstantValues.saveGetResource.getPropertyName(), Boolean.toString(false));
        RemoteRepoResource repoResource = new RemoteRepoResource(repoPath, 123, "etag", 50, remoteChecksums, null);

        httpRepo.reconstructResourceFromGetResponseIfNeeded(repoResource, trafficAwareRemoteResourceStreamHandle,
                requestContext.getRequest().getRepoPath());

        assertEquals(remoteChecksums, repoResource.getChecksums());
        assertEquals(123, repoResource.getLastModified());
        assertEquals(50, repoResource.getSize());
        assertEquals(repoPath, repoResource.getRepoPath());
        Properties properties = new PropertiesImpl();
        properties.put(DbStoringRepoMixin.ETAG_PROP_KEY, repoResource.getEtag());
        properties = httpRepo.updateEtagInProperties(repoResource, properties);
        assertEquals("etag", properties.getFirst("artifactory.internal.etag"));
        ArtifactoryHome.get().getArtifactoryProperties().setProperty(ConstantValues.saveGetResource.getPropertyName(), Boolean.toString(isSavedResource));
    }

    @Test(dataProvider = "providePathsContainsURL")
    public void testAppendAndGetUrlWithURLInPath(String pathWithURL, String expectedIfAllowed) throws IOException {
        try {
            when(layoutsCoreAddon.translateArtifactPath(any(), any(), anyString())).thenReturn(pathWithURL);
            httpRepo.downloadResource(pathWithURL, requestContext);
            assertThat(true).isFalse();
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains(pathWithURL);
        }

        assertThat(httpRepo.appendAndGetUrl(pathWithURL, requestContext))
                .isEqualTo(expectedIfAllowed);
    }

    @Test(dataProvider = "provideValidPaths")
    public void testAppendAndGetUrlValidPath(String validPath) {
        assertThat(httpRepo.appendAndGetUrl(validPath, requestContext)).isEqualTo("http://someUrl/" + validPath);
    }

    @DataProvider
    public static Object[][] provideValidPaths() {
        return new Object[][]{
                {"some/path/to/append/"},
                {"some/path/to/append/:properties"},
        };
    }

    @DataProvider
    public static Object[][] providePathsContainsURL() {
        return new Object[][]{
                {"http://localhost:8080/some/path/to/append/", "http://localhost:8080/some/path/to/append/"},
                {"http://localhost:8080/some/path/to/append/:properties", "http://someUrl/http://localhost:8080/some/path/to/append/:properties"},
        };
    }

    public void testAlternativeRemoteSiteUrl() {
        Request request = mock(UIBuildArtifactoryRequest.class);
        assertAlternateUrl(request, "http://alternativeSite/");

        request = mock(InternalArtifactoryRequest.class);
        assertAlternateUrl(request, "http://alternativeSite/");

        request = mock(HttpArtifactoryRequest.class);
        assertAlternateUrl(request, "http://someUrl/");
    }

    private void assertAlternateUrl(Request request, String expected) {
        when(requestContext.getRequest()).thenReturn(request);
        when(request.getParameter(PARAM_ALTERNATIVE_REMOTE_SITE_URL)).thenReturn("http://alternativeSite");
        assertThat(httpRepo.appendAndGetUrl("", requestContext)).isEqualTo(expected);
    }

    public void testAlternativeRemoteDownloadUrl() {
        Request request = mock(UIBuildArtifactoryRequest.class);
        assertAlternateDownloadUrl(request, "http://alternativeSite/");

        request = mock(InternalArtifactoryRequest.class);
        assertAlternateDownloadUrl(request, "http://alternativeSite/");

        request = mock(HttpArtifactoryRequest.class);
        assertAlternateDownloadUrl(request, "");
    }

    private void assertAlternateDownloadUrl(Request request, String expected) {
        when(request.getParameter(PARAM_ALTERNATIVE_REMOTE_DOWNLOAD_URL)).thenReturn("http://alternativeSite/");
        assertThat(httpRepo.resolvePathForUrl("", request)).isEqualTo(expected);
    }

    /**
     * Check the different values that are read from the given checksum are as expected
     *
     * @param checksumType  Type of checksum test file to read
     * @param expectedValue Expected checksum value
     */
    private void invokeReadChecksum(String checksumType, String expectedValue) throws Exception {
        InputStream stream = getClass().getResourceAsStream("/org/artifactory/repo/test-" + checksumType + ".md5");
        try {
            String checksum = ChecksumUtils.checksumStringFromStream(stream);
            Assert.assertEquals(expectedValue, checksum, "Incorrect checksum value returned.");
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    private void createCacheRepo() {
        httpRepoDescriptor.setKey("remote-repo");
        DbCacheRepo localCacheRepo = new DbCacheRepo(httpRepo, null);
        ReflectionTestUtils.setField(httpRepo, "localCacheRepo", localCacheRepo);

        DbStoringRepoMixin mixin = mock(DbStoringRepoMixin.class);
        ReflectionTestUtils.setField(localCacheRepo, "mixin", mixin);
        when(mixin.itemExists(any())).thenReturn(true);
        when(mixin.getKey()).thenReturn("remote-repo-cache");
    }

    private void expectCanRead(boolean canRead) {
        when(authService.canRead(repoPath)).thenReturn(canRead);
    }

    private void expectXrayBlocked() {
        when(xrayAddon.isDownloadBlocked(any(RepoPath.class))).thenReturn(false);
    }

    /**
     * Try to download the the given path
     *
     * @param path Repopath to test the download with
     * @return StatusHolder - Contains any errors which might be monitored
     */
    private StatusHolder tryAllowsDownload(RepoPath path) {
        return httpRepo.checkDownloadIsAllowed(path);
    }
}
