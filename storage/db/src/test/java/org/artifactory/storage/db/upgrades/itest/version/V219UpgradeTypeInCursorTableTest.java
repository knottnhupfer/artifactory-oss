package org.artifactory.storage.db.upgrades.itest.version;

import org.artifactory.storage.db.upgrades.common.UpgradeBaseTest;
import org.artifactory.storage.db.version.ArtifactoryDBVersion;
import org.artifactory.storage.db.version.converter.DBConverter;
import org.jfrog.storage.util.DbUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.testng.Assert.assertTrue;

/**
 * @author Gal Ben Ami
 */
@Test
public class V219UpgradeTypeInCursorTableTest extends UpgradeBaseTest {

    @BeforeMethod
    public void setup() throws IOException, SQLException {
        resetToVersion(ArtifactoryDBVersion.v218);
        importSql("/sql/event_cursor_metadata.sql");
        for (DBConverter dbConverter : ArtifactoryDBVersion.v219.getConverters()) {
            dbConverter.convert(jdbcHelper, dbProperties.getDbType());
        }
    }

    public void testMigration() throws SQLException {
        assertTrue(DbUtils.columnExists(jdbcHelper, dbProperties.getDbType(), "node_event_cursor", "type"));
        try (ResultSet res = jdbcHelper.executeSelect("SELECT * FROM node_event_cursor")) {
            Assert.assertTrue(res.next());
            Assert.assertEquals(res.getString("operator_id"), "metadata-operator-events");
            Assert.assertEquals(res.getString("type"), "METADATA_EVENTS");
        }
    }
}
