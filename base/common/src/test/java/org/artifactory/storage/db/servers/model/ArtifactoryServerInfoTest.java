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

package org.artifactory.storage.db.servers.model;

import org.artifactory.state.ArtifactoryServerState;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * @author Shay Bagants
 */
@Test
public class ArtifactoryServerInfoTest {

    @Test(dataProvider = "activeServerInfoData")
    public void testIsActive(int seconds, ArtifactoryServerState state, boolean expectedActive) {
        ArtifactoryServer server = mock(ArtifactoryServer.class);
        when(server.getLastHeartbeat()).thenReturn(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(seconds));
        when(server.getServerState()).thenReturn(state);
        ArtifactoryServerInfo serverInfo = new ArtifactoryServerInfo(server, 5);
        boolean isActive = serverInfo.isActive(seconds);
        assertEquals(expectedActive, isActive);
    }

    @Test(dataProvider = "heartbeatsData")
    public void testIsStaleHeartbeat(int seconds, boolean expectedStale) {
        ArtifactoryServer server = mock(ArtifactoryServer.class);
        when(server.getLastHeartbeat()).thenReturn(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(seconds));
        ArtifactoryServerInfo serverInfo = new ArtifactoryServerInfo(server, 5);
        boolean isStaleHeartbeat = serverInfo.isStaleHeartbeat(5);
        assertEquals(expectedStale, isStaleHeartbeat);
    }

    @DataProvider
    private Object[][] activeServerInfoData() {
        return new Object[][]{
                // using valid heartbeat with different running modes
                {0, ArtifactoryServerState.RUNNING, true},
                {0, ArtifactoryServerState.CONVERTING, true},
                {0, ArtifactoryServerState.STOPPING, true},
                {0, ArtifactoryServerState.STARTING, true},
                {0, ArtifactoryServerState.OFFLINE, false},
                {0, ArtifactoryServerState.STOPPED, false},
                {0, ArtifactoryServerState.UNAVAILABLE, false},
                {0, ArtifactoryServerState.UNKNOWN, false},
                // using stale heartbeat with different running modes
                {-10, ArtifactoryServerState.CONVERTING, false},
                {-10, ArtifactoryServerState.STOPPING, false},
                {-10, ArtifactoryServerState.RUNNING, false},
                {-10, ArtifactoryServerState.STARTING, false},
                {-10, ArtifactoryServerState.OFFLINE, false},
                {-10, ArtifactoryServerState.STOPPED, false},
                {-10, ArtifactoryServerState.UNAVAILABLE, false},
                {-10, ArtifactoryServerState.UNKNOWN, false}
        };
    }

    @DataProvider
    private Object[][] heartbeatsData() {
        return new Object[][]{
                {0, false},
                {-1, false},
                {-2, false},
                {-6, true},
                {-10, true}
        };
    }
}