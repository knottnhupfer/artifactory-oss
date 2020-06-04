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

package org.artifactory.storage.db.mbean;

import org.artifactory.storage.db.spring.ArtifactoryDataSource;
import org.artifactory.storage.db.util.JdbcHelper;

/**
 * MBean wrapper for {@link org.artifactory.storage.db.spring.ArtifactoryDataSource}
 *
 * @author mamo
 */
public class ManagedDataSource implements ManagedDataSourceMBean {

    private final ArtifactoryDataSource artifactoryDataSource;
    private final JdbcHelper jdbcHelper;

    public ManagedDataSource(ArtifactoryDataSource dataSource, JdbcHelper jdbcHelper) {
        artifactoryDataSource = dataSource;
        this.jdbcHelper = jdbcHelper;
    }

    @Override
    public int getActiveConnectionsCount() {
        return artifactoryDataSource.getActiveConnectionsCount();
    }

    @Override
    public int getIdleConnectionsCount() {
        return artifactoryDataSource.getIdleConnectionsCount();
    }

    @Override
    public int getMaxActive() {
        return artifactoryDataSource.getMaxActive();
    }

    @Override
    public int getMaxIdle() {
        return artifactoryDataSource.getMaxIdle();
    }

    @Override
    public long getMaxWait() {
        return artifactoryDataSource.getMaxWait();
    }

    @Override
    public int getMinIdle() {
        return artifactoryDataSource.getMinIdle();
    }

    @Override
    public String getUrl() {
        return artifactoryDataSource.getUrl();
    }

    @Override
    public long getSelectQueriesCount() {
        return jdbcHelper.getSelectQueriesCount();
    }

    @Override
    public long getUpdateQueriesCount() {
        return jdbcHelper.getUpdateQueriesCount();
    }
}
