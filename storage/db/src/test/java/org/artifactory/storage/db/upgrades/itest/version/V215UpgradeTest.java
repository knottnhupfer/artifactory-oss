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
 * Test DB version v215
 *
 * @author Yoaz Menda
 */
@Test
public class V215UpgradeTest extends UpgradeBaseTest {

    public void testOriginalContentTypeConverter() throws SQLException {
        String tableName = "bundle_files";
        assertTrue(tableExists(tableName), "Missing table bundle_files");
        assertTrue(columnExists(tableName, "id"));
        assertTrue(columnExists(tableName, "node_id"));
        assertTrue(columnExists(tableName, "bundle_id"));
        assertTrue(columnExists(tableName, "repo_path"));
        assertTrue(columnExists(tableName, "original_component_details"));
    }
}
