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

package org.artifactory.storage;

import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.config.db.ArtifactoryDbProperties;
import org.artifactory.test.ArtifactoryHomeStub;
import org.jfrog.common.ResourceUtils;
import org.jfrog.storage.DbType;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.*;

/**
 * Unit tests for the {@link org.artifactory.storage.StorageProperties} class.
 *
 * @author Yossi Shaul
 */
@Test
public class DBPropertiesTest {

    @Test(expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = ".*Space.*")
    public void failUnsupportedDatabaseType() throws IOException {
        ArtifactoryHome.bind(new ArtifactoryHomeStub());
        new ArtifactoryDbProperties(ArtifactoryHome.get(), ResourceUtils.getResourceAsFile("/storage/unsupported.properties"));
    }

    public void minimalPropertiesFile() throws IOException {
        ArtifactoryHome.bind(new ArtifactoryHomeStub());
        try {
            ArtifactoryDbProperties sp = new ArtifactoryDbProperties(ArtifactoryHome.get(),
                    ResourceUtils.getResourceAsFile("/storage/minimalstorage.properties"));

            assertEquals(sp.getDbType(), DbType.DERBY);
            assertEquals(sp.getConnectionUrl(), "jdbc:to:somewhere");
            assertEquals(sp.getDriverClass(), "some.driver");
            assertNull(sp.getUsername());
            assertNull(sp.getPassword());
            assertEquals(sp.getMaxActiveConnections(), ArtifactoryDbProperties.DEFAULT_MAX_ACTIVE_CONNECTIONS);
            assertEquals(sp.getMaxIdleConnections(), ArtifactoryDbProperties.DEFAULT_MAX_IDLE_CONNECTIONS);
        } finally {
            ArtifactoryHome.unbind();
        }
    }

    public void minimalWithCacheSize() throws IOException {
        ArtifactoryHome.bind(new ArtifactoryHomeStub());
        try {
            ArtifactoryDbProperties sp = new ArtifactoryDbProperties(ArtifactoryHome.get(),
                    ResourceUtils.getResourceAsFile("/storage/gigscachesize.properties"));

            assertEquals(sp.getDbType(), DbType.DERBY);
            assertEquals(sp.getConnectionUrl(), "jdbc:to:somewhere");
            assertEquals(sp.getDriverClass(), "some.driver");
            assertNull(sp.getUsername());
            assertNull(sp.getPassword());
            assertEquals(sp.getMaxActiveConnections(), ArtifactoryDbProperties.DEFAULT_MAX_ACTIVE_CONNECTIONS);
            assertEquals(sp.getMaxIdleConnections(), ArtifactoryDbProperties.DEFAULT_MAX_IDLE_CONNECTIONS);
        } finally {
            ArtifactoryHome.unbind();
        }
    }

    public void valuesWithSpaces() throws IOException {
        ArtifactoryHome.bind(new ArtifactoryHomeStub());
        ArtifactoryDbProperties sp = new ArtifactoryDbProperties(ArtifactoryHome.get(), ResourceUtils.getResourceAsFile("/storage/trim.properties"));

        assertEquals(sp.getDbType(), DbType.DERBY);
        assertEquals(sp.getConnectionUrl(), "jdbc:to:removespaces");
        assertEquals(sp.getDriverClass(), "some.driver");
        assertEquals(sp.getProperty("binary.provider.filesystem.dir", ""), "a/b/c");
        assertEquals(sp.getProperty("empty", ""), "");
        assertEquals(sp.getProperty("emptySpaces", ""), "");
    }

    public void isDerby() throws IOException {
        ArtifactoryDbProperties sp = new ArtifactoryDbProperties(ArtifactoryHome.get(),
                ResourceUtils.getResourceAsFile("/storage/minimalstorage.properties"));
        assertEquals(sp.getDbType(), DbType.DERBY);
    }

    public void isPostgres() throws IOException {
        ArtifactoryDbProperties sp = new ArtifactoryDbProperties(ArtifactoryHome.get(),
                ResourceUtils.getResourceAsFile("/storage/storagepostgres.properties"));
        assertTrue(sp.isPostgres());
        assertEquals(sp.getDbType(), DbType.POSTGRESQL);
    }

}
