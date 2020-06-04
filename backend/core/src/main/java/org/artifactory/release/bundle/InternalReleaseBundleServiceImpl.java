package org.artifactory.release.bundle;

import org.artifactory.api.rest.distribution.bundle.models.ArtifactsBundleModel;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.bundle.BundleTransactionStatus;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.spring.InternalContextHelper;
import org.artifactory.storage.db.bundle.dao.ArtifactBundlesDao;
import org.artifactory.storage.db.bundle.dao.BundleBlobsDao;
import org.artifactory.storage.db.bundle.model.BundleNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import static org.artifactory.util.distribution.DistributionConstants.IN_TRANSIT_REPO_KEY;

/**
 * @author Inbar Tal
 */
@Service
public class InternalReleaseBundleServiceImpl implements InternalReleaseBundleService {
    private static final Logger log = LoggerFactory.getLogger(InternalReleaseBundleServiceImpl.class);

    @Autowired
    private ArtifactBundlesDao bundlesDao;
    @Autowired
    private BundleBlobsDao blobsDao;
    @Autowired
    private InternalRepositoryService repoService;
    @Autowired
    private AuthorizationService authService;

    @Override
    public void deleteBundleInternal(boolean deleteContent, ArtifactsBundleModel bundleModel, List<BundleNode> bundleNodes,
            Set<Long> nodeIdsWithArtifactsToDelete) throws SQLException {
        log.debug("Deleting Release Bundle with id '{}'", bundleModel.getId());
        bundlesDao.deleteBundleNodes(bundleModel.getId());
        if (deleteContent) {
            deleteBundleContent(bundleModel, bundleNodes, nodeIdsWithArtifactsToDelete);
        }
        blobsDao.deleteBlob(bundleModel.getId());
        bundlesDao.deleteArtifactsBundle(bundleModel.getId());
    }

    private void deleteBundleContent(ArtifactsBundleModel artifactsBundle, List<BundleNode> bundleNodes,
            Set<Long> nodeIdsWithArtifactsToDelete) {
        if (BundleTransactionStatus.INPROGRESS.equals(artifactsBundle.getStatus())) {
            if (authService.isAdmin()) {
                deleteArtifactsFromIntransitRepo(artifactsBundle.getName(), artifactsBundle.getVersion());
            } else {
                Authentication originalAuthentication = SecurityContextHolder.getContext().getAuthentication();
                InternalContextHelper.get().getSecurityService().authenticateAsSystem();
                try {
                    deleteArtifactsFromIntransitRepo(artifactsBundle.getName(), artifactsBundle.getVersion());
                } finally {
                    SecurityContextHolder.getContext().setAuthentication(originalAuthentication);
                }
            }
        } else {
            for (BundleNode bundleNode : bundleNodes) {
                // If the artifact is related to a different bundle, skip deletion
                if (nodeIdsWithArtifactsToDelete.contains(bundleNode.getId())) {
                    repoService.undeploy(RepoPathFactory.create(bundleNode.getRepoPath()));
                } else {
                    log.info("Artifact " + bundleNode.getRepoPath() +
                            " is related to a different release bundle and will not be deleted.");
                }
            }
        }
    }

    /**
     * Delete the temp folder and all of its content
     */
    private void deleteArtifactsFromIntransitRepo(String bundleName, String bundleVersion) {
        String txPath = String.join("/", IN_TRANSIT_REPO_KEY, bundleName, bundleVersion);
        repoService.undeploy(RepoPathFactory.create(txPath));
    }
}
