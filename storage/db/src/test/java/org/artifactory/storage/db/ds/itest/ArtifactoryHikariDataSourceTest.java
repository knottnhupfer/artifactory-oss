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

package org.artifactory.storage.db.ds.itest;

import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.db.spring.ArtifactoryDataSource;
import org.artifactory.storage.db.spring.ArtifactoryHikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.testng.Assert.*;

/**
 * Tests the connection pooling done with {@link org.artifactory.storage.db.spring.ArtifactoryHikariDataSource}.
 *
 * @author Yossi Shaul
 */
@Test
public class ArtifactoryHikariDataSourceTest extends DbBaseTest {

    @Autowired
    private DataSource dataSource;

    private boolean isHikariDS;

    @BeforeClass
    public void setup() {
        isHikariDS = dataSource instanceof ArtifactoryHikariDataSource;
    }

    public void instanceType() {
        assertTrue(dataSource instanceof ArtifactoryDataSource, "Unexpected datasource type: " + dataSource.getClass());
    }

    public void verifyDefaultParams() {
        if (isHikariDS) {
            ArtifactoryHikariDataSource ds = (ArtifactoryHikariDataSource) dataSource;
            assertEquals(ds.getTransactionIsolation(), "TRANSACTION_READ_COMMITTED");
            assertTrue(ds.isAutoCommit());
        }
    }

    public void verifyActiveConnections() throws SQLException {
        assertEquals(getDs().getActiveConnectionsCount(), 0);
        //assertEquals(getDs().getIdleConnectionsCount(), dbProps.get);
        try (Connection conn = getDs().getConnection()) {
            assertNotNull(conn);
            assertEquals(getDs().getActiveConnectionsCount(), 1);
        }
        assertEquals(getDs().getActiveConnectionsCount(), 0);
    }

    private ArtifactoryDataSource getDs() {
        return (ArtifactoryDataSource) dataSource;
    }
}
