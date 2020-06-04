package org.artifactory.storage.db.migration.itest;

import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.db.migration.dao.MigrationStatusDao;
import org.artifactory.storage.db.migration.entity.DbMigrationStatus;
import org.jfrog.common.ClockUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.Optional;

import static org.testng.Assert.*;

/**
 * @author Uriah Levy
 */
@Test
public class MigrationStatusDaoTest extends DbBaseTest {
    @Autowired
    private MigrationStatusDao dao;

    public void testInsertMigration() throws SQLException {
        final long started = ClockUtils.epochMillis();
        dao.insertMigration(new DbMigrationStatus("metadata-service-migration", started, 0, null));
        Optional<DbMigrationStatus> migration = dao.findMigrationById("metadata-service-migration");
        assertTrue(migration.isPresent());
        assertEquals(migration.get().getIdentifier(), "metadata-service-migration");
        assertEquals(migration.get().getStarted(), started);
        assertEquals(migration.get().getFinished(), 0);
    }

    public void testInsertMigrationWithInfoBlob() throws SQLException {
        final long started = ClockUtils.epochMillis();
        dao.insertMigration(new DbMigrationStatus("metadata-service-migration-2", started, 0,
                "{\"migrationTargetNodeId\":, \"123456789\", \"currentEventLogTimestamp\": \"123456\"}".getBytes()));
        Optional<DbMigrationStatus> migration = dao.findMigrationById("metadata-service-migration-2");
        assertTrue(migration.isPresent());
        assertEquals(migration.get().getIdentifier(), "metadata-service-migration-2");
        assertEquals(migration.get().getStarted(), started);
        assertEquals(migration.get().getFinished(), 0);
        assertNotNull(migration.get().getMigrationInfoBlob());
        assertEquals(new String(migration.get().getMigrationInfoBlob()),
                "{\"migrationTargetNodeId\":, \"123456789\", \"currentEventLogTimestamp\": \"123456\"}");
    }

    @Test(dependsOnMethods = "testInsertMigration", expectedExceptions = SQLException.class)
    public void testUniqueConstraint() throws SQLException {
        dao.insertMigration(new DbMigrationStatus("metadata-service-migration", 0, 0, null));
    }

    @Test(dependsOnMethods = "testInsertMigration")
    public void testUpdateMigration() throws SQLException {
        long finished = ClockUtils.epochMillis() + 100;
        dao.updateFinishTimeById("metadata-service-migration", finished);
        Optional<DbMigrationStatus> migration = dao.findMigrationById("metadata-service-migration");
        assertTrue(migration.isPresent());
        assertEquals(migration.get().getIdentifier(), "metadata-service-migration");
        assertEquals(migration.get().getFinished(), finished);
    }
}
