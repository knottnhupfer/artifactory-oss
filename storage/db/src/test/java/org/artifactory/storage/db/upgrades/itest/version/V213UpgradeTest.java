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
import org.jfrog.storage.DbType;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.sql.SQLException;

import static org.jfrog.storage.util.DbUtils.indexExists;

/**
 * Test DB version v213
 *
 * @author Shay Bagants
 */
@Test
public class V213UpgradeTest extends UpgradeBaseTest {

    public void testNodesIndexReplacementForPostgres() throws SQLException {
        if (dbProperties.getDbType().equals(DbType.POSTGRESQL)) {
            Assert.assertTrue(indexExists(jdbcHelper, "node_props", "prop_key",
                    "node_props_prop_key_value_idx", dbProperties.getDbType()));
            Assert.assertTrue(indexExists(jdbcHelper, "node_props", "prop_value",
                    "node_props_prop_key_value_idx", dbProperties.getDbType()));

            Assert.assertTrue(indexExists(jdbcHelper, "node_props", "prop_value",
                    "node_props_prop_value_key_idx", dbProperties.getDbType()));
            Assert.assertTrue(indexExists(jdbcHelper, "node_props", "prop_key",
                    "node_props_prop_value_key_idx", dbProperties.getDbType()));

            Assert.assertTrue(indexExists(jdbcHelper, "node_props", "node_id",
                    "node_props_node_prop_value_idx", dbProperties.getDbType()));
            Assert.assertTrue(indexExists(jdbcHelper, "node_props", "prop_value",
                    "node_props_node_prop_value_idx", dbProperties.getDbType()));
            Assert.assertTrue(indexExists(jdbcHelper, "node_props", "prop_key",
                    "node_props_node_prop_value_idx", dbProperties.getDbType()));
        }
    }
}
