package org.artifactory.release.bundle;

import com.google.common.collect.Lists;
import org.apache.http.concurrent.BasicFuture;
import org.artifactory.api.component.ComponentDetailsFetcher;
import org.artifactory.api.properties.PropertiesService;
import org.artifactory.api.rest.distribution.bundle.models.ReleaseArtifact;
import org.artifactory.api.rest.distribution.bundle.models.ReleaseBundleModel;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.bundle.BundleTransactionStatus;
import org.artifactory.bundle.BundleType;
import org.artifactory.repo.service.RepositoryServiceImpl;
import org.artifactory.storage.db.bundle.dao.ArtifactBundlesDao;
import org.artifactory.storage.db.bundle.dao.BundleBlobsDao;
import org.artifactory.storage.db.bundle.model.BundleNode;
import org.artifactory.storage.db.fs.dao.NodesDao;
import org.artifactory.storage.fs.service.FileService;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

/**
 * @author Eli Skoran 04/05/2020
 */

@Test
public class ReleaseBundleServiceImplTest {

    @Mock
    ArtifactBundlesDao bundlesDao;
    @Mock
    BundleBlobsDao blobsDao;
    @Mock
    NodesDao nodesDao;
    @Mock
    RepositoryServiceImpl repoService;
    @Mock
    PropertiesService propertiesService;
    @Mock
    ArtifactBundlesDao artifactsBundleDao;
    @Mock
    ComponentDetailsFetcher componentDetailsFetcher;
    @Mock
    AuthorizationService authorizationService;
    @Mock
    InternalReleaseBundleService internalReleaseBundleService;
    @Mock
    FileService fileService;

    private ReleaseBundleServiceImpl service;

    @BeforeMethod
    public void setup() throws SQLException {
        MockitoAnnotations.initMocks(this);

        service = new ReleaseBundleServiceImpl(
                bundlesDao,
                blobsDao,
                nodesDao,
                repoService,
                propertiesService,
                artifactsBundleDao,
                componentDetailsFetcher,
                authorizationService,
                internalReleaseBundleService,
                fileService
        );

        service.executorService = new SyncedExecutorService();
        service.initialTimeBetweenRetries = 1;
    }

    public void testCopyBundleArtifactsAsync_basic() throws SQLException {
        when(propertiesService.setProperties(any(), any(), anyBoolean())).thenReturn(true);
        when(nodesDao.getNodeId(any())).thenReturn(111111L);
        when(artifactsBundleDao.create(any(BundleNode.class))).thenReturn(1);
        when(bundlesDao.completeBundle(any(), any(), any())).thenReturn(1);

        service.copyBundleArtifactsAsync(getTestModel(), "dummyRepo", getTestMapping());

        verify(propertiesService, times(2)).setProperties(any(), any(), anyBoolean());
        verify(nodesDao, times(2)).getNodeId(any());
        verify(artifactsBundleDao, times(2)).create(any(BundleNode.class));
        verify(bundlesDao, times(1)).completeBundle(any(), any(), any());

    }

    public void testCopyBundleArtifactsAsync_failStore() throws SQLException {
        when(propertiesService.setProperties(any(), any(), anyBoolean())).thenReturn(true);
        when(nodesDao.getNodeId(any())).thenReturn(111111L);
        when(artifactsBundleDao.create(any(BundleNode.class))).thenThrow(new SQLException("MySQLTransactionRollbackException: Lock wait timeout exceeded; try restarting transaction"));
        when(bundlesDao.setBundleStatus("platform", "7.5.1", BundleType.SOURCE, BundleTransactionStatus.FAILED))
                .thenReturn(1);

        service.copyBundleArtifactsAsync(getTestModel(), "dummyRepo", getTestMapping());

        verify(propertiesService, times(1)).setProperties(any(), any(), anyBoolean());
        verify(nodesDao, times(1)).getNodeId(any());
        verify(artifactsBundleDao, times(1)).create(any(BundleNode.class));
        verify(bundlesDao, times(1))
                .setBundleStatus("platform", "7.5.1", BundleType.SOURCE, BundleTransactionStatus.FAILED);
    }

    public void testCopyBundleArtifactsAsync_failStoreWithRetries() throws SQLException {
        when(propertiesService.setProperties(any(), any(), anyBoolean())).thenReturn(true);
        when(nodesDao.getNodeId(any())).thenReturn(111111L);
        when(artifactsBundleDao.create(any(BundleNode.class))).thenThrow(new SQLException("MySQLTransactionRollbackException: Lock wait timeout exceeded; try restarting transaction"));
        // fail for the first few tries, then succeed..
        when(bundlesDao.setBundleStatus("platform", "7.5.1", BundleType.SOURCE, BundleTransactionStatus.FAILED))
                .thenThrow(new SQLException("MySQLTransactionRollbackException: Lock wait timeout exceeded; try restarting transaction"))
                .thenThrow(new SQLException("MySQLTransactionRollbackException: Lock wait timeout exceeded; try restarting transaction"))
                .thenThrow(new SQLException("MySQLTransactionRollbackException: Lock wait timeout exceeded; try restarting transaction"))
                .thenReturn(1);

        service.copyBundleArtifactsAsync(getTestModel(), "dummyRepo", getTestMapping());

        verify(propertiesService, times(1)).setProperties(any(), any(), anyBoolean());
        verify(nodesDao, times(1)).getNodeId(any());
        verify(artifactsBundleDao, times(1)).create(any(BundleNode.class));
        verify(bundlesDao, times(4))
                .setBundleStatus("platform", "7.5.1", BundleType.SOURCE, BundleTransactionStatus.FAILED);
    }

    private ReleaseBundleModel getTestModel() {
        ReleaseArtifact artifact1 = new ReleaseArtifact();
        artifact1.setRepoPath("dummyRepo/file1.txt");
        artifact1.setChecksum("111aaa");

        ReleaseArtifact artifact2 = new ReleaseArtifact();
        artifact2.setRepoPath("dummyRepo/file2.txt");
        artifact2.setChecksum("222bbb");

        ReleaseBundleModel model = new ReleaseBundleModel();
        model.setName("platform");
        model.setVersion("7.5.1");
        model.setArtifacts(Lists.newArrayList(
                artifact1,
                artifact2
        ));

        return model;
    }

    private Map<String, String> getTestMapping() {
        Map<String, String> mapping = new HashMap<>();
        mapping.put("dummyRepo/file1.txt", "dummyRepo/file1.txt");
        mapping.put("dummyRepo/file2.txt", "dummyRepo/file2.txt");

        return mapping;
    }

    //TODO: extract
    private static class SyncedExecutorService extends ThreadPoolExecutor {

        public SyncedExecutorService() {
            super(0,
                    2,
                    60L,
                    TimeUnit.SECONDS,
                    new SynchronousQueue<>());
        }

        @Override
        public Future<?> submit(Runnable task) {
            task.run();
            return new BasicFuture<>(null);
        }
    }
}
