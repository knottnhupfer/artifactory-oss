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

package org.artifactory.work.queue;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.HaAddon;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.api.repo.WorkItem;
import org.artifactory.api.repo.WorkQueue;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.mbean.MBeanRegistrationService;
import org.artifactory.spring.InternalArtifactoryContext;
import org.artifactory.work.queue.mbean.WorkQueueMBean;
import org.easymock.EasyMock;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.Map;

import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;

/**
 * @author Shay Bagants
 */
@Test
public class AsyncWorkQueueServiceTest {
    private File home;

    @BeforeMethod
    private void setup() throws IOException {
        home = Files.createTempDirectory(this.getClass().getSimpleName()).toFile();
        ArtifactoryHome artifactoryHome = new ArtifactoryHome(home);
        artifactoryHome.initArtifactorySystemProperties();
        ArtifactoryHome.bind(artifactoryHome);

        HaAddon haAddon = createNiceMock(HaAddon.class);
        expect(haAddon.getConflictsGuard(anyObject(String.class))).andReturn(null);

        AddonsManager addonsManager = createNiceMock(AddonsManager.class);
        expect(addonsManager.addonByType(HaAddon.class)).andReturn(haAddon).times(2);

        MBeanRegistrationService mbeanService = createNiceMock(MBeanRegistrationService.class);
        mbeanService.register(anyObject(WorkQueueMBean.class), anyObject(String.class), anyObject(String.class));
        expectLastCall().anyTimes();

        InternalArtifactoryContext context = EasyMock.createMock(InternalArtifactoryContext.class);
        expect(context.beanForType(AddonsManager.class)).andReturn(addonsManager).anyTimes();
        expect(context.beanForType(MBeanRegistrationService.class)).andReturn(mbeanService).times(2);

        EasyMock.replay(context, addonsManager, haAddon, mbeanService);
        ArtifactoryContextThreadBinder.bind(context);
    }

    @AfterMethod
    private void cleanup() {
        ArtifactoryContextThreadBinder.unbind();
        home.delete();
    }

    // Request the queue using two different method names and expecting both to return the same exact queue object
    public void testGetQueue() throws NoSuchMethodException {
        DebianTestService debianTestService = new DebianTestService();
        Method asyncMethod = debianTestService.getClass().getMethod("calculateDebianMetadataInternalAsync");
        Method syncMethod = debianTestService.getClass().getMethod("calculateDebianMetadataInternalSync");
        Method mvnMethod = debianTestService.getClass().getMethod("calculateMavenMetadataAsync");

        AsyncWorkQueueServiceImpl workQueueService = new AsyncWorkQueueServiceImpl();
        // request workqueue for both methods, expecting both to return the same queue object
        WorkQueue<WorkItem> asyncQueue = workQueueService.getWorkQueue(asyncMethod, debianTestService);
        WorkQueue<WorkItem> syncQueue = workQueueService.getWorkQueue(syncMethod, debianTestService);
        WorkQueue<WorkItem> mvnQueue = workQueueService.getWorkQueue(mvnMethod, debianTestService);
        // the comparision should not be by the content, but by the reference!
        assertTrue(asyncQueue == syncQueue);
        // make sure the maven queue is not the same as the debian
        assertTrue(asyncQueue != mvnQueue);

        // validate the queues map content
        Map<String, WorkQueue<WorkItem>> queues = workQueueService.getExistingWorkQueues();
        assertEquals(queues.size(), 3);
        assertNotEquals(queues.get("calculateDebianMetadataInternalAsync"), null);
        // the comparision should not be by the content, but by the reference! we expect the queue to have two keys, each key points on the same object
        assertTrue(queues.get("calculateDebianMetadataInternalAsync") == queues.get("calculateDebianMetadataInternalSync"));
        // make sure the maven queue is not the same as the debian
        assertTrue(queues.get("mvnQueue") != queues.get("calculateDebianMetadataInternalAsync"));
    }

    // we only use this class with these two methods to pass it into the AsyncWorkQueueServiceImpl when requesting a workQueue
    public static class DebianTestService {

        public void calculateDebianMetadataInternalAsync() {
        }

        public void calculateDebianMetadataInternalSync() {
        }

        public void calculateMavenMetadataAsync() {
        }
    }
}
