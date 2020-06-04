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

import org.apache.commons.lang.RandomStringUtils;
import org.artifactory.storage.db.upgrades.common.UpgradeBaseTest;
import org.jfrog.storage.DbType;
import org.testng.annotations.Test;

import java.sql.SQLException;

import static org.artifactory.storage.db.itest.DbTestUtils.getColumnSize;
import static org.jfrog.storage.util.DbUtils.withConnection;
import static org.testng.Assert.assertEquals;

/**
 * Test DB version v102 (art version v311 -> v402)
 *
 * @author Yoav Luft
 */
@Test
public class V102UpgradeTest extends UpgradeBaseTest {

    public void test311DBChanges() throws SQLException {
        int actualColSize = withConnection(jdbcHelper, conn -> getColumnSize(conn, "node_props", "prop_value"));
        assertEquals(actualColSize, 4000);
        if (dbProperties.getDbType() == DbType.MSSQL || dbProperties.getDbType() == DbType.POSTGRESQL) {
                return; // RTFACT-5768, RTFACT-17337
        }
            jdbcHelper.executeUpdate("INSERT INTO node_props VALUES(?, ?, ?, ?)",
                    15, 15, "longProp", RandomStringUtils.randomAscii(3999));
    }
}
