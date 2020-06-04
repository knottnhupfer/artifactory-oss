package org.artifactory.storage.db.upgrades.itest.version;

import org.artifactory.storage.db.migration.dao.MigrationStatusDao;
import org.artifactory.storage.db.upgrades.common.UpgradeBaseTest;
import org.artifactory.storage.db.version.ArtifactoryDBVersion;
import org.artifactory.storage.db.version.converter.DBConverter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Uriah Levy
 */
@Test
public class V217RemoveMetadataMigrationEntryTest extends UpgradeBaseTest {
    @BeforeMethod
    public void setup() throws IOException, SQLException {
        resetToVersion(ArtifactoryDBVersion.v216);
        jdbcHelper.executeUpdate("INSERT INTO migration_status VALUES\n" +
                "(?, ?, ?, ?)", "metadata-service-migration", 0, 0, new byte[]{});
        assertTrue(hasEntries());
        for (DBConverter dbConverter : ArtifactoryDBVersion.v217.getConverters()) {
            dbConverter.convert(jdbcHelper, dbProperties.getDbType());
        }
    }

    public void testMetadataMigrationEntryRemoved() throws SQLException {
        assertFalse(hasEntries());
    }

    private boolean hasEntries() throws SQLException {
        try (ResultSet resultSet = jdbcHelper
                .executeSelect("SELECT * from " + MigrationStatusDao.MIGRATION_STATUS_TABLE + " where 1 = 1")) {
            return resultSet.next();
        }
    }
}
