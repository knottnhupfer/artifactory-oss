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

import java.sql.SQLException;

import static org.artifactory.storage.db.itest.DbTestUtils.getColumnSize;
import static org.jfrog.storage.util.DbUtils.withConnection;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test DB version v214
 *
 * @author Lior Gur
 */
@Test
public class V214UpgradeTest extends UpgradeBaseTest {

    public void testServerIdColumnChange() throws SQLException {
        int expectedColSize = withConnection(jdbcHelper, conn -> getColumnSize(conn, "artifactory_servers", "server_id"));
        assertEquals(expectedColSize, 128);
    }

    public void testReplicationErrors() throws SQLException {
        String tableName = "replication_errors";
        assertTrue(tableExists(tableName), "Missing table replication_errors");
        assertTrue(columnExists(tableName, "error_id"));
        assertTrue(columnExists(tableName, "first_error_time"));
        assertTrue(columnExists(tableName, "last_error_time"));
        assertTrue(columnExists(tableName, "error_count"));
        assertTrue(columnExists(tableName, "error_message"));
        assertTrue(columnExists(tableName, "replication_key"));
        assertTrue(columnExists(tableName, "task_time"));
        assertTrue(columnExists(tableName, "task_type"));
        assertTrue(columnExists(tableName, "task_path"));
    }
}
