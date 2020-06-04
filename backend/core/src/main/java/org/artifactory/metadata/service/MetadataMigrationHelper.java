package org.artifactory.metadata.service;

import com.google.common.collect.ImmutableList;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.aql.AqlService;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.descriptor.repo.RepoBaseDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.event.provider.EventLogTableEventProvider;
import org.artifactory.event.provider.NodesTableEventProvider;
import org.artifactory.event.work.NodeEventTaskManager;
import org.artifactory.metadata.migration.MetadataMigrationInfoBlob;
import org.artifactory.storage.db.event.entity.NodeEventCursorType;
import org.artifactory.storage.db.event.model.NodeEventCursor;
import org.artifactory.storage.db.event.service.NodeEventCursorService;
import org.artifactory.storage.db.fs.dao.NodesDao;
import org.artifactory.storage.db.migration.model.MigrationStatus;
import org.artifactory.storage.db.migration.service.MigrationStatusStorageService;
import org.artifactory.storage.event.EventsService;
import org.artifactory.util.CollectionUtils;
import org.jfrog.common.ClockUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.artifactory.aql.api.domain.sensitive.AqlApiItem.*;

/**
 * @author Uriah Levy
 */
class MetadataMigrationHelper {
    private static final Logger log = LoggerFactory.getLogger(MetadataMigrationHelper.class);
    private MigrationStatusStorageService migrationStatusStorageService;
    private NodeEventCursorService nodeEventCursorService;
    private NodesDao nodesDao;
    private AqlService aqlService;
    private EventsService eventsService;
    private long currentEventLogTimestamp;
    private long lastNodeId;
    private List<String> reposToMigrate;
    private RepositoryService repoService;
    private MetadataEventService metadataEventService;
    // Represents the migration state
    private boolean isMigrating;

    static final String MIGRATION_IDENTIFIER = "metadata-service-migration";

    MetadataMigrationHelper(MigrationStatusStorageService migrationStatusStorageService,
                            NodeEventCursorService nodeEventCursorService, NodesDao nodesDao, AqlService aqlService,
                            EventsService eventsService, RepositoryService repoService, MetadataEventService metadataEventService) {
        this.migrationStatusStorageService = migrationStatusStorageService;
        this.nodeEventCursorService = nodeEventCursorService;
        this.nodesDao = nodesDao;
        this.aqlService = aqlService;
        this.eventsService = eventsService;
        this.repoService = repoService;
        this.metadataEventService = metadataEventService;
    }

    void migrateOrStartEventPipe(MetadataEventService metadataEventService, NodeEventTaskManager nodeEventTaskManager) {
        if (shouldMigrate()) {
            handleMigration(nodeEventTaskManager, metadataEventService);
        } else {
            nodeEventTaskManager
                    .startWithInfiniteProvider(metadataEventService, new EventLogTableEventProvider(eventsService));
        }
    }

    boolean shouldMigrate() {
        reposToMigrate = getReposToMigrate();
        if (isNodesTableEmpty() || CollectionUtils.isNullOrEmpty(reposToMigrate)) {
            insertOrSetCompletedMigrationAndUpdateOperatorCursor();
            return false;
        }
        Optional<MigrationStatus> migrationStatus = getMigrationStatus();
        return migrationStatus
                .map(status -> shouldMigrateByMigrationStatus(
                        nodeEventCursorService.cursorForOperator(metadataEventService.getMigrationCursorId()), status))
                .orElse(true);
    }

    boolean isMigrating() {
        return isMigrating;
    }

    void initMigrationState() {
        isMigrating = true;
        MigrationStatus metadataMigration = migrationStatusStorageService
                .findMigrationByIdWithInfoBlob(MIGRATION_IDENTIFIER, MetadataMigrationInfoBlob.class);
        if (metadataMigration == null) {
            currentEventLogTimestamp = eventsService.getLastEventLogTimestamp();
            lastNodeId = findLastNodeId(); // migration target
            insertNewDueMigration();
        } else {
            currentEventLogTimestamp = ((MetadataMigrationInfoBlob) metadataMigration.getMigrationInfoBlob())
                    .getCurrentEventLogTimestamp();
            lastNodeId = getMigrationTargetNodeId();
            reposToMigrate = ((MetadataMigrationInfoBlob) metadataMigration.getMigrationInfoBlob())
                    .getReposToMigrate();
        }
    }

    long getMigrationTargetNodeId() {
        MigrationStatus metadataMigration = migrationStatusStorageService
                .findMigrationByIdWithInfoBlob(MIGRATION_IDENTIFIER, MetadataMigrationInfoBlob.class);
        return ((MetadataMigrationInfoBlob) metadataMigration
                .getMigrationInfoBlob()).getMigrationTargetNodeId();
    }

    private void handleMigration(NodeEventTaskManager nodeEventTaskManager, MetadataEventService metadataEventService) {
        initMigrationState();
        log.info("Starting/continuing Metadata Migration for the following repos: {}", reposToMigrate);
        nodeEventTaskManager.startWithFiniteProvider(metadataEventService,
                new NodesTableEventProvider(aqlService, onBetweenBatches(),
                        onFinish(nodeEventTaskManager, metadataEventService), this::migrationBatchQuery));
    }

    private Runnable onFinish(NodeEventTaskManager nodeEventTaskManager, MetadataEventService metadataEventService) {
        return () -> {
            isMigrating = false;
            migrationStatusStorageService.migrationDone(MIGRATION_IDENTIFIER);
            nodeEventTaskManager.stopDispatcherThread(metadataEventService.getOperatorId());
            // Update the events cursor
            nodeEventCursorService.updateCursor(metadataEventService.getEventPipelineCursorId(),
                    currentEventLogTimestamp);
            // Delete the migration cursor
            nodeEventCursorService.deleteCursor(metadataEventService.getMigrationCursorId());
            nodeEventTaskManager
                    .startWithInfiniteProvider(metadataEventService, new EventLogTableEventProvider(eventsService));
        };
    }

    private List<RepoType> getSupportedRepoTypes() {
        return ImmutableList
                .of(RepoType.Docker, RepoType.Npm, RepoType.YUM, RepoType.Maven, RepoType.NuGet, RepoType.Bower,
                        RepoType.Chef, RepoType.Conda, RepoType.Gems, RepoType.Go, RepoType.CocoaPods, RepoType.Debian,
                        RepoType.CRAN);
    }

    private AqlApiItem migrationBatchQuery(NodeEventCursor currentCursor) {
        return createWithEmptyResults()
                .filter(buildAqlFilterByNodeIdAndRepoCriteria(currentCursor))
                .include(repo(), path(), name(), itemId())
                .limit(1000)
                .addSortElement(itemId()).asc();
    }

    private AndClause<AqlApiItem> buildAqlFilterByNodeIdAndRepoCriteria(NodeEventCursor currentCursor) {
        AndClause<AqlApiItem> nodeIdAndReposCriteria = new AndClause<>();
        // node-id criteria
        nodeIdAndReposCriteria.append(and(
                itemId().greater(currentCursor.getEventMarker()),
                itemId().lessEquals(lastNodeId)
        ));
        // Repos to migrate criteria
        OrClause<AqlApiItem> reposCriteria = or();
        reposToMigrate.forEach(repo -> reposCriteria.append(repo().equal(repo)));
        if (!reposCriteria.isEmpty()) {
            nodeIdAndReposCriteria.append(reposCriteria);
        }
        return nodeIdAndReposCriteria;
    }

    void insertNewDueMigration() {
        log.info("Migration Status is missing for '{}'. Triggering the migration process.", MIGRATION_IDENTIFIER);
        MigrationStatus migrationStatus = new MigrationStatus(MIGRATION_IDENTIFIER, ClockUtils.epochMillis(), 0,
                new MetadataMigrationInfoBlob(lastNodeId, currentEventLogTimestamp, reposToMigrate));
        migrationStatusStorageService.insertMigrationWithInfoBlob(migrationStatus);
        nodeEventCursorService
                .insertCursor(metadataEventService.getMigrationCursorId(), NodeEventCursorType.METADATA_MIGRATION);
    }

    private List<String> getReposToMigrate() {
        List<String> repoKeys = repoService.getLocalAndCachedRepoDescriptors().stream()
                .filter(repo -> getSupportedRepoTypes().contains(repo.getType()))
                .map(RepoBaseDescriptor::getKey)
                .collect(Collectors.toList());
        if (CollectionUtils.isNullOrEmpty(repoKeys)) {
            log.info(
                    "No repositories found that correspond to the supported Metadata Migration types. Supported types are {}",
                    getSupportedRepoTypes());
        }
        return repoKeys;
    }

    void insertOrSetCompletedMigrationAndUpdateOperatorCursor() {
        if (migrationStatusStorageService.findMigrationById(MIGRATION_IDENTIFIER) == null) {
            log.info("Inserting completed migration status for '{}'", MIGRATION_IDENTIFIER);
            MigrationStatus migrationStatus = new MigrationStatus(MIGRATION_IDENTIFIER, ClockUtils.epochMillis(),
                    ClockUtils.epochMillis(), new MetadataMigrationInfoBlob());
            migrationStatusStorageService.insertMigration(migrationStatus);
        } else {
            log.info("Updating existing migration status for '{}' to finished", MIGRATION_IDENTIFIER);
            migrationStatusStorageService.migrationDone(MIGRATION_IDENTIFIER);
        }
        // The event pipe will start from the most recent timestamp of the event log.
        nodeEventCursorService
                .updateCursor(metadataEventService.getEventPipelineCursorId(), resolveLatestEventLogTimestamp());
    }

    private long resolveLatestEventLogTimestamp() {
        // Go back up to 1 hour to compensate for any possible deployments to other cluster nodes during an upgrade
        long latestTimestampMinusBacklog = eventsService.getLastEventLogTimestamp() - TimeUnit.HOURS.toMillis(1);
        if (latestTimestampMinusBacklog < 0) {
            return 0;
        }
        log.debug("Last event log timestamp set to {}", latestTimestampMinusBacklog);
        return latestTimestampMinusBacklog;
    }

    private boolean shouldMigrateByMigrationStatus(NodeEventCursor currentCursor, MigrationStatus migrationStatus) {
        if (migrationStatus.isFinished()) { // Migration done
            return false;
        }
        MetadataMigrationInfoBlob migrationInfoBlob = (MetadataMigrationInfoBlob) migrationStatus
                .getMigrationInfoBlob();
        if (migrationInfoBlob == null) {
            log.warn("Metadata Migration Info Blob is empty. Metadata migration will not start.");
            return false;
        }
        if (!migrationTargetReached(currentCursor, migrationInfoBlob.getMigrationTargetNodeId())) {
            log.info(
                    "Current migration cursor is at '{}', and migration target is '{}'. Starting / continuing migration.",
                    currentCursor.getEventMarker(), migrationInfoBlob.getMigrationTargetNodeId());
            return true;
        }
        log.debug("Current migration cursor is at '{}', and migration target is '{}'. Metadata Migration will not run.",
                currentCursor.getEventMarker(), migrationInfoBlob.getMigrationTargetNodeId());
        return false;
    }

    private boolean migrationTargetReached(NodeEventCursor currentCursor, long migrationTargetNodeId) {
        return currentCursor.getEventMarker() >= migrationTargetNodeId;
    }

    private Consumer<Integer> onBetweenBatches() {
        return resultSetSize -> log
                .info("Metadata Migration is handling a batch of {} results", resultSetSize);
    }

    private boolean isNodesTableEmpty() {
        if (findLastNodeId() == 0) {
            log.info("No node candidates found for migration. Migration will not run");
            return true;
        }
        return false;
    }

    private long findLastNodeId() {
        try {
            return nodesDao.findLastNodeId();
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private Optional<MigrationStatus> getMigrationStatus() {
        return Optional.ofNullable(migrationStatusStorageService
                .findMigrationByIdWithInfoBlob(MIGRATION_IDENTIFIER, MetadataMigrationInfoBlob.class));
    }
}
