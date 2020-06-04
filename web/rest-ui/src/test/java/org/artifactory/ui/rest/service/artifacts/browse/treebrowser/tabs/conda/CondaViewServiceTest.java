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

package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.conda;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.conda.CondaAddon;
import org.artifactory.addon.conda.CondaMetadataInfo;
import org.artifactory.addon.conda.CondaUiDependency;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.ArtifactoryRestResponse;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.conda.CondaArtifactInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.conda.CondaInfo;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.Arrays;

import static org.mockito.Mockito.*;

/**
 * @author Uriah Levy
 * @author Dudi Morad
 */
@Test
public class CondaViewServiceTest {

    @Test(dataProvider = "extensions")
    public void testExecute(String extension) {
        RepositoryService repositoryServiceMock = mock(RepositoryService.class);
        AuthorizationService authorizationServiceMock = mock(AuthorizationService.class);
        AddonsManager addonsManagerMock = mock(AddonsManager.class);
        CondaViewService condaViewService = spy(
                new CondaViewService(authorizationServiceMock, repositoryServiceMock, addonsManagerMock));
        ArtifactoryRestResponse artifactoryRestResponseMock = mock(ArtifactoryRestResponse.class);
        ArtifactoryRestRequest artifactoryRestRequestMock = mock(ArtifactoryRestRequest.class);
        CondaArtifactInfo requestArtifactInfo = new CondaArtifactInfo();
        requestArtifactInfo.setRepoKey("conda-local");
        requestArtifactInfo.setPath("package" + extension);
        RepoPath repoPath = RepoPathFactory.create(requestArtifactInfo.getRepoKey(), requestArtifactInfo.getPath());
        when(artifactoryRestRequestMock.getImodel()).thenReturn(requestArtifactInfo);
        when(authorizationServiceMock.canRead(repoPath)).thenReturn(true);
        when(repositoryServiceMock.isVirtualRepoExist(requestArtifactInfo.getRepoKey())).thenReturn(false);
        condaViewService.execute(artifactoryRestRequestMock, artifactoryRestResponseMock);
        verify(condaViewService, times(1)).getCondaMetaData(requestArtifactInfo, repoPath, addonsManagerMock);

    }

    @Test(dataProvider = "extensions")
    public void testExecuteLocalRepository(String extension) {
        RepositoryService repositoryServiceMock = mock(RepositoryService.class);
        AuthorizationService authorizationServiceMock = mock(AuthorizationService.class);
        CondaViewService condaViewService = new CondaViewService(authorizationServiceMock, repositoryServiceMock,
                mock(AddonsManager.class));
        ArtifactoryRestResponse artifactoryRestResponseMock = mock(ArtifactoryRestResponse.class);
        ArtifactoryRestRequest artifactoryRestRequestMock = mock(ArtifactoryRestRequest.class);
        CondaArtifactInfo requestArtifactInfo = new CondaArtifactInfo();
        requestArtifactInfo.setRepoKey("conda-local");
        requestArtifactInfo.setPath("package" + extension);
        when(artifactoryRestRequestMock.getImodel()).thenReturn(requestArtifactInfo);
        when(repositoryServiceMock.isVirtualRepoExist(requestArtifactInfo.getRepoKey())).thenReturn(false);
        when(authorizationServiceMock
                .canRead(RepoPathFactory.create(requestArtifactInfo.getRepoKey(), requestArtifactInfo.getPath())))
                .thenReturn(false);
        when(artifactoryRestResponseMock.responseCode(anyInt())).thenReturn(artifactoryRestResponseMock);
        when(artifactoryRestResponseMock.buildResponse()).thenReturn(Response.status(403).build());
        condaViewService.execute(artifactoryRestRequestMock, artifactoryRestResponseMock);
        verify(repositoryServiceMock, times(0)).getVirtualFileInfo(any());
    }

    @Test(dataProvider = "extensions")
    public void testGetCondaMetaData(String extension) {
        RepositoryService repositoryServiceMock = mock(RepositoryService.class);
        AuthorizationService authorizationServiceMock = mock(AuthorizationService.class);
        AddonsManager addonsManagerMock = mock(AddonsManager.class);
        CondaAddon condaAddonMock = mock(CondaAddon.class);
        when(addonsManagerMock.addonByType(CondaAddon.class)).thenReturn(condaAddonMock);
        CondaViewService condaViewService = new CondaViewService(authorizationServiceMock, repositoryServiceMock,
                addonsManagerMock);
        CondaArtifactInfo requestArtifactInfo = new CondaArtifactInfo();
        requestArtifactInfo.setRepoKey("conda-local");
        requestArtifactInfo.setPath("package" + extension);
        RepoPath repoPath = RepoPathFactory.create(requestArtifactInfo.getRepoKey(), requestArtifactInfo.getPath());
        CondaMetadataInfo condaMetadataInfo = createMetadataInfo();
        when(condaAddonMock.getCondaMetadataToUiModel(repoPath)).thenReturn(condaMetadataInfo);
        CondaArtifactInfo condaArtifactInfo = condaViewService
                .getCondaMetaData(requestArtifactInfo, repoPath, addonsManagerMock);
        CondaInfo condaInfo = condaArtifactInfo.getCondaInfo();
        Assert.assertEquals(condaInfo.getArch(), "Arch");
        Assert.assertEquals(condaInfo.getPlatform(), "Platform");
        Assert.assertEquals(condaInfo.getBuild(), "Build");
        Assert.assertEquals(condaInfo.getBuildNumber().intValue(), 1);
        Assert.assertEquals(condaInfo.getDepends(),
                Arrays.asList(new CondaUiDependency("depend1 <=3.0.1"), new CondaUiDependency("depend2 <=2.0.0")));
        Assert.assertEquals(condaInfo.getLicense(), "License");
        Assert.assertEquals(condaInfo.getName(), "Name");
        Assert.assertEquals(condaInfo.getVersion(), "Version");
        Assert.assertEquals(condaInfo.getNoarch(), "NoArch");
        Assert.assertEquals(condaInfo.getFeatures(), "FeAtures");
        Assert.assertEquals(condaInfo.getTrackFeatures(), "TrackFeatures");
        Assert.assertEquals(condaInfo.getLicenseFamily(), "family");
    }

    private CondaMetadataInfo createMetadataInfo() {
        CondaMetadataInfo condaMetadataInfo = new CondaMetadataInfo();
        condaMetadataInfo.setArch("Arch");
        condaMetadataInfo.setPlatform("Platform");
        condaMetadataInfo.setBuild("Build");
        condaMetadataInfo.setBuildNumber(1);
        condaMetadataInfo.setDepends(
                Arrays.asList(new CondaUiDependency("depend1 <=3.0.1"), new CondaUiDependency("depend2 <=2.0.0")));
        condaMetadataInfo.setLicense("License");
        condaMetadataInfo.setName("Name");
        condaMetadataInfo.setTimestamp(2L);
        condaMetadataInfo.setVersion("Version");
        condaMetadataInfo.setNoarch("NoArch");
        condaMetadataInfo.setFeatures("FeAtures");
        condaMetadataInfo.setTrackFeatures("TrackFeatures");
        condaMetadataInfo.setLicenseFamily("family");
        return condaMetadataInfo;
    }

    @DataProvider
    private Object[][] fileNames() {
        return new String[][]{{"repodata.json"}, {"current_repodata.json"}};
    }

    @DataProvider
    private Object[][] extensions() {
        return new String[][]{{".tar.bz2"}, {".conda"}};
    }

}