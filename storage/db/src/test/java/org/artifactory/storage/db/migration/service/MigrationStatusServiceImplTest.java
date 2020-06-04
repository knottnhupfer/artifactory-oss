package org.artifactory.storage.db.migration.service;

import org.artifactory.storage.db.migration.dao.MigrationStatusDao;
import org.artifactory.storage.db.migration.entity.DbMigrationStatus;
import org.artifactory.storage.db.migration.model.MigrationStatus;
import org.artifactory.storage.db.migration.service.mapper.MigrationStatusMapperImpl;
import org.jfrog.common.ClockUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * @author Uriah Levy
 */
@Test
public class MigrationStatusServiceImplTest {

    @Mock
    MigrationStatusDao migrationStatusDao;

    @BeforeMethod
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    public void testFindMigrationByIdWithInfoBlob() throws SQLException {
        MigrationStatusStorageServiceImpl migrationStatusService = new MigrationStatusStorageServiceImpl(
                migrationStatusDao, new MigrationStatusMapperImpl());

        String theGreatestPresident = "{\"some_info\": \"I will be the greatest president\"}";
        when(migrationStatusDao.findMigrationById("metadata-migration"))
                .thenReturn(Optional.of(new DbMigrationStatus("metadata-migration",
                        ClockUtils.epochMillis(), ClockUtils.epochMillis() + 10,
                        theGreatestPresident.getBytes())));

        MigrationStatus migrationWithInfo = migrationStatusService
                .findMigrationByIdWithInfoBlob("metadata-migration", DummyInfoBlob.class);
        assertEquals(((DummyInfoBlob) migrationWithInfo.getMigrationInfoBlob()).getSomeInfo(), "I will be the greatest president");
    }
}
