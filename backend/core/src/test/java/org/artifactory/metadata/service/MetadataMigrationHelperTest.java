package org.artifactory.metadata.service;

import com.google.common.collect.ImmutableList;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.aql.AqlService;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.event.EventOperatorId;
import org.artifactory.metadata.migration.MetadataMigrationInfoBlob;
import org.artifactory.storage.db.event.entity.NodeEventCursorType;
import org.artifactory.storage.db.event.model.NodeEventCursor;
import org.artifactory.storage.db.event.service.NodeEventCursorService;
import org.artifactory.storage.db.fs.dao.NodesDao;
import org.artifactory.storage.db.migration.model.MigrationStatus;
import org.artifactory.storage.db.migration.service.MigrationStatusStorageService;
import org.artifactory.storage.event.EventsService;
import org.jfrog.common.ClockUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.Collections;

import static org.artifactory.metadata.service.MetadataMigrationHelper.MIGRATION_IDENTIFIER;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertFalse;

/**
 * @author Uriah Levy
 */
@Test
public class MetadataMigrationHelperTest {

    @Mock
    MigrationStatusStorageService migrationStatusStorageService;

    @Mock
    NodeEventCursorService cursorService;

    @Mock
    NodesDao nodesDao;

    @Mock
    AqlService aqlService;

    @Mock
    RepositoryService repoService;

    @Mock
    MetadataEventService metadataEventService;

    @Mock
    EventsService eventsService;

    @BeforeMethod
    private void beforeMethod() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void shouldMigrateThrowsRuntimeAfterSqlException() throws SQLException {
        MetadataMigrationHelper metadataMigrationHelper = new MetadataMigrationHelper(migrationStatusStorageService,
                cursorService, nodesDao, aqlService, eventsService, repoService, metadataEventService);
        when(nodesDao.findLastNodeId()).thenThrow(SQLException.class);
        metadataMigrationHelper.shouldMigrate();
    }

    public void shouldMigrateFalseOnNoNodes() throws SQLException {
        MetadataMigrationHelper metadataMigrationHelper = new MetadataMigrationHelper(migrationStatusStorageService,
                cursorService, nodesDao, aqlService, eventsService, repoService, metadataEventService);

        when(nodesDao.findLastNodeId()).thenReturn(0L);

        assertFalse(metadataMigrationHelper.shouldMigrate());
        verify(migrationStatusStorageService).insertMigration(any());
    }

    public void testHandleMissingMigration() throws SQLException {
        MetadataMigrationHelper metadataMigrationHelper = new MetadataMigrationHelper(migrationStatusStorageService,
                cursorService, nodesDao, aqlService, eventsService, repoService, metadataEventService);

        long targetNodeId = 11245;
        when(nodesDao.findLastNodeId()).thenReturn(targetNodeId);
        long lastEventLogTimestamp = ClockUtils.epochMillis();
        when(eventsService.getLastEventLogTimestamp()).thenReturn(lastEventLogTimestamp);

        metadataMigrationHelper.initMigrationState();
        verify(migrationStatusStorageService)
                .insertMigrationWithInfoBlob(argThat(migrationStatus ->
                        ((MetadataMigrationInfoBlob) migrationStatus.getMigrationInfoBlob())
                                .getMigrationTargetNodeId() == targetNodeId &&
                                ((MetadataMigrationInfoBlob) migrationStatus.getMigrationInfoBlob())
                                        .getCurrentEventLogTimestamp() == lastEventLogTimestamp));

    }

    public void testShouldMigrateFalseOnNoReposToMigrate() throws SQLException {
        MetadataMigrationHelper metadataMigrationHelper = new MetadataMigrationHelper(migrationStatusStorageService,
                cursorService, nodesDao, aqlService, eventsService, repoService, metadataEventService);

        when(nodesDao.findLastNodeId()).thenReturn(1L);

        when(repoService.getLocalAndCachedRepoDescriptors()).thenReturn(Collections.emptyList());

        boolean shouldMigrate = metadataMigrationHelper.shouldMigrate();
        assertFalse(shouldMigrate);
        verify(migrationStatusStorageService)
                .insertMigration(argThat(migrationStatus -> migrationStatus.getMigrationInfoBlob()
                        .equals(new MetadataMigrationInfoBlob())));
        verify(cursorService).updateCursor(any(), anyLong());

        LocalRepoDescriptor pypiRepo = new LocalRepoDescriptor();
        pypiRepo.setType(RepoType.Pypi);

        when(repoService.getLocalAndCachedRepoDescriptors()).thenReturn(ImmutableList.of(pypiRepo));
        // Not a supported repo type, still false
        shouldMigrate = metadataMigrationHelper.shouldMigrate();
        assertFalse(shouldMigrate);
        verify(migrationStatusStorageService, times(2))
                .insertMigration(argThat(migrationStatus -> migrationStatus.getMigrationInfoBlob()
                        .equals(new MetadataMigrationInfoBlob())));
    }

    public void testShouldMigrateTrueWithReposToMigrate() throws SQLException {
        MetadataMigrationHelper metadataMigrationHelper = new MetadataMigrationHelper(migrationStatusStorageService,
                cursorService, nodesDao, aqlService, eventsService, repoService, metadataEventService);

        when(nodesDao.findLastNodeId()).thenReturn(1L);
        LocalRepoDescriptor dockerRepo = new LocalRepoDescriptor();
        dockerRepo.setType(RepoType.Docker);

        when(repoService.getLocalAndCachedRepoDescriptors()).thenReturn(ImmutableList.of(dockerRepo));

        boolean shouldMigrate = metadataMigrationHelper.shouldMigrate();
        assertTrue(shouldMigrate);
        verify(migrationStatusStorageService, times(0)).insertMigration(any());
    }

    public void testShouldMigrateByCursorTrue() throws SQLException {
        MetadataMigrationHelper metadataMigrationHelper = new MetadataMigrationHelper(migrationStatusStorageService,
                cursorService, nodesDao, aqlService, eventsService, repoService, metadataEventService);

        when(nodesDao.findLastNodeId()).thenReturn(ClockUtils.epochMillis());
        NodeEventCursor currentCursor = new NodeEventCursor(EventOperatorId.METADATA_OPERATOR_ID.getId(),
                ClockUtils.epochMillis() + 100, NodeEventCursorType.METADATA_MIGRATION);

        LocalRepoDescriptor dockerRepo = new LocalRepoDescriptor();
        dockerRepo.setType(RepoType.Docker);
        when(metadataEventService.getOperatorId()).thenReturn(EventOperatorId.METADATA_OPERATOR_ID.getId());
        when(metadataEventService.getMigrationCursorId()).thenReturn(EventOperatorId.METADATA_OPERATOR_ID.getId() + "-migration");
        when(repoService.getLocalAndCachedRepoDescriptors()).thenReturn(ImmutableList.of(dockerRepo));
        when(cursorService.cursorForOperator(EventOperatorId.METADATA_OPERATOR_ID.getId() + "-migration")).thenReturn(currentCursor);
        when(migrationStatusStorageService.findMigrationByIdWithInfoBlob(MIGRATION_IDENTIFIER,
                MetadataMigrationInfoBlob.class))
                .thenReturn(new MigrationStatus(MIGRATION_IDENTIFIER, ClockUtils.epochMillis()
                        - 10_000, 0, new MetadataMigrationInfoBlob(ClockUtils.epochMillis() + Integer.MAX_VALUE, 0,
                        ImmutableList.of("docker-local", "npm-local"))));
        assertTrue(metadataMigrationHelper.shouldMigrate());
    }

    public void testInitMigrationStatusWithExistingMigration() {
        MetadataMigrationHelper metadataMigrationHelper = spy(new MetadataMigrationHelper(migrationStatusStorageService,
                cursorService, nodesDao, aqlService, eventsService, repoService, metadataEventService));

        when(migrationStatusStorageService.findMigrationById(MIGRATION_IDENTIFIER))
                .thenReturn(new MigrationStatus(MIGRATION_IDENTIFIER, ClockUtils.epochMillis()
                        - 10_000, 0,
                        new MetadataMigrationInfoBlob(ClockUtils.epochMillis() + Integer.MAX_VALUE, 0, null)));
        when(migrationStatusStorageService
                .findMigrationByIdWithInfoBlob(MIGRATION_IDENTIFIER, MetadataMigrationInfoBlob.class))
                .thenReturn(new MigrationStatus(MIGRATION_IDENTIFIER, ClockUtils.epochMillis()
                        - 10_000, 0,
                        new MetadataMigrationInfoBlob(ClockUtils.epochMillis() + Integer.MAX_VALUE, 0, null)));

        metadataMigrationHelper.initMigrationState();

        verify(eventsService, times(0)).getLastEventLogTimestamp();
        verify(metadataMigrationHelper).getMigrationTargetNodeId();
    }

    public void testInitMigrationStatusWithNonExistingMigration() throws SQLException {
        MetadataMigrationHelper metadataMigrationHelper = spy(new MetadataMigrationHelper(migrationStatusStorageService,
                cursorService, nodesDao, aqlService, eventsService, repoService, metadataEventService));

        when(migrationStatusStorageService.findMigrationById(MIGRATION_IDENTIFIER))
                .thenReturn(null);
        when(nodesDao.findLastNodeId()).thenReturn(10_000L);
        doNothing().when(metadataMigrationHelper).insertNewDueMigration();

        metadataMigrationHelper.initMigrationState();

        verify(metadataMigrationHelper, times(0)).insertOrSetCompletedMigrationAndUpdateOperatorCursor();
        verify(metadataMigrationHelper, times(0)).getMigrationTargetNodeId();
        verify(metadataMigrationHelper).insertNewDueMigration();
        verify(eventsService).getLastEventLogTimestamp();
    }

    public void assertMigrationIdIsSame() {
        assertEquals(MetadataMigrationHelper.MIGRATION_IDENTIFIER, "metadata-service-migration");
    }
}