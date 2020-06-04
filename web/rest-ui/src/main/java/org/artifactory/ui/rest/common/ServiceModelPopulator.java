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

package org.artifactory.ui.rest.common;

import org.artifactory.descriptor.index.IndexerDescriptor;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.ui.rest.model.admin.services.indexer.Indexer;
import org.artifactory.ui.rest.model.empty.EmptyModel;

import javax.annotation.Nonnull;

/**
 * @author Chen Keinan
 */
public class ServiceModelPopulator {

    /**
     * populate indexer descriptor data to indexer model
     *
     * @param indexerDescriptor - indexer descriptor
     * @return indexer model
     */
    @Nonnull
    public static RestModel populateIndexerConfiguration(IndexerDescriptor indexerDescriptor) {
        if (indexerDescriptor != null) {
            return new Indexer(indexerDescriptor);
        }
        return new EmptyModel();
    }
}
