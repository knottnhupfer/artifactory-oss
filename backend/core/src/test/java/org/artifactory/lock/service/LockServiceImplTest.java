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

package org.artifactory.lock.service;

import com.google.common.collect.ImmutableMap;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.ArtifactoryRunningMode;
import org.artifactory.addon.HaAddon;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.state.ArtifactoryServerState;
import org.artifactory.storage.db.locks.service.DbLocksService;
import org.artifactory.storage.db.servers.model.ArtifactoryServer;
import org.artifactory.storage.db.servers.model.ArtifactoryServerInfo;
import org.artifactory.storage.db.servers.model.ArtifactoryServerRole;
import org.artifactory.storage.db.servers.service.ArtifactoryServersCommonService;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.mockito.ArgumentCaptor;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.artifactory.common.ConstantValues.haHeartbeatStaleIntervalSecs;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

/**
 * @author Shay Bagants
 */
@Test
public class LockServiceImplTest extends ArtifactoryHomeBoundTest {

    private LockServiceImpl.ReleaseExpiredLocksJob job = new LockServiceImpl.ReleaseExpiredLocksJob();
    private JobExecutionContext callbackContext;
    private ArtifactoryContext context;

    @BeforeMethod
    public void setup() {
        callbackContext = mock(JobExecutionContext.class);
        context = mock(ArtifactoryContext.class);
        ArtifactoryContextThreadBinder.bind(context);
    }

    @AfterMethod
    public void cleanup() {
        ArtifactoryContextThreadBinder.unbind();
        reset(context, callbackContext);
    }

    public void testExecuteNoContextBound() throws JobExecutionException {
        ArtifactoryContextThreadBinder.unbind();
        job.onExecute(callbackContext);
        verify(context, times(0)).beanForType(any());
    }

    @Test(dataProvider = "serverInformation")
    public void testOnExecute(boolean isHaEnabled, boolean isPrimary, ArtifactoryServer server,
            boolean expectPrimaryCleanup) throws JobExecutionException {
        ArtifactoryServersCommonService serverServiceMock = mock(ArtifactoryServersCommonService.class);
        // Map of server<>active
        Map<ArtifactoryServerInfo, Boolean> servers = prepareServers();
        when(serverServiceMock.getAllArtifactoryServersInfo()).thenReturn(new ArrayList<>(servers.keySet()));

        AddonsManager addonsManagerMock = mock(AddonsManager.class);
        DbLocksService dbLocksServiceMock = mock(DbLocksService.class);
        HaAddon haAddonMock = mock(HaAddon.class);
        when(haAddonMock.isHaEnabled()).thenReturn(isHaEnabled);
        when(haAddonMock.isPrimary()).thenReturn(isPrimary);
        when(serverServiceMock.getActiveRunningHaPrimary()).thenReturn(server);
        when(addonsManagerMock.addonByType(HaAddon.class)).thenReturn(haAddonMock);
        when(context.beanForType(DbLocksService.class)).thenReturn(dbLocksServiceMock);

        when(context.beanForType(AddonsManager.class)).thenReturn(addonsManagerMock);
        when(context.beanForType(ArtifactoryServersCommonService.class)).thenReturn(serverServiceMock);
        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        job.onExecute(callbackContext);
        verifyCleanupExecuted(expectPrimaryCleanup, servers, dbLocksServiceMock, captor);
    }

    private void verifyCleanupExecuted(boolean expecrPrimaryCleanup, Map<ArtifactoryServerInfo, Boolean> servers,
            DbLocksService dbLocksServiceMock, ArgumentCaptor<List<String>> captor) {
        if (expecrPrimaryCleanup) {
            verify(dbLocksServiceMock).cleanOrphanLocks(captor.capture());
            HashSet<String> calculatedIds = new HashSet<>(captor.getValue());
            Set<String> expectedActiveServers = getActiveServerIds(servers);
            assertEquals(calculatedIds, expectedActiveServers);
            verify(dbLocksServiceMock).cleanDbExpiredLocks();
        }
        verify(dbLocksServiceMock).cleanCachedExpiredLocks();
    }

    @DataProvider
    public Object[][] serverInformation() {
        return new Object[][]{
                {true, true, null, true},
                {true, false, null, false},
                {false, true, null, false},
                {false, false, mock(ArtifactoryServer.class), false},
                {true, false, mock(ArtifactoryServer.class), false},
                {false, true, mock(ArtifactoryServer.class), false},
                {true, true, mock(ArtifactoryServer.class), true},
                {false, false, null, false}
        };
    }

    private Set<String> getActiveServerIds(Map<ArtifactoryServerInfo, Boolean> servers) {
        return servers.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .map(ArtifactoryServerInfo::getServerId)
                .collect(Collectors.toSet());
    }

    private Map<ArtifactoryServerInfo, Boolean> prepareServers() {
        ArtifactoryServer staleServer1 = new ArtifactoryServer("staleServer1", 2L, null, 0,
                ArtifactoryServerState.RUNNING, ArtifactoryServerRole.MEMBER,
                System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(haHeartbeatStaleIntervalSecs.getInt() * 4), null,
                0, 0L,
                ArtifactoryRunningMode.HA, "lic");

        ArtifactoryServer staleServer2 = new ArtifactoryServer("staleServer2", 2L, null, 0,
                ArtifactoryServerState.CONVERTING, ArtifactoryServerRole.MEMBER,
                System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(haHeartbeatStaleIntervalSecs.getInt() * 4), null,
                0, 0L,
                ArtifactoryRunningMode.HA, "lic");

        ArtifactoryServer activeServer1 = new ArtifactoryServer("activeServer1", 2L, null, 0,
                ArtifactoryServerState.RUNNING, ArtifactoryServerRole.MEMBER,
                System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(haHeartbeatStaleIntervalSecs.getInt() * 2), null,
                0, 0L,
                ArtifactoryRunningMode.HA, "lic");

        ArtifactoryServer activeServer2 = new ArtifactoryServer("activeServer2", 2L, null, 0,
                ArtifactoryServerState.RUNNING, ArtifactoryServerRole.MEMBER, System.currentTimeMillis(), null, 0, 0L,
                ArtifactoryRunningMode.HA, "lic");

        return ImmutableMap.of(new ArtifactoryServerInfo(staleServer1, haHeartbeatStaleIntervalSecs.getInt()), false,
                new ArtifactoryServerInfo(staleServer2, haHeartbeatStaleIntervalSecs.getInt()), false,
                new ArtifactoryServerInfo(activeServer1, haHeartbeatStaleIntervalSecs.getInt()), true,
                new ArtifactoryServerInfo(activeServer2, haHeartbeatStaleIntervalSecs.getInt()), true);
    }
}