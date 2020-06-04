package org.artifactory.storage.db.upgrades.itest.version;

import org.artifactory.storage.db.upgrades.common.UpgradeBaseTest;
import org.jfrog.storage.util.DbUtils;
import org.testng.annotations.Test;

import java.sql.SQLException;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Lior Gur
 */
@Test
public class V217UpgradeReleaseBundleIndexesTest extends UpgradeBaseTest {

    public void testArtifactBundleIndexes() throws SQLException {
        assertTrue(DbUtils.indexExists(jdbcHelper, "artifact_bundles", "name", "name_ver_sta_date_typ_repo_idx", dbProperties.getDbType()));
        assertTrue(DbUtils.indexExists(jdbcHelper, "artifact_bundles", "version", "name_ver_sta_date_typ_repo_idx", dbProperties.getDbType()));
        assertTrue(DbUtils.indexExists(jdbcHelper, "artifact_bundles", "status", "name_ver_sta_date_typ_repo_idx", dbProperties.getDbType()));
        assertTrue(DbUtils.indexExists(jdbcHelper, "artifact_bundles", "date_created", "name_ver_sta_date_typ_repo_idx", dbProperties.getDbType()));
        assertTrue(DbUtils.indexExists(jdbcHelper, "artifact_bundles", "type", "name_ver_sta_date_typ_repo_idx", dbProperties.getDbType()));
        assertTrue(DbUtils.indexExists(jdbcHelper, "artifact_bundles", "storing_repo", "name_ver_sta_date_typ_repo_idx", dbProperties.getDbType()));
    }
}
