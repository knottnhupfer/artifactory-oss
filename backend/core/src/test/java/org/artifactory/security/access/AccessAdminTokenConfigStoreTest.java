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

package org.artifactory.security.access;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.security.SecurityService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.config.InternalCentralConfigService;
import org.codehaus.plexus.util.IOUtil;
import org.jfrog.access.common.ServiceId;
import org.jfrog.access.token.JwtAccessToken;
import org.jfrog.common.ResourceUtils;
import org.jfrog.security.crypto.PlainTextEncryptionWrapper;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.artifactory.common.ArtifactoryHome.*;
import static org.testng.Assert.assertFalse;

@Test
public class AccessAdminTokenConfigStoreTest {
    public static final ServiceId serviceId = ServiceId.generateUniqueId("test");
    private String goodToken = "token:" + serviceId.getFormattedName();

    @Mock
    private AccessServiceImpl accessService;
    @Mock
    private InternalCentralConfigService centralConfigService;
    @Mock
    private SecurityService securityService;
    @Mock
    private ArtifactoryHome artifactoryHome;

    @BeforeMethod
    void setMocks() throws IOException {
        MockitoAnnotations.initMocks(this);
        Mockito.when(accessService.centralConfigService()).thenReturn(centralConfigService);
        Mockito.when(accessService.securityService()).thenReturn(securityService);
        Mockito.when(accessService.artifactoryHome()).thenReturn(artifactoryHome);
        Mockito.when(accessService.parseToken(Mockito.anyString()))
                .thenAnswer(
                        invocation -> {
                            JwtAccessToken accessToken = Mockito.mock(JwtAccessToken.class);
                            String[] split = ((String) invocation.getArgument(0)).split(":");
                            if (!split[0].equals("token")) {
                                throw new IllegalArgumentException("!!");
                            }
                            Mockito.when(accessToken.getIssuer()).thenReturn(split[1]);
                            return accessToken;
                        });
        String home = Files.createTempDirectory(getClass().getSimpleName()).toString();
        File destDir = Paths.get(home).toFile();
        FileUtils.deleteDirectory(destDir);
        URL source = ResourceUtils.class.getResource("/org/artifactory/security/access/clientdir");
        try {
            URI sourceUri = source.toURI();
            FileUtils.copyDirectory(Paths.get(sourceUri).toFile(), destDir);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed parse uri " + source, e);
        }

        Mockito.when(artifactoryHome.getAccessClientDir()).thenReturn(new File(home));
        Mockito.when(artifactoryHome.getAccessAdminCredsFile())
                .thenReturn(new File(home + "/" + ACCESS_KEYS_DIR_NAME + "/" + ACCESS_CLIENT_CREDS_FILE_NAME));
        Mockito.when(artifactoryHome.getAccessAdminTokenFile())
                .thenReturn(new File(home + "/" + ACCESS_ADMIN_TOKEN_FILE_NAME));
        Mockito.when(artifactoryHome.getArtifactoryEncryptionWrapper()).thenReturn(new PlainTextEncryptionWrapper());
    }

    public void testGetCreateAdminToken() throws IOException {
        ArtifactoryAccessClientConfigStore store = createTestStore();
        Assert.assertFalse(artifactoryHome.getAccessAdminTokenFile().exists());

        Assert.assertNull(store.getCachedAdminToken());
        assertFalse(store.isAdminTokenExists());

        store.storeAdminToken(goodToken);
        Assert.assertTrue(artifactoryHome.getAccessAdminTokenFile().exists());
        Assert.assertTrue(store.isAdminTokenExists());

        checkMatchGoodToken(store.getCachedAdminToken());

        String fromFile = IOUtil.toString(Files.readAllBytes(artifactoryHome.getAccessAdminTokenFile().toPath()));
        checkMatchGoodToken(fromFile);
    }

    public void testRevokeToken() {
        ArtifactoryAccessClientConfigStore store = createTestStore();

        store.storeAdminToken(goodToken);
        Assert.assertTrue(artifactoryHome.getAccessAdminTokenFile().exists());
        Assert.assertTrue(store.isAdminTokenExists());

        store.revokeAdminToken();
        Assert.assertTrue(!artifactoryHome.getAccessAdminTokenFile().exists());
        Assert.assertNull(store.getCachedAdminToken());
        assertFalse(store.isAdminTokenExists());
    }

    private void checkMatchGoodToken(String fromFile) {
        Assert.assertEquals(clean(fromFile), clean(goodToken));
    }

    private String clean(String str) {
        return StringUtils.strip(str, "\r\n ");
    }

    private TestStore createTestStore() {
        return new TestStore(accessService, serviceId);
    }

    private class TestStore extends ArtifactoryAccessClientConfigStore {
        private TestStore(AccessServiceImpl accessService, ServiceId serviceId) {
            super(accessService, serviceId);
        }
    }
}