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

import ch.qos.logback.classic.LoggerContext;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.jfrog.common.logging.logback.servlet.LoggerConfigInfo;

/**
 * @author Yinon Avraham
 */
public class LogbackContextSelector extends org.jfrog.common.logging.logback.servlet.LogbackContextSelector {

    public LogbackContextSelector(LoggerContext context) {
        super(context);
    }

    @Override
    protected LoggerConfigInfo getConfigInfo() {
        LoggerConfigInfo configInfo = super.getConfigInfo();
        if (configInfo == null) {
            ArtifactoryContext context = ContextHelper.get();
            if (context != null) {
                configInfo = new ArtifactoryLoggerConfigInfo(context.getContextId(), context.getArtifactoryHome());
            }
        }
        return configInfo;
    }
}
