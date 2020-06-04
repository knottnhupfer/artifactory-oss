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

package org.artifactory.ui.rest.service.admin.configuration.repositories.util;

import org.apache.http.client.methods.HttpRequestBase;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteNetworkRepositoryConfigModel;
import org.jfrog.security.crypto.EncryptionWrapper;
import org.jfrog.security.crypto.result.DecryptionStringResult;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * @author Maxim Yurkovsky
 */
public class TestMethodFactoryTest {

    private final String npmRepoUrl = "https://registry.npmjs.org/";

    @Mock
    private AddonsManager addonsManager;

    @Mock
    private HaCommonAddon haCommonAddon;

    @Mock
    private ArtifactoryContext context;

    @Mock
    private ArtifactoryHome artifactoryHomeMock;

    @BeforeMethod
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(addonsManager.addonByType(HaCommonAddon.class)).thenReturn(haCommonAddon);
        when(haCommonAddon.getHostId()).thenReturn("4");
        when(context.beanForType(AddonsManager.class)).thenReturn(addonsManager);
        ArtifactoryContextThreadBinder.bind(context);
        ArtifactoryHome.bind(artifactoryHomeMock);
        EncryptionWrapper encryptionWrapperMock = mock(EncryptionWrapper.class);
        when(artifactoryHomeMock.getArtifactoryEncryptionWrapper()).thenReturn(encryptionWrapperMock);
        when(encryptionWrapperMock.decryptIfNeeded("fakePassword"))
                .thenReturn(new DecryptionStringResult("fakePassword"));
        when(encryptionWrapperMock.decryptIfNeeded("")).thenReturn(new DecryptionStringResult(""));
    }

    @Test(dataProvider = "getFakeCredentialsAndExpectedValues")
    public void testCreateTestMethodForNpmWithOrWithoutCredentials(String fakeUsername, String fakePassword,
            String expectedMethod, String expectedUrl) throws Exception {
        //arrange
        RemoteNetworkRepositoryConfigModel mockedNetworkModel = mock(RemoteNetworkRepositoryConfigModel.class);
        when(mockedNetworkModel.getUsername()).thenReturn(fakeUsername);
        when(mockedNetworkModel.getPassword()).thenReturn(fakePassword);

        //act
        final HttpRequestBase request = TestMethodFactory
                .createTestMethod(npmRepoUrl, RepoType.Npm, null, mockedNetworkModel);
        //assert
        assertEquals(request.getMethod(), expectedMethod);
        assertEquals(request.getURI().toString(), expectedUrl);
    }

    @Test
    public void testCreateTestMethodForNpmWithNetworkModelNull() throws Exception {
        //arrange

        //act
        final HttpRequestBase request = TestMethodFactory
                .createTestMethod(npmRepoUrl, RepoType.Npm, null, null);
        //assert
        assertEquals(request.getMethod(), "HEAD");
        assertEquals(request.getURI().toString(), npmRepoUrl);
    }

    @DataProvider
    public Object[][] getFakeCredentialsAndExpectedValues() {
        return new Object[][]{
                {"fakeUsername", "fakePassword", "GET", npmRepoUrl + "-/whoami"},
                {"fakeUsername", "", "HEAD", npmRepoUrl},
                {"fakeUsername", null, "HEAD", npmRepoUrl},
                {"", "fakePassword", "HEAD", npmRepoUrl},
                {null, "fakePassword", "HEAD", npmRepoUrl},
                {null, null, "HEAD", npmRepoUrl},
                {"", "", "HEAD", npmRepoUrl}
        };
    }
}