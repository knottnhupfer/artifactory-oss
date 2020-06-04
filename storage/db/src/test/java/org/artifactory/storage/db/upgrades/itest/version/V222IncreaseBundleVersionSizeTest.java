package org.artifactory.storage.db.upgrades.itest.version;

import org.artifactory.storage.db.upgrades.common.UpgradeBaseTest;
import org.testng.annotations.Test;

import java.sql.SQLException;

import static org.artifactory.storage.db.itest.DbTestUtils.getColumnSize;
import static org.jfrog.storage.util.DbUtils.withConnection;
import static org.testng.Assert.assertEquals;

/**
 * @author AndreiK.
 */
@Test
public class V222IncreaseBundleVersionSizeTest extends UpgradeBaseTest {

    @Test
    public void testArtifactBundlesVersionSizeChange() throws SQLException {
        int fetchedColumnSize = withConnection(jdbcHelper, conn -> getColumnSize(conn, "artifact_bundles", "version"));
        assertEquals(fetchedColumnSize, 32);
    }
}