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

import org.artifactory.repo.local.IsLocalGenerated;

import java.io.Serializable;

/**
 * Interface for classes that check and determine whether an artifact can expire.
 * Examples of expirable artifacts are calculated metadata, non-unique snapshots, filtered resources etc.
 * The CacheExpiry service combine all checkers them together.
 *
 * @author Noam Y. Tenne
 */
public interface CacheExpiryChecker extends Serializable, IsLocalGenerated, IsCacheExpirable {
}
