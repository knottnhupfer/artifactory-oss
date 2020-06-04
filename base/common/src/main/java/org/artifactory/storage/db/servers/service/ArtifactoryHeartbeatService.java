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

package org.artifactory.storage.db.servers.service;

import org.artifactory.spring.ReloadableBean;

/**
 * @author gidis
 */
public interface ArtifactoryHeartbeatService extends ReloadableBean {

    /**
     * Update the 'artifactory_servers' table with the last heartbeat time
     */
    void updateHeartbeat();

    /**
     * When no license is activate to this node, or the license is expired, try to get an available license (which is
     * not in use by any other cluster members) and activate it to the current node. In case that the node already has
     * a license, try to replace the activated license with non-expired license if available.
     */
    void activateLicenseIfNeeded();
}
