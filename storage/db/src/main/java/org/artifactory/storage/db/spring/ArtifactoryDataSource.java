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

package org.artifactory.storage.db.spring;

import org.artifactory.mbean.MBeanRegistrationService;

import javax.sql.DataSource;
import java.io.Closeable;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * Common interface for (non JNDI) JDBC data sources used by Artifactory.
 *
 * @author Yossi Shaul
 */
public interface ArtifactoryDataSource extends DataSource, Closeable {
    @Override
    Logger getParentLogger() throws SQLFeatureNotSupportedException;

    /**
     * Register datasource specific mbeans if jmx monitoring is enabled.
     */
    void registerMBeans(MBeanRegistrationService mbeansService);

    //for now only used for mbean
    int getActiveConnectionsCount();

    int getIdleConnectionsCount();

    int getMaxActive();

    int getMaxIdle();

    int getMaxWait();

    int getMinIdle();

    String getUrl();

    void reset();
}
