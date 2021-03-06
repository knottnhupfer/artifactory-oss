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

package org.artifactory.request;

import org.artifactory.repo.cache.expirable.CacheExpiryChecker;

/**
 * @author Yoav Landman
 */
public interface InternalRequestContext extends RequestContext {

    /**
     * Tests that the client's "User-Agent" header value does not match the pattern of older Maven versions (prior to
     * and including 2.0.9) that don't support Maven 3 type metadata which includes a list of snapshot versions.
     *
     * @return True if the client "User-Agent" header value does not match the pattern of older Maven versions
     */
    boolean clientSupportsM3SnapshotVersions();

    /**
     * Force resource expiry check (regardless of the resource cache age), usually triggered by a user plugin
     * to add expired resources others than those who implements CacheExpirable.
     *
     * @return True if should force expiry check
     * @see CacheExpiryChecker
     * @see org.artifactory.addon.plugin.download.BeforeDownloadRequestAction
     */
    boolean isForceExpiryCheck();
}
