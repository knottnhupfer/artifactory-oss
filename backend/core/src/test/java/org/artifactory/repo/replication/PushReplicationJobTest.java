/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2018 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.artifactory.repo.replication;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.HaAddon;
import org.artifactory.addon.replication.ReplicationAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.api.security.SecurityService;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.replication.GlobalReplicationsConfigDescriptor;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.schedule.TaskBase;
import org.artifactory.spring.InternalArtifactoryContext;
import org.artifactory.storage.db.locks.service.DbLocksService;
import org.artifactory.storage.fs.lock.MonitoringReentrantLock;
import org.artifactory.storage.fs.lock.provider.JVMLockWrapper;
import org.artifactory.storage.fs.lock.provider.JvmConflictGuard;
import org.artifactory.storage.fs.lock.provider.LockWrapper;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.jfrog.storage.common.ConflictsGuard;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.artifactory.schedule.Task.PUSH_REPLICATION_URL;
import static org.artifactory.schedule.Task.REPO_KEY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Shay Bagants
 */
public class PushReplicationJobTest extends ArtifactoryHomeBoundTest {

    private final String SRC_REPO_KEY = "myRepo";
    private final String TARGET_URL = "http://momo.koko.com";
    private final String REPLICATION_KEY = SRC_REPO_KEY + ":" + TARGET_URL;
    private PushReplicationJob replicationJob;

    @Mock
    private LocalReplicationDescriptor replicationDescriptor;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private JobExecutionContext callbackContext;
    @Mock
    private InternalArtifactoryContext artifactoryContext;
    @Mock
    private CentralConfigService configService;
    @Mock
    private AddonsManager addonsManager;
    @Mock
    private ReplicationAddon replicationAddon;
    @Mock
    private CentralConfigDescriptor configDescriptor;
    @Mock
    private GlobalReplicationsConfigDescriptor globalReplicationsConfigDescriptor;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.initMocks(this);
        replicationJob = new PushReplicationJob();
    }

    @BeforeMethod
    public void beforeMethod() {
        ArtifactoryContextThreadBinder.bind(artifactoryContext);
        when(artifactoryContext.isReady()).thenReturn(true);
        when(globalReplicationsConfigDescriptor.isBlockPullReplications()).thenReturn(false);
        when(artifactoryContext.getCentralConfig()).thenReturn(configService);
        when(configService.getDescriptor()).thenReturn(configDescriptor);
        when(configDescriptor.getReplicationsConfig()).thenReturn(globalReplicationsConfigDescriptor);
        when(replicationDescriptor.getRepoKey()).thenReturn(SRC_REPO_KEY);
        when(replicationDescriptor.getUrl()).thenReturn(TARGET_URL);
        when(replicationDescriptor.getReplicationKey()).thenReturn(REPLICATION_KEY);
        when(configDescriptor.getLocalReplication(any(), any())).thenReturn(replicationDescriptor);
        when(artifactoryContext.beanForType(SecurityService.class)).thenReturn(mock(SecurityService.class));
        when(addonsManager.addonByType(ReplicationAddon.class)).thenReturn(replicationAddon);
        when(artifactoryContext.beanForType(AddonsManager.class)).thenReturn(addonsManager);
        replicateOnPrimaryOnly(false);
    }

    @AfterMethod
    public void cleanup() {
        reset(replicationAddon, callbackContext, configService, configDescriptor, globalReplicationsConfigDescriptor);
        ArtifactoryContextThreadBinder.unbind();
    }

    @Test
    public void testContextNotReadyReplicationSkipped() throws Exception {
        when(artifactoryContext.isReady()).thenReturn(false);
        testOnExecute(callbackContext, false);
    }

    @Test(dataProvider = "replicationJobAttributes")
    public void testExecuteAllowedOnPrimaryOnlyUsingPrimary(Map<String, Object> attributes) throws Exception {
        replicateOnPrimaryOnly(true);
        HaAddon haAddonMock = mock(HaAddon.class);
        when(haAddonMock.isPrimary()).thenReturn(true);
        when(addonsManager.addonByType(HaAddon.class)).thenReturn(haAddonMock);
        when(callbackContext.getJobDetail().getJobDataMap()).thenReturn(new JobDataMap(attributes));
        testOnExecute(callbackContext, true);
    }

    @Test(dataProvider = "replicationJobAttributesWithExpectedStatus")
    public void testExecuteAllowedOnPrimaryOnlyUsingSecondary(Map<String, Object> attributes,
            boolean expectedToPropagate) throws Exception {
        replicateOnPrimaryOnly(true);
        JobDataMap jobDataMap = new JobDataMap(attributes);
        when(callbackContext.getJobDetail().getJobDataMap()).thenReturn(jobDataMap);
        when(callbackContext.getMergedJobDataMap()).thenReturn(jobDataMap);
        HaAddon haAddonMock = mock(HaAddon.class);
        when(addonsManager.addonByType(HaAddon.class)).thenReturn(haAddonMock);
        when(haAddonMock.isPrimary()).thenReturn(false);

        testOnExecute(callbackContext, false);
        if (expectedToPropagate) {
            ArgumentCaptor<TaskBase> capturedTask = ArgumentCaptor.forClass(TaskBase.class);
            verify(haAddonMock).propagateTaskToPrimary(capturedTask.capture());
            attributes.forEach((key, val) -> Assert.assertEquals(capturedTask.getValue().getAttribute(key), val));
        } else {
            verify(haAddonMock, times(0)).propagateTaskToPrimary(any());
        }
    }

    @Test(dataProvider = "replicationJobAttributes")
    public void testExecuteAllowedOnAnyNode(Map<String, Object> attributes) throws Exception {
        AtomicInteger successfulReplications = new AtomicInteger(0);
        AtomicBoolean releaseLock = new AtomicBoolean(false);
        performLocalReplication(successfulReplications, releaseLock);
        int numOfNodes = 3;
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        when(callbackContext.getJobDetail().getJobDataMap()).thenReturn(
                new JobDataMap(attributes));
        HaAddon haAddonMock = mock(HaAddon.class);
        ConflictsGuard<Object> lockingMap = mock(ConflictsGuard.class);

        DummyConflictGuard conflictGuard = new DummyConflictGuard(new JVMLockWrapper(new MonitoringReentrantLock()));
        when(lockingMap.getLock(any())).thenReturn(conflictGuard);
        when(haAddonMock.getConflictsGuard(DbLocksService.REPLICATION_LOCK_CATEGORY)).thenReturn(lockingMap);
        when(addonsManager.addonByType(HaAddon.class)).thenReturn(haAddonMock);

        List<Future> futures = Lists.newArrayList();
        for (int i = 0; i < numOfNodes; i++) {
            futures.add(executorService.submit(getRunnable()));
        }
        //TODO [by shayb]: ugly code, replace by waiting condition soon I'll move it to the common test module
        long startedTime = System.currentTimeMillis();
        while (conflictGuard.count.get() != 3) {
            if (System.currentTimeMillis() - startedTime > TimeUnit.SECONDS.toMillis(10)) {
                shutdownExecutorService(executorService);
                Assert.fail();
            }
            Thread.sleep(30);
        }
        releaseLock.set(true);
        try {
            for (Future future : futures) {
                future.get(10, TimeUnit.SECONDS);
            }
        } finally {
            shutdownExecutorService(executorService);
        }
        verifyHandleReplicatrionExecuted(true);
    }

    private void shutdownExecutorService(ExecutorService executorService) throws InterruptedException {
        executorService.shutdown();
        executorService.awaitTermination(100, TimeUnit.SECONDS);
    }

    @DataProvider
    private Object[][] replicationJobAttributes() {
        return new Object[][]{
                // attributed for cron scheduled replication
                {ImmutableMap.of(REPO_KEY, SRC_REPO_KEY, PUSH_REPLICATION_URL, TARGET_URL)},
                // attributed for manual replication (containing the TASK_MANUAL_DESCRIPTOR attribute)
                {ImmutableMap.of(REPO_KEY, SRC_REPO_KEY, PUSH_REPLICATION_URL, TARGET_URL,
                        ReplicationAddon.TASK_MANUAL_DESCRIPTOR, replicationDescriptor)},
        };
    }

    @DataProvider
    private Object[][] replicationJobAttributesWithExpectedStatus() {
        return new Object[][]{
                {ImmutableMap.of(REPO_KEY, SRC_REPO_KEY, PUSH_REPLICATION_URL, TARGET_URL), false},
                {ImmutableMap.of(REPO_KEY, SRC_REPO_KEY, PUSH_REPLICATION_URL, TARGET_URL,
                        ReplicationAddon.TASK_MANUAL_DESCRIPTOR, replicationDescriptor), true},
        };
    }

    private class DummyConflictGuard extends JvmConflictGuard {
        public AtomicInteger count = new AtomicInteger(0);

        DummyConflictGuard(LockWrapper lockWrapper) {
            super(lockWrapper);
        }

        @Override
        public boolean tryToLock(long timeout, TimeUnit timeUnit) throws InterruptedException {
            count.incrementAndGet();
            return super.tryToLock(timeout, timeUnit);
        }
    }

    private Runnable getRunnable() {
        return () -> {
            try {
                replicationJob.onExecute(callbackContext);
            } catch (JobExecutionException e) {
                throw new RuntimeException(e);
            }
        };
    }


    private void performLocalReplication(AtomicInteger successfulReplication, AtomicBoolean shouldReleaseLock)
            throws IOException {
        doAnswer(invocation -> {
            long startedTime = System.currentTimeMillis();
            successfulReplication.incrementAndGet();
            // TODO: 11/07/2018 replace this with Condition once we move it to test-commons
            while (!shouldReleaseLock.get()) {
                if (System.currentTimeMillis() - startedTime > TimeUnit.SECONDS.toMillis(10)) {
                    Assert.fail("Failed waiting for other threads to give up while trying to acquire a lock");
                }
                Thread.sleep(50);
            }
            return null;
        }).when(replicationAddon).performLocalReplication(any());
    }

    private void replicateOnPrimaryOnly(boolean allowOnPrimaryOnly) {
        homeStub.setProperty(ConstantValues.replicationPrimaryOnly, String.valueOf(allowOnPrimaryOnly));
    }

    private void testOnExecute(JobExecutionContext callbackContext, boolean expectedReplication)
            throws Exception {
        replicationJob.onExecute(callbackContext);
        verifyHandleReplicatrionExecuted(expectedReplication);
    }

    private void verifyHandleReplicatrionExecuted(boolean expectedReplication) throws IOException {
        int expectedCalls = expectedReplication ? 1 : 0;
        verify(replicationAddon, times(expectedCalls)).performLocalReplication(any());
    }
}