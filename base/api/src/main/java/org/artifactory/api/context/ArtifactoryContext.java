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

package org.artifactory.api.context;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.config.ImportableExportable;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.converter.ConverterManager;
import org.artifactory.spring.SpringConfigPaths;
import org.artifactory.version.VersionProvider;
import org.jfrog.common.logging.logback.servlet.LogbackConfigManager;
import org.jfrog.config.ConfigurationManager;
import org.springframework.beans.factory.BeanFactory;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * @author yoavl
 */
public interface ArtifactoryContext extends ImportableExportable {
    String CONTEXT_ID_PROP = "artifactory.contextId";
    String APPLICATION_CONTEXT_KEY = "org.artifactory.spring.ApplicationContext";
    String DEFAULT_CONTEXT_PATH = "artifactory";

    CentralConfigService getCentralConfig();

    /**
     * @return Bean of the specified type. If there are multiple beans of this type, the one marked as primary is
     * preferred.
     */
    @Nonnull
    <T> T beanForType(Class<T> type);

    /**
     * @see BeanFactory#getBean(java.lang.String, java.lang.Class)
     */
    <T> T beanForType(String name, Class<T> type);

    <T> Map<String, T> beansForType(Class<T> type);

    RepositoryService getRepositoryService();

    AuthorizationService getAuthorizationService();

    long getUptime();

    ArtifactoryHome getArtifactoryHome();

    /**
     * Context id used to identify this context in environments with multiple contexts on the same JVM.
     * This is usually taken from the servlet context path but can also can be customized with
     * {@link ArtifactoryContext#CONTEXT_ID_PROP}
     */
    String getContextId();

    SpringConfigPaths getConfigPaths();

    String getServerId();

    boolean isReady();

    boolean isOffline();

    void setOffline();

    ConfigurationManager getConfigurationManager();

    ConverterManager getConverterManager();

    VersionProvider getVersionProvider();

    LogbackConfigManager getLogbackConfigManager();

    void destroy();

    default String getContextPath() {
        return DEFAULT_CONTEXT_PATH;
    }
}
