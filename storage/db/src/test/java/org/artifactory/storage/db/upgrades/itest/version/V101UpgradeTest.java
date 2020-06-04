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

package org.artifactory.storage.db.upgrades.itest.version;

import org.artifactory.storage.db.upgrades.common.UpgradeBaseTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Test DB version v101 (art version v310)
 *
 * @author freds
 */
@Test
public class V101UpgradeTest extends UpgradeBaseTest {

    private static final String DB_PROPERTIES_TABLE = "db_properties";
    private static final String ARTIFACTORY_SERVERS_TABLE = "artifactory_servers";

    public void testDdlUpgrade() throws Exception {
        rollBackTo300Version();
        // Now the DB is like in 3.0.x, should be missing the new tables of 3.1.x
        assertFalse(tableExists(DB_PROPERTIES_TABLE));
        assertFalse(tableExists(ARTIFACTORY_SERVERS_TABLE));
        //run 310 conversion
        executeSqlStream(getDbSchemaUpgradeSql("v310", dbProperties.getDbType()));
        //verify tables are there now
        assertTrue(columnExists(DB_PROPERTIES_TABLE, "installation_date"));
        assertTrue(columnExists(DB_PROPERTIES_TABLE, "artifactory_version"));
        assertTrue(columnExists(DB_PROPERTIES_TABLE, "artifactory_revision"));
        assertTrue(columnExists(DB_PROPERTIES_TABLE, "artifactory_release"));

        assertTrue(columnExists(ARTIFACTORY_SERVERS_TABLE, "server_id"));
        assertTrue(columnExists(ARTIFACTORY_SERVERS_TABLE, "server_id"));
        assertTrue(columnExists(ARTIFACTORY_SERVERS_TABLE, "start_time"));
        assertTrue(columnExists(ARTIFACTORY_SERVERS_TABLE, "context_url"));
        assertTrue(columnExists(ARTIFACTORY_SERVERS_TABLE, "membership_port"));
        assertTrue(columnExists(ARTIFACTORY_SERVERS_TABLE, "server_state"));
        assertTrue(columnExists(ARTIFACTORY_SERVERS_TABLE, "server_role"));
        assertTrue(columnExists(ARTIFACTORY_SERVERS_TABLE, "last_heartbeat"));
        assertTrue(columnExists(ARTIFACTORY_SERVERS_TABLE, "artifactory_version"));
        assertTrue(columnExists(ARTIFACTORY_SERVERS_TABLE, "artifactory_revision"));
        assertTrue(columnExists(ARTIFACTORY_SERVERS_TABLE, "artifactory_release"));
        assertTrue(columnExists(ARTIFACTORY_SERVERS_TABLE, "artifactory_running_mode"));
        assertTrue(columnExists(ARTIFACTORY_SERVERS_TABLE, "license_hash"));
    }
}
