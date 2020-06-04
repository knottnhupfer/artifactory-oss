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

package org.artifactory.search.aql;

import java.io.Closeable;
import java.util.Map;

/**
 * Iterable AQL search result interface for the Artifactory public API.
 *
 * @author Shay Bagants
 */
public interface AqlResult extends Iterable<Map<String, Object>>, Closeable {

    /**
     * The offset from the first record from which to return results
     *
     * @return The offset from the first record from which to return results
     */
    Long getStart();

    /**
     * The total number of results
     *
     * @return The total number of results
     */
    Long getTotal();

    /**
     * The limit value
     *
     * @return The limit value
     */
    Long getLimited();
}
