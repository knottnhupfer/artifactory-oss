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

package org.artifactory.release.bundle;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.NonNull;
import org.artifactory.api.component.ComponentDetails;
import org.artifactory.api.component.ComponentDetailsFetcher;
import org.artifactory.api.properties.PropertiesService;
import org.artifactory.api.release.bundle.ReleaseBundleService;
import org.artifactory.api.rest.distribution.bundle.models.ArtifactProperty;
import org.artifactory.api.rest.distribution.bundle.models.ArtifactsBundleModel;
import org.artifactory.api.rest.distribution.bundle.models.ReleaseArtifact;
import org.artifactory.api.rest.distribution.bundle.models.ReleaseBundleModel;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.bundle.BundleNameAndRepo;
import org.artifactory.bundle.BundleTransactionStatus;
import org.artifactory.bundle.BundleType;
import org.artifactory.md.Properties;
import org.artifactory.model.xstream.fs.PropertiesImpl;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.rest.exception.ForbiddenWebAppException;
import org.artifactory.storage.db.bundle.dao.ArtifactBundlesDao;
import org.artifactory.storage.db.bundle.dao.BundleBlobsDao;
import org.artifactory.storage.db.bundle.model.BundleNode;
import org.artifactory.storage.db.bundle.model.DBArtifactsBundle;
import org.artifactory.storage.db.bundle.model.DBBundleResult;
import org.artifactory.storage.db.fs.dao.NodesDao;
import org.artifactory.storage.db.fs.entity.NodePath;
import org.artifactory.storage.fs.service.FileService;
import org.jfrog.common.ExecutionUtils;
import org.jfrog.common.RetryException;
import org.jfrog.storage.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Rotem Kfir
 */
@Service
public class ReleaseBundleServiceImpl implements ReleaseBundleService {
    private static final Logger log = LoggerFactory.getLogger(ReleaseBundleServiceImpl.class);

    private ArtifactBundlesDao bundlesDao;

    private BundleBlobsDao blobsDao;

    private NodesDao nodesDao;

    private InternalRepositoryService repoService;

    private PropertiesService propertiesService;

    private ArtifactBundlesDao artifactsBundleDao;

    private ComponentDetailsFetcher componentDetailsFetcher;

    private AuthorizationService authService;

    private InternalReleaseBundleService internalReleaseBundleService;

    private FileService fileService;

    long initialTimeBetweenRetries = TimeUnit.MINUTES.toMillis(1);
    ExecutorService executorService = new ThreadPoolExecutor(0, 2, 60L, TimeUnit.SECONDS, new SynchronousQueue<>());

    @Autowired
    public ReleaseBundleServiceImpl(ArtifactBundlesDao bundlesDao, BundleBlobsDao blobsDao, NodesDao nodesDao,
            InternalRepositoryService repoService, PropertiesService propertiesService,
            ArtifactBundlesDao artifactsBundleDao, ComponentDetailsFetcher componentDetailsFetcher,
            AuthorizationService authorizationService, InternalReleaseBundleService internalReleaseBundleService,
            FileService fileService) {
        this.bundlesDao = bundlesDao;
        this.blobsDao = blobsDao;
        this.nodesDao = nodesDao;
        this.repoService = repoService;
        this.propertiesService = propertiesService;
        this.artifactsBundleDao = artifactsBundleDao;
        this.componentDetailsFetcher = componentDetailsFetcher;
        this.authService = authorizationService;
        this.internalReleaseBundleService = internalReleaseBundleService;
        this.fileService = fileService;
    }

    @Override
    @Nonnull
    public List<Long> findExpiredBundlesIds(Long cleanupPeriod) {
        try {
            return bundlesDao.getExpiredBundlesIds(cleanupPeriod);
        } catch (SQLException e) {
            throw new StorageException("Could not get expired Release Bundle transactions", e);
        }
    }

    @Override
    public boolean deleteBundle(ArtifactsBundleModel bundleModel, boolean deleteContent) {
        try {
            List<BundleNode> bundleNodes = bundlesDao.getBundleNodes(bundleModel.getId());
            Set<Long> nodeIdsWithArtifactsToDelete = Sets.newHashSet();
            if (deleteContent) {
                List<String> pathsWithoutDeletePermission = bundleNodes.stream().filter(bundleNode -> {
                    try {
                        return shouldDeleteBundleArtifact(bundleNode.getId(), bundleModel.getId(),
                                nodeIdsWithArtifactsToDelete) &&
                                !authService.canDelete(RepoPathFactory.create(bundleNode.getRepoPath()));
                    } catch (SQLException e) {
                        throw new StorageException("Could not delete Release Bundle", e);
                    }
                }).map(BundleNode::getRepoPath).collect(Collectors.toList());

                assertNoPathsWithoutDeletePermission(pathsWithoutDeletePermission, bundleModel);
            }
            internalReleaseBundleService
                    .deleteBundleInternal(deleteContent, bundleModel, bundleNodes, nodeIdsWithArtifactsToDelete);
            return true;
        } catch (SQLException e) {
            throw new StorageException("Could not delete Release Bundle", e);
        }
    }

    @Override
    public boolean deleteBundle(Long id) {
        log.debug("Deleting Release Bundle with id '{}'", id);
        try {
            DBArtifactsBundle artifactsBundle = bundlesDao.getArtifactsBundle(id);
            return artifactsBundle != null && deleteBundle(populateDbArtifactsBundle(artifactsBundle), true);
        } catch (SQLException e) {
            throw new StorageException("Could not delete Release Bundle", e);
        }
    }

    private ArtifactsBundleModel populateDbArtifactsBundle(DBArtifactsBundle artifactsBundle) {
        return new ArtifactsBundleModel(artifactsBundle.getId(), artifactsBundle.getName(),
                artifactsBundle.getVersion(), artifactsBundle.getStatus(), artifactsBundle.getType());
    }

    @Override
    public boolean redistributeBundle(Long id) {
        log.info("Redistributing Release Bundle with id '{}'", id);
        try {
            return bundlesDao.updateArtifactsBundleCreationDate(id) > 0;
        } catch (SQLException e) {
            throw new StorageException("Could not redistribute Release Bundle with id '" + id + "'", e);
        }
    }

    @Override
    public boolean isRepoPathRelatedToBundle(RepoPath repoPath) {
        try {
            if (repoPath.isRoot() || isFolder(repoPath)) {
                return bundlesDao.isDirectoryRelatedToBundle(repoPath.toPath());
            }
            return bundlesDao.isRepoPathRelatedToBundle(repoPath.toPath());
        } catch (SQLException e) {
            String message = "Could not check if Repo path '" + repoPath + "' is related to a Release Bundle";
            log.error(message, e);
            throw new StorageException(message, e);
        }
    }

    private boolean isFolder(RepoPath repoPath) {
        try {
            Boolean isFolder = fileService.isFolder(repoPath);
            return isFolder == null ? false : isFolder;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void deleteAllBundles() {
        try {
            bundlesDao.deleteAllBundleNodes();
            blobsDao.deleteAllBlobs();
            artifactsBundleDao.deleteAllArtifactsBundles();
        } catch (SQLException e) {
            log.error("Failed to delete all release bundles", e);
        }
    }

    @Override
    public void copyBundleArtifactsAsync(ReleaseBundleModel bundleModel, String storingRepo,
                                         Map<String, String> artifactMapping) throws SQLException {
        String bundleNameAndVersion = bundleModel.getName() + "/" + bundleModel.getVersion();
        // artifacts should be stored as storingRepo/(RB-name/RB-version)/targetRepo/artifacts
        log.debug("Starting Async release bundle copy of {} to repository {}", bundleNameAndVersion, storingRepo);
        for (ReleaseArtifact artifact : bundleModel.getArtifacts()) {
            try {
                copyArtifact(artifact, storingRepo, bundleNameAndVersion, artifactMapping, bundleModel);
            } catch (Exception e) {
                log.error("Failed to copy artifact {} in source release bundle {}: {}", artifact.getRepoPath(), bundleNameAndVersion,
                        e.getMessage());
                log.debug("", e);
                setBundleStatusWithRetries(bundleModel.getName(), bundleModel.getVersion(), BundleTransactionStatus.FAILED, BundleType.SOURCE);
                return;
            }
        }
        // set bundle as completed
        int updated = bundlesDao
                .completeBundle(bundleModel.getName(), bundleModel.getVersion(), BundleType.SOURCE);
        if (updated == 0) {
            log.warn("Could not set source release bundle for {} as COMPLETED.", bundleNameAndVersion);
        }
    }

    @Override
    public void createBundleNode(long artifactsBundleId, RepoPath targetRepoPath, ComponentDetails componentDetails)
            throws SQLException {
        long nodeId = nodesDao.getNodeId(NodePath.fromRepoPath(targetRepoPath));
        BundleNode bundleNode = new BundleNode();
        bundleNode.setBundleId(artifactsBundleId);
        bundleNode.setNodeId(nodeId);
        bundleNode.setRepoPath(targetRepoPath.toPath());
        if (componentDetails != null) { // in case this node is created at the target artifactory it doesn't have details
            bundleNode.setOriginalFileDetails(ComponentDetails.toJson(componentDetails));
        }
        artifactsBundleDao.create(bundleNode);
    }

    @Override
    public ComponentDetails getOriginalComponentDetails(@NonNull RepoPath repoPath) throws SQLException {
        String jsonDetails = artifactsBundleDao.getComponentDetails(repoPath.toPath());
        if (jsonDetails == null) {
            log.debug("Original component details does not exist for repoPath {}", repoPath);
            return null;
        }
        return ComponentDetails.fromJson(jsonDetails);
    }

    @Override
    public List<BundleNameAndRepo> getAllCompletedBundles() {
        List<DBBundleResult> dbBundles;
        try {
            dbBundles = artifactsBundleDao.getAllCompletedBundles();
        } catch (SQLException e) {
            throw new org.artifactory.storage.StorageException("Could not retrieve the list of release-bundles", e);
        }
        List<BundleNameAndRepo> bundles = Lists.newArrayList();
        dbBundles.stream().map(dbBundle -> new BundleNameAndRepo(dbBundle.getName(), dbBundle.getStoringRepo()))
                .forEach(bundles::add);
        return bundles;
    }

    @Override
    public String getDefaultStoringRepo() {
        return "release-bundles";
    }

    private void assertNoPathsWithoutDeletePermission(List<String> paths, ArtifactsBundleModel bundleModel) {
        if (!paths.isEmpty()) {
            log.error("Delete release bundle {}:{} if forbidden for user: '{}'", bundleModel.getName(),
                    bundleModel.getVersion(), authService.currentUsername());
            throw new ForbiddenWebAppException(
                    String.format(
                            "Delete release bundle %s:%s is forbidden for user: '%s' because user does not have delete permission on the following paths: %s",
                            bundleModel.getName(), bundleModel.getVersion(), authService.currentUsername(), paths));
        }
    }

    private boolean shouldDeleteBundleArtifact(Long nodeId, Long bundleId, Set<Long> nodeIdsWithArtifactsToDelete) throws SQLException {
        boolean shouldDelete = bundlesDao.getAllBundlesRelatedToNode(nodeId).stream()
                .allMatch(relatedBundleId -> relatedBundleId.equals(bundleId));
        if (shouldDelete) {
            nodeIdsWithArtifactsToDelete.add(nodeId);
        }
        return shouldDelete;
    }

    private void copyArtifact(ReleaseArtifact artifact, String storingRepo, String bundlePrefix,
                              Map<String, String> artifactMapping, ReleaseBundleModel bundleModel) throws SQLException {
        // target path is storingRepo/bundleName/bundleVersion/artifactPath
        String targetPath = bundlePrefix + '/' + artifact.getRepoPath();
        RepoPath target = RepoPathFactory.create(storingRepo, targetPath);
        String mapped = artifactMapping.get(artifact.getRepoPath());
        if (mapped == null) {
            String path = target.toPath();
            log.error("No artifact mapping provided for {}, can't determine source artifact", path);
            throw new IllegalArgumentException(
                    "No artifact mapping provided for " + path + ", can't determine source artifact");
        }
        RepoPath source = RepoPathFactory.create(mapped);
        log.trace("Copying {} to {}", source.getPath(), target.getPath());
        repoService.copy(source, target, false, true, false);
        setProperties(artifact.getProps(), target);
        try {
            ComponentDetails originalComponentDetails = componentDetailsFetcher.calcComponentDetails(source);
            createBundleNode(bundleModel.getBundleId(), target, originalComponentDetails);
        } catch (SQLException e) {
            log.error("Unable to add artifact '{}' to bundle {}", target.toPath(), bundlePrefix);
            throw e;
        }
    }

    private void setProperties(List<ArtifactProperty> propsToAttach, RepoPath targetPath) {
        Properties properties = new PropertiesImpl();
        propsToAttach.forEach(
                artifactProperty -> properties.putAll(artifactProperty.getKey(), artifactProperty.getValues()));
        if (propertiesService.setProperties(targetPath, properties, false)) {
            log.trace("Properties were set successfully on {}", targetPath);
        } else {
            log.error("Failed to set properties on {}", targetPath);
        }
    }

    @Override
    public void setBundleStatusWithRetries(@NonNull String bundleName, @NonNull String bundleVersion,
            @NonNull BundleTransactionStatus status, @NonNull BundleType type) {
        ExecutionUtils.RetryOptions options = ExecutionUtils.RetryOptions.builder()
                .timeout((int) initialTimeBetweenRetries)
                .backoffMaxDelay((int) (10 * initialTimeBetweenRetries))
                .exponentialBackoffMultiplier(2)
                .numberOfRetries(10)
                .build();

        ExecutionUtils.retry(() -> {
            int updated;

            try {
                updated = bundlesDao.setBundleStatus(bundleName, bundleVersion, type, status);
                if (updated == 0) {
                    log.warn("Could not set source release bundle for {} as {}}.", bundleName + "/" + bundleVersion, status);
                }
            } catch (SQLException e) {
                String errorMessage = String.format("Failed setting store of bundle %s/%s status as %s}. %s", bundleName, bundleVersion, status.name(), e.getMessage());
                throw new RetryException(errorMessage, e);
            }

            return updated;
        }, options, executorService);
    }
}
