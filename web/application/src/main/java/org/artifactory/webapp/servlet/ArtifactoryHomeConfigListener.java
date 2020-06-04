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

package org.artifactory.webapp.servlet;

import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.jfrog.common.logging.BootstrapLogger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;

/**
 * @author yoavl
 */
public class ArtifactoryHomeConfigListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext servletContext = event.getServletContext();
        String artHomeCtx = servletContext.getInitParameter(ArtifactoryHome.SYS_PROP);
        BootstrapLogger.info("Fetched Artifactory [artifactory.home=" + artHomeCtx + "] from servlet context");
        BasicConfigManagers basicConfigManagers = null;
        if (Boolean.valueOf(System.getProperty(ConstantValues.test.getPropertyName()))) {
            // See if ArtifactoryHome already in context
            basicConfigManagers = (BasicConfigManagers) servletContext.getAttribute("artifactory.test.basicManagers");
        }

        if (basicConfigManagers == null) {
            // Create ArtifactoryHome
            ArtifactoryHome artifactoryHome = initArtifactoryHome(servletContext, artHomeCtx);
            BootstrapLogger.init("ARTIFACTORY", artifactoryHome.getLogDir());
            basicConfigManagers = new BasicConfigManagers(artifactoryHome);
            basicConfigManagers.initialize();
        } else {
            BootstrapLogger.init("ARTIFACTORY", basicConfigManagers.artifactoryHome.getLogDir());
        }

        // Finally we can start artifactory
        BootstrapLogger.info("Starting Artifactory [artifactory.home=" + basicConfigManagers.artifactoryHome.getHomeDir().getAbsolutePath() + "].");
        basicConfigManagers.addServletAttributes(servletContext);
        basicConfigManagers.inheritInitParamsAsConstantValues(servletContext);
    }

    private ArtifactoryHome initArtifactoryHome(ServletContext servletContext, String artHomeCtx) {
        ArtifactoryHome artifactoryHome;
        try {
            if (artHomeCtx != null) {
                // Use home dir from Context
                artifactoryHome = new ArtifactoryHome(new File(artHomeCtx));
                BootstrapLogger.info("Resolved Artifactory home by param [artifactory.home=" + artifactoryHome.getHomeDir().getAbsolutePath() + "].");
            } else {
                // Initialize home dir using default behavior
                artifactoryHome = new ArtifactoryHome(new ServletLogger(servletContext));
                BootstrapLogger.info("Resolved Artifactory home by logger [artifactory.home=" + artifactoryHome.getHomeDir().getAbsolutePath() + "].");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ArtifactoryHome.bind(artifactoryHome);
        return artifactoryHome;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }

    private static class ServletLogger implements ArtifactoryHome.SimpleLog {
        private final ServletContext servletContext;

        private ServletLogger(ServletContext servletContext) {
            this.servletContext = servletContext;
        }

        @Override
        public void log(String message) {
            servletContext.log(message);
        }
    }
}
