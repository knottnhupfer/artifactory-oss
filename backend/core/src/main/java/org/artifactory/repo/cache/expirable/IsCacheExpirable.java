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

import org.artifactory.descriptor.repo.RepoType;

import javax.annotation.Nonnull;

/**
 * Interface  method if artifact can expire.
 * Examples of expirable artifacts are calculated metadata, non-unique snapshots, filtered resources etc.
 *
 * @author saffi
 */
public interface IsCacheExpirable {

    /**
     * Indicates whether the specified path can ever expire
     *
     * @param repoType Repository Descriptor Type
     * @param repoKey  Repository name
     * @param path     Path to check
     * @return True if the path can ever expire
     */
    boolean isExpirable(RepoType repoType, String repoKey, @Nonnull String path);
}
