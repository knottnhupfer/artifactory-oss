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

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.index.IndexerDescriptor;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.common.util.AolUtils;
import org.artifactory.ui.rest.common.ServiceModelPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
public class GetIndexerService implements RestService {

    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        if (!ConstantValues.aolDedicatedServer.getBoolean()) {
            AolUtils.assertNotAol("GetIndexer");
        }
        getIndexerDescriptor(response);
    }

    /**
     * get the indexer descriptor from config and populate data to indexer model
     *
     * @param artifactoryResponse - encapsulate data require for response
     */
    private void getIndexerDescriptor(RestResponse artifactoryResponse) {
        IndexerDescriptor indexerDescriptor = centralConfigService.getDescriptor().getIndexer();
        RestModel indexer = ServiceModelPopulator.populateIndexerConfiguration(indexerDescriptor);
        artifactoryResponse.iModel(indexer);
    }
}
