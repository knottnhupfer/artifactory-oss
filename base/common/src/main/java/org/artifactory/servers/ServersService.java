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

package org.artifactory.servers;


import javax.annotation.Nonnull;
import java.util.List;

/**
 * This service links between the DB layer and the REST layer
 *
 * @author Rotem Kfir
 */
public interface ServersService {

    String ARTIFACTORY_SERVICE_NAME = "Server";
    String NO_LICENSE_MESSAGE = " - No license installed";

    /**
     * Find all Artifactory servers
     *
     * @return a list of servers
     */
    @Nonnull
    List<ServerModel> getAllArtifactoryServers();
}
