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

package org.artifactory.storage.db.fs.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.repo.exception.FileExpectedException;
import org.artifactory.api.repo.exception.FolderExpectedException;
import org.artifactory.api.repo.storage.FolderSummeryInfo;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.checksum.ChecksumsInfo;
import org.artifactory.common.ConstantValues;
import org.artifactory.exception.SQLIntegrityException;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.FolderInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.model.xstream.fs.FileInfoImpl;
import org.artifactory.model.xstream.fs.FolderInfoImpl;
import org.artifactory.repo.RepoPath;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.storage.RepoStorageSummary;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.db.InternalDbService;
import org.artifactory.storage.db.fs.dao.NodesDao;
import org.artifactory.storage.db.fs.entity.FolderSummeryNodeInfo;
import org.artifactory.storage.db.fs.entity.Node;
import org.artifactory.storage.db.fs.entity.NodeBuilder;
import org.artifactory.storage.db.fs.entity.NodePath;
import org.artifactory.storage.db.fs.model.DbFsFile;
import org.artifactory.storage.db.fs.model.DbFsFolder;
import org.artifactory.storage.fs.VfsException;
import org.artifactory.storage.fs.VfsItemNotFoundException;
import org.artifactory.storage.fs.repo.StoringRepo;
import org.artifactory.storage.fs.service.FileService;
import org.artifactory.storage.fs.service.MigrationFileService;
import org.artifactory.util.PathValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Yossi Shaul
 */
@SuppressWarnings("DuplicateThrows")
@Service
public class FileServiceImpl implements FileService, MigrationFileService {
    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    @Autowired
    private InternalDbService dbService;

    @Autowired
    private NodesDao nodesDao;

    @Override
    public boolean exists(RepoPath repoPath) throws VfsException {
        try {
            return nodesDao.exists(NodePath.fromRepoPath(repoPath));
        } catch (SQLException e) {
            log.debug("Failed existence check of path '{}", repoPath, e);
            throw new VfsException("Failed existence check of path '" + repoPath + "'", e);
        }
    }

    @Override
    public boolean existsBySha1(String sha1) throws VfsException {
        try {
            return nodesDao.existsBySha1(sha1);
        } catch (SQLException e) {
            log.debug("Failed existence check by hash '{}", sha1, e);
            throw new VfsException("Failed existence check by hash '" + sha1 + "'", e);
        }
    }

    @Nonnull
    @Override
    public ItemInfo loadItem(RepoPath repoPath) throws VfsItemNotFoundException, VfsException {
        Node node = loadNode(repoPath);
        return itemInfoFromNode(node);
    }

    @Override
    public ItemInfo loadItem(long id) {
        Node node = loadNode(id);
        return itemInfoFromNode(node);
    }

    @Override
    public VfsItem loadVfsItem(StoringRepo storingRepo, RepoPath repoPath) throws VfsItemNotFoundException,
            VfsException {
        Node node = loadNode(repoPath);
        return fsItemFromNode(storingRepo, node);
    }

    @Override
    public long createFolder(FolderInfo folder) throws VfsException {
        PathValidator.validate(folder.getRepoPath().toPath());
        //TODO: [by YS] verify parent exists?
        long nodeId = dbService.nextId();
        Node node = folderInfoToNode(nodeId, folder);
        int updateCount;
        try {
            updateCount = nodesDao.create(node);
        } catch (SQLIntegrityConstraintViolationException e) {
            nodeId = getNodeId(folder.getRepoPath());
            if (DbService.NO_DB_ID == nodeId) {
                throw new SQLIntegrityException("Couldn't find folder after SQL integrity exception for path" +
                        folder.getRepoPath(), e);
            }
            return nodeId;
        } catch (SQLException e) {
            throw new VfsException(e);
        }
        if (updateCount != 1) {
            // Create new node should return with exactly 1 record updated
            throw new IllegalStateException("Unexpected update count when creating new node: '" +
                    node.getNodePath() + "'");
        }
        return nodeId;
    }

    @Override
    public int updateFolder(long id, FolderInfo folder) {
        log.debug("Updating folder: {}", folder.getRepoPath());
        try {
            return nodesDao.update(folderInfoToNode(id, folder));
        } catch (SQLException e) {
            throw new VfsException("Failed to update folder: '" + folder.getRepoPath() + "' id: '" + id + "': " +
                    e.getMessage(), e);
        }
    }

    @Override
    public long createFile(FileInfo file) {
        PathValidator.validate(file.getRepoPath().toPath());
        //TODO: [by YS] verify parent exists?
        log.debug("Creating file {}", file.getRepoPath());
        long nodeId = dbService.nextId();
        Node node = fileInfoToNode(nodeId, file);
        int updateCount;
        try {
            updateCount = nodesDao.create(node);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new SQLIntegrityException("Failed to create file, the file already exists", e);
        } catch (SQLException e) {
            throw new VfsException(e);
        }
        if (updateCount != 1) {
            // Create new node should return with exactly 1 record updated
            throw new IllegalStateException("Unexpected update count when creating new node: '" +
                    node.getNodePath() + "'");
        }
        return nodeId;
    }

    @Override
    public int updateFile(long id, FileInfo file) {
        log.debug("Updating file: {}", file.getRepoPath());
        try {
            return nodesDao.update(fileInfoToNode(id, file));
        } catch (SQLException e) {
            throw new VfsException("Failed to update file: '" + file.getRepoPath() + "' id: '" + id + "': " +
                    e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteItem(long id) {
        try {
            return nodesDao.delete(id);
        } catch (SQLException e) {
            throw new VfsException("Failed to delete item with id '" + id + "': " + e.getMessage(), e);
        }
    }

    @Override
    public boolean hasChildren(RepoPath repoPath) {
        try {
            return nodesDao.hasChildren(NodePath.fromRepoPath(repoPath));
        } catch (SQLException e) {
            throw new VfsException("Failed to check for children of: '" + repoPath + "': " + e.getMessage(), e);
        }
    }

    @Override
    public void printNodesTable() {
        if (!ConstantValues.dev.getBoolean()) {
            return;
        }
        try {
            List<? extends Node> nodes = nodesDao.getAllNodes();
            StringBuilder sb = new StringBuilder(String.format(
                    "%-4s%-4s %-16s%-44s%-16s%n", "ID", "Depth", "Repo", "Path", "Name"));
            for (Node n : nodes) {
                sb.append(String.format("%-4s%4s  %-16s%-44s%-16s%n",
                        n.getNodeId(), n.getRepo(), n.getPath(), n.getName(), n.getDepth()));

            }
            log.info("Dumping Checksum Paths Table ({} records):\n {}", nodes.size(), sb);
        } catch (SQLException e) {
            log.error("Failed to print nodes table", e);
        }
    }

    @Override
    public long getNodeId(RepoPath repoPath) {
        //TODO: [by YS] requires caching
        try {
            return nodesDao.getNodeId(NodePath.fromRepoPath(repoPath));
        } catch (SQLException e) {
            throw new VfsException("Couldn't get node id for: " + repoPath, e);
        }
    }

    private Node loadNode(RepoPath repoPath) throws VfsItemNotFoundException, VfsException {
        try {
            Node node = nodesDao.get(NodePath.fromRepoPath(repoPath));
            if (node == null) {
                throw new VfsItemNotFoundException("Item not found: '" + repoPath + "'");
            }
            return node;
        } catch (SQLException e) {
            throw new VfsException("Failed to load item '" + repoPath + "'", e);
        }
    }

    private Node loadNode(long id) throws VfsItemNotFoundException, VfsException {
        try {
            Node node = nodesDao.get(id);
            if (node == null) {
                throw new VfsItemNotFoundException("Item not found for id '" + id + "'");
            }
            return node;
        } catch (SQLException e) {
            throw new VfsException("Failed to load item with id '" + id + "'", e);
        }
    }

    @Override
    @Nonnull
    public List<ItemInfo> loadChildren(RepoPath repoPath) throws VfsException {
        //TODO: [by YS] consider always sorting by name to help prevent locking errors later
        try {
            List<Node> childrenNode = nodesDao.getChildren(NodePath.fromRepoPath(repoPath));
            List<ItemInfo> children = Lists.newArrayList();
            for (Node child : childrenNode) {
                children.add(itemInfoFromNode(child));
            }
            return children;
        } catch (SQLException e) {
            throw new VfsException("Failed to load children for node '" + repoPath + "'", e);
        }
    }

    @Override
    public int getFileCount(String repoKey, String fileName) {
        try {
            return nodesDao.getFileCount(repoKey, fileName);
        } catch (SQLException e) {
            throw new VfsException(e);
        }
    }

    @Override
    public int getFilesCount() throws VfsException {
        try {
            return nodesDao.getFilesCount();
        } catch (SQLException e) {
            throw new VfsException(e);
        }
    }

    @Override
    public FolderSummeryInfo getFilesCountAndSize(@Nonnull RepoPath repoPath) throws VfsException {
        FolderSummeryNodeInfo filesCountAndSize;
        try {
            filesCountAndSize = nodesDao.getFilesCountAndSize(NodePath.fromRepoPath(repoPath));
            return new FolderSummeryInfoImpl(filesCountAndSize.getFileCount(),
                    filesCountAndSize.getFolderSize());
        } catch (SQLException e) {
            throw new VfsException(e);
        }
    }

    @Override
    public int getFilesCount(RepoPath repoPath) throws VfsException {
        try {
            if (repoPath.isRoot()) {
                return nodesDao.getFilesCount(repoPath.getRepoKey());
            } else {
                return nodesDao.getFilesCount(NodePath.fromRepoPath(repoPath));
            }
        } catch (SQLException e) {
            throw new VfsException(e);
        }
    }

    @Override
    public long getFilesTotalSize(RepoPath repoPath) {
        try {
            if (repoPath.isRoot()) {
                return nodesDao.getFilesTotalSize(repoPath.getRepoKey());
            } else {
                return nodesDao.getFilesTotalSize(NodePath.fromRepoPath(repoPath));
            }
        } catch (SQLException e) {
            throw new VfsException(e);
        }
    }

    @Override
    public int getNodesCount(RepoPath repoPath) throws VfsException {
        try {
            if (repoPath.isRoot()) {
                return nodesDao.getNodesCount(repoPath.getRepoKey());
            } else {
                return nodesDao.getNodesCount(NodePath.fromRepoPath(repoPath));
            }
        } catch (SQLException e) {
            throw new VfsException(e);
        }
    }

    @Override
    public List<FileInfo> searchFilesByProperty(String repo, String propKey, String propValue) {
        try {
            List<Node> childrenNode = nodesDao.searchNodesByProperty(repo, propKey);
            List<FileInfo> children = Lists.newArrayList();
            for (Node child : childrenNode) {
                if (child.isFile()) {
                    children.add(fileInfoFromNode(child));
                }
            }
            return children;
        } catch (SQLException e) {
            throw new VfsException("Search by properties failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<FileInfo> searchGrandchildPoms(RepoPath repoPath) {
        try {
            List<Node> nodes = nodesDao.searchGrandchildPoms(NodePath.fromRepoPath(repoPath));
            return nodes.stream().map(this::fileInfoFromNode).collect(Collectors.toList());
        } catch (SQLException e) {
            throw new VfsException("Search by properties failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<FileInfo> searchFilesWithBadChecksum(ChecksumType type) {
        try {
            List<Node> childrenNode = nodesDao.searchBadChecksums(type);
            List<FileInfo> children = Lists.newArrayList();
            for (Node child : childrenNode) {
                children.add(fileInfoFromNode(child));
            }
            return children;
        } catch (SQLException e) {
            throw new VfsException("Search files with bad checksum failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Set<RepoStorageSummary> getRepositoriesStorageSummary() {
        try {
            return nodesDao.getRepositoriesStorageSummary();
        } catch (SQLException e) {
            throw new VfsException("Repository storage summary failed with exception: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ItemInfo> getOrphanItems(RepoPath repoPath) {
        try {
            List<Node> orphanNodes = nodesDao.getOrphanNodes(NodePath.fromRepoPath(repoPath));
            List<ItemInfo> orphanItems = Lists.newArrayList();
            for (Node orphanNode : orphanNodes) {
                orphanItems.add(itemInfoFromNode(orphanNode));
            }
            return orphanItems;
        } catch (SQLException e) {
            throw new VfsException("Failed to find orphan nodes under: '" + repoPath + "': " + e.getMessage(), e);
        }
    }

    @Override
    public Boolean isFile(RepoPath repoPath) {
        try {
            Integer itemType = nodesDao.getItemType(NodePath.fromRepoPath(repoPath));
            if (itemType != null) {
                return itemType == 1;
            }
        } catch (SQLException e) {
            throw new VfsException("Failed to check item type for node: '" + repoPath + "': " + e + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public Boolean isFolder(RepoPath repoPath) {
        try {
            Integer itemType = nodesDao.getItemType(NodePath.fromRepoPath(repoPath));
            if (itemType != null) {
                return itemType == 0;
            }
        } catch (SQLException e) {
            throw new VfsException("Failed to check item type for node: '" + repoPath + "': " + e + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void debugNodeStructure(RepoPath repoPath) {
        log.info("************************ Tree structure dump ************************");
        debugNodeStructure(loadItem(repoPath), 0);
        log.info("*********************************************************************");
    }

    private void debugNodeStructure(ItemInfo item, int depth) {
        StringBuilder itemString = new StringBuilder(depth * 2);
        for (int i = 0; i < depth; i++) {
            itemString.append("  ");
        }
        itemString.append(item.getRepoKey()).append("/").append(item.getRelPath())
                .append(" (").append(item).append(") ");
        log.info("{}", itemString);
        if (item.isFolder()) {
            for (ItemInfo child : loadChildren(item.getRepoPath())) {
                debugNodeStructure(child, depth + 1);
            }
        }
    }

    private Node folderInfoToNode(long nodeId, FolderInfo folder) {
        return itemNodeBuilder(nodeId, folder).file(false).build();
    }

    private Node fileInfoToNode(long nodeId, FileInfo file) {
        NodeBuilder builder = itemNodeBuilder(nodeId, file).file(true);

        builder.length(file.getSize());

        ChecksumsInfo checksums = file.getChecksumsInfo();
        ChecksumInfo sha1Checksums = checksums.getChecksumInfo(ChecksumType.sha1);
        if (sha1Checksums == null || StringUtils.isBlank(sha1Checksums.getActual())) {
            throw new VfsException("Cannot persist file '" + file.getRepoPath() + "' without sha1 checksum");
        }
        builder.sha1Actual(sha1Checksums.getActual()).sha1Original(sha1Checksums.getOriginalOrNoOrig());
        ChecksumInfo md5Checksums = checksums.getChecksumInfo(ChecksumType.md5);
        if (md5Checksums != null) {
            builder.md5Actual(md5Checksums.getActual()).md5Original(md5Checksums.getOriginalOrNoOrig());
        }
        ChecksumInfo sha2Checksums = checksums.getChecksumInfo(ChecksumType.sha256);
        if (sha2Checksums != null) {
            //There are no 'original' values for sha2, next step is removing this field for the others
            builder.sha2(sha2Checksums.getActual());
        }
        return builder.build();
    }

    private NodeBuilder itemNodeBuilder(long nodeId, ItemInfo item) {
        return new NodeBuilder().nodeId(nodeId).nodePath(item.getRepoPath())
                .created(item.getCreated()).createdBy(item.getCreatedBy())
                .modified(item.getLastModified()).modifiedBy(item.getModifiedBy())
                .updated(item.getLastUpdated());
    }

    private ItemInfo itemInfoFromNode(Node node) {
        if (node.isFile()) {
            return fileInfoFromNode(node);
        } else {
            return folderInfoFromNode(node);
        }
    }

    private FolderInfo folderInfoFromNode(Node node) {
        if (node.isFile()) {
            throw new FolderExpectedException(node.getNodePath().toRepoPath());
        }
        FolderInfoImpl folderInfo = new FolderInfoImpl(node.getNodePath().toRepoPath(), node.getNodeId());
        folderInfo.setCreated(node.getCreated());
        folderInfo.setCreatedBy(node.getCreatedBy());
        folderInfo.setLastModified(node.getModified());
        folderInfo.setModifiedBy(node.getModifiedBy());
        folderInfo.setLastUpdated(node.getUpdated());
        return folderInfo;
    }

    private FileInfo fileInfoFromNode(Node node) {
        if (!node.isFile()) {
            throw new FileExpectedException(node.getNodePath().toRepoPath());
        }
        FileInfoImpl fileInfo = new FileInfoImpl(node.getNodePath().toRepoPath(), node.getNodeId());
        fileInfo.setCreated(node.getCreated());
        fileInfo.setCreatedBy(node.getCreatedBy());
        fileInfo.setLastModified(node.getModified());
        fileInfo.setModifiedBy(node.getModifiedBy());
        fileInfo.setLastUpdated(node.getUpdated());
        Set<ChecksumInfo> checksums = Sets.newHashSet(
                new ChecksumInfo(ChecksumType.sha1, node.getSha1Original(), node.getSha1Actual()),
                new ChecksumInfo(ChecksumType.sha256, ChecksumInfo.TRUSTED_FILE_MARKER, node.getSha2()),
                new ChecksumInfo(ChecksumType.md5, node.getMd5Original(), node.getMd5Actual())
        );
        fileInfo.setChecksums(checksums);
        fileInfo.setSize(node.getLength());
        return fileInfo;
    }

    private VfsItem fsItemFromNode(StoringRepo storingRepo, Node node) {
        if (node.isFile()) {
            FileInfo fileInfo = fileInfoFromNode(node);
            return new DbFsFile(storingRepo, node.getNodeId(), fileInfo);
        } else {
            FolderInfo folderInfo = folderInfoFromNode(node);
            return new DbFsFolder(storingRepo, node.getNodeId(), folderInfo);
        }
    }

    @Override
    public void updateRepoPathChecksum(RepoPath repoPath) {
        try {
            int updates = nodesDao.updateRepoPathChecksum(NodePath.fromRepoPath(repoPath));
            if (updates != 1) {
                throw new VfsException("Expecting single update for repoPath: '" + repoPath + "'");
            }
        } catch (SQLException e) {
            throw new VfsException("Failed to update repoPath checksum for path: '" + repoPath + "'", e);
        }
    }
}
