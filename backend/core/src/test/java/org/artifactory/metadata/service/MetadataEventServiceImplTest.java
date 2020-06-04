package org.artifactory.metadata.service;

import com.google.common.collect.ImmutableList;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.config.CentralConfigDescriptorImpl;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.event.EventOperatorId;
import org.artifactory.event.priority.service.model.PrioritizedNodeEvent;
import org.artifactory.event.work.NodeEventTaskManager;
import org.artifactory.event.work.StatusProbe;
import org.artifactory.event.work.StatusProbeImpl;
import org.artifactory.metadata.service.provider.RpmMetadataProvider;
import org.artifactory.model.xstream.fs.FileInfoImpl;
import org.artifactory.model.xstream.fs.PropertiesImpl;
import org.artifactory.model.xstream.fs.StatsImpl;
import org.artifactory.security.access.AccessService;
import org.artifactory.spring.ArtifactoryApplicationContext;
import org.artifactory.storage.db.event.service.NodeEventCursorService;
import org.artifactory.storage.db.event.service.metadata.mapper.MetadataEntityMapperImpl;
import org.artifactory.storage.db.event.service.metadata.model.MetadataEvent;
import org.artifactory.storage.db.migration.service.MigrationStatusStorageService;
import org.artifactory.storage.event.EventType;
import org.artifactory.storage.event.EventsService;
import org.artifactory.storage.fs.service.FileService;
import org.artifactory.storage.fs.service.PropertiesService;
import org.artifactory.storage.fs.service.StatsService;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.jfrog.common.ClockUtils;
import org.jfrog.metadata.client.MetadataClient;
import org.jfrog.metadata.client.MetadataClientBuilder;
import org.jfrog.metadata.client.PackageClient;
import org.jfrog.metadata.client.SystemClient;
import org.jfrog.metadata.client.confstore.MetadataClientConfigStore;
import org.jfrog.metadata.client.exception.MetadataClientException;
import org.jfrog.metadata.client.model.MetadataFile;
import org.jfrog.metadata.client.model.MetadataLicense;
import org.jfrog.metadata.client.model.MetadataPackageImpl;
import org.jfrog.metadata.client.model.MetadataVersion;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.RejectedExecutionException;
import java.util.stream.IntStream;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author Uriah Levy
 */
@Test
public class MetadataEventServiceImplTest extends ArtifactoryHomeBoundTest {

    @Mock
    FileService fileService;

    @Mock
    PropertiesService propertiesService;

    @Mock
    RepositoryService repositoryService;

    @Mock
    NodeEventCursorService nodeEventCursorService;

    @Mock
    StatsService statsService;

    @Mock
    MetadataClient metadataClient;

    @Mock
    NodeEventTaskManager taskManager;

    @Mock
    PackageClient packageClient;

    @Mock
    ArtifactoryApplicationContext context;

    @Mock
    private AccessService accessService;

    @Mock
    MigrationStatusStorageService migrationStatusStorageService;

    @Mock
    EventsService eventsService;

    private RpmMetadataProvider rpmMetadataResolver = new RpmMetadataProvider();


    @BeforeMethod
    void beforeMethod() {
        MockitoAnnotations.initMocks(this);
        homeStub.setProperty(ConstantValues.metadataServerEventsEnabled, "true");
    }

    public void testEventEntryToMetadataEvent() {
        ArtifactoryContextThreadBinder.bind(context);
        CentralConfigService configService = mock(CentralConfigService.class);
        MetadataEntityMapperImpl metadataEntityMapper = mock(MetadataEntityMapperImpl.class);
        MetadataEventServiceImpl metadataEventService = new MetadataEventServiceImpl(
                new MetadataEntityFacade(ImmutableList.of(rpmMetadataResolver), fileService, propertiesService,
                        repositoryService, metadataEntityMapper));
        setFields(metadataEventService);

        PrioritizedNodeEvent prioritizedNodeEvent = new PrioritizedNodeEvent(123456L, "yum-local/curl/curl.rpm",
                EventType.props,
                EventOperatorId.METADATA_OPERATOR_ID.getId(), 0, ClockUtils.epochMillis(), 0, PrioritizedNodeEvent.EventStatus.PENDING);
        MetadataPackageImpl metadataEntity = getMdsPackage();
        MetadataVersion metadataVersion = getMdsVersion();
        metadataVersion.setFiles(Collections.singletonList(getMdsFile()));
        metadataEntity.setVersions(Collections.singletonList(metadataVersion));
        FileInfoImpl fileInfo = new FileInfoImpl(prioritizedNodeEvent.getRepoPath(), 1);

        when(metadataEntityMapper.metadataBomToMdsPackage(any())).thenReturn(getMdsPackage());
        when(metadataEntityMapper.metadataBomToMdsVersion(any())).thenReturn(getMdsVersion());
        when(metadataEntityMapper.metadataBomToMdsFile(any())).thenReturn(getMdsFile());
        when(fileService.loadItem(prioritizedNodeEvent.getRepoPath())).thenReturn(fileInfo);
        when(propertiesService.getProperties(fileInfo)).thenReturn(getRpmProperties());
        when(repositoryService.localOrCachedRepoDescriptorByKey("yum-local")).thenReturn(getYumLocalRepoDescriptor());
        when(context.beanForType(RepositoryService.class)).thenReturn(repositoryService);
        when(context.beanForType(StatsService.class)).thenReturn(statsService);
//        when(taskManager.isInfinite(metadataEventService.getOperatorId())).thenReturn(true);
        StatsImpl stats = new StatsImpl();
        stats.setDownloadCount(10);
        when(statsService.getStats(fileInfo)).thenReturn(stats);
        MutableCentralConfigDescriptor descriptor = new CentralConfigDescriptorImpl();
        descriptor.setUrlBase("http://localhost:8080/artifactory");
        when(context.beanForType(CentralConfigService.class)).thenReturn(configService);
        when(configService.getDescriptor()).thenReturn(descriptor);
        metadataEventService.setMetadataMigrationHelper(mock(MetadataMigrationHelper.class));
        MetadataEvent metadataEvent = metadataEventService.prioritizedEventToMetadataEvent(prioritizedNodeEvent);

        assertNotNull(metadataEvent);
        assertEquals(metadataEvent.getMetadataEntity(), metadataEntity);
    }

    private void setFields(MetadataEventServiceImpl metadataEventService) {
        metadataEventService.setAccessService(accessService);
        metadataEventService.setRepositoryService(repositoryService);
        metadataEventService.setNodeEventCursorService(nodeEventCursorService);
        metadataEventService.setMetadataClient(metadataClient);
        metadataEventService.setNodeEventTaskManager(taskManager);
        metadataEventService.setMigrationStatusStorageService(migrationStatusStorageService);
        metadataEventService.setEventsService(eventsService);
        metadataEventService.onContextCreated();
    }

    public void testOperateOnNodeEvent() {
        ArtifactoryContextThreadBinder.bind(context);
        MetadataEntityMapperImpl metadataEntityMapper = mock(MetadataEntityMapperImpl.class);
        MetadataEventServiceImpl metadataEventService = new MetadataEventServiceImpl(
                new MetadataEntityFacade(ImmutableList.of(rpmMetadataResolver), fileService, propertiesService,
                        repositoryService, metadataEntityMapper));
        CentralConfigService configService = mock(CentralConfigService.class);
        setFields(metadataEventService);
        when(metadataClient.packages()).thenReturn(packageClient);
        PrioritizedNodeEvent prioritizedNodeEvent = new PrioritizedNodeEvent(123456L, "yum-local/curl/curl.rpm",
                EventType.props,
                EventOperatorId.METADATA_OPERATOR_ID.getId() + "-migration", 0, ClockUtils.epochMillis(), 0, PrioritizedNodeEvent.EventStatus.PENDING);
        FileInfoImpl fileInfo = new FileInfoImpl(prioritizedNodeEvent.getRepoPath(), 1);

        when(metadataEntityMapper.metadataBomToMdsPackage(any())).thenReturn(getMdsPackage());
        when(metadataEntityMapper.metadataBomToMdsVersion(any())).thenReturn(getMdsVersion());
        when(metadataEntityMapper.metadataBomToMdsFile(any())).thenReturn(getMdsFile());
        when(fileService.loadItem(prioritizedNodeEvent.getRepoPath())).thenReturn(fileInfo);
        when(propertiesService.getProperties(fileInfo)).thenReturn(getRpmProperties());
        when(repositoryService.localOrCachedRepoDescriptorByKey("yum-local")).thenReturn(getYumLocalRepoDescriptor());
        StatsImpl stats = new StatsImpl();
        stats.setDownloadCount(10);
        when(statsService.getStats(fileInfo)).thenReturn(stats);
        when(context.beanForType(RepositoryService.class)).thenReturn(repositoryService);
        when(context.beanForType(StatsService.class)).thenReturn(statsService);
        MetadataMigrationHelper migrationHelper = mock(MetadataMigrationHelper.class);
        when(migrationHelper.isMigrating()).thenReturn(true);
        metadataEventService.setMetadataMigrationHelper(migrationHelper);

        MutableCentralConfigDescriptor descriptor = new CentralConfigDescriptorImpl();
        descriptor.setUrlBase("http://localhost:8080/artifactory");
        when(context.beanForType(CentralConfigService.class)).thenReturn(configService);
        when(configService.getDescriptor()).thenReturn(descriptor);
        doNothing().when(packageClient).createOrUpdate(any());

        metadataEventService.operateOnEvent(prioritizedNodeEvent);
        verify(packageClient).createOrUpdate(any());
    }

    @Test(expectedExceptions = RejectedExecutionException.class)
    public void testOnlyOneProbeThread() {
        MetadataEventServiceImpl metadataEventService = new MetadataEventServiceImpl(
                new MetadataEntityFacade(ImmutableList.of(rpmMetadataResolver), fileService, propertiesService,
                        repositoryService, null));
        // Using the probe directly to avoid having to mock other entities
        setFields(metadataEventService);
        StatusProbe statusProbe = new StatusProbeImpl();
        IntStream.range(0, 50).forEach(i -> statusProbe
                .startProbing(metadataEventService.getAsyncProbeExecutor(), () -> false, () -> {
                }));
    }

    public void testInitFlow() {
        MetadataEventServiceImpl metadataEventService = new MetadataEventServiceImpl(
                new MetadataEntityFacade(ImmutableList.of(rpmMetadataResolver), fileService, propertiesService,
                        repositoryService, null));
        MetadataClientConfigStore configStore = mock(MetadataClientConfigStore.class);
        MetadataClientBuilder clientBuilder = mock(MetadataClientBuilder.class);

        metadataEventService.setConfigStore(configStore);
        StatusProbeImpl probeSpy = spy(new StatusProbeImpl());
        metadataEventService.setStatusProbe(probeSpy);

        when(configStore.authenticatedClientBuilder()).thenReturn(clientBuilder);
        when(configStore.noAuthClientBuilder()).thenReturn(clientBuilder);
        when(clientBuilder.create()).thenReturn(metadataClient);
        SystemClient systemClient = mock(SystemClient.class);
        when(metadataClient.system()).thenReturn(systemClient);
        when(systemClient.ping()).thenThrow(MetadataClientException.class).thenReturn(true);

        setFields(metadataEventService);
        metadataEventService.initMetadataClient();

        verify(probeSpy).startProbing(any(), any(), any());
        verify(systemClient, timeout(15000).times(3)).ping();
    }

    public void testInitFlowWithDisabledNativeUI() {
        homeStub.setProperty(ConstantValues.metadataServerEventsEnabled, "false");
        MetadataEventServiceImpl metadataEventService = new MetadataEventServiceImpl(
                new MetadataEntityFacade(ImmutableList.of(rpmMetadataResolver), fileService, propertiesService,
                        repositoryService, null));
        metadataEventService.setAccessService(accessService);
        MetadataClientConfigStore configStore = mock(MetadataClientConfigStore.class);
        MetadataClientBuilder clientBuilder = mock(MetadataClientBuilder.class);

        metadataEventService.setConfigStore(configStore);
        StatusProbeImpl probeSpy = spy(new StatusProbeImpl());
        metadataEventService.setStatusProbe(probeSpy);

        when(configStore.authenticatedClientBuilder()).thenReturn(clientBuilder);
        when(configStore.noAuthClientBuilder()).thenReturn(clientBuilder);
        when(clientBuilder.create()).thenReturn(metadataClient);
        SystemClient systemClient = mock(SystemClient.class);
        when(metadataClient.system()).thenReturn(systemClient);
        when(systemClient.ping()).thenThrow(MetadataClientException.class).thenReturn(true);

        metadataEventService.init();

        verify(probeSpy, times(0)).startProbing(any(), any(), any());
    }

    public void testInitFlowNonCyclicProbe() {
        MetadataEventServiceImpl metadataEventService = new MetadataEventServiceImpl(
                new MetadataEntityFacade(ImmutableList.of(rpmMetadataResolver), fileService, propertiesService,
                        repositoryService, null));
        MetadataClientConfigStore configStore = mock(MetadataClientConfigStore.class);
        MetadataClientBuilder clientBuilder = mock(MetadataClientBuilder.class);
        metadataEventService.setConfigStore(configStore);

        StatusProbeImpl probeSpy = spy(new StatusProbeImpl());
        metadataEventService.setStatusProbe(probeSpy);

        when(configStore.authenticatedClientBuilder()).thenReturn(clientBuilder);
        when(configStore.noAuthClientBuilder()).thenReturn(clientBuilder);
        when(clientBuilder.create()).thenReturn(metadataClient);

        SystemClient systemClient = mock(SystemClient.class);
        when(metadataClient.system()).thenReturn(systemClient);
        when(systemClient.ping()).thenThrow(MetadataClientException.class).thenReturn(false);

        setFields(metadataEventService);
        metadataEventService.initMetadataClient();

        verify(probeSpy).startProbing(any(), any(), any());
        verify(systemClient, atMost(2)).ping(); // Async probe may or may not start, but never more than 2.
    }

    private LocalRepoDescriptor getYumLocalRepoDescriptor() {
        LocalRepoDescriptor localRepoDescriptor = new LocalRepoDescriptor();
        localRepoDescriptor.setType(RepoType.YUM);
        return localRepoDescriptor;
    }

    private PropertiesImpl getRpmProperties() {
        PropertiesImpl itemProperties = new PropertiesImpl();
        itemProperties.put("rpm.metadata.name", "curl");
        itemProperties.put("artifactory.licenses", "Apache-2.0");
        itemProperties.put("artifactory.licenses", "LGPL");
        return itemProperties;
    }

    private MetadataPackageImpl getMdsPackage() {
        MetadataPackageImpl mdsPackage = new MetadataPackageImpl();
        mdsPackage.setPkgid("rpm:curl");
        mdsPackage.setPackageType("rpm");
        mdsPackage.setName("curl");
        mdsPackage
                .setLicenses(Arrays.asList(new MetadataLicense("Apache-2.0", "apache..bla", "http://apache", 123L, "OSS?"),
                        new MetadataLicense("LGPL", "lgpl..bla", "http://lgpl", 123L, "OSS?")));
        return mdsPackage;
    }

    private MetadataVersion getMdsVersion() {
        MetadataVersion metadataVersion = new MetadataVersion();
        metadataVersion.setPkgid("rpm:curl");
        metadataVersion.setName("1.0.0");
        return metadataVersion;
    }

    private MetadataFile getMdsFile() {
        MetadataFile metadataFile = new MetadataFile();
        metadataFile.setLead(true);
        metadataFile.setName("curl.rpm");
        return metadataFile;
    }
}