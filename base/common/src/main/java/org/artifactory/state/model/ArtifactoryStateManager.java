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

package org.artifactory.state.model;

import org.artifactory.spring.ReloadableBean;
import org.artifactory.state.ArtifactoryServerState;

/**
 * @author gidis
 */
public interface ArtifactoryStateManager extends ReloadableBean {

    /**
     * Set the {@link org.artifactory.storage.db.servers.model.ArtifactoryServer} matching the
     * given {@code serverId} state to the {@code state}
     * <p><i>Use this method with caution.</i>
     *
     * @throws IllegalStateException when given {@code serverId} does not match any existing server
     */
    boolean forceState(ArtifactoryServerState state);

    void beforeDestroy();
}
