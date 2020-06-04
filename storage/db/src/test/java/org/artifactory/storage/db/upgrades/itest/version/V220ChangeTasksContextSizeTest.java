package org.artifactory.storage.db.upgrades.itest.version;

import org.artifactory.storage.db.upgrades.common.UpgradeBaseTest;
import org.testng.annotations.Test;

import java.sql.SQLException;

import static org.artifactory.storage.db.itest.DbTestUtils.getColumnSize;
import static org.jfrog.storage.util.DbUtils.withConnection;
import static org.testng.Assert.assertEquals;

/**
 * @author Shay Bagants
 */
@Test
public class V220ChangeTasksContextSizeTest extends UpgradeBaseTest {

    public void testArtifactBundleIndexes() throws SQLException {
        int expectedSize = withConnection(jdbcHelper, conn -> getColumnSize(conn, "tasks", "task_context"));
        assertEquals(expectedSize, 2048);
    }
}
