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
import org.artifactory.storage.db.version.converter.DBSqlConverter;
import org.testng.annotations.Test;

import java.sql.*;

import static org.jfrog.storage.util.DbUtils.*;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Test DB version v203 (art version v550)
 *
 * @author Dan Feldman
 */
@Test
public class V203UpgradeTest extends UpgradeBaseTest {

    private static final String NODES_TABLE = "nodes";
    private static final String DISTRIBUTED_LOCKS_TABLE = "distributed_locks";
    private static final String REPO_PATH_CHECKSUM_COL = "repo_path_checksum";

    //v550(+a,b,c) conversion adds the sha256 column to nodes and binaries tables.
    public void testV203Sha2Conversion() throws SQLException {
        assertTrue(columnExists("binaries", "sha256"));
        assertTrue(isColumnNullable(NODES_TABLE, "sha256"), "sha256 column should be nullable after v550 conversion");
        assertTrue(columnExists(NODES_TABLE, "sha256"));
        assertTrue(indexExists(jdbcHelper, NODES_TABLE, "sha256", "nodes_sha256_idx", dbProperties.getDbType()));
    }

    //v550d adds the not null constraint to the binaries table.
    @Test(dependsOnMethods = {"testV203Sha2Conversion"})
    public void testV203Sha2MigrationConversion() throws SQLException {
        //kombina to allow setting the not null constraint on the sha2 col
        jdbcHelper.executeUpdate("UPDATE binaries SET sha256 = '1' WHERE sha256 IS NULL");
        new DBSqlConverter("v550d").convert(jdbcHelper, dbProperties.getDbType());
        assertFalse(isColumnNullable("binaries", "sha256"), "Expected v550d conversion to pass.");
    }

    //v550(+a,b,c) conversion adds the repo_path_checksum column to the nodes table and the distributed_locks table
    @Test(dependsOnMethods = {"testV203Sha2MigrationConversion"})
    public void testV203NoHazelcastConversion() throws SQLException {
        assertTrue(columnExists(NODES_TABLE, REPO_PATH_CHECKSUM_COL));
        assertTrue(isColumnNullable(NODES_TABLE, REPO_PATH_CHECKSUM_COL));
        assertTrue(isColumnNullable(NODES_TABLE, REPO_PATH_CHECKSUM_COL));
        assertTrue(tableExists(DISTRIBUTED_LOCKS_TABLE));
        assertTrue(columnExists(DISTRIBUTED_LOCKS_TABLE, "category"));
        assertTrue(columnExists(DISTRIBUTED_LOCKS_TABLE, "lock_key"));
        assertTrue(columnExists(DISTRIBUTED_LOCKS_TABLE, "owner"));
        assertTrue(columnExists(DISTRIBUTED_LOCKS_TABLE, "owner_thread"));
        assertTrue(columnExists(DISTRIBUTED_LOCKS_TABLE, "acquire_time"));
        assertTrue(indexExists(jdbcHelper, DISTRIBUTED_LOCKS_TABLE, "owner", "distributed_locks_owner", dbProperties.getDbType()));
        assertTrue(indexExists(jdbcHelper, DISTRIBUTED_LOCKS_TABLE, "owner_thread", "distributed_locks_owner_thread", dbProperties.getDbType()));
    }

    //v550e adds the 'nodes_repo_path_checksum' index on the repo_path_checksum column in nodes table
    @Test(dependsOnMethods = {"testV203NoHazelcastConversion"})
    public void testV203NoHazelcastMigrationConversion() throws SQLException {
        doWithConnection(jdbcHelper, this::makeChecksumColumnUnique);
        new DBSqlConverter("v550e").convert(jdbcHelper, dbProperties.getDbType());
        assertTrue(indexExists(jdbcHelper, NODES_TABLE, REPO_PATH_CHECKSUM_COL, "nodes_repo_path_checksum", dbProperties.getDbType()));
    }

    private boolean isColumnNullable(String tableName, String colName) throws SQLException {
        return withConnection(jdbcHelper, conn -> isColumnNullable(conn, tableName, colName));
    }

    private boolean isColumnNullable(Connection conn, String tableName, String colName) throws SQLException {
        boolean columnNullable = false;
        DatabaseMetaData metadata = conn.getMetaData();
        try (ResultSet rs = metadata.getColumns(null, null,
                normalizedName(tableName, metadata), normalizedName(colName, metadata))) {
            if (rs.next()) {
                columnNullable = "YES".equalsIgnoreCase(rs.getString(normalizedName("IS_NULLABLE", metadata)));
            }
        }
        return columnNullable;
    }

    //kombina to allow setting the unique index constraint on column repo_path_checksum
    private void makeChecksumColumnUnique(Connection con) throws SQLException {
        try (Statement statement = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
             ResultSet rs = statement.executeQuery("SELECT node_id, repo_path_checksum FROM nodes WHERE repo_path_checksum IS NULL")) {
            int i = 0;
            while (rs.next()) {
                rs.updateString(REPO_PATH_CHECKSUM_COL, "a" + i);
                rs.updateRow();
                i++;
            }
        }
    }
}
