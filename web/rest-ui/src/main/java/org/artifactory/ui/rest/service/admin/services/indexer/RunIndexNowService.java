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

package org.artifactory.ui.rest.service.admin.services.indexer;

import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.repo.index.MavenIndexerService;
import org.artifactory.common.ConstantValues;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.common.util.AolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
public class RunIndexNowService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(RunIndexNowService.class);

    @Autowired
    private MavenIndexerService mavenIndexer;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        if (!ConstantValues.aolDedicatedServer.getBoolean()) {
            AolUtils.assertNotAol("RunIndexNow");
        }

        BasicStatusHolder statusHolder = new BasicStatusHolder();
        try {
            // run index now
            runIndexNow(response);
        } catch (Exception e) {
            updateErrorFeedBack(response, statusHolder, e);
        }
    }

    /**
     * update error feedback
     *
     * @param artifactoryResponse - encapsulate data require for response
     * @param statusHolder        - status holder
     * @param e                   - exception
     */
    private void updateErrorFeedBack(RestResponse artifactoryResponse, BasicStatusHolder statusHolder, Exception e) {
        log.error("Could not run indexer.", e);
        statusHolder.error(e.getMessage(), log);
        artifactoryResponse.error("Indexer did not run: " + e.getMessage());
    }

    /**
     * schedule indexing for now
     *
     * @param artifactoryResponse - encapsulate data require for response
     */
    private void runIndexNow(RestResponse artifactoryResponse) {
        BasicStatusHolder status = new BasicStatusHolder();
        mavenIndexer.scheduleImmediateIndexing(status);
        if (status.isError()) {
            artifactoryResponse.error(status.getStatusMsg());
        } else {
            artifactoryResponse.info("Indexer was successfully scheduled to run in the background");
        }
    }
}
