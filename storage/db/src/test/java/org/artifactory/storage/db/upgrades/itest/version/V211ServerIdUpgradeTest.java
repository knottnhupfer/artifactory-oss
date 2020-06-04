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
import org.jfrog.storage.util.DbUtils;
import org.testng.annotations.Test;

import java.sql.SQLException;

import static org.testng.Assert.assertTrue;

/**
 * @author Tamir Hadad
 */
@Test
public class V211ServerIdUpgradeTest extends UpgradeBaseTest {

    public void testArtifactorySessionTablesExist() throws SQLException {
        assertTrue(tableExists("UI_SESSION"));
        assertTrue(tableExists("UI_SESSION_ATTRIBUTES"));
        assertTrue(DbUtils.indexExists(jdbcHelper, "UI_SESSION", "LAST_ACCESS_TIME", "UI_SESSION_IX1", dbProperties.getDbType()));
        assertTrue(DbUtils.indexExists(jdbcHelper, "UI_SESSION_ATTRIBUTES", "SESSION_ID", "UI_SESSION_ATTRIBUTES_IX1", dbProperties.getDbType()));
    }
}
