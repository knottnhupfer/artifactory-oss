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

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.xray.XrayAddon;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.StatusHolder;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.snapshot.MavenSnapshotVersionAdapter;
import org.artifactory.spring.InternalArtifactoryContext;
import org.artifactory.util.RepoLayoutUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;
import static org.testng.Assert.assertTrue;

/**
 * Unit test the DbRepoBase.
 *
 * @author Yossi Shaul
 */
@Test
public class DbRepoBaseTest {

    private AuthorizationService authService;
    private DbLocalRepo repo;
    private ArtifactoryContext context;
    private AddonsManager addonsManager;

    @BeforeMethod
    public void setup() {
        context = createNiceMock(InternalArtifactoryContext.class);
        authService = createNiceMock(AuthorizationService.class);
        addonsManager = createNiceMock(AddonsManager.class);
        expect(context.beanForType(AddonsManager.class)).andReturn(addonsManager);
        expect(context.getAuthorizationService()).andReturn(authService);
        ArtifactoryContextThreadBinder.bind(context);
        repo = createDbRepoBase();
        replay(context);
    }

    @AfterMethod
    public void cleanUp() {
        ArtifactoryContextThreadBinder.unbind();
    }

    @Test
    public void anonAccessDisabledAndNoReadPermissions() {
        expectXrayBlocked(false);
        RepoPath dummyPath = InternalRepoPathFactory.create("target", "blabla");
        expect(authService.canRead(dummyPath)).andReturn(false);
        replay(authService);

        StatusHolder status = repo.checkDownloadIsAllowed(dummyPath);

        assertTrue(status.isError(), "Download is allowed: " + status.getStatusMsg());

        verify(authService);
    }

    @Test
    public void anonAccessEnabledAndNoReadPermissions() {
        expectXrayBlocked(false);
        RepoPath dummyPath = InternalRepoPathFactory.create("target", "blabla");
        expect(authService.canRead(dummyPath)).andReturn(false);
        replay(authService);

        StatusHolder status = repo.checkDownloadIsAllowed(dummyPath);

        assertTrue(status.isError(), "Download is allowed: " + status.getStatusMsg());

        verify(authService);
    }

    @Test
    public void xrayBlockedArtifact() {
        XrayAddon xrayAddon = createNiceMock(XrayAddon.class);

        expect(addonsManager.addonByType(XrayAddon.class)).andReturn(xrayAddon);
        expect(xrayAddon.isDownloadBlocked(anyObject(RepoPath.class))).andReturn(false);
        replay(addonsManager, xrayAddon);

        RepoPath dummyPath = InternalRepoPathFactory.create("target", "blabla");
        StatusHolder status = repo.checkDownloadIsNotBlocked(dummyPath);

        assertTrue(!status.isError(), "Download is allowed: " + status.getStatusMsg());

        verify(addonsManager);
    }

    private DbLocalRepo<LocalRepoDescriptor> createDbRepoBase() {
        LocalRepoDescriptor descriptor = new LocalRepoDescriptor();
        DbLocalRepo<LocalRepoDescriptor> repo = new DbLocalRepo<LocalRepoDescriptor>(descriptor, null, null) {

            @Override
            public MavenSnapshotVersionAdapter getMavenSnapshotVersionAdapter() {
                return null;
            }

            @Override
            public boolean isSuppressPomConsistencyChecks() {
                return false;
            }
        };
        descriptor.setRepoLayout(RepoLayoutUtils.MAVEN_2_DEFAULT);
        return repo;
    }

    private void expectXrayBlocked(boolean blocked) {
        XrayAddon xrayAddon = createNiceMock(XrayAddon.class);
        expect(addonsManager.addonByType(XrayAddon.class)).andReturn(xrayAddon);
        expect(xrayAddon.isDownloadBlocked(anyObject(RepoPath.class))).andReturn(blocked);
        replay(addonsManager, xrayAddon);
    }
}
