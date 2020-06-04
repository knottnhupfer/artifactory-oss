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

package org.artifactory.storage.db.servers.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.artifactory.addon.ArtifactoryRunningMode;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.converter.ConverterManager;
import org.artifactory.sapi.common.ExportSettings;
import org.artifactory.sapi.common.ImportSettings;
import org.artifactory.spring.SpringConfigPaths;
import org.artifactory.state.ArtifactoryServerState;
import org.artifactory.storage.db.servers.model.ArtifactoryServer;
import org.artifactory.storage.db.servers.model.ArtifactoryServerRole;
import org.artifactory.test.ArtifactoryHomeStub;
import org.artifactory.version.VersionProvider;
import org.jfrog.common.logging.logback.servlet.LogbackConfigManager;
import org.jfrog.config.ConfigurationManager;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.*;

/**
 * @author Shay Bagants
 */
@Test
public class ArtifactoryServersCommonServiceImplTest {

    private ArtifactoryServersCommonServiceImpl serversCommonService;
    private final String art1 = "art-01";
    private final String art2 = "art-02";

    @BeforeMethod
    public void setup() {
        serversCommonService = new ArtifactoryServersCommonServiceImpl();
        // Bind the ArtifactoryHome and a Dummy context that will return the serverId
        ArtifactoryHome.bind(new ArtifactoryHomeStub());
        ArtifactoryContextThreadBinder.bind(new DummyArtifactoryContext());
    }

    /**
     * Test update licenseHash to hash that is currently not in use by any other server
     */
    public void testUpdateServerLicenseHash() {
        // Create mock ArtifactoryServersServiceImpl (Internal service that used by the ArtifactoryServersCommonServiceImpl and basically runs the ArtifactoryServersDao)
        ArtifactoryServersServiceImpl serversService = createMock(ArtifactoryServersServiceImpl.class);
        ReflectionTestUtils.setField(serversCommonService, "serversService", serversService);
        expect(serversService.getAllArtifactoryServers())
                .andReturn(getAllServers(System.currentTimeMillis(), DigestUtils.sha1Hex("otherLicense1" + 3),
                        System.currentTimeMillis(), DigestUtils.shaHex("otherLicense2" + 3))).once();
        expect(serversService.getAllArtifactoryServers())
                .andReturn(getAllServers(System.currentTimeMillis(), DigestUtils.sha1Hex("otherLicense1" + 3),
                        System.currentTimeMillis(), DigestUtils.shaHex("otherLicense2" + 3))).once();
        long lastHeartbeatToUpdate = System.currentTimeMillis();
        String licenseHashToUpdate = DigestUtils.shaHex("newLicense" + 3);
        expect(serversService.updateArtifactoryServerHeartbeat(art1, lastHeartbeatToUpdate, licenseHashToUpdate))
                .andReturn(1);
        replay(serversService);

        // First update scenario, should succeed
        boolean updateSucceeded = serversCommonService
                .updateArtifactoryLicenseHash(art1, lastHeartbeatToUpdate, licenseHashToUpdate);
        Assert.assertTrue(updateSucceeded);
    }

    /**
     * Test update licenseHash while there is a race condition between this node and another HA node, means that some
     * other server just updated the licenseHash as well right after this node did, so two nodes now holds the same
     * licenseHash in the artifactory_servers table. The expected result is rollback while identifying that more than
     * one node holds this licenseHash
     */
    public void testUpdateServerLicenseHashWithRollback() {
        // Create mock ArtifactoryServersServiceImpl (Internal service that used by the ArtifactoryServersCommonServiceImpl and basically runs the ArtifactoryServersDao)
        ArtifactoryServersServiceImpl serversService = createMock(ArtifactoryServersServiceImpl.class);
        String licenseHashToUpdate = DigestUtils.shaHex("newLicense" + 3);
        String licenseToRollbackInto = DigestUtils.shaHex("otherLicense1" + 3);
        ReflectionTestUtils.setField(serversCommonService, "serversService", serversService);
        expect(serversService.getAllArtifactoryServers())
                .andReturn(getAllServers(System.currentTimeMillis(), licenseToRollbackInto,
                        System.currentTimeMillis(), DigestUtils.sha1Hex("otherLicense2" + 3))).once();
        // This is the 'race conditions'. Note that the this call (that validates that no other server is using this license) return duplicate license and should rollback
        expect(serversService.getAllArtifactoryServers())
                .andReturn(getAllServers(System.currentTimeMillis(), licenseToRollbackInto,
                        System.currentTimeMillis(), licenseHashToUpdate)).once();
        long lastHeartbeatToUpdate = System.currentTimeMillis();
        expect(serversService.updateArtifactoryServerHeartbeat(art1, lastHeartbeatToUpdate, licenseHashToUpdate))
                .andReturn(1);
        expect(serversService.updateArtifactoryServerHeartbeat(art1, lastHeartbeatToUpdate, licenseToRollbackInto))
                .andReturn(1);
        replay(serversService);

        // First update scenario, should succeed
        boolean updateSucceeded = serversCommonService
                .updateArtifactoryLicenseHash(art1, lastHeartbeatToUpdate, licenseHashToUpdate);
        Assert.assertFalse(updateSucceeded);
    }

    /**
     * Similar as testUpdateServerLicenseHashWithRollback, but this time, instead of race condition with another node,
     * it's the same node that the DB indicates that already has the license. In other words, after updating the
     * licenseHash, we validate that no one else is using this license, in this test, we validate that indeed no one
     * else uses it, but if the DB indicates that I'm using it, test that NO ROLLBACK WILL OCCUR
     */
    public void testUpdateServerLicense2() {
        // Create mock ArtifactoryServersServiceImpl (Internal service that used by the ArtifactoryServersCommonServiceImpl and basically runs the ArtifactoryServersDao)
        ArtifactoryServersServiceImpl serversService = createMock(ArtifactoryServersServiceImpl.class);
        String licenseHashToUpdate = DigestUtils.shaHex("newLicense" + 3);
        ReflectionTestUtils.setField(serversCommonService, "serversService", serversService);
        expect(serversService.getAllArtifactoryServers())
                .andReturn(getAllServers(System.currentTimeMillis(), DigestUtils.shaHex("otherLicense1" + 3),
                        System.currentTimeMillis(), DigestUtils.sha1Hex("otherLicense2" + 3))).once();
        // This is the duplicate license check, we return our node as the holder of the license and expect no rollback to occur
        expect(serversService.getAllArtifactoryServers())
                .andReturn(getAllServers(System.currentTimeMillis(), licenseHashToUpdate,
                        System.currentTimeMillis(), DigestUtils.sha1Hex("otherLicense2" + 3))).once();
        long lastHeartbeatToUpdate = System.currentTimeMillis();
        expect(serversService.updateArtifactoryServerHeartbeat(art1, lastHeartbeatToUpdate, licenseHashToUpdate))
                .andReturn(1);
        expect(serversService.updateArtifactoryServerHeartbeat(art1, lastHeartbeatToUpdate,
                DigestUtils.sha1Hex("otherLicense2" + 3)))
                .andReturn(1);
        replay(serversService);

        // First update scenario, should succeed
        boolean updateSucceeded = serversCommonService
                .updateArtifactoryLicenseHash(art1, lastHeartbeatToUpdate, licenseHashToUpdate);
        Assert.assertTrue(updateSucceeded);
    }

    /**
     * Try to update the licenseHash while another server already own this hash in the artifactory_servers table. We
     * expect the update to fail
     */
    public void testUpdateServerLicense3() {
        // Create mock ArtifactoryServersServiceImpl (Internal service that used by the ArtifactoryServersCommonServiceImpl and basically runs the ArtifactoryServersDao)
        ArtifactoryServersServiceImpl serversService = createMock(ArtifactoryServersServiceImpl.class);
        String licenseHashToUpdate = DigestUtils.shaHex("newLicense" + 3);
        ReflectionTestUtils.setField(serversCommonService, "serversService", serversService);
        expect(serversService.getAllArtifactoryServers())
                .andReturn(getAllServers(System.currentTimeMillis(), DigestUtils.sha1Hex("otherLicense1" + 3),
                        System.currentTimeMillis(), licenseHashToUpdate)).once();
        expect(serversService.getAllArtifactoryServers())
                .andReturn(getAllServers(System.currentTimeMillis(), DigestUtils.sha1Hex("otherLicense1" + 3),
                        System.currentTimeMillis(), DigestUtils.shaHex("otherLicense2" + 3))).once();
        replay(serversService);

        // First update scenario, should succeed
        boolean updateSucceeded = serversCommonService
                .updateArtifactoryLicenseHash(art1, System.currentTimeMillis(), licenseHashToUpdate);
        Assert.assertFalse(updateSucceeded);
    }

    public void testGetConvertingMembers() {
        ArtifactoryServersService serversService = Mockito.mock(ArtifactoryServersService.class);
        List<ArtifactoryServer> servers = new LinkedList<>();

        ArtifactoryServer s1 = new ArtifactoryServer("art1", 100L, "123.4.5.6", 5701, ArtifactoryServerState.CONVERTING,
                ArtifactoryServerRole.PRIMARY, System.currentTimeMillis(), "6.1", 2, 3L,
                ArtifactoryRunningMode.HA, "somehash");
        servers.add(s1);
        Mockito.when(serversService.getAllArtifactoryServers()).thenReturn(servers);

        ArtifactoryServersCommonServiceImpl service = new ArtifactoryServersCommonServiceImpl();
        ReflectionTestUtils.setField(service, "serversService", serversService);

        List<ArtifactoryServer> members = service.getConvertingMembers();
        Assert.assertEquals(members.size(), 1);
        Assert.assertEquals(members.get(0).getServerState(), ArtifactoryServerState.CONVERTING);

        s1 = new ArtifactoryServer("art2", 100L, "123.4.5.7", 5702, ArtifactoryServerState.RUNNING,
                ArtifactoryServerRole.PRIMARY, System.currentTimeMillis(), "6.2", 2, 3L,
                ArtifactoryRunningMode.HA, "somehash1");
        servers.add(s1);


        members = service.getConvertingMembers();
        Assert.assertEquals(members.size(), 1);
        Assert.assertEquals(members.get(0).getServerState(), ArtifactoryServerState.CONVERTING);
        Assert.assertTrue(service.isConversionRunning());

        servers.clear();
        members = service.getConvertingMembers();
        Assert.assertEquals(members.size(), 0);
        Assert.assertFalse(service.isConversionRunning());


    }

        /**
         * Return list of two servers
         *
         * @param server1ExpectedDate        The first server lastHeartbeatDate to return
         * @param server1ExpectedLicenseHash The first server licenseHash to return
         * @param server2ExpectedDate        The second server lastHeartbeatDate to return
         * @param server2ExpectedLicenseHash The second server licenseHash to return
         */
    private List<ArtifactoryServer> getAllServers(long server1ExpectedDate, String server1ExpectedLicenseHash,
            long server2ExpectedDate, String server2ExpectedLicenseHash) {
        ArtifactoryServer server1 = new ArtifactoryServer(art1, 1000000L,
                "123.4.5.6", 5701, ArtifactoryServerState.RUNNING, ArtifactoryServerRole.PRIMARY,
                server1ExpectedDate, "6.1", 2, 3L,
                ArtifactoryRunningMode.HA, server1ExpectedLicenseHash);
        ArtifactoryServer server2 = new ArtifactoryServer(art2, 1000000L,
                "123.4.5.7", 5701, ArtifactoryServerState.RUNNING, ArtifactoryServerRole.MEMBER,
                server2ExpectedDate, "6.1", 2, 3L,
                ArtifactoryRunningMode.HA, server2ExpectedLicenseHash);
        return Arrays.asList(server1, server2);
    }

    private class DummyArtifactoryContext implements ArtifactoryContext {

        @Override
        public void exportTo(ExportSettings settings) {

        }

        @Override
        public void importFrom(ImportSettings settings) {

        }

        @Override
        public CentralConfigService getCentralConfig() {
            return null;
        }

        @Override
        public <T> T beanForType(Class<T> type) {
            return null;
        }

        @Override
        public <T> T beanForType(String name, Class<T> type) {
            return null;
        }

        @Override
        public <T> Map<String, T> beansForType(Class<T> type) {
            return null;
        }

        @Override
        public RepositoryService getRepositoryService() {
            return null;
        }

        @Override
        public AuthorizationService getAuthorizationService() {
            return null;
        }

        @Override
        public long getUptime() {
            return 0;
        }

        @Override
        public ArtifactoryHome getArtifactoryHome() {
            return null;
        }

        @Override
        public String getContextId() {
            return null;
        }

        @Override
        public SpringConfigPaths getConfigPaths() {
            return null;
        }

        @Override
        public String getServerId() {
            return art1;
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public boolean isOffline() {
            return false;
        }

        @Override
        public void setOffline() {

        }

        @Override
        public ConfigurationManager getConfigurationManager() {
            return null;
        }

        @Override
        public ConverterManager getConverterManager() {
            return null;
        }

        @Override
        public VersionProvider getVersionProvider() {
            return null;
        }

        @Override
        public LogbackConfigManager getLogbackConfigManager() {
            return null;
        }

        @Override
        public void destroy() {

        }
    }
}
