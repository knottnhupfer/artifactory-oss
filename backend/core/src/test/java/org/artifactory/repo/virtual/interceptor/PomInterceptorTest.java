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

package org.artifactory.repo.virtual.interceptor;

import org.artifactory.api.repo.exception.RepoRejectException;
import org.artifactory.descriptor.repo.PomCleanupPolicy;
import org.artifactory.fs.RepoResource;
import org.artifactory.io.SimpleResourceStreamHandle;
import org.artifactory.md.MetadataInfo;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.model.xstream.fs.FileInfoImpl;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.repo.virtual.VirtualRepo;
import org.artifactory.request.NullRequestContext;
import org.artifactory.resource.FileResource;
import org.artifactory.resource.MetadataResource;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.jfrog.common.ResourceUtils.getResource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * @author Uriah Levy
 */
@Test
public class PomInterceptorTest extends ArtifactoryHomeBoundTest {

    public void sameResourceReturnedIfNotPom() {
        RepoPathImpl nonPomRepoPath = new RepoPathImpl("repo", "not-pom");
        RepoResource original = new FileResource(new FileInfoImpl(nonPomRepoPath));
        RepoResource returnedResource = new PomInterceptor(null).onBeforeReturn(null,
                new NullRequestContext(nonPomRepoPath), original);
        assertSame(returnedResource, original);
    }

    public void sameResourceReturnedOnNothingPolicy() {
        VirtualRepo virtualRepo = mock(VirtualRepo.class);
        when(virtualRepo.getPomRepositoryReferencesCleanupPolicy()).thenReturn(PomCleanupPolicy.nothing);
        RepoPathImpl pomRepoPath = new RepoPathImpl("repo", "some.pom");
        RepoResource original = new FileResource(new FileInfoImpl(pomRepoPath));
        RepoResource returnedResource = new PomInterceptor(null)
                .onBeforeReturn(virtualRepo, new NullRequestContext(pomRepoPath), original);
        assertSame(returnedResource, original);
    }

    public void unchangedPomResourceNotSavedWithDiscardPolicy() throws IOException, RepoRejectException {
        VirtualRepo virtualRepo = Mockito.mock(VirtualRepo.class);
        when(virtualRepo.getPomRepositoryReferencesCleanupPolicy()).thenReturn(PomCleanupPolicy.discard_any_reference);
        RepoPathImpl pomRepoPath = new RepoPathImpl("repo", "nothing-to-clean.pom");
        RepoResource original = new FileResource(new FileInfoImpl(pomRepoPath));
        InternalRepositoryService repoService = mock(InternalRepositoryService.class);
        when(repoService.repositoryByKey("a-repo")).thenReturn(null);
        when(repoService.getResourceStreamHandle(any(), any(), any())).thenReturn(new SimpleResourceStreamHandle(
                getResource("/org/artifactory/repo/virtual/interceptor/clean-test.pom")));

        RepoResource returnedResource = new PomInterceptor(repoService)
                .onBeforeReturn(virtualRepo, new NullRequestContext(pomRepoPath), original);

        assertSame(returnedResource, original);
        verify(repoService, times(0)).saveResource(any(), any());
    }

    public void changedPomResourceSavedWithDiscardPolicy() throws IOException, RepoRejectException {
        VirtualRepo virtualRepo = mock(VirtualRepo.class);
        when(virtualRepo.getPomRepositoryReferencesCleanupPolicy()).thenReturn(PomCleanupPolicy.discard_any_reference);
        when(virtualRepo.getKey()).thenReturn("virtual");
        RepoPathImpl pomRepoPath = new RepoPathImpl("a-repo", "clean-this.pom");
        RepoResource original = new FileResource(new FileInfoImpl(pomRepoPath));
        InternalRepositoryService repoService = mock(InternalRepositoryService.class);
        when(repoService.getResourceStreamHandle(any(), any(), any())).thenReturn(new SimpleResourceStreamHandle(
                getResource("/org/artifactory/repo/virtual/interceptor/activeByDefault-test.pom")));
        when(repoService.repositoryByKey("a-repo")).thenReturn(null);
        when(repoService.saveResource(any(), any())).thenReturn(new MetadataResource((MetadataInfo) null));

        RepoResource returnedResource = new PomInterceptor(repoService)
                .onBeforeReturn(virtualRepo, new NullRequestContext(pomRepoPath), original);

        assertNotSame(returnedResource, original);
        assertTrue(returnedResource.isFound());
        verify(repoService, times(1)).saveResource(any(), any());
    }
}