package org.artifactory.storage.db.bundle.itest.dao;

import org.artifactory.bundle.BundleTransactionStatus;
import org.artifactory.bundle.BundleType;
import org.artifactory.storage.db.bundle.dao.ArtifactBundlesDao;
import org.artifactory.storage.db.bundle.dao.BundleBlobsDao;
import org.artifactory.storage.db.bundle.model.DBArtifactsBundle;
import org.artifactory.storage.db.bundle.model.DBBundleBlobsResult;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.sql.SQLException;

import static org.artifactory.descriptor.repo.releasebundle.ReleaseBundlesRepoDescriptor.RELEASE_BUNDLE_DEFAULT_REPO;
import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Inbar Tal
 */
@Test
public class BundleBlobsDaoTest extends DbBaseTest {

    @Autowired
    private ArtifactBundlesDao artifactBundlesDao;

    @Autowired
    private BundleBlobsDao bundleBlobsDao;

    @Test
    public void testGetBundleBlobDataTarget() throws SQLException {
        createBundle("myBundle", "1.0.0", RELEASE_BUNDLE_DEFAULT_REPO, BundleType.TARGET);

        DBBundleBlobsResult myBundle = bundleBlobsDao.getBundleBlobData("myBundle", "1.0.0", BundleType.TARGET);
        assertThat(myBundle.getStoringRepo()).isEqualTo(RELEASE_BUNDLE_DEFAULT_REPO);
        assertThat(myBundle.getData()).isEqualTo("XDSFJ#$I@#JR");
    }

    @Test
    public void testGetBundleBlobDataSource() throws SQLException {
        createBundle("myBundle1", "1.0.0", "my-rb-repo", BundleType.SOURCE);
        createBundle("myBundle2", "2.0.0", RELEASE_BUNDLE_DEFAULT_REPO, BundleType.TARGET);

        DBBundleBlobsResult myBundle = bundleBlobsDao.getBundleBlobData("myBundle1", "1.0.0", BundleType.SOURCE);
        assertThat(myBundle.getStoringRepo()).isEqualTo("my-rb-repo");
        assertThat(myBundle.getData()).isEqualTo("XDSFJ#$I@#JR");
    }

    private void createBundle(String name, String version, String storingRepo, BundleType type) throws SQLException {
        DBArtifactsBundle bundle = new DBArtifactsBundle();
        bundle.setName(name);
        bundle.setVersion(version);
        bundle.setSignature("****");
        bundle.setStatus(BundleTransactionStatus.COMPLETE);
        bundle.setDateCreated(DateTime.now());
        bundle.setStoringRepo(storingRepo);
        bundle.setType(type);
        artifactBundlesDao.create(bundle);
        bundleBlobsDao.create("XDSFJ#$I@#JR", bundle.getId());
    }
}
