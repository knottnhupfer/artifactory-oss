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

package org.artifactory.log.logback;

import org.apache.commons.lang3.StringUtils;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.common.ArtifactoryHome;
import org.jfrog.common.logging.logback.LogbackContextConfigurator;
import org.jfrog.common.logging.logback.servlet.LoggerConfigInfo;

import java.util.Arrays;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.trimToEmpty;

/**
 * @author Yinon Avraham.
 */
public class ArtifactoryLoggerConfigInfo extends LoggerConfigInfo {

    private final ArtifactoryHome artifactoryHome;

    public ArtifactoryLoggerConfigInfo(String contextId, ArtifactoryHome artifactoryHome) {
        super(contextId, artifactoryHome.getLogbackConfig());
        this.artifactoryHome = artifactoryHome;
    }

    @Override
    protected void configure(LogbackContextConfigurator configurator) {
        super.configure(configurator);
        configurator
                .property(ArtifactoryContext.CONTEXT_ID_PROP, normalizedContextId())
                .property(ArtifactoryHome.SYS_PROP, artifactoryHome.getHomeDir().getAbsolutePath());

        // add custom logback properties to be used by logback (pattern/file names) if defined for testing
        String customProps = artifactoryHome.getArtifactoryProperties()
                .getProperty("artifactory.test.logback.custom", "");
        if (StringUtils.isNotBlank(customProps)) {
            String[] customParams = customProps.split(",");
            Arrays.stream(customParams).forEach(
                    p -> configurator.property(p.split("::")[0], p.split("::")[1])
            );
        }
    }

    private String normalizedContextId() {
        String contextId = trimToEmpty(getContextId());
        contextId = "artifactory".equalsIgnoreCase(contextId) ? "" : contextId + " ";
        return isBlank(contextId) ? "" : contextId.toLowerCase();
    }
}
