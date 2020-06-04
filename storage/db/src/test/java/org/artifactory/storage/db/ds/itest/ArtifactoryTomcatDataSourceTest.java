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
import org.artifactory.storage.db.spring.ArtifactoryTomcatDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.testng.Assert.*;

/**
 * Tests the connection pooling done with {@link org.artifactory.storage.db.spring.ArtifactoryTomcatDataSource}.
 *
 * @author Yossi Shaul
 */
@Test
public class ArtifactoryTomcatDataSourceTest extends DbBaseTest {

    @Autowired
    private DataSource dataSource;

    private boolean isTomcatDS;

    @BeforeClass
    public void setup() {
        isTomcatDS = dataSource instanceof ArtifactoryTomcatDataSource;
    }


    public void instanceType() {
        assertTrue(dataSource instanceof ArtifactoryDataSource, "Unexpected datasource type: " + dataSource.getClass());
    }

    public void verifyDefaultParams() {
        if (isTomcatDS) {
            ArtifactoryTomcatDataSource ds = (ArtifactoryTomcatDataSource) dataSource;
            assertEquals(ds.getDefaultTransactionIsolation(), Connection.TRANSACTION_READ_COMMITTED);
            assertTrue(ds.isDefaultAutoCommit());
        }
    }

    public void verifyValidationQuery() throws SQLException {
        if (isTomcatDS) {
            ArtifactoryTomcatDataSource ds = (ArtifactoryTomcatDataSource) dataSource;
            String validationQuery = ds.getValidationQuery();
            assertNotNull(validationQuery, "Validation query shouldn't be null");

            try (Connection con = dataSource.getConnection();
                 Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(validationQuery)) {
                if (!rs.next()) {
                    fail("No result returned from the validation query");
                }
            }
        }
    }
}
