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
import org.artifactory.version.ArtifactoryVersion;
import org.artifactory.version.CompoundVersionDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;

import static org.testng.Assert.*;

/**
 * @author gidis
 */
public class DBPropertiesWithRealArtifactoryVersionTest extends DbBaseTest {

    @Autowired
    private DbPropertiesDao dbPropertiesDao;

    @Autowired
    private ArtifactoryDbPropertiesService dbPropertiesService;

    @BeforeClass
    public void setup() {
        importSql("/sql/db-props-with-artifactory-version.sql");
    }

    private DbVersionInfo getLatestProperties() throws SQLException {
        return dbPropertiesService.getDbVersionInfo();
    }

    @Test(enabled = false)
    public void loadExistingProps() throws SQLException {
        DbVersionInfo versionInfo = getLatestProperties();
        assertNotNull(versionInfo);
        assertEquals(versionInfo.getInstallationDate(), 1349000000000L);
        assertEquals(versionInfo.getArtifactoryVersion(), "4.7.1");
        assertEquals(versionInfo.getArtifactoryRevision(), 12000);
        assertEquals(versionInfo.getArtifactoryRelease(), 1300000000000L);
    }

    @Test(dependsOnMethods = {"loadExistingProps"}, enabled = false)
    public void createArtifactoryVersion() throws SQLException, InterruptedException {
        long now = System.currentTimeMillis() - 10000L;
        DbVersionInfo dbTest = new DbVersionInfo(now, "4.7.2", 12001, 2L);
        dbPropertiesDao.createProperties(dbTest);
        DbVersionInfo versionInfo = getLatestProperties();
        assertNotNull(versionInfo);
        assertEquals(versionInfo.getInstallationDate(), now);
        assertEquals(versionInfo.getArtifactoryVersion(), "4.7.2");
        assertEquals(versionInfo.getArtifactoryRevision(), 12001);
        assertEquals(versionInfo.getArtifactoryRelease(), 2L);
    }

    @Test(dependsOnMethods = {"createArtifactoryVersion"}, enabled = false, description = "fail in automatic release job with revision number equal to latest milestone")
    public void latestVersion() throws SQLException, InterruptedException {
        long now = System.currentTimeMillis();
        ArtifactoryVersion current = ArtifactoryVersion.getCurrent();
        Thread.sleep(100);
        DbVersionInfo fromVersion = createDbPropertiesFromVersion(new CompoundVersionDetails(
                current, "456", now - 1000000L));
        dbPropertiesDao.createProperties(fromVersion);
        DbVersionInfo versionInfo = getLatestProperties();
        assertNotNull(versionInfo);
        assertTrue(now <= fromVersion.getInstallationDate());
        assertEquals(versionInfo.getInstallationDate(), fromVersion.getInstallationDate());
        assertEquals(versionInfo.getArtifactoryVersion(), current.getVersion());
        assertEquals(versionInfo.getArtifactoryRevision(), Integer.MAX_VALUE);
        assertEquals(versionInfo.getArtifactoryRelease(), now - 1000000L);
    }

    @Test(dependsOnMethods = {"latestVersion"}, enabled = false)
    public void downgrade() throws SQLException, InterruptedException {
        long now = System.currentTimeMillis() - 10000L;
        DbVersionInfo dbTest = new DbVersionInfo(now, "4.7.4", 1, 2L);
        dbPropertiesDao.createProperties(dbTest);
        DbVersionInfo versionInfo = getLatestProperties();
        assertNotNull(versionInfo);
        ArtifactoryVersion current = ArtifactoryVersion.getCurrent();
        DbVersionInfo fromVersion = createDbPropertiesFromVersion(new CompoundVersionDetails(
                current, "456", now - 1000000L));
        assertTrue(now <= fromVersion.getInstallationDate());
        assertEquals(versionInfo.getArtifactoryVersion(), current.getVersion());
        assertEquals(versionInfo.getArtifactoryRevision(), Integer.MAX_VALUE);
    }

    private DbVersionInfo createDbPropertiesFromVersion(CompoundVersionDetails versionDetails) {
        long installTime = System.currentTimeMillis();
        return new DbVersionInfo(installTime,
                versionDetails.getVersionName(),
                (int) versionDetails.getRevision(),
                versionDetails.getTimestamp()
        );
    }
}
