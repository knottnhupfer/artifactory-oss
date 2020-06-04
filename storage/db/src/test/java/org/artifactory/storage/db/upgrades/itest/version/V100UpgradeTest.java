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

import static org.artifactory.storage.db.itest.DbTestUtils.isTableMissing;
import static org.jfrog.storage.util.DbUtils.withConnection;
import static org.testng.Assert.assertTrue;

/**
 * Test DB version v100 (art version v300)
 *
 * @author Shay Yaakov
 */
@Test
public class V100UpgradeTest extends UpgradeBaseTest {

    public void test300DBChanges() throws SQLException {
        // Now the DB is like in 3.0.x, should be missing the new tables of 3.1.x
        assertTrue(withConnection(jdbcHelper, conn -> isTableMissing(conn, dbProperties.getDbType())));
    }
}
