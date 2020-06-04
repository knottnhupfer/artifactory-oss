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

package org.artifactory.repo.cache.expirable;

import org.artifactory.fs.RepoResource;
import org.artifactory.request.RepoRequests;

/**
 * Default implementation of expiry strategy which checks the file last modified date
 *
 * @author Shay Yaakov
 */
public class CacheExpiryStrategyImpl implements CacheExpiryStrategy {

    @Override
    public boolean foundExpiredAndRemoteIsNewer(RepoResource remoteResource, RepoResource cachedResource) {
        boolean remoteIsNewer = cachedResource.isExpired() && remoteResource.getLastModified() > cachedResource.getLastModified();
        RepoRequests.logToContext("Found expired cached resource but remote is newer = %s. Cached resource: %s, Remote resource: %s",
                remoteIsNewer, cachedResource.getLastModified(), remoteResource.getLastModified());
        return remoteIsNewer;
    }
}
