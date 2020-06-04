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

import org.artifactory.descriptor.repo.LocalCacheRepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.repo.LocalCacheRepo;
import org.artifactory.repo.service.InternalRepositoryService;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.expect;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Noam Y. Tenne
 */
public class NonUniqueSnapshotArtifactExpirableOrOverridableTest {

    private NonUniqueSnapshotArtifactExpirableOrOverridable expirable;
    private InternalRepositoryService repoService;

    @Test
    public void testIsExpirableOnNoIntegrationModuleInfo() throws Exception{
        LocalCacheRepo localCacheRepo = getLocalCacheRepoMock("momo");
        expectUnExpirable(localCacheRepo, "momo");
        EasyMock.verify(localCacheRepo);
    }

    @Test
    public void testIsExpirable() throws Exception{
        expectExpirable("g/a/1.0-SNAPSHOT/artifact-5.4-SNAPSHOT.pom");

        LocalCacheRepo localCacheRepo = getLocalCacheRepoMock("g/a/1.0-SNAPSHOT/artifact-5.4-20081214.090217-4.pom");

        expectUnExpirable(localCacheRepo, "g/a/1.0/artifact-5.4-20081214.090217-4.pom");
        expectUnExpirable(localCacheRepo, "g/a/1.0-20081214.090217-4/artifact-5.4-20081214.090217-4.pom");
        expectUnExpirable(localCacheRepo, "g/a/1.0-SNAPSHOT/artifact-5.4-20081214.090217-4.pom");
        EasyMock.verify(localCacheRepo);
    }

    private void expectExpirable(String artifactPath) throws Exception {
        LocalCacheRepo localCacheRepo = getLocalCacheRepoMock(artifactPath);

        assertTrue(expirable.isExpirable(RepoType.Maven, "key", artifactPath),
                artifactPath + " Should be expirable.");
        assertTrue(expirable.isExpirable(RepoType.Gradle, "key", artifactPath),
                artifactPath + " Should be expirable.");
        assertTrue(expirable.isExpirable(RepoType.Ivy, "key", artifactPath),
                artifactPath + " Should be expirable.");
        assertTrue(expirable.isExpirable(RepoType.SBT, "key", artifactPath),
                artifactPath + " Should be expirable.");
        assertTrue(expirable.isOverridable(localCacheRepo, artifactPath),
                artifactPath + " Should be overridable.");
        EasyMock.verify(localCacheRepo);
    }

    private void expectUnExpirable(LocalCacheRepo localCacheRepo, String artifactPath) {
        assertFalse(expirable.isExpirable(RepoType.Maven, "key", artifactPath),
                artifactPath + " Shouldn't be expirable.");
        assertFalse(expirable.isOverridable(localCacheRepo, artifactPath),
                artifactPath + " Shouldn't be overridable.");
    }

    private LocalCacheRepo getLocalCacheRepoMock(String artifactPath) throws NoSuchFieldException, IllegalAccessException {
        LocalCacheRepo localCacheRepo = EasyMock.createMock(LocalCacheRepo.class);
        repoService = EasyMock.createMock(InternalRepositoryService.class);
        expirable = new NonUniqueSnapshotArtifactExpirableOrOverridable(repoService);
        expect(repoService.repositoryByKey("key")).andReturn(localCacheRepo).anyTimes();
        LocalCacheRepoDescriptor descriptor = EasyMock.createMock(LocalCacheRepoDescriptor.class);
        expect(descriptor.getType()).andReturn(RepoType.Maven).anyTimes();
        expect(localCacheRepo.getDescriptor()).andReturn(descriptor).anyTimes();
        expect(localCacheRepo.getKey()).andReturn("key").anyTimes();
        EasyMock.replay(localCacheRepo, descriptor, repoService);
        return localCacheRepo;
    }
}