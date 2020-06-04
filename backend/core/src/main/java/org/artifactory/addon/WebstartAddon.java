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

package org.artifactory.addon;

import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.repo.virtual.VirtualRepo;
import org.artifactory.sapi.common.ExportSettings;
import org.artifactory.sapi.common.ImportSettings;

/**
 * Webstart addon factory.
 *
 * @author Yossi Shaul
 */
public interface WebstartAddon extends Addon {
    /**
     * Creates new VirtualRepo instance. The actual type of the virtual repo is determined by the addon impl.
     *
     * @param repoService Repository service
     * @param descriptor  The repository descriptor
     * @return New virtual repository
     */
    default VirtualRepo createVirtualRepo(InternalRepositoryService repoService, VirtualRepoDescriptor descriptor) {
        return new VirtualRepo(descriptor, repoService);
    }

    /**
     * Import key store as part of the full system import.
     */
    default void importKeyStore(ImportSettings settings) {
    }

    /**
     * Export key store as part of the full system export.
     */
    default void exportKeyStore(ExportSettings exportSettings) {
    }
}
