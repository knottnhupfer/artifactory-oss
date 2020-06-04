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

package org.artifactory.storage.db.fs.dao;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.common.ConstantValues;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.storage.GCCandidate;
import org.artifactory.storage.RepoStorageSummary;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.db.fs.entity.FolderSummeryNodeInfo;
import org.artifactory.storage.db.fs.entity.FolderSummeryNodeInfoImpl;
import org.artifactory.storage.db.fs.entity.Node;
import org.artifactory.storage.db.fs.entity.NodePath;
import org.artifactory.storage.db.util.BaseDao;
import org.artifactory.storage.db.util.JdbcHelper;
import org.jfrog.storage.util.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A data access object for node table access.
 *
 * @author Yossi Shaul
 */
@Repository
public class NodesDao extends BaseDao {
    private static final Logger log = LoggerFactory.getLogger(NodesDao.class);

    public static final String TABLE_NAME = "nodes";
    private static final String SELECT_NODE_QUERY = "SELECT * FROM nodes ";
    private String nodeByPathQuery;
    private String nodeIdByPathQuery;
    private String nodesByPropertyQuery;
    private String nodesExistsQuery;
    private String nodesGetNodeTypeQuery;
    private String nodeHasChildren;
    private String nodeGetChildren;

    @Autowired
    public NodesDao(JdbcHelper jdbcHelper) {
        super(jdbcHelper);
        initExternalQueries();
    }

    /**
     * these are queries that can be altered through the artifactory.system.properties for adding hints {@see RTFACT-15325}
     */
    private void initExternalQueries() {
        nodeByPathQuery = ConstantValues.nodesDaoSqlGetNodeByPath.isSet() ?
                ConstantValues.nodesDaoSqlGetNodeByPath.getString() :
                "SELECT * FROM nodes WHERE repo = ? AND node_path = ? AND node_name = ?";
        nodeIdByPathQuery = ConstantValues.nodesDaoSqlGetNodeIdByPath.isSet() ?
                ConstantValues.nodesDaoSqlGetNodeIdByPath.getString() :
                "SELECT node_id FROM nodes WHERE repo = ? AND node_path = ? AND node_name = ?";
        nodeHasChildren = ConstantValues.nodesDaoSqlNodeHasChildren.isSet() ?
                ConstantValues.nodesDaoSqlNodeHasChildren.getString() :
                "SELECT COUNT(1) FROM nodes WHERE repo = ? AND node_path = ? AND depth = ?";
        nodeGetChildren = ConstantValues.nodesDaoSqlNodeGetChildren.isSet() ?
                ConstantValues.nodesDaoSqlNodeGetChildren.getString() :
                SELECT_NODE_QUERY + "WHERE repo = ? AND node_path = ? AND depth = ?";
        nodesByPropertyQuery = ConstantValues.nodesDaoSqlSearchFilesByProperty.isSet() ?
                ConstantValues.nodesDaoSqlSearchFilesByProperty.getString() :
                "SELECT n.* FROM nodes n JOIN node_props p ON n.node_id = p.node_id WHERE repo = ? AND p.prop_key = ?";
        nodesExistsQuery = ConstantValues.nodesDaoSqlNodeExists.isSet() ?
                ConstantValues.nodesDaoSqlNodeExists.getString() :
                "SELECT count(*) FROM nodes WHERE repo = ? AND node_path = ? AND node_name = ?";
        nodesGetNodeTypeQuery = ConstantValues.nodesDaoSqlGetItemType.isSet() ?
                ConstantValues.nodesDaoSqlGetItemType.getString() :
                "SELECT node_type FROM nodes WHERE repo = ? AND node_path = ? AND node_name = ?";
    }

    @Nullable
    public Node get(NodePath path) throws SQLException {
        Node node = null;
        try (ResultSet resultSet = jdbcHelper.executeSelect(nodeByPathQuery, path.getRepo(),
                dotIfNullOrEmpty(path.getPath()), dotIfNullOrEmpty(path.getName()))) {
            if (resultSet.next()) {
                node = nodeFromResultSet(resultSet);
            }
            return node;
        }
    }

    @Nullable
    public Node get(long id) throws SQLException {
        ResultSet resultSet = null;
        Node node = null;
        try {
            resultSet = jdbcHelper.executeSelect("SELECT * FROM nodes WHERE node_id = ?", id);
            if (resultSet.next()) {
                node = nodeFromResultSet(resultSet);
            }
            return node;
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public long getNodeId(NodePath path) throws SQLException {
        try (ResultSet resultSet = jdbcHelper.executeSelect(nodeIdByPathQuery, path.getRepo(),
                dotIfNullOrEmpty(path.getPath()), dotIfNullOrEmpty(path.getName()))) {
            if (resultSet.next()) {
                return resultSet.getLong(1);
            } else {
                return DbService.NO_DB_ID;
            }
        }
    }

    public int getFileCount(String repoKey, String fileName) throws SQLException {
        return jdbcHelper.executeSelectCount("SELECT COUNT(*) FROM nodes WHERE node_type=1 and node_name = ? and repo = ?", fileName, repoKey);
    }

    public Integer getItemType(NodePath path) throws SQLException {
        ResultSet resultSet = null;
        try {
            resultSet = jdbcHelper.executeSelect(nodesGetNodeTypeQuery,
                    path.getRepo(), dotIfNullOrEmpty(path.getPath()), dotIfNullOrEmpty(path.getName()));
            if (resultSet.next()) {
                return resultSet.getInt("node_type");
            }
            return null;
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public String getNodeSha1(NodePath path) throws SQLException {
        ResultSet resultSet = null;
        String sha1 = null;
        try {
            resultSet = jdbcHelper.executeSelect("SELECT sha1_actual FROM nodes " +
                            "WHERE repo = ? AND node_path = ? AND node_name = ?",
                    path.getRepo(), dotIfNullOrEmpty(path.getPath()), dotIfNullOrEmpty(path.getName()));
            if (resultSet.next()) {
                sha1 = resultSet.getString(1);
            }
            return sha1;
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public int updateRepoPathChecksum(NodePath path) throws SQLException {
        // node id and type are not updatable
        return jdbcHelper.executeUpdate("UPDATE nodes SET repo_path_checksum = ? " +
                        "WHERE repo = ? AND node_path = ? AND node_name = ?",
                //calcUniqueKey(path.getRepo(),path.getPath(),path.getName()), path.getRepo(),
                calcUniqueKey(path.getRepo(), path.getPath(), path.getName()), path.getRepo(),
                dotIfNullOrEmpty(path.getPath()), dotIfNullOrEmpty(path.getName()));
    }

    public int create(Node node) throws SQLException {
        return jdbcHelper.executeUpdate("INSERT INTO nodes " +
                        "(node_id, node_type, repo, node_path, node_name, " +
                        "depth, created, created_by, " +
                        "modified, modified_by, updated, " +
                        "bin_length, sha1_actual, sha1_original, md5_actual, md5_original, sha256, repo_path_checksum) " +
                        "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                node.getNodeId(), booleanAsByte(node.isFile()), node.getRepo(), dotIfNullOrEmpty(node.getPath()),
                dotIfNullOrEmpty(node.getName()), node.getDepth(), node.getCreated(), node.getCreatedBy(),
                node.getModified(), node.getModifiedBy(), node.getUpdated(), node.getLength(),
                node.getSha1Actual(), node.getSha1Original(), node.getMd5Actual(), node.getMd5Original(),
                node.getSha2(), calcUniqueKey(node));
    }

    public int update(Node node) throws SQLException {
        // node id and type are not updatable
        return jdbcHelper.executeUpdate("UPDATE nodes " +
                        "SET repo = ?,  node_path = ?, node_name = ?, " +
                        "depth = ?, created = ?, created_by = ?, " +
                        "modified = ?, modified_by = ?, updated = ?, bin_length = ?, " +
                        "sha1_actual = ?, sha1_original = ?, md5_actual = ?, md5_original = ?, sha256 = ?, " +
                        "repo_path_checksum = ? " +
                        "WHERE node_id = ?",
                node.getRepo(), dotIfNullOrEmpty(node.getPath()), dotIfNullOrEmpty(node.getName()),
                node.getDepth(), node.getCreated(), node.getCreatedBy(),
                node.getModified(), node.getModifiedBy(), node.getUpdated(),
                node.getLength(), node.getSha1Actual(), node.getSha1Original(), node.getMd5Actual(),
                node.getMd5Original(), node.getSha2(), calcUniqueKey(node), node.getNodeId());
    }

    public boolean exists(NodePath path) throws SQLException {

        try (ResultSet resultSet = jdbcHelper.executeSelect(nodesExistsQuery, path.getRepo(),
                dotIfNullOrEmpty(path.getPath()), dotIfNullOrEmpty(path.getName()))) {
            int count = 0;
            if (resultSet.next()) {
                count = resultSet.getInt(1);
                if (count > 1) {
                    if (log.isDebugEnabled()) {
                        StorageException bigWarning = new StorageException(
                                "Unexpected node count for absolute path: '" + path + "' - " + count);
                        log.warn(bigWarning.getMessage(), bigWarning);
                    } else {
                        log.warn("Unexpected node count for absolute path: '{}' - {}", path, count);
                    }
                }
            }
            return count > 0;
        }
    }

    public boolean delete(long id) throws SQLException {
        int deleted = jdbcHelper.executeUpdate("DELETE FROM nodes WHERE node_id = ?", id);
        return deleted > 0;
    }

    public List<Node> getChildren(NodePath path) throws SQLException {
        ResultSet resultSet = null;
        List<Node> results = Lists.newArrayList();
        try {
            // the child path must be the path+name of the parent
            String childPath = path.getPathName();
            resultSet = jdbcHelper.executeSelect(nodeGetChildren,
                    path.getRepo(), dotIfNullOrEmpty(childPath), path.getDepth() + 1);
            while (resultSet.next()) {
                results.add(nodeFromResultSet(resultSet));
            }
            return results;
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public boolean hasChildren(NodePath path) throws SQLException {
        ResultSet resultSet = null;
        try {
            // the child path must be the path+name of the parent
            String childPath = path.getPathName();
            resultSet = jdbcHelper.executeSelect(nodeHasChildren,
                    path.getRepo(), dotIfNullOrEmpty(childPath), path.getDepth() + 1);
            if (resultSet.next()) {
                int childrenCount = resultSet.getInt(1);
                log.trace("Children count of '{}': {}", path, childrenCount);
                return childrenCount > 0;
            }
            return false;
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public List<? extends Node> getAllNodes() throws SQLException {
        if (!ConstantValues.dev.getBoolean()) {
            return Lists.newArrayList();
        }

        ResultSet resultSet = null;
        List<Node> results = Lists.newArrayList();
        try {
            resultSet = jdbcHelper.executeSelect(SELECT_NODE_QUERY);
            while (resultSet.next()) {
                results.add(nodeFromResultSet(resultSet));
            }
            return results;
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public int getFilesCount() throws SQLException {
        return jdbcHelper.executeSelectCount("SELECT COUNT(*) FROM nodes WHERE node_type = 1");
    }

    public int getFilesCount(String repoKey) throws SQLException {
        return jdbcHelper.executeSelectCount("SELECT COUNT(*) FROM nodes WHERE node_type=1 and repo = ?", repoKey);
    }

    public int getFilesCount(NodePath nodePath) throws SQLException {
        ResultSet resultSet = null;
        int result = 0;
        try {
            resultSet = jdbcHelper.executeSelect(
                    "SELECT COUNT(*) FROM nodes WHERE node_type=1 and repo = ? and depth > ? and " +
                            "(node_path = ? or node_path like ?)",
                    nodePath.getRepo(), nodePath.getDepth(), nodePath.getPathName(), nodePath.getPathName() + "/%");
            if (resultSet.next()) {
                result = resultSet.getInt(1);
            }
        } finally {
            DbUtils.close(resultSet);
        }
        return result;
    }

    public FolderSummeryNodeInfo getFilesCountAndSize(@Nonnull NodePath nodePath) throws SQLException {
        try (ResultSet resultSet = executeCountAndSizeResultSet(nodePath)) {
            if (resultSet.next()) {
                return new FolderSummeryNodeInfoImpl(resultSet.getLong(1), resultSet.getLong(2));
            } else {
                log.warn("Could not calculate artifact count and size for folder '{}' - query return no results",
                        nodePath);
                return new FolderSummeryNodeInfoImpl(0, 0);
            }
        }
    }

    // used by the sha2 migration, can probably remove in V6.0 :(
    public int getMissingSha2ArtifactCount() throws SQLException {
        return jdbcHelper.executeSelectCount("SELECT COUNT(*) FROM nodes WHERE sha256 IS NULL and node_type = 1");
    }

    public int getMissingRepoPathChecksumArtifactCount() throws SQLException {
        return jdbcHelper.executeSelectCount("SELECT COUNT(*) FROM nodes WHERE repo_path_checksum IS NULL");
    }

    // Used by the Conan V2 migration
    public int getConanV1LayoutArtifactCount() throws SQLException {
        return jdbcHelper.executeSelectCount("SELECT COUNT(*) FROM nodes WHERE depth = 6 and node_type = 1 and node_name = 'conanfile.py'");
    }

    private ResultSet executeCountAndSizeResultSet(NodePath nodePath) throws SQLException {
        if (nodePath.isRoot()) {
            return jdbcHelper.executeSelect(
                    "SELECT COUNT(*) AS file_count, SUM(bin_length) AS total_size FROM nodes WHERE node_type=1 and repo = ?",
                    nodePath.getRepo());
        } else {
            return jdbcHelper.executeSelect(
                    "SELECT COUNT(*) AS file_count, SUM(bin_length) AS total_size FROM nodes WHERE node_type=1 and repo = ? and depth > ? and " +
                            "(node_path = ? or node_path like ?)",
                    nodePath.getRepo(), nodePath.getDepth(), nodePath.getPathName(), nodePath.getPathName() + "/%");
        }
    }

    public long getFilesTotalSize(String repoKey) throws SQLException {
        return jdbcHelper.executeSelectLong("SELECT SUM(bin_length) FROM nodes WHERE node_type=1 and repo = ?",
                repoKey);
    }

    public long getFilesTotalSize(NodePath nodePath) throws SQLException {
        return jdbcHelper.executeSelectLong(
                "SELECT SUM(bin_length) FROM nodes WHERE node_type=1 and repo = ? and depth > ? and " +
                        "(node_path = ? or node_path like ?)",
                nodePath.getRepo(), nodePath.getDepth(), nodePath.getPathName(), nodePath.getPathName() + "/%");
    }

    public int getNodesCount(String repoKey) throws SQLException {
        return jdbcHelper.executeSelectCount(
                "SELECT COUNT(*) FROM nodes WHERE repo = ?", repoKey);
    }

    public int getNodesCount(NodePath nodePath) throws SQLException {
        return jdbcHelper.executeSelectCount(
                "SELECT COUNT(*) FROM nodes WHERE repo = ? and depth > ? and (node_path = ? or node_path like ?)",
                nodePath.getRepo(), nodePath.getDepth(), nodePath.getPathName(), nodePath.getPathName() + "/%");
    }

    public List<Node> searchByChecksum(ChecksumType type, String checksum) throws SQLException {
        if (!type.isValid(checksum)) {
            throw new IllegalArgumentException(
                    "Cannot search for invalid " + type.name() + " checksum value '" + checksum + "'");
        }
        //until we rename to sha1 and md5....
        String fieldName = type.equals(ChecksumType.sha256) ? "sha256" : type.name() + "_actual";
        List<Node> results = Lists.newArrayList();
        ResultSet rs = null;
        try {
            rs = jdbcHelper.executeSelect("SELECT * FROM nodes " +
                    "WHERE " + fieldName + " = ?", checksum);
            while (rs.next()) {
                results.add(nodeFromResultSet(rs));
            }
        } finally {
            DbUtils.close(rs);
        }
        return results;
    }

    public List<Node> searchBadChecksums(ChecksumType type) throws SQLException {
        String query = "SELECT * FROM nodes WHERE node_type = 1 and ";
        if (ChecksumType.sha1.equals(type)) {
            query += "((sha1_original IS NULL) or " +
                    "(sha1_actual IS NULL) or " +
                    "(sha1_original != ? and sha1_original != sha1_actual))";
        } else if (ChecksumType.sha256.equals(type)) {
            query += "sha256 IS NULL";
        } else if (ChecksumType.md5.equals(type)) {
            query += "((md5_original IS NULL) or " +
                    "(md5_actual IS NULL) or " +
                    "(md5_original != ? and md5_original != md5_actual))";
        }
        List<Node> results = Lists.newArrayList();
        ResultSet rs = null;
        try {
            //until we rename to sha1 and md5....
            if (type.equals(ChecksumType.sha256)) {
                rs = jdbcHelper.executeSelect(query);
            } else {
                rs = jdbcHelper.executeSelect(query, ChecksumInfo.TRUSTED_FILE_MARKER);
            }
            while (rs.next()) {
                results.add(nodeFromResultSet(rs));
            }
        } finally {
            DbUtils.close(rs);
        }
        return results;
    }

    //TODO: [by YS] this is a just a temp naive search for maven plugin metadata
    public List<Node> searchNodesByProperty(String repo, String propKey) throws SQLException {
        List<Node> results = new ArrayList<>();
        try (ResultSet resultSet = jdbcHelper.executeSelect(nodesByPropertyQuery, repo, propKey)) {
            while (resultSet.next()) {
                results.add(nodeFromResultSet(resultSet));
            }
        }
        return results;
    }

    /**
     * Searches for pom files two levels deep (grandchild) from the input path.
     *
     * @param path Path of the node to search grandchild poms
     * @return List of grandchild poms
     */
    public List<Node> searchGrandchildPoms(NodePath path) throws SQLException {
        ResultSet resultSet = null;
        List<Node> results = new ArrayList<>();
        try {
            resultSet = jdbcHelper.executeSelect("SELECT * FROM nodes " +
                            "WHERE repo = ? AND node_path like ? " +
                            "AND node_name like '%.pom' AND depth = ? AND node_type = 1",
                    path.getRepo(), path.getPath() + "%", path.getDepth() + 2);
            while (resultSet.next()) {
                results.add(nodeFromResultSet(resultSet));
            }
        } finally {
            DbUtils.close(resultSet);
        }
        return results;
    }

    public List<Node> getOrphanNodes(NodePath path) throws SQLException {
        ResultSet resultSet = null;
        List<Node> results = new ArrayList<>();
        try {
            resultSet = jdbcHelper.executeSelect("SELECT * FROM nodes n1" +
                            " WHERE n1.repo = ?" +
                            " AND n1.node_path like ?" +
                            " AND n1.node_name NOT IN" +
                            " (SELECT n2.node_name FROM nodes n2, nodes n3" +
                            " WHERE (n2.node_path like '%/%' AND n2.node_path like CONCAT('%/', n3.node_name))" +
                            " OR (n2.node_path not like '%/%' AND n2.node_path like CONCAT('%', n3.node_name)))",
                    path.getRepo(), emptyIfNullOrDot(path.getPath()) + "%");
            while (resultSet.next()) {
                results.add(nodeFromResultSet(resultSet));
            }
        } finally {
            DbUtils.close(resultSet);
        }
        return results;
    }

    public Set<RepoStorageSummary> getRepositoriesStorageSummary() throws SQLException {
        ResultSet resultSet = null;
        Set<RepoStorageSummary> results = Sets.newHashSet();
        try {
            resultSet = jdbcHelper.executeSelect(
                    "SELECT repo, " +
                            "SUM(CASE WHEN node_type = 0 THEN 1 ELSE 0 END) as folders, " +
                            "SUM(CASE WHEN node_type = 1 THEN 1 ELSE 0 END) as files, " +
                            "SUM(bin_length) " +
                            "FROM nodes GROUP BY repo");
            while (resultSet.next()) {
                results.add(repoSummaryFromResultSet(resultSet));
            }
        } finally {
            DbUtils.close(resultSet);
        }
        return results;
    }

    public long findLastNodeId() throws SQLException {
        ResultSet resultSet = null;
        long lastNodeId = 0;
        try {
            resultSet = jdbcHelper
                    .executeSelect("SELECT MAX(node_id) as node_id FROM nodes");
            if (resultSet.next()) {
                lastNodeId = resultSet.getLong("node_id");
            }
            return lastNodeId;
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public GCCandidate gcCandidateFromResultSet(ResultSet resultSet) {
        try {
            String repo = resultSet.getString("repo");
            String path = resultSet.getString("node_path");
            String name = resultSet.getString("node_name");
            boolean isFile = resultSet.getBoolean("node_type");
            RepoPath repoPath;
            if (StringUtils.equals(path, ".")) {
                repoPath = InternalRepoPathFactory.create(repo, name, !isFile);
            } else {
                repoPath = InternalRepoPathFactory.create(repo, path + "/" + name, !isFile);
            }
            String sha1 = resultSet.getString("sha1_actual");
            String sha2 = resultSet.getString("sha256");
            String md5 = resultSet.getString("md5_actual");
            String length = resultSet.getString("bin_length");
            return new GCCandidate(repoPath, sha1, sha2, md5, Long.valueOf(length));
        } catch (SQLException e) {
            throw new StorageException("Unable to read GC Candidate from result set", e);
        }
    }

    private Node nodeFromResultSet(ResultSet resultSet) throws SQLException {
        long nodeId = resultSet.getLong(1);
        boolean isFile = resultSet.getBoolean(2);
        String repoName = resultSet.getString(3);
        String path = emptyIfNullOrDot(resultSet.getString(4));
        String fileName = emptyIfNullOrDot(resultSet.getString(5));
        short depth = resultSet.getShort(6);
        long created = resultSet.getLong(7);
        String createdBy = resultSet.getString(8);
        long modified = resultSet.getLong(9);
        String modifiedBy = resultSet.getString(10);
        long updated = resultSet.getLong(11);
        long length = resultSet.getLong(12);
        String sha1Actual = resultSet.getString(13);
        String sha1Original = resultSet.getString(14);
        String md5Actual = resultSet.getString(15);
        String md5Original = resultSet.getString(16);
        String sha2 = resultSet.getString(17);
        return new Node(nodeId, isFile, repoName, path, fileName, depth, created, createdBy, modified, modifiedBy,
                updated, length, sha1Actual, sha1Original, md5Actual, md5Original, sha2);
    }

    private RepoStorageSummary repoSummaryFromResultSet(ResultSet rs) throws SQLException {
        // don't count the repo folder itself -> folderCount - 1
        return new RepoStorageSummary(rs.getString(1), rs.getLong(2) - 1, rs.getLong(3), rs.getLong(4));
    }

    private String calcUniqueKey(Node node) {
        return calcUniqueKey(node.getRepo(), node.getPath(), node.getName());
    }

    private String calcUniqueKey(String repo, String path, String name) {
        String key = repo + "/" + path + "/" + name;
        return DigestUtils.sha1Hex(key.getBytes(Charset.forName("UTF8")));
    }

    public boolean existsBySha1(String sha1) throws SQLException {
        ResultSet resultSet = null;
        try {
            resultSet = jdbcHelper
                    .executeSelect("SELECT node_id FROM nodes WHERE sha1_actual = ?", sha1);
            return resultSet.next();
        } finally {
            DbUtils.close(resultSet);
        }
    }
}
