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

package org.artifactory.repo.db;

import org.artifactory.api.repo.exception.RepoRejectException;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoBaseDescriptor;
import org.artifactory.fs.RepoResource;
import org.artifactory.repo.SaveResourceContext;
import org.artifactory.storage.StorageException;
import org.artifactory.util.RepoPathUtils;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the save resource process.
 *
 * @author Yossi Shaul
 * @see org.artifactory.repo.db.DbStoringRepoMixin#saveResource(org.artifactory.repo.SaveResourceContext)
 */
@Test
public class DbStoringRepoMixinSaveResourceTest {

    @Test(expectedExceptions = StorageException.class, expectedExceptionsMessageRegExp = ".*parent.*")
    public void failToSaveRootFolder() throws IOException, RepoRejectException {
        DbStoringRepoMixin<RepoBaseDescriptor> repo = createStoringRepo();
        RepoResource repoResourceMock = mock(RepoResource.class);
        when(repoResourceMock.getRepoPath()).thenReturn(RepoPathUtils.repoRootPath("repo"));
        repo.saveResource(new SaveResourceContext.Builder(repoResourceMock, (InputStream) null).build());
    }

    private DbStoringRepoMixin<RepoBaseDescriptor> createStoringRepo() {
        LocalRepoDescriptor repoDescriptor = new LocalRepoDescriptor();
        repoDescriptor.setKey("repo");
        return new DbStoringRepoMixin<>(repoDescriptor, null);
    }

}