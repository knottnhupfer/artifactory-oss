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

import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.mime.MavenNaming;
import org.artifactory.model.common.RepoPathImpl;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author dudim
 */
@PrepareForTest({MavenNaming.class})
public class UniqueSnapshotArtifactExpirableOverridableTest extends PowerMockTestCase {

    @BeforeMethod
    public void setUp() throws Exception {
        PowerMockito.mockStatic(MavenNaming.class);
    }

    @Test
    public void testIsExpirableValid() {
        UniqueSnapshotArtifactExpirableOverridable uniqueSnapshotArtifactExpirableOverridable =
                new UniqueSnapshotArtifactExpirableOverridable();
        RepoDescriptor repoDescriptor = mock(RepoDescriptor.class);
        when(repoDescriptor.getType()).thenReturn(RepoType.Maven);
        when(repoDescriptor.isMavenRepoLayout()).thenReturn(true);
        PowerMockito.when(MavenNaming.isNonUniqueSnapshot("package-SNAPSHOT.jar")).thenReturn(true);
        Assert.assertTrue(uniqueSnapshotArtifactExpirableOverridable
                .isExpirable(repoDescriptor, new RepoPathImpl("maven", "package-SNAPSHOT.jar")));
    }

    @Test
    public void testIsExpirableFalseWhenNullRepoType() {
        UniqueSnapshotArtifactExpirableOverridable uniqueSnapshotArtifactExpirableOverridable =
                new UniqueSnapshotArtifactExpirableOverridable();
        RepoDescriptor repoDescriptor = mock(RepoDescriptor.class);
        when(repoDescriptor.getType()).thenReturn(null);
        Assert.assertFalse(uniqueSnapshotArtifactExpirableOverridable
                .isExpirable(repoDescriptor, new RepoPathImpl("maven", "package-SNAPSHOT.jar")));
    }

    @Test
    public void testIsExpirableFalseWhenNotMavenRepoLayout() {
        UniqueSnapshotArtifactExpirableOverridable uniqueSnapshotArtifactExpirableOverridable =
                new UniqueSnapshotArtifactExpirableOverridable();
        RepoDescriptor repoDescriptor = mock(RepoDescriptor.class);
        when(repoDescriptor.getType()).thenReturn(RepoType.Bower);
        when(repoDescriptor.isMavenRepoLayout()).thenReturn(false);
        Assert.assertFalse(uniqueSnapshotArtifactExpirableOverridable
                .isExpirable(repoDescriptor, new RepoPathImpl("maven", "package-SNAPSHOT.jar")));
    }

    @Test
    public void testIsExpirableFalseWhenNonUniqueSnapshot() {
        UniqueSnapshotArtifactExpirableOverridable uniqueSnapshotArtifactExpirableOverridable =
                new UniqueSnapshotArtifactExpirableOverridable();
        RepoDescriptor repoDescriptor = mock(RepoDescriptor.class);
        when(repoDescriptor.getType()).thenReturn(RepoType.Maven);
        when(repoDescriptor.isMavenRepoLayout()).thenReturn(true);
        PowerMockito.when(MavenNaming.isNonUniqueSnapshot("package.jar")).thenReturn(false);
        Assert.assertFalse(uniqueSnapshotArtifactExpirableOverridable
                .isExpirable(repoDescriptor, new RepoPathImpl("maven", "package.jar")));
    }
}