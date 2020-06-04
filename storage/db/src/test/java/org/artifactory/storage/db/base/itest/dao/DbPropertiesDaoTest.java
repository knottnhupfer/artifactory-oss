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

package org.artifactory.storage.db.base.itest.dao;

import org.artifactory.common.storage.db.properties.DbVersionInfo;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.db.properties.dao.DbPropertiesDao;
import org.artifactory.storage.db.properties.service.ArtifactoryDbPropertiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.Comparator;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Date: 7/10/13 3:31 PM
 *
 * @author freds
 */
@Test
public class DbPropertiesDaoTest extends DbBaseTest {
    @Autowired
    private DbPropertiesDao dbPropertiesDao;

    @Autowired
    private ArtifactoryDbPropertiesService dbPropertiesService;

    @BeforeClass
    public void setup() {
        importSql("/sql/db-props.sql");
    }

    @Test
    public void loadExistingProps() throws SQLException {
        DbVersionInfo versionInfo = getLatestProperties();
        assertNotNull(versionInfo);
        assertEquals(versionInfo.getInstallationDate(), 1349000000000L);
        assertEquals(versionInfo.getArtifactoryVersion(), "5-t");
        assertEquals(versionInfo.getArtifactoryRevision(), 12000);
        assertEquals(versionInfo.getArtifactoryRelease(), 1300000000000L);
    }

    private DbVersionInfo getLatestProperties() throws SQLException {
        return dbPropertiesService.getDbVersionInfo();
    }

    @Test(dependsOnMethods = {"loadExistingProps"})
    public void createNewLatestProps() throws SQLException {
        long now = System.currentTimeMillis() - 10000L;
        DbVersionInfo dbTest = new DbVersionInfo(now, "6-a", 12001, 2L);
        dbPropertiesDao.createProperties(dbTest);
        DbVersionInfo versionInfo = getLatestProperties();
        assertNotNull(versionInfo);
        assertEquals(versionInfo.getInstallationDate(), now);
        assertEquals(versionInfo.getArtifactoryVersion(), "6-a");
        assertEquals(versionInfo.getArtifactoryRevision(), 12001);
        assertEquals(versionInfo.getArtifactoryRelease(), 2L);
    }

    @Test(dependsOnMethods = {"createNewLatestProps"})
    public void createNewDevModeProps() throws SQLException {
        long now = System.currentTimeMillis();
        DbVersionInfo dbTest = new DbVersionInfo(now, "7-dev", 12002, -3L);
        dbPropertiesDao.createProperties(dbTest);
        DbVersionInfo versionInfo = getLatestProperties();
        assertNotNull(versionInfo);
        assertEquals(versionInfo.getInstallationDate(), now);
        assertEquals(versionInfo.getArtifactoryVersion(), "7-dev");
        assertEquals(versionInfo.getArtifactoryRevision(), 12002);
        assertEquals(versionInfo.getArtifactoryRelease(), 0L);
    }


    private class CreationComparator implements Comparator<DbVersionInfo> {
        @Override
        public int compare(DbVersionInfo o1, DbVersionInfo o2) {
            return (int) (o1.getInstallationDate() - o1.getInstallationDate());
        }
    }
}
