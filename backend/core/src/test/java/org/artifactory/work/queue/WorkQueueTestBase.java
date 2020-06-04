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
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.converter.ConverterManager;
import org.artifactory.sapi.common.ExportSettings;
import org.artifactory.sapi.common.ImportSettings;
import org.artifactory.schedule.CachedThreadPoolTaskExecutor;
import org.artifactory.schedule.TaskService;
import org.artifactory.spring.SpringConfigPaths;
import org.artifactory.storage.binstore.service.BinaryService;
import org.artifactory.storage.fs.lock.provider.JvmConflictsGuard;
import org.artifactory.storage.spring.ArtifactoryStorageContext;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.artifactory.version.VersionProvider;
import org.jfrog.common.logging.logback.servlet.LogbackConfigManager;
import org.jfrog.config.ConfigurationManager;
import org.jfrog.storage.common.ConflictsGuard;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.util.Map;

import static org.easymock.EasyMock.*;

/**
 * @author Dan Feldman
 */
public class WorkQueueTestBase extends ArtifactoryHomeBoundTest {

    static String QUEUE_NAME = "Test Queue";
    protected AddonsManager addonsManager;
    protected ConflictsGuard lockingMapFactory;

    @BeforeMethod
    public void _setup() {
        DummyArtifactoryContext artifactoryContext = new DummyArtifactoryContext();
        ArtifactoryContextThreadBinder.bind(artifactoryContext);
        HaAddon haAddon = createMock(HaAddon.class);
        addonsManager = createMock(AddonsManager.class);
        lockingMapFactory = new JvmConflictsGuard(120);
        expect(haAddon.getConflictsGuard(QUEUE_NAME)).andReturn(lockingMapFactory).anyTimes();
        expect(addonsManager.addonByType(HaAddon.class)).andReturn(haAddon).anyTimes();
        replay(haAddon, addonsManager);
    }

    @AfterMethod
    public void _tearDown() {
        ArtifactoryContextThreadBinder.unbind();
    }


    private class DummyArtifactoryContext implements ArtifactoryStorageContext {

        @Override
        public <T> T beanForType(String name, Class<T> type) {
            return null;
        }

        @Override
        public CentralConfigService getCentralConfig() {
            return null;
        }

        @Override
        public <T> T beanForType(Class<T> type) {
            if (type.getTypeName().equals(CachedThreadPoolTaskExecutor.class.getName())) {
                return (T) new CachedThreadPoolTaskExecutor();
            } else if (type.getName().equals(AddonsManager.class.getName())) {
                return (T) addonsManager;
            } else {
                return null;
            }
        }

        @Override
        public <T> Map<String, T> beansForType(Class<T> type) {
            return null;
        }

        @Override
        public RepositoryService getRepositoryService() {
            return null;
        }

        @Override
        public AuthorizationService getAuthorizationService() {
            return null;
        }

        @Override
        public long getUptime() {
            return 0;
        }

        @Override
        public ArtifactoryHome getArtifactoryHome() {
            return null;
        }

        @Override
        public String getContextId() {
            return null;
        }

        @Override
        public SpringConfigPaths getConfigPaths() {
            return null;
        }

        @Override
        public String getServerId() {
            return "serverIdTest";
        }

        @Override
        public boolean isOffline() {
            return false;
        }

        @Override
        public void setOffline() {
        }

        @Override
        public ConverterManager getConverterManager() {
            return null;
        }

        @Override
        public VersionProvider getVersionProvider() {
            return null;
        }

        @Override
        public LogbackConfigManager getLogbackConfigManager() {
            return null;
        }

        @Override
        public ConfigurationManager getConfigurationManager() {
            return null;
        }

        @Override
        public void destroy() {
        }

        @Override
        public void exportTo(ExportSettings settings) {
        }

        @Override
        public void importFrom(ImportSettings settings) {
        }

        @Override
        public BinaryService getBinaryStore() {
            return null;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public TaskService getTaskService() {
            return null;
        }
    }


}
