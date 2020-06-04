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
package org.artifactory.repo.http.mbean;

/**
 * @author Ofer Cohen
 */
public interface HTTPConnectionPoolMBean {

    /**
     * @return Gets the number idle persistent connections.
     */
    int	getAvailable();

    /**
     * @return   Gets the number of persistent connections tracked by the connection manager currently being used to execute requests.
     */

    int	getLeased();

    /**
     * @return   Gets the maximum number of allowed persistent connections.
     */

    int	getMax();

    /**
     * @return   Gets the number of connection requests being blocked awaiting a free connection
    */

    int	getPending();

}
