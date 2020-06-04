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

package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.docker;

import com.google.common.collect.ImmutableList;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.docker.DockerAddon;
import org.artifactory.addon.docker.DockerBlobInfoModel;
import org.artifactory.addon.docker.DockerV2InfoModel;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.model.xstream.fs.FileInfoImpl;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.ArtifactoryRestResponse;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.ViewArtifact;
import org.artifactory.ui.rest.model.artifacts.search.versionsearch.result.DockerNativeV2InfoRequest;
import org.jfrog.common.ResourceUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.artifactory.mime.DockerNaming.MANIFEST_FILENAME;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

/**
 * @author Shay Bagants
 */
@Test
public class DockerV2ManifestNativeServiceTest {

    private DockerV2ManifestNativeService nativeService = new DockerV2ManifestNativeService();
    @Mock
    private ArtifactoryRestRequest restRequest;
    @Mock
    private ArtifactoryRestResponse restResponse;
    @Mock
    private RepositoryService repositoryService;
    @Mock
    private AuthorizationService authorizationService;
    @Mock
    private ArtifactoryContext artifactoryContext;
    @Mock
    private AddonsManager addonsManager;
    @Mock
    private DockerAddon dockerAddon;
    private final String MANIFEST_STRING = ResourceUtils.getResourceAsString("/docker/manifest.json");

    @BeforeMethod
    private void setup() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(nativeService, "repositoryService", repositoryService);
        nativeService.setRepoService(repositoryService);
        nativeService.setAuthorizationService(authorizationService);
        ArtifactoryContextThreadBinder.bind(artifactoryContext);
        when(artifactoryContext.beanForType(AddonsManager.class)).thenReturn(addonsManager);
    }

    @AfterMethod
    private void cleanup() {
        ArtifactoryContextThreadBinder.unbind();
    }

    @Test(dataProvider = "returnManifest", enabled = false)
    public void testPackageView(boolean returnManifest) throws IOException {
        when(restRequest.getPathParamByKey("repoKey")).thenReturn("docker-local");
        when(restRequest.getQueryParamByKey("packageName")).thenReturn("jfrog-cli");
        when(restRequest.getQueryParamByKey("versionName")).thenReturn("sha256:dc472a59fb006797aa2a6bfb54cc9c57959bb0a6d11fadaa608df8c16dea39cf");
        when(restRequest.getQueryParamByKey("manifest")).thenReturn(String.valueOf(returnManifest));

        RepoPath repoPath = RepoPathFactory.create("docker-local", "jfrog-cli/sha256__dc472a59fb006797aa2a6bfb54cc9c57959bb0a6d11fadaa608df8c16dea39cf/");
        when(repositoryService.exists(repoPath)).thenReturn(true);
        when(authorizationService.canRead(repoPath)).thenReturn(true);
        when(repositoryService.isVirtualRepoExist("docker-local")).thenReturn(false);

        RepoPath manifestRepoPath = RepoPathFactory.create("docker-local", repoPath.getPath() + "/" + MANIFEST_FILENAME);
        FileInfoImpl manifestItemInfo = new FileInfoImpl(manifestRepoPath);
        when(repositoryService.getChildren(repoPath)).thenReturn(ImmutableList.of(manifestItemInfo));
        when(repositoryService.getStringContent(manifestItemInfo)).thenReturn(MANIFEST_STRING);

        if (!returnManifest) {
            when(addonsManager.addonByType(DockerAddon.class)).thenReturn(dockerAddon);
            DockerV2InfoModel dockerV2InfoModel = new DockerV2InfoModel();
            dockerV2InfoModel.tagInfo.title = "docker-local/jfrog-cli/sha256:dc472a59fb006797aa2a6bfb54cc9c57959bb0a6d11fadaa608df8c16dea39cf";
            dockerV2InfoModel.tagInfo.digest = "sha256:dc472a59fb006797aa2a6bfb54cc9c57959bb0a6d11fadaa608df8c16dea39cf";

            DockerBlobInfoModel blobInfoModel = new DockerBlobInfoModel("somelongidwhichisbiggerthan12", "sha256:03b1be98f3f9b05cb57782a3a71a44aaf6ec695de5f4f8e6c1058cd42f04953e", "100", "now");
            dockerV2InfoModel.blobsInfo.add(blobInfoModel);
            when(dockerAddon.getDockerV2Model(manifestRepoPath, false)).thenReturn(dockerV2InfoModel);
        }

        nativeService.execute(restRequest, restResponse);
        verify(repositoryService, times(2)).exists(repoPath);
        verify(repositoryService, times(1)).getStringContent(manifestItemInfo);
        if (returnManifest) {
            ArgumentCaptor<ViewArtifact> argument = ArgumentCaptor.forClass(ViewArtifact.class);
            verify(restResponse, times(1)).iModel(argument.capture());
            assertEquals(MANIFEST_STRING, argument.getValue().getFileContent());
        } else {
            ArgumentCaptor<DockerNativeV2InfoRequest> argument = ArgumentCaptor.forClass(DockerNativeV2InfoRequest.class);
            verify(restResponse, times(1)).iModel(argument.capture());
            assertEquals(1, argument.getValue().getBlobsInfo().size());
            assertEquals("jfrog-cli", argument.getValue().getPackageName());
            assertEquals("sha256:dc472a59fb006797aa2a6bfb54cc9c57959bb0a6d11fadaa608df8c16dea39cf", argument.getValue().getName());
        }
    }

    @DataProvider
    public static Object[][] returnManifest() {
        return new Object[][]{
                {true},
                {false},
        };
    }
}