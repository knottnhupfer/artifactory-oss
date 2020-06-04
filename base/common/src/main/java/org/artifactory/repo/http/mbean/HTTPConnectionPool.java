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

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

/**
 * @author Ofer Cohen
 */
public class HTTPConnectionPool implements HTTPConnectionPoolMBean {

    private PoolingHttpClientConnectionManager connectionPool;

    public HTTPConnectionPool(PoolingHttpClientConnectionManager connectionPool) {
        this.connectionPool = connectionPool;

    }

    @Override
    public int getAvailable() {
        return connectionPool.getTotalStats().getAvailable();
    }

    @Override
    public int getLeased() {
        return connectionPool.getTotalStats().getLeased();
    }

    @Override
    public int getMax() {
        return connectionPool.getTotalStats().getMax();
    }

    @Override
    public int getPending() {
        return connectionPool.getTotalStats().getPending();
    }
}
