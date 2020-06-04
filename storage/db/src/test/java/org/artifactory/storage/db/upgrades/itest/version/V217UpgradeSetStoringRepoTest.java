package org.artifactory.storage.db.upgrades.itest.version;

import org.artifactory.storage.db.upgrades.common.UpgradeBaseTest;
import org.artifactory.storage.db.version.ArtifactoryDBVersion;
import org.artifactory.storage.db.version.converter.DBConverter;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Inbar Tal
 */
@Test
public class V217UpgradeSetStoringRepoTest extends UpgradeBaseTest {

    @BeforeMethod
    public void setup() throws IOException, SQLException {
        resetToVersion(ArtifactoryDBVersion.v216);
        importSql("/sql/bundles_non_default_storing_repo.sql");
        for (DBConverter dbConverter : ArtifactoryDBVersion.v217.getConverters()) {
            dbConverter.convert(jdbcHelper,  dbProperties.getDbType());
        }
    }

    public void testStoringRepoSetToDefaultOnlyIfTypeIsTarget() throws SQLException {
        try (ResultSet res = jdbcHelper.executeSelect("SELECT * FROM artifact_bundles")) {
            while (res.next()) {
                String type = res.getString("type");
                String storingRepo = res.getString("storing_repo");
                if ("SOURCE".equals(type)) {
                    Assert.assertEquals(storingRepo, "my-repo");
                } else {
                    Assert.assertEquals(type, "TARGET");
                    Assert.assertEquals(storingRepo, "release-bundles");
                }
            }
        }
    }
}

