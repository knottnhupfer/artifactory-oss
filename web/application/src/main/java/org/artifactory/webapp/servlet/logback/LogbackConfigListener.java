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

package org.artifactory.webapp.servlet.logback;

import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.log.logback.ArtifactoryLoggerConfigInfo;
import org.artifactory.util.HttpUtils;
import org.jfrog.common.logging.logback.servlet.LogbackConfigListenerBase;
import org.jfrog.common.logging.logback.servlet.LoggerConfigInfo;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

/**
 * Configured logback with the config file from etc directory.
 *
 * @author Yossi Shaul
 * @author Yoav Landman
 */
public class LogbackConfigListener extends LogbackConfigListenerBase {

    private ArtifactoryHome home;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        initArtifactoryHome(event);
        super.contextInitialized(event);
    }

    private void initArtifactoryHome(ServletContextEvent event) {
        ServletContext servletContext = event.getServletContext();
        home = (ArtifactoryHome) servletContext.getAttribute(ArtifactoryHome.SERVLET_CTX_ATTR);
        if (home == null) {
            throw new IllegalStateException("Artifactory home not initialized");
        }
    }

    @Override
    protected LoggerConfigInfo createLoggerConfigInfo(ServletContext servletContext) {
        return new ArtifactoryLoggerConfigInfo(HttpUtils.getContextId(servletContext), home);
    }

    @Override
    protected boolean isDevOrTest() {
        return ConstantValues.isDevOrTest(ArtifactoryHome.get());
    }
}
