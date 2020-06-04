package org.artifactory.metadata.service;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.aql.AqlService;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.result.AqlLazyResult;
import org.artifactory.aql.result.rows.AqlItem;
import org.artifactory.aql.result.rows.AqlLazyObjectResultStreamer;
import org.artifactory.aql.result.rows.FileInfoItemRow;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.event.EventOperatorId;
import org.artifactory.event.priority.service.model.PrioritizedNodeEvent;
import org.artifactory.event.work.NodeEventOperator;
import org.artifactory.event.work.NodeEventTaskManager;
import org.artifactory.event.work.StatusProbe;
import org.artifactory.event.work.StatusProbeImpl;
import org.artifactory.metadata.service.store.ArtifactoryMetadataClientConfigStore;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.security.access.AccessService;
import org.artifactory.spring.ContextCreationListener;
import org.artifactory.spring.Reloadable;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.db.event.entity.NodeEventCursorType;
import org.artifactory.storage.db.event.model.NodeEventCursor;
import org.artifactory.storage.db.event.service.NodeEventCursorService;
import org.artifactory.storage.db.event.service.metadata.model.MetadataEvent;
import org.artifactory.storage.db.fs.dao.NodesDao;
import org.artifactory.storage.db.fs.service.UpdateArtifactStatsEvent;
import org.artifactory.storage.db.migration.service.MigrationStatusStorageService;
import org.artifactory.storage.event.EventType;
import org.artifactory.storage.event.EventsService;
import org.jfrog.common.ClockUtils;
import org.jfrog.metadata.client.MetadataClient;
import org.jfrog.metadata.client.confstore.MetadataClientConfigStore;
import org.jfrog.metadata.client.model.MetadataPackage;
import org.jfrog.metadata.client.model.MetadataStatistics;
import org.jfrog.metadata.client.model.MetadataVersionRepo;
import org.jfrog.metadata.client.model.MetadataEntity;
import org.jfrog.metadata.client.model.event.MetadataEventEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

import javax.annotation.Nonnull;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static org.artifactory.aql.api.domain.sensitive.AqlApiItem.*;
import static org.artifactory.aql.api.internal.AqlBase.and;

/**
 * A business service that handles Metadata Events. Responsible for starting the event pipeline and initializing
 * the {@link MetadataClient}. Additionally, this service implements the {@link NodeEventOperator} which provides
 * the logic for handling Metadata events.
 *
 * @author Uriah Levy
 */
@Reloadable(beanClass = MetadataEventService.class, initAfter = {DbService.class, AccessService.class})
public class MetadataEventServiceImpl implements MetadataEventService, ContextCreationListener,
        ApplicationListener<UpdateArtifactStatsEvent> {

    private static final Logger log = LoggerFactory.getLogger(MetadataEventServiceImpl.class);

    private RepositoryService repositoryService;
    private NodeEventCursorService nodeEventCursorService;
    private AccessService accessService;
    private NodeEventTaskManager nodeEventTaskManager;
    private StatusProbe statusProbe;
    private MetadataClient metadataClient;
    private MetadataClientConfigStore configStore;
    private boolean operatorEnabled = false;
    private ExecutorService asyncProbeExecutor;
    private MigrationStatusStorageService migrationStatusStorageService;
    private NodesDao nodesDao;
    private AqlService aqlService;
    private EventsService eventsService;
    private MetadataEntityFacade metadataEntityFacade;
    private MetadataMigrationHelper metadataMigrationHelper;

    @Autowired
    public MetadataEventServiceImpl(MetadataEntityFacade metadataEntityFacade) {
        this.metadataEntityFacade = metadataEntityFacade;
        metadataEntityFacade.setMetadataService(this);
        asyncProbeExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1),
                new ThreadFactoryBuilder().setDaemon(true)
                        .setNameFormat("metadata-server-status-probe").build());
        this.statusProbe = new StatusProbeImpl();
    }

    @Override
    public void init() {
        metadataMigrationHelper = new MetadataMigrationHelper(migrationStatusStorageService,
                nodeEventCursorService, nodesDao, aqlService, eventsService, repositoryService, this);
        if (ConstantValues.metadataServerEventsEnabled.getBoolean()) {
            initOperatorCursor();
            configStore = new ArtifactoryMetadataClientConfigStore(accessService);
            initMetadataClient();
        }
    }

    @Override
    public void onContextCreated() {
        if (ConstantValues.metadataServerEventsEnabled.getBoolean()) {
            if (eventsService.isEnabled()) {
                metadataMigrationHelper.migrateOrStartEventPipe(this, nodeEventTaskManager);
            } else {
                log.info(
                        "The Node Tree event log is disabled, the Metadata Migration and Event Pipeline will not start");
            }
        }
    }

    @Override
    public boolean isOperatorEnabled() {
        return this.operatorEnabled;
    }

    @Override
    public MetadataClient getMetadataClient() {
        return metadataClient;
    }

    @Override
    public void initOperatorCursor() {
        String eventPipelineCursorId = getEventPipelineCursorId();
        NodeEventCursor eventCursor = nodeEventCursorService.cursorForOperator(eventPipelineCursorId);
        if (eventCursor == null) {
            nodeEventCursorService.insertCursor(eventPipelineCursorId, NodeEventCursorType.METADATA_EVENTS);
        }
    }

    @Override
    public void reindexAsync(List<String> reindexPaths) {
        try {
            reindexPaths.forEach(this::doReindex);
        } catch (Exception e) {
            log.error("Caught exception while attempting to reindex paths {}", reindexPaths, e);
        }
    }

    @Override
    public Response reindexSync(List<String> reindexPaths) {
        try {
            reindexPaths.forEach(this::doReindex);
        } catch (Exception e) {
            log.error("Caught exception while attempting to reindex paths {}", reindexPaths, e);
            return Response.serverError().build();
        }
        return Response.ok().build();
    }

    private void doReindex(String path) {
        if (StringUtils.isBlank(path)) {
            log.warn("Empty path found while re-indexing Metadata Server, skipping current path.");
            return;
        }
        RepoPath reindexPath = RepoPathFactory.create(path);
        Optional<LocalRepoDescriptor> localOrCacheRepoDescriptor = metadataEntityFacade.getLocalOrCacheRepoDescriptor(
                reindexPath.getRepoKey());
        if (localOrCacheRepoDescriptor.isPresent()) {
            RepoType repoType = localOrCacheRepoDescriptor.get().getType();
            Optional<Set<String>> supportedExtensionByRepoType = metadataEntityFacade
                    .getSupportedExtensionsByRepoType(repoType);
            if (supportedExtensionByRepoType.isPresent()) {
                reindexPathOrItem(reindexPath, supportedExtensionByRepoType.get());
            } else {
                log.warn("Unsupported repository type {}", repoType);
            }
        } else {
            log.warn("Unable to associate any local or remote repositories with path: {}", path);
        }
    }

    private void reindexPathOrItem(RepoPath reindexPath, Set<String> extension) {
        if (reindexPath.isFolder()) {
            // Index recursively
            AqlLazyObjectResultStreamer<FileInfoItemRow> result = getAllArtifactsUnderMeLazy(reindexPath, extension);
            FileInfoItemRow item;
            while ((item = result.getRow()) != null) {
                reindexItem(item.getRepoPath());
            }
        } else {
            reindexItem(reindexPath);
        }
    }

    private void reindexItem(RepoPath itemPath) {
        Optional<MetadataPackage> mdsPackage = metadataEntityFacade.createMdsPackageEntity(itemPath);
        if (mdsPackage.isPresent()) {
            log.info("Reindexing {} on the Metadata Server", itemPath);
            MetadataEvent metadataEvent = MetadataEvent.builder()
                    .eventTime(ClockUtils.epochMillis())
                    .eventType(EventType.update)
                    .metadataEventEntity(MetadataEventEntity.PACKAGE_UPDATED)
                    .metadataEntity(mdsPackage.get())
                    .path(itemPath.toPath())
                    .build();
            createOrUpdatePackage(metadataEvent);
        }
    }

    private AqlLazyObjectResultStreamer<FileInfoItemRow> getAllArtifactsUnderMeLazy(RepoPath reindexPath,
            Set<String> supportedLeadFileExtension) {
        AndClause<AqlApiItem> aqlFilter = and(
                repo().equal(reindexPath.getRepoKey()),
                getExtensionCriteria(supportedLeadFileExtension)
        );
        if (!reindexPath.isRoot()) {
            aqlFilter.append(
                    or(
                            AqlApiItem.path().matches(reindexPath.getPath() + "/*"), // deep child
                            AqlApiItem.path().equal(reindexPath.getPath())) // direct child
            );
        }
        AqlApiItem aqlQuery = createWithEmptyResults()
                .filter(aqlFilter)
                .include(repo(), path(), name());
        AqlLazyResult<AqlItem> lazyResult = aqlService.executeQueryLazy(aqlQuery);
        return new AqlLazyObjectResultStreamer<>(lazyResult, FileInfoItemRow.class);
    }

    private OrClause<AqlApiItem> getExtensionCriteria(Set<String> supportedLeadFileExtension) {
        OrClause<AqlApiItem> extensionCriteria = or();
        supportedLeadFileExtension.forEach(extension -> extensionCriteria.append(name().matches("*" + extension)));
        return extensionCriteria;
    }

    @Override
    public void operateOnEvent(PrioritizedNodeEvent nodeEvent) {
        MetadataEvent metadataEvent = prioritizedEventToMetadataEvent(nodeEvent);
        if (metadataEvent != null) {
            handleMetadataEvent(metadataEvent);
        }
    }

    public boolean meetsExecutionCriteria(PrioritizedNodeEvent nodeEvent) {
        return metadataEntityFacade.isLeadArtifact(nodeEvent.getRepoPath());
    }

    @Override
    public boolean isSquashEventsByPathAndTypeCombination() {
        return true;
    }

    @Override
    public long getDispatcherIntervalSecs() {
        return ConstantValues.metadataEventOperatorDispatcherIntervalSecs.getLong();
    }

    @Override
    public String getOperatorId() {
        if (metadataMigrationHelper.isMigrating()) {
            return getMigrationCursorId();
        }
        return getEventPipelineCursorId();
    }

    @Override
    public int getNumberOfEventThreads() {
        return ConstantValues.metadataEventOperatorThreads.getInt();
    }

    @Override
    public void onErrorThresholdReached() {
        disableServiceAndProbeSync();
    }

    @Override
    public Predicate<PrioritizedNodeEvent> getExecutionPredicate() {
        return this::meetsExecutionCriteria;
    }

    @Override
    public void onApplicationEvent(@Nonnull UpdateArtifactStatsEvent event) {
        if (isOperatorEnabled()) {
            try {
                updateVersionStatistics(event.getPathToDownloadCount());
            } catch (Exception e) {
                log.error("Unable to send statistics event to Metadata Server. Caught exception: {}", e.getMessage());
                log.debug("", e);
            }
        }
    }

    @Override
    public boolean isMigrating() {
        return metadataMigrationHelper.isMigrating();
    }

    @Override
    public String getMigrationCursorId() {
        return EventOperatorId.METADATA_OPERATOR_ID.getId() + "-migration";
    }

    @Override
    public String getEventPipelineCursorId() {
        return EventOperatorId.METADATA_OPERATOR_ID.getId() + "-events";
    }

    void setConfigStore(MetadataClientConfigStore configStore) {
        this.configStore = configStore;
    }

    void initMetadataClient() {
        closeCurrentClient();
        try {
            metadataClient = configStore.authenticatedClientBuilder().create();
            operatorEnabled = metadataClient.system().ping();
        } catch (Exception e) {
            log.warn("Unable to init the Metadata client. The Metadata Event pipeline will be disabled: {}",
                    e.getMessage());
            log.debug("", e);
            if (!statusProbe.isProbing()) { // If we're already probing, avoid cyclic re-init
                disableServiceAndProbeAsync();
            }
        }
    }

    private void handleMetadataEvent(MetadataEvent metadataEvent) {
        if (isPackageCreateOrUpdateEvent(metadataEvent)) {
            createOrUpdatePackage(metadataEvent);
        } else if (isPathDeletedEvent(metadataEvent)) {
            deletePath(metadataEvent);
        }
    }

    private void updateVersionStatistics(Map<RepoPath, Long> pathToDownloadCount) {
        MetadataStatistics metadataStatistics = metadataEntityFacade.getMdsItemStatistics(pathToDownloadCount);
        if (!metadataStatistics.getItems().isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Updating package statistics for '{}' items", metadataStatistics.getItems().size());
            }
            metadataClient.versions().updateVersionStatistics(metadataStatistics);
        }
    }

    private void createOrUpdatePackage(MetadataEvent metadataEvent) {
        MetadataPackage metadataPackage = (MetadataPackage) metadataEvent.getMetadataEntity();
        if (metadataPackage != null) {
            log.debug("Creating/updating Package Metadata for '{}' ({})", metadataPackage.getPkgid(),
                    metadataEvent.getPath());
            metadataClient.packages().createOrUpdate(metadataPackage);
        }
    }

    private void deletePath(MetadataEvent metadataEvent) {
        log.debug("Deleting Metadata Entity with path '{}'", metadataEvent.getPath());
        metadataClient.paths().deletePath((MetadataVersionRepo) metadataEvent.getMetadataEntity());
    }

    private boolean isPathDeletedEvent(MetadataEvent metadataEvent) {
        return metadataEvent.getMetadataEventEntity().equals(MetadataEventEntity.PATH_DELETED);
    }

    private boolean isPackageCreateOrUpdateEvent(MetadataEvent metadataEvent) {
        return metadataEvent.getMetadataEventEntity().equals(MetadataEventEntity.PACKAGE_CREATED) ||
                metadataEvent.getMetadataEventEntity().equals(MetadataEventEntity.PACKAGE_UPDATED);
    }

    private void closeCurrentClient() {
        if (metadataClient != null) {
            IOUtils.closeQuietly(metadataClient);
        }
    }

    private void disableServiceAndProbeSync() {
        if (operatorEnabled) {
            this.operatorEnabled = false;
            statusProbe.startProbing(MoreExecutors.newDirectExecutorService(), this::probeMetadataServer,
                    this::onSuccessfulStatusProbe);
        }
    }

    private void disableServiceAndProbeAsync() {
        this.operatorEnabled = false;
        statusProbe
                .startProbing(asyncProbeExecutor, this::probeMetadataServer, this::onSuccessfulStatusProbe);
    }

    private void onSuccessfulStatusProbe() {
        initMetadataClient();
        log.info("Metadata Service Probing succeeded. The event pipeline was re-enabled.");
    }

    private boolean probeMetadataServer() {
        try (MetadataClient noAuthMetadataClient = configStore.noAuthClientBuilder().create()) {
            return noAuthMetadataClient.system().ping();
        } catch (IOException e) {
            log.error("Unable to create the status probe HTTP client");
            log.debug("Unable to create the status probe HTTP client", e);
        }
        return false;
    }

    ExecutorService getAsyncProbeExecutor() {
        return asyncProbeExecutor;
    }

    MetadataEvent prioritizedEventToMetadataEvent(PrioritizedNodeEvent prioritizedNodeEvent) {
        return metadataEntityFacade.createMetadataEntity(prioritizedNodeEvent)
                .map(entityToEventEntity -> createMetadataEvent(prioritizedNodeEvent, entityToEventEntity.getLeft(),
                        entityToEventEntity.getRight()))
                .orElse(null);
    }

    private MetadataEvent createMetadataEvent(PrioritizedNodeEvent nodeEvent, MetadataEntity metadataEntity,
            MetadataEventEntity eventEntity) {
        return MetadataEvent.builder()
                .eventType(nodeEvent.getType())
                .metadataEventEntity(eventEntity)
                .eventTime(nodeEvent.getTimestamp())
                .metadataEntity(metadataEntity)
                .path(nodeEvent.getPath())
                .build();
    }

    void setStatusProbe(StatusProbe statusProbe) {
        this.statusProbe = statusProbe;
    }

    void setMetadataClient(MetadataClient metadataClient) {
        this.metadataClient = metadataClient;
    }

    @Autowired
    void setRepositoryService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @Autowired
    void setAccessService(AccessService accessService) {
        this.accessService = accessService;
    }

    @Autowired
    void setNodeEventCursorService(NodeEventCursorService nodeEventCursorService) {
        this.nodeEventCursorService = nodeEventCursorService;
    }

    @Autowired
    void setNodeEventTaskManager(NodeEventTaskManager nodeEventTaskManager) {
        this.nodeEventTaskManager = nodeEventTaskManager;
    }

    @Autowired
    void setMigrationStatusStorageService(
            MigrationStatusStorageService migrationStatusStorageService) {
        this.migrationStatusStorageService = migrationStatusStorageService;
    }

    @Autowired
    void setNodesDao(NodesDao nodesDao) {
        this.nodesDao = nodesDao;
    }

    @Autowired
    void setAqlService(AqlService aqlService) {
        this.aqlService = aqlService;
    }

    @Autowired
    void setEventsService(EventsService eventsService) {
        this.eventsService = eventsService;
    }

    public void setMetadataMigrationHelper(MetadataMigrationHelper metadataMigrationHelper) {
        this.metadataMigrationHelper = metadataMigrationHelper;
    }
}
