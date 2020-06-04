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

import static org.testng.Assert.assertTrue;

/**
 * Test DB version v103 (art version v410 -> v412)
 *
 * @author Dan Feldman
 */
@Test
public class V103UpgradeTest extends UpgradeBaseTest {

    private final static String USER_PROPS_TABLE = "user_props";
    private final static String STATS_REMOTE_TABLE = "stats_remote";

    public void testV103Conversion() throws SQLException {
        assertTrue(columnExists(USER_PROPS_TABLE, "user_id"));
        assertTrue(columnExists(USER_PROPS_TABLE, "prop_key"));
        assertTrue(columnExists(USER_PROPS_TABLE, "prop_value"));

        assertTrue(columnExists(STATS_REMOTE_TABLE, "node_id"));
        assertTrue(columnExists(STATS_REMOTE_TABLE, "origin"));
        assertTrue(columnExists(STATS_REMOTE_TABLE, "download_count"));
        assertTrue(columnExists(STATS_REMOTE_TABLE, "last_downloaded"));
        assertTrue(columnExists(STATS_REMOTE_TABLE, "last_downloaded_by"));
    }
}
