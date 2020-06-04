package org.artifactory.storage.db.upgrades.itest.version;

import org.artifactory.storage.db.upgrades.common.UpgradeBaseTest;
import org.testng.annotations.Test;

import java.sql.SQLException;

import static org.testng.Assert.assertTrue;

/**
 * @author Uriah Levy
 */
@Test
public class V216UpgradeTest extends UpgradeBaseTest {
    public void testNodePrioritiesExists() throws SQLException {
        String tableName = "node_event_priorities";
        assertTrue(tableExists(tableName), "Missing table node_event_priorities");
        assertTrue(columnExists(tableName, "priority_id"));
        assertTrue(columnExists(tableName, "path"));
        assertTrue(columnExists(tableName, "type"));
        assertTrue(columnExists(tableName, "operator_id"));
        assertTrue(columnExists(tableName, "priority"));
        assertTrue(columnExists(tableName, "timestamp"));
        assertTrue(columnExists(tableName, "retry_count"));
    }

    public void testNodeEventCursorExists() throws SQLException {
        String tableName = "node_event_cursor";
        assertTrue(tableExists(tableName), "Missing table node_event_cursor");
        assertTrue(columnExists(tableName, "operator_id"));
        assertTrue(columnExists(tableName, "event_marker"));
    }

    public void testMigrationStatusExists() throws SQLException {
        String tableName = "migration_status";
        assertTrue(tableExists(tableName), "Missing table migration_status");
        assertTrue(columnExists(tableName, "identifier"));
        assertTrue(columnExists(tableName, "started"));
        assertTrue(columnExists(tableName, "finished"));
        assertTrue(columnExists(tableName, "migration_info_blob"));
    }
}
