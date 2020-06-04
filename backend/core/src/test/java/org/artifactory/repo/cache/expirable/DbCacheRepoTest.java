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

package org.artifactory.repo.cache.expirable;

import com.google.common.collect.ImmutableMap;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.descriptor.repo.ChecksumPolicyType;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.factory.InfoFactory;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.RepoResource;
import org.artifactory.mime.MavenNaming;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RemoteRepo;
import org.artifactory.repo.db.DbCacheRepo;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.resource.FileResource;
import org.artifactory.resource.MetadataResource;
import org.artifactory.resource.MutableRepoResourceInfo;
import org.artifactory.spring.ArtifactoryApplicationContext;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.artifactory.util.RepoLayoutUtils;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Yoav Landman
 */
public class DbCacheRepoTest extends ArtifactoryHomeBoundTest {
    public static final String REPO_KEY = "repox";
    protected InfoFactory factory = InfoFactoryHolder.get();
    @Mock
    private InternalRepositoryService repositoryService;
    @Mock
    private ArtifactoryApplicationContext context;

    @BeforeMethod
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);

        CacheExpiryImpl cacheExpiry = new CacheExpiryImpl();
        Mockito.when(context.beanForType(CacheExpiry.class)).thenReturn(cacheExpiry);
        cacheExpiry.setApplicationContext(context);
        Mockito.when(context.beansForType(CacheExpiryChecker.class))
                .thenReturn(ImmutableMap.of(
                        "non-unique", new NonUniqueSnapshotArtifactExpirableOrOverridable(repositoryService),
                        "maven", new MavenIndexChecker(),
                        "maven-metadata", new MavenMetadataChecker()));
        cacheExpiry.init();

        ArtifactoryContextThreadBinder.bind(context);
    }

    @AfterMethod
    public void afterMethod() {
        ArtifactoryContextThreadBinder.unbind();
    }

    @Test
    public void testExpiry() throws InterruptedException {
        FileResource releaseRes = new FileResource(factory.createFileInfo(
                factory.createRepoPath(REPO_KEY, "g1/g2/g3/a/v1/a.jar")));
        releaseRes.getInfo().setLastModified(0);
        FileResource snapRes = new FileResource(factory.createFileInfo(
                factory.createRepoPath(REPO_KEY, "g1/g2/g3/a/v1-SNAPSHOT/a-v1-SNAPSHOT.pom")));
        snapRes.getInfo().setLastModified(0);
        FileResource uniqueSnapRes = new FileResource(factory.createFileInfo(
                factory.createRepoPath(REPO_KEY, "g1/g2/g3/a/v1-SNAPSHOT/a-v1-20081214.090217-4.pom")));
        uniqueSnapRes.getInfo().setLastModified(0);
        FileResource nonSnapRes = new FileResource(factory.createFileInfo(
                factory.createRepoPath(REPO_KEY, "g1/g2/g3/a/v1/aSNAPSHOT.pom")));
        nonSnapRes.getInfo().setLastModified(0);
        MetadataResource relMdRes = new MetadataResource(
                InternalRepoPathFactory.create(REPO_KEY, "g1/g2/g3/a/v1/maven-metadata.xml"));
        ((MutableRepoResourceInfo) relMdRes.getInfo()).setLastModified(0);
        FileResource snapMdRes = new FileResource(factory.createFileInfo(
                factory.createRepoPath(REPO_KEY, "g1/g2/g3/a/v1-SNAPSHOT/maven-metadata.xml")));
        snapMdRes.getInfo().setLastModified(0);
        FileResource nonsnapMdRes = new FileResource(factory.createFileInfo(
                factory.createRepoPath(REPO_KEY, "g1/g2/g3/a/v1/maven-metadata.metadata")));
        nonsnapMdRes.getInfo().setLastModified(0);
        FileResource indexRes = new FileResource(factory.createFileInfo(
                factory.createRepoPath(REPO_KEY, MavenNaming.NEXUS_INDEX_GZ)));
        indexRes.getInfo().setLastModified(0);

        // Sleep so we will have difference with the last updated and the current time
        Thread.sleep(100);

        RemoteRepo<?> remoteRepo = createRemoteRepoMock(0L, RepoType.YUM);

        DbCacheRepo cacheRepo = new DbCacheRepo(remoteRepo, null);

        assertFalse(isExpired(cacheRepo, releaseRes));
        assertTrue(isExpired(cacheRepo, snapRes));
        assertFalse(isExpired(cacheRepo, uniqueSnapRes));
        assertFalse(isExpired(cacheRepo, nonSnapRes));
        assertTrue(isExpired(cacheRepo, relMdRes));
        assertTrue(isExpired(cacheRepo, snapMdRes));
        assertFalse(isExpired(cacheRepo, nonsnapMdRes));
        assertTrue(isExpired(cacheRepo, indexRes));

        remoteRepo = createRemoteRepoMock(10L, RepoType.YUM);
        cacheRepo = new DbCacheRepo(remoteRepo, null);

        assertFalse(isExpired(cacheRepo, indexRes));
    }

    public boolean isExpired(DbCacheRepo cacheRepo, RepoResource releaseRes) {
        return ReflectionTestUtils.invokeMethod(cacheRepo, "isExpired", releaseRes);
    }

    private RemoteRepo<?> createRemoteRepoMock(long expiry, RepoType yum) {
        RemoteRepo remoteRepo = Mockito.mock(RemoteRepo.class);
        HttpRepoDescriptor httpRepoDescriptor = new HttpRepoDescriptor();
        httpRepoDescriptor.setChecksumPolicyType(ChecksumPolicyType.FAIL);
        httpRepoDescriptor.setRepoLayout(RepoLayoutUtils.MAVEN_2_DEFAULT);
        httpRepoDescriptor.setKey(REPO_KEY);
        httpRepoDescriptor.setType(RepoType.Maven);
        Mockito.when(remoteRepo.getDescriptor()).thenReturn(httpRepoDescriptor);
        Mockito.when(remoteRepo.getDescription()).thenReturn("desc");
        Mockito.when(remoteRepo.getKey()).thenReturn(REPO_KEY);
        Mockito.when(remoteRepo.getRetrievalCachePeriodSecs()).thenReturn(expiry);
        Mockito.when(remoteRepo.getUrl()).thenReturn("http://jfrog");
        Mockito.when(repositoryService.repositoryByKey(REPO_KEY + "-cache")).thenReturn(remoteRepo);
        return remoteRepo;
    }
}