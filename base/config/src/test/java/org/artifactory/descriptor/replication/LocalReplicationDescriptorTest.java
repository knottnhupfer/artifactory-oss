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

package org.artifactory.descriptor.replication;

import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Noam Y. Tenne
 */
@Test
public class LocalReplicationDescriptorTest extends ReplicationBaseDescriptorTest<LocalReplicationDescriptor> {

    @Test
    public void testDefaultValues() throws Exception {
        LocalReplicationDescriptor replicationDescriptor = constructDescriptor();
        assertNull(replicationDescriptor.getUrl(), "Unexpected default local replication URL.");
        assertNull(replicationDescriptor.getProxy(), "Unexpected default local replication proxy.");
        assertEquals(replicationDescriptor.getSocketTimeoutMillis(), 15000,
                "Unexpected default local replication timeout.");
        assertNull(replicationDescriptor.getUsername(), "Unexpected default local replication username.");
        assertNull(replicationDescriptor.getPassword(), "Unexpected default local replication password.");
        assertFalse(replicationDescriptor.isEnableEventReplication(),
                "Unexpected default enabled event replication state.");
        assertTrue(replicationDescriptor.isSyncProperties(), "Unexpected default local replication sync properties");
        assertFalse(replicationDescriptor.isSyncDeletes(), "Unexpected default local replication sync deleted");
        assertFalse(replicationDescriptor.isSyncStatistics(), "Unexpected default local replication sync statistics");
        assertNull(replicationDescriptor.getReplicationKey(), "Unexpected default local replication sync statistics");
    }

    @Test
    public void testSetters() throws Exception {
        LocalReplicationDescriptor replicationDescriptor = constructDescriptor();
        replicationDescriptor.setUrl("http://asfaf.com");

        ProxyDescriptor proxy = new ProxyDescriptor();
        replicationDescriptor.setReplicationKey("keykey");
        replicationDescriptor.setProxy(proxy);
        replicationDescriptor.setSocketTimeoutMillis(545454);
        replicationDescriptor.setUsername("momo");
        replicationDescriptor.setPassword("popo");
        replicationDescriptor.setEnableEventReplication(true);
        replicationDescriptor.setSyncProperties(false);
        replicationDescriptor.setSyncStatistics(true);
        replicationDescriptor.setSyncDeletes(true);

        assertEquals(replicationDescriptor.getReplicationKey(), "keykey");
        assertEquals(replicationDescriptor.getUrl(), "http://asfaf.com", "Unexpected local replication URL.");
        assertEquals(replicationDescriptor.getProxy(), proxy, "Unexpected local replication proxy.");
        assertEquals(replicationDescriptor.getSocketTimeoutMillis(), 545454, "Unexpected local replication timeout.");
        assertEquals(replicationDescriptor.getUsername(), "momo", "Unexpected local replication username.");
        assertEquals(replicationDescriptor.getPassword(), "popo", "Unexpected local replication password.");
        assertTrue(replicationDescriptor.isEnableEventReplication(), "Unexpected enabled event replication state.");
        assertFalse(replicationDescriptor.isSyncProperties(), "Unexpected local replication sync properties");
        assertTrue(replicationDescriptor.isSyncDeletes(), "Unexpected local replication sync deleted");
        assertTrue(replicationDescriptor.isSyncStatistics(), "Unexpected local replication sync statistics");
    }

    @Override
    protected LocalReplicationDescriptor constructDescriptor() {
        return new LocalReplicationDescriptor();
    }
}
