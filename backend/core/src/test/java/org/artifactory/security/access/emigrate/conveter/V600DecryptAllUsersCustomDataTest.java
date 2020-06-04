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

package org.artifactory.security.access.emigrate.conveter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FileUtils;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.crypto.CryptoHelper;
import org.artifactory.security.SecurityServiceImpl;
import org.artifactory.security.access.AccessService;
import org.artifactory.storage.db.security.service.access.UserMapper;
import org.artifactory.storage.db.security.service.access.UserPropertiesSearchHelper;
import org.artifactory.test.ArtifactoryHomeStub;
import org.artifactory.test.TestUtils;
import org.jfrog.access.client.AccessClient;
import org.jfrog.access.client.user.UsersClient;
import org.jfrog.access.rest.user.CustomDatumRequest;
import org.jfrog.access.rest.user.UpdateUserRequest;
import org.jfrog.access.rest.user.User;
import org.jfrog.access.rest.user.Users;
import org.jfrog.security.crypto.CipherAlg;
import org.jfrog.security.crypto.DecodedKeyPair;
import org.jfrog.security.crypto.EncodedKeyPair;
import org.jfrog.security.crypto.JFrogCryptoHelper;
import org.jfrog.security.crypto.result.DecryptionStatusHolder;
import org.jfrog.security.file.SecurityFolderHelper;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

import java.io.IOException;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Noam Shemesh
 */
@Test(singleThreaded = true)
public class V600DecryptAllUsersCustomDataTest {
    @Mock
    private AccessService accessService;

    private V600DecryptAllUsersCustomData decryptAllUsersCustomData;

    private String artifactoryKey;

    @BeforeMethod
    public void beforeMethod() throws IOException {
        MockitoAnnotations.initMocks(this);
        ArtifactoryHome artifactoryHome = new ArtifactoryHomeStub();
        FileUtils.deleteQuietly(artifactoryHome.getArtifactoryKey());
        SecurityFolderHelper.createKeyFile(artifactoryHome.getArtifactoryKey(), CipherAlg.AES128);
        artifactoryKey = Files.readFile(artifactoryHome.getArtifactoryKey());
        ArtifactoryHome.bind(artifactoryHome);
        SecurityServiceImpl securityService = new SecurityServiceImpl();
        TestUtils.setField(securityService, "accessService", accessService);
        decryptAllUsersCustomData = new V600DecryptAllUsersCustomData(securityService);
    }

    @AfterMethod
    public void afterMethod() {
        FileUtils.deleteQuietly(ArtifactoryHome.get().getArtifactoryKey());
        ArtifactoryHome.unbind();
    }
    //disabled per noams request
    @Test(singleThreaded = true, enabled = false)
    public void testDecryptUserProperties() {
        assertTrue(CryptoHelper.hasArtifactoryKey(ArtifactoryHome.get()));
        AccessClient accessClient = Mockito.mock(AccessClient.class);
        UsersClient usersMock = Mockito.mock(UsersClient.class);
        Users usersResponseMock = Mockito.mock(Users.class);
        when(accessService.getAccessClient()).thenReturn(accessClient);
        when(accessClient.users()).thenReturn(usersMock);
        when(usersMock.findUsers()).thenReturn(usersResponseMock);

        User userMock = Mockito.mock(User.class);
        DecodedKeyPair decoded = JFrogCryptoHelper.encodeKeyPair(
                JFrogCryptoHelper.generateKeyPair())
                .decode(ArtifactoryHome.get().getArtifactoryEncryptionWrapper(), new DecryptionStatusHolder());
        EncodedKeyPair encodedKeyPair = new EncodedKeyPair(decoded,
                ArtifactoryHome.get().getArtifactoryEncryptionWrapper());
        EncodedKeyPair toValidate = new EncodedKeyPair(decoded, null);

        when(usersResponseMock.getUsers()).thenReturn(ImmutableList.of(userMock));
        when(userMock.getUsername()).thenReturn("noam");
        when(userMock.getCustomData(UserMapper.ArtifactoryBuiltInUserProperty.private_key.name()))
                .thenReturn(encodedKeyPair.getEncodedPrivateKey());
        when(userMock.getCustomData(UserMapper.ArtifactoryBuiltInUserProperty.public_key.name()))
                .thenReturn(encodedKeyPair.getEncodedPublicKey());

        when(userMock.getCustomData()).thenReturn(ImmutableMap.of("key", "regularValue", "apiKey",
                CryptoHelper.encryptIfNeeded(ArtifactoryHome.get(), "encrypt")));

        decryptAllUsersCustomData.convert();

        ArgumentCaptor<UpdateUserRequest> captor = ArgumentCaptor.forClass(UpdateUserRequest.class);
        verify(usersMock).updateUser(captor.capture());

        assertEquals(captor.getValue().getUsername(), "noam");
        Map<String, CustomDatumRequest> customData = captor.getValue().getCustomData();
        System.out.println("customData: " + customData);
        System.out.println("CryptoHelper.hasArtifactoryKey(ArtifactoryHome.get()): " +
                CryptoHelper.hasArtifactoryKey(ArtifactoryHome.get()));
        System.out.println("CryptoHelper.getArtifactoryKey(ArtifactoryHome.get()): " +
                CryptoHelper.getArtifactoryKey(ArtifactoryHome.get()));
        System.out.println("artifactoryKey (before): " + artifactoryKey);
        assertTrue(customData.containsKey("key"));
        assertEquals(customData.get("key").getValue(), "regularValue");
        assertTrue(customData.containsKey("key_shash"));
        assertEquals(customData.get("key_shash").getValue().length(), 6);
        assertEquals(customData.get("apiKey").getValue(), "encrypt");
        assertEquals(customData.get("apiKey_shash").getValue().length(), 6);
        assertEquals(customData.get("apiKey_shash").getValue(),
                UserPropertiesSearchHelper.getSearchableProp("apiKey", "encrypt").getRight());
        assertEquals(customData.get("public_key").getValue(), toValidate.getEncodedPublicKey());
        assertEquals(customData.get("private_key").getValue(), toValidate.getEncodedPrivateKey());
    }

}