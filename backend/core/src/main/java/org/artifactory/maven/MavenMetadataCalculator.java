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

package org.artifactory.maven;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.maven.MavenArtifactInfo;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.module.ModuleInfoUtils;
import org.artifactory.aql.AqlService;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.result.AqlLazyResult;
import org.artifactory.aql.result.rows.AqlLazyObjectResultStreamer;
import org.artifactory.aql.result.rows.AqlRowResult;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.SnapshotVersionBehavior;
import org.artifactory.fs.ItemInfo;
import org.artifactory.maven.snapshot.BuildNumberSnapshotComparator;
import org.artifactory.maven.snapshot.SnapshotComparator;
import org.artifactory.maven.versioning.MavenMetadataVersionComparator;
import org.artifactory.maven.versioning.VersionNameMavenMetadataVersionComparator;
import org.artifactory.mime.MavenNaming;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.repo.RepoPath;
import org.artifactory.storage.db.fs.entity.NodePath;
import org.artifactory.storage.fs.tree.ItemNode;
import org.artifactory.storage.fs.tree.ItemTree;
import org.artifactory.util.RepoLayoutUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.artifactory.aql.api.domain.sensitive.AqlApiItem.*;
import static org.artifactory.aql.api.internal.AqlBase.and;

/**
 * Calculates maven metadata recursively for folder in a local non-cache repository. Plugins metadata is calculated for
 * the whole repository.
 *
 * @author Yossi Shaul
 */
public class MavenMetadataCalculator extends AbstractMetadataCalculator {

    private static final Logger log = LoggerFactory.getLogger(MavenMetadataCalculator.class);
    private static Predicate<? super ItemNode> uniqueSnapshotFileOnly() {
        return pom -> pom != null &&   MavenNaming.isUniqueSnapshotFileName(pom.getName());
    }
    private final RepoPath baseFolder;
    private final boolean recursive;

    /**
     * Creates new instance of maven metadata calculator.
     *
     * @param baseFolder Folder to calculate metadata for
     * @param recursive  True if the calculator should recursively calculate maven metadata for all sub folders
     */
    MavenMetadataCalculator(RepoPath baseFolder, boolean recursive) {
        this.baseFolder = baseFolder;
        this.recursive = recursive;
    }

    private static MavenMetadataVersionComparator createVersionComparator() {
        String comparatorFqn = ConstantValues.mvnMetadataVersionsComparator.getString();
        if (StringUtils.isBlank(comparatorFqn)) {
            // return the default comparator
            return VersionNameMavenMetadataVersionComparator.get();
        }

        try {
            Class<?> comparatorClass = Class.forName(comparatorFqn);
            return (MavenMetadataVersionComparator) comparatorClass.newInstance();
        } catch (Exception e) {
            log.warn("Failed to create custom maven metadata version comparator '{}': {}", comparatorFqn,
                    e.getMessage());
            return VersionNameMavenMetadataVersionComparator.get();
        }
    }

    public static SnapshotComparator createSnapshotComparator() {
        SnapshotComparator comparator = BuildNumberSnapshotComparator.get();
        // Try to load custom comparator
        String comparatorFqn = ConstantValues.mvnMetadataSnapshotComparator.getString();
        if (!StringUtils.isBlank(comparatorFqn)) {
            try {
                Class comparatorClass = Class.forName(comparatorFqn);
                Method get = comparatorClass.getMethod("get");
                comparator = (SnapshotComparator) get.invoke(null);
                log.debug("Using custom snapshot comparator '{}' to calculate the latest snapshot", comparatorFqn);
            } catch (NoSuchMethodException e1) {
                log.warn(
                        "Failed to create custom maven metadata snapshot comparator, the comparator should contain" +
                                " static get method to avoid unnecessary object creation '{}': {}", comparatorFqn,
                        e1.getMessage());
            } catch (Exception e) {
                log.warn("Failed to create custom maven metadata snapshot comparator '{}': {}", comparatorFqn,
                        e.getMessage());
            }
        }
        return comparator;
    }

    /**
     * Starts calculation of the maven metadata from the base repo path
     *
     * @return Status of the metadata calculation
     */
    public BasicStatusHolder calculate() {
        long start = System.nanoTime();
        log.debug("Started {} maven metadata calculation on '{}'",
                (recursive ? "recursive" : "non recursive"), baseFolder);

        ItemTree itemTree = new ItemTree(baseFolder, item -> {
            if (item.isFolder()) {
                return true;
            }
            String path = item.getRepoPath().getPath();
            return MavenNaming.isPom(path) || MavenNaming.isUniqueSnapshot(path);
        });
        ItemNode rootNode = itemTree.getRootNode();
        if (rootNode != null) {
            calculateAndSet(rootNode);
            long duration = System.nanoTime() - start;
            log.debug("Finished {} maven metadata calculation on '{}' in {} ms",
                    (recursive ? "recursive" : "non recursive"), baseFolder, TimeUnit.NANOSECONDS.toMillis(duration));
        } else {
            log.debug("Root path for metadata calculation not found: '{}'", baseFolder);
        }
        return status;
    }

    private void calculateAndSet(ItemNode treeNode) {
        long start = System.currentTimeMillis();
        ItemInfo itemInfo = treeNode.getItemInfo();
        if (!itemInfo.isFolder()) {
            // Nothing to do here for non folder tree node
            return;
        }

        RepoPath repoPath = itemInfo.getRepoPath();

        String nodePath = repoPath.getPath();
        boolean containsMetadataInfo;
        if (MavenNaming.isSnapshot(nodePath)) {
            // if this folder contains snapshots create snapshots maven.metadata
            log.trace("Detected snapshots container: {}", nodePath);
            containsMetadataInfo = createSnapshotsMetadata(repoPath, treeNode);
        } else {
            // if this folder contains "version folders" create versions maven metadata
            List<MavenMetaDataInfo> poms = getSubFoldersContainingPoms(treeNode);
            if (!poms.isEmpty()) {
                log.trace("Detected versions container: {} with {} child versions",
                        repoPath.toPath(), poms.size());
                createVersionsMetadata(repoPath, poms);
                containsMetadataInfo = true;
            } else {
                containsMetadataInfo = false;
            }
        }

        if (!containsMetadataInfo) {
            // note: this will also remove plugins metadata. not sure it should
            removeMetadataIfExist(repoPath);
        }

        // Recursive call to calculate and set if recursive calc is on
        if (recursive && itemInfo.isFolder()) {
            List<ItemNode> children = treeNode.getChildren();
            if (children != null) {
                children.forEach(this::calculateAndSet);
            }
        }
        long end = System.currentTimeMillis();
        log.trace("Maven metadata calculation total time is:{}", (end - start));
    }

    private boolean createSnapshotsMetadata(RepoPath repoPath, ItemNode treeNode) {
        if (!folderContainsPoms(treeNode)) {
            return false;
        }
        List<ItemNode> folderItems = treeNode.getChildren();
        Iterable<ItemNode> poms = folderItems.stream()
                .filter(input -> (input != null) && MavenNaming.isPom(input.getItemInfo().getName()))
                .collect(Collectors.toList());

        RepoPath firstPom = poms.iterator().next().getRepoPath();
        MavenArtifactInfo artifactInfo = MavenArtifactInfo.fromRepoPath(firstPom);
        if (!artifactInfo.isValid()) {
            return true;
        }
        Metadata metadata = new Metadata();
        metadata.setGroupId(artifactInfo.getGroupId());
        metadata.setModelVersion(artifactInfo.getModelVersion());
        metadata.setArtifactId(artifactInfo.getArtifactId());
        String baseVersion = StringUtils.substringBefore(artifactInfo.getVersion(), "-");
        metadata.setVersion(baseVersion + MavenNaming.SNAPSHOT_SUFFIX);
        Versioning versioning = new Versioning();
        metadata.setVersioning(versioning);
        versioning.setLastUpdatedTimestamp(new Date());
        Snapshot snapshot = new Snapshot();
        versioning.setSnapshot(snapshot);

        LocalRepoDescriptor localRepoDescriptor =
                getRepositoryService().localOrCachedRepoDescriptorByKey(repoPath.getRepoKey());
        SnapshotVersionBehavior snapshotBehavior = localRepoDescriptor.getSnapshotVersionBehavior();
        String latestUniquePom = getLatestUniqueSnapshotPomName(poms);
        if (snapshotBehavior.equals(SnapshotVersionBehavior.NONUNIQUE) ||
                (snapshotBehavior.equals(SnapshotVersionBehavior.DEPLOYER) && latestUniquePom == null)) {
            snapshot.setBuildNumber(1);
        } else if (snapshotBehavior.equals(SnapshotVersionBehavior.UNIQUE)) {
            // take the latest unique snapshot file file
            if (latestUniquePom != null) {
                snapshot.setBuildNumber(MavenNaming.getUniqueSnapshotVersionBuildNumber(latestUniquePom));
                snapshot.setTimestamp(MavenNaming.getUniqueSnapshotVersionTimestamp(latestUniquePom));
            }

            if (ConstantValues.mvnMetadataVersion3Enabled.getBoolean()) {
                List<SnapshotVersion> snapshotVersions = Lists.newArrayList(getFolderItemSnapshotVersions(folderItems));
                if (!snapshotVersions.isEmpty()) {
                    versioning.setSnapshotVersions(snapshotVersions);
                }
            }
        }
        saveMetadata(repoPath, metadata);
        return true;
    }

    private Collection<SnapshotVersion> getFolderItemSnapshotVersions(Collection<ItemNode> folderItems) {
        List<SnapshotVersion> snapshotVersionsToReturn = Lists.newArrayList();

        Map<SnapshotVersionType, ModuleInfo> latestSnapshotVersions = Maps.newHashMap();

        for (ItemNode folderItem : folderItems) {
            String folderItemPath = folderItem.getItemInfo().getRelPath();
            if (MavenNaming.isUniqueSnapshot(folderItemPath)) {
                ModuleInfo folderItemModuleInfo;
                if (MavenNaming.isPom(folderItemPath)) {
                    folderItemModuleInfo = ModuleInfoUtils.moduleInfoFromDescriptorPath(folderItemPath,
                            RepoLayoutUtils.MAVEN_2_DEFAULT);
                } else {
                    folderItemModuleInfo = ModuleInfoUtils.moduleInfoFromArtifactPath(folderItemPath,
                            RepoLayoutUtils.MAVEN_2_DEFAULT);
                }
                if (!folderItemModuleInfo.isValid() || !folderItemModuleInfo.isIntegration()) {
                    continue;
                }
                SnapshotVersionType folderItemSnapshotVersionType = new SnapshotVersionType(
                        folderItemModuleInfo.getExt(), folderItemModuleInfo.getClassifier());
                if (latestSnapshotVersions.containsKey(folderItemSnapshotVersionType)) {
                    SnapshotComparator snapshotComparator = createSnapshotComparator();
                    ModuleInfo latestSnapshotVersion = latestSnapshotVersions.get(folderItemSnapshotVersionType);
                    if (snapshotComparator.compare(folderItemModuleInfo, latestSnapshotVersion) > 0) {
                        latestSnapshotVersions.put(folderItemSnapshotVersionType, folderItemModuleInfo);
                    }
                } else {
                    latestSnapshotVersions.put(folderItemSnapshotVersionType, folderItemModuleInfo);
                }
            }
        }

        for (ModuleInfo latestSnapshotVersion : latestSnapshotVersions.values()) {
            SnapshotVersion snapshotVersion = new SnapshotVersion();
            snapshotVersion.setClassifier(latestSnapshotVersion.getClassifier());
            snapshotVersion.setExtension(latestSnapshotVersion.getExt());

            String fileItegRev = latestSnapshotVersion.getFileIntegrationRevision();
            snapshotVersion.setVersion(latestSnapshotVersion.getBaseRevision() + "-" + fileItegRev);
            snapshotVersion.setUpdated(StringUtils.remove(StringUtils.substringBefore(fileItegRev, "-"), '.'));
            snapshotVersionsToReturn.add(snapshotVersion);
        }

        return snapshotVersionsToReturn;
    }

    private void createVersionsMetadata(RepoPath repoPath, List<MavenMetaDataInfo> mavenPathInfos) {

        // get artifact info from the first pom
        RepoPath samplePomRepoPath = mavenPathInfos.get(0).getRepoPath();
        MavenArtifactInfo artifactInfo = MavenArtifactInfo.fromRepoPath(samplePomRepoPath);
        if (!artifactInfo.isValid()) {
            return;
        }
        Metadata metadata = new Metadata();
        metadata.setGroupId(artifactInfo.getGroupId());
        metadata.setArtifactId(artifactInfo.getArtifactId());
        metadata.setVersion(artifactInfo.getVersion());
        metadata.setModelVersion(artifactInfo.getModelVersion());
        Versioning versioning = new Versioning();
        metadata.setVersioning(versioning);
        versioning.setLastUpdatedTimestamp(new Date());

        MyComparator<MavenMetaDataInfo> comparator = new MyComparator<>();
        Map<String, MavenMetaDataInfo> distinctVersions = Maps.newConcurrentMap();
        mavenPathInfos.forEach(mavenMetaDataInfo -> {
            String version = mavenMetaDataInfo.getVersion();
            MavenMetaDataInfo last = distinctVersions.get(version);
            if (last == null || comparator.compare(mavenMetaDataInfo, last) > 0) {
                distinctVersions.put(version, mavenMetaDataInfo);
            }
        });
        List<MavenMetaDataInfo> orderedVersions = Lists.newArrayList(distinctVersions.values());
        Collections.sort(orderedVersions, comparator);

        // latest is simply the last (be it snapshot or release version)
        String latestVersion = orderedVersions.get(orderedVersions.size() - 1).getVersion();
        versioning.setLatest(latestVersion);

        // Set version as latest
        metadata.setVersion(latestVersion);

        // add the versions to the versioning section
        for (MavenMetaDataInfo sortedVersion : orderedVersions) {
            versioning.addVersion(sortedVersion.getVersion());
        }

        // release is the latest non snapshot version
        for (MavenMetaDataInfo sortedVersion : orderedVersions) {
            String versionNodeName = sortedVersion.getVersion();
            if (!MavenNaming.isSnapshot(versionNodeName)) {
                versioning.setRelease(versionNodeName);
            }
        }

        saveMetadata(repoPath, metadata);
    }


    private String getLatestUniqueSnapshotPomName(Iterable<ItemNode> poms) {
        // Get Default Comparator
        Comparator<ItemNode> comparator = createSnapshotComparator();
        ArrayList<ItemNode> list = Lists.newArrayList(poms);
        list = Lists.newArrayList(list.stream().filter(uniqueSnapshotFileOnly()).collect(Collectors.toList()));
        list.sort(comparator);
        return list.isEmpty() ? null : list.get(list.size() - 1).getName();
    }

    private List<MavenMetaDataInfo> getSubFoldersContainingPoms(ItemNode treeNode) {
        // version metadata is only applicable for folders that contain other folders
        // (child folder is checked to prevent the costly query below)
        if (!treeNode.isFolder()) {
            log.trace("Item is not a folder or has no child folders: {}", treeNode.getRepoPath());
            return Lists.newArrayList();
        }
        NodePath nodePath = NodePath.fromRepoPath(treeNode.getRepoPath());
        AqlApiItem query = createWithEmptyResults().filter(
                and(
                        repo().equal(nodePath.getRepo()),
                        path().matches(nodePath.getPathName() + "/*"),
                        name().matches("*.pom"),
                        AqlApiItem.depth().equals(nodePath.getDepth() + 2)
                )
        ).include(path(), name(), created());

        AqlService aqlService = ContextHelper.get().beanForType(AqlService.class);
        AqlLazyResult<? extends AqlRowResult> result = null;
        try {
            long start = System.currentTimeMillis();
            result = aqlService.executeQueryLazy(query);
            AqlLazyObjectResultStreamer<MavenMetaDataInfo> streamer = new AqlLazyObjectResultStreamer<>(result,
                    MavenMetaDataInfo.class);
            List<MavenMetaDataInfo> items = Lists.newArrayList();
            MavenMetaDataInfo row;
            while ((row = streamer.getRow()) != null) {
                row.setRepo(nodePath.getRepo());
                items.add(row);
            }
            long end = System.currentTimeMillis();
            log.trace("Maven metadata calculation query time is:{}", (end - start));
            return items;
        } finally {
            closeResultSet(result);
        }
    }

    private void closeResultSet(AqlLazyResult<? extends AqlRowResult> result) {
        if (result != null) {
            try {
                result.getResultSet().close();
            } catch (SQLException e) {
                log.error("Failed to close result set", e);
            }
        }
    }

    private boolean folderContainsPoms(ItemNode treeNode) {
        if (!treeNode.isFolder()) {
            return false;
        }

        List<ItemNode> children = treeNode.getChildren();
        for (ItemNode child : children) {
            if (!child.isFolder() && MavenNaming.isPom(child.getName())) {
                return true;
            }
        }

        return false;
    }

    private void removeMetadataIfExist(RepoPath repoPath) {
        try {
            RepoPathImpl mavenMetadataPath = new RepoPathImpl(repoPath, MavenNaming.MAVEN_METADATA_NAME);
            if (getRepositoryService().exists(mavenMetadataPath)) {
                boolean delete = true;
                String metadataStr = getRepositoryService().getStringContent(mavenMetadataPath);
                try {
                    Metadata metadata = MavenModelUtils.toMavenMetadata(metadataStr);
                    if (isSnapshotMavenMetadata(metadata) && !MavenNaming.isSnapshot(repoPath.getPath())) {
                        // RTFACT-6242 - don't delete user deployed maven-metadata (maven 2 bug)
                        delete = false;
                    }
                } catch (IOException e) {
                    // ignore -> delete
                }
                if (delete) {
                    log.debug("Deleting {}", mavenMetadataPath);
                    getRepositoryService().undeploy(mavenMetadataPath, false, false);
                }
            }
        } catch (Exception e) {
            status.error("Error while removing maven metadata from " + repoPath + ".", e, log);
        }
    }

    private boolean isSnapshotMavenMetadata(Metadata metadata) {
        Versioning versioning = metadata.getVersioning();
        if (versioning == null) {
            return false;
        }
        List<SnapshotVersion> snapshots = versioning.getSnapshotVersions();
        return snapshots != null && !snapshots.isEmpty();
    }

    private static class SnapshotVersionType {

        private String extension;
        private String classifier;

        private SnapshotVersionType(String extension, String classifier) {
            this.extension = extension;
            this.classifier = classifier;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof SnapshotVersionType)) {
                return false;
            }

            SnapshotVersionType that = (SnapshotVersionType) o;

            return classifier != null ? classifier.equals(that.classifier) : that.classifier == null &&
                    (extension != null ? extension.equals(that.extension) : that.extension == null);

        }

        @Override
        public int hashCode() {
            int result = extension != null ? extension.hashCode() : 0;
            result = 31 * result + (classifier != null ? classifier.hashCode() : 0);
            return result;
        }
    }

    private class MyComparator<T extends MavenMetaDataInfo> implements Comparator<T> {

        private final MavenMetadataVersionComparator comparator;

        MyComparator() {
            comparator = createVersionComparator();
        }

        @Override
        public int compare(T o1, T o2) {
            return comparator.compare(o1, o2);
        }
    }
}
