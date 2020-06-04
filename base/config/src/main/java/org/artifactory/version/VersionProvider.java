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

package org.artifactory.version;


import org.jfrog.config.ConfigurationManagerInternal;
import org.jfrog.config.DbChannel;

import javax.annotation.Nullable;

/**
 * @author Gidi Shabat
 */
public interface VersionProvider {

    /**
     * During init the provider will resolve the current and original (coming from) version for this instance.
     * NOTE: at this stage db.properties must be available in the local file system at least
     * (i.e after {@link ConfigurationManagerInternal#initDbChannels()} ()}) --> Database access is a must for this action!
     */
    void init(DbChannel dbChannel);

    /**
     * @return the current version of this Artifactory code
     */
    CompoundVersionDetails getRunning();

    /**
     * @return the latest version this instance was started with, either from database (if exists) or from the
     * filesystem (legacy for 4.x -> 5.x conversion).
     * NOTE: null is returned to denote new installations (no db and no filesystem version info available)
     */
    @Nullable
    CompoundVersionDetails getOriginalDbVersion();

    @Nullable
    CompoundVersionDetails getOriginalHomeVersion();

    void initOriginalHomeVersion();
}
