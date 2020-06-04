package org.artifactory.storage.db.upgrades.itest.version;

import org.artifactory.storage.db.upgrades.common.UpgradeBaseTest;
import org.jfrog.storage.util.DbUtils;
import org.testng.annotations.Test;

import java.sql.SQLException;

import static org.testng.Assert.assertTrue;

/**
 * @author Gal Ben Ami
 */
@Test
public class V218UpgradeBundleFilesIndexTest extends UpgradeBaseTest {

    public void testArtifactBundleIndexes() throws SQLException {
        assertTrue(DbUtils.indexExists(jdbcHelper, "bundle_files", "bundle_id", "bundle_id_bundle_files_idx", dbProperties.getDbType()));
    }
}
