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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.artifactory.common.ConstantValues;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.storage.GCCandidate;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.db.binstore.service.garbage.TrashUtil;
import org.artifactory.storage.db.fs.entity.NodeProperty;
import org.artifactory.storage.db.util.BaseDao;
import org.artifactory.storage.db.util.JdbcHelper;
import org.artifactory.storage.db.util.querybuilder.ArtifactorySqlServerQueryBuilder;
import org.jfrog.storage.DbType;
import org.jfrog.storage.util.DbUtils;
import org.jfrog.storage.util.querybuilder.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * A data access object for the properties table.
 *
 * @author Yossi Shaul
 */
@Repository
public class PropertiesDao extends BaseDao {
    private static final Logger log = LoggerFactory.getLogger(PropertiesDao.class);
    private static final int PROP_VALUE_MAX_SIZE = 4000;
    private final String DELETE_BASE_SQL_TRASHCAN_CLEANUP =
            "SELECT n.repo, \n" +
                    "       n.node_path, \n" +
                    "       n.node_name, \n" +
                    "       n.sha1_actual, \n" +
                    "       n.sha256, \n" +
                    "       n.md5_actual, \n" +
                    "       n.bin_length, \n" +
                    "       n.node_type \n" +
                    "FROM   nodes n \n" +
                    "       LEFT OUTER JOIN node_props np100 \n" +
                    "                    ON np100.node_id = n.node_id \n" +
                    "WHERE  np100.prop_key = 'trash.time' \n" +
                    "       AND prop_value < ? \n" +
                    "       AND n.node_type = 1 ";

    @Autowired
    private DbService dbService;

    @Autowired
    public PropertiesDao(JdbcHelper jdbcHelper) {
        super(jdbcHelper);
    }

    public boolean hasNodeProperties(long nodeId) throws SQLException {
        ResultSet resultSet = null;
        try {
            resultSet = jdbcHelper.executeSelect("SELECT COUNT(1) FROM node_props WHERE node_id = ?", nodeId);
            if (resultSet.next()) {
                int propsCount = resultSet.getInt(1);
                return propsCount > 0;
            }
            return false;
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public List<NodeProperty> getNodeProperties(long nodeId) throws SQLException {
        ResultSet resultSet = null;
        List<NodeProperty> results = Lists.newArrayList();
        try {
            // the child path must be the path+name of the parent
            resultSet = jdbcHelper.executeSelect("SELECT * FROM node_props WHERE node_id = ?", nodeId);
            while (resultSet.next()) {
                results.add(propertyFromResultSet(resultSet));
            }
            return results;
        } finally {
            DbUtils.close(resultSet);
        }
    }

    /**
     * This query is meant to return all properties of nodes that have property key {@param propKey} and values that are
     * in {@param propValues} - for instance, give me all properties of all artifacts that have property bower.name
     * with values 'jquery' or 'bootstrap'. results are limited to artifact that reside in {@param repo}
     * @throws SQLException
     */
    public Multimap<Long, NodeProperty> getNodesProperties(String repo, String propKey, List<String> propValues) throws SQLException {
        Multimap<Long, NodeProperty> results = HashMultimap.create();
        if (CollectionUtils.isEmpty(propValues)) {
            return results;
        }
        // Oracle limits the max elements in the IN clause to 1000. Lists bigger than max chunk value are done in multiple queries
        final int CHUNK = ConstantValues.propertiesSearchChunkSize.getInt();
        // split to chunks of no more than CHUNK
        for (int i = 0; i < propValues.size(); i += CHUNK) {
            int chunkMaxIndex = Math.min(i + CHUNK, propValues.size());
            List<String> chunk = propValues.subList(i, chunkMaxIndex);
            String allPropsQuery =
                    "SELECT p1.prop_id, p1.node_id, p1.prop_key, p1.prop_value " +
                            "FROM node_props p INNER JOIN node_props p1 ON p.node_id = p1.node_id INNER JOIN nodes n " +
                            "ON n.node_id = p.node_id " +
                            "WHERE n.node_type = 1 AND n.repo = ? AND p.prop_key = ? AND p.prop_value IN (#)";
            try (ResultSet resultSet = jdbcHelper.executeSelect(allPropsQuery, repo, propKey, chunk)) {
                while (resultSet.next()) {
                    NodeProperty nodeProperty = propertyFromResultSet(resultSet);
                    results.put(nodeProperty.getNodeId(), nodeProperty);
                }
            }
        }
        return results;
    }

    /**
     * This query is meant to return all property values of key {@param propKey} for the nodeid in {@param nodeIdList}
     * for instance, give me all properties values of artifacts 221,432123,343434 that have property bower.name
     * @throws SQLException
     */
    public Map<Long, Set<String>> getNodesProperties(List<Long> nodeIdList, String propKey) throws SQLException {
        Map<Long, Set<String>> results = new HashMap<>();
        if (CollectionUtils.isEmpty(nodeIdList)) {
            return results;
        }
        final int CHUNK = ConstantValues.propertiesSearchChunkSize.getInt();
        for (int i = 0; i < nodeIdList.size(); i += CHUNK) {
            int chunkMaxIndex = Math.min(i + CHUNK, nodeIdList.size());
            List<Long> chunk = nodeIdList.subList(i, chunkMaxIndex);
            String allPropsQuery =
                    "SELECT node_id, prop_value from node_props where prop_key = ? and node_props.node_id IN (#)";
            try (ResultSet resultSet = jdbcHelper.executeSelect(allPropsQuery, propKey, chunk)) {
                while (resultSet.next()) {
                    long nodeId = resultSet.getLong(1);
                    String propValue = emptyIfNull(resultSet.getString(2));
                    results.putIfAbsent(nodeId, new HashSet<>());
                    results.get(nodeId).add(propValue);
                }
            }
        }
        return results;
    }

    /**
     * Return properties that are longer than allowed. Current implementation is for PostgreSQL only.
     */
    public List<NodeProperty> getPostgresPropValuesLongerThanAllowed() throws SQLException {
        List<NodeProperty> results = Lists.newArrayList();
        // a state where old property values which are longer than the current max allowed size can only be with
        // PostgreSQL and the reason for that is because we have changed the indexes and removed the substring, and
        // PostgreSQL limit the index size.
        if (isPostgresDb()) {
            int propValueMaxSize = getDbIndexedValueMaxSize(PROP_VALUE_MAX_SIZE);
            String propsLongerThanQuery = "SELECT prop_id, node_id, prop_key, prop_value FROM node_props WHERE length(prop_value) > ?";
            try (ResultSet resultSet = jdbcHelper.executeSelect(propsLongerThanQuery, propValueMaxSize)) {
                while (resultSet.next()) {
                    NodeProperty nodeProperty = propertyFromResultSet(resultSet);
                    results.add(nodeProperty);
                }
            }
        }
        return results;
    }

    /**
     * Trip property values size to match the max allowed value size. Trimming the values on PostgreSQL only.
     *
     * @return num of rows affected
     */
    public int trimPostgresPropValuesToMaxAllowedLength() throws SQLException {
        if (isPostgresDb()) {
            int maxAllowedLength = getDbIndexedValueMaxSize(PROP_VALUE_MAX_SIZE);
            return jdbcHelper.executeUpdate(
                    "UPDATE node_props SET prop_value=SUBSTR(prop_value, 1, ?) WHERE LENGTH(prop_value) > ?",
                    maxAllowedLength, maxAllowedLength);
        }
        return 0;
    }

    public List<GCCandidate> getGCCandidatesFromTrash(String validFrom) throws SQLException {
        ResultSet resultSet = null;
        List<GCCandidate> results = Lists.newArrayList();

        int batchSize = ConstantValues.trashcanMaxSearchResults.getInt();
        try {
            resultSet = executeBatchQuery(validFrom, batchSize);
            while (resultSet.next()) {
                GCCandidate gcCandidate = gcCandidateFromResultSet(resultSet);
                if (TrashUtil.isTrashItem(gcCandidate)) {
                    results.add(gcCandidate);
                }
            }
            return results;
        } finally {
            DbUtils.close(resultSet);
        }
    }

    private ResultSet executeBatchQuery(String validFrom, int batchSize) throws SQLException {
        return jdbcHelper.executeSelect(getGCCandidateQuery(batchSize), validFrom);
    }

    private String getGCCandidateQuery(int batchSize) {
        DbType dbType = dbService.getDatabaseType();
        String query = DELETE_BASE_SQL_TRASHCAN_CLEANUP;
        switch (dbType) {
            case ORACLE:
                query = "SELECT * FROM (" + DELETE_BASE_SQL_TRASHCAN_CLEANUP + ")";
                query += " WHERE rownum <= " + batchSize;
                break;
            case DERBY:
                DerbyQueryBuilder derbyBuilder = new DerbyQueryBuilder();
                query = derbyBuilder.uniqueBuild(query, null, 0, batchSize);
                break;
            case MSSQL:
                ArtifactorySqlServerQueryBuilder mssqlBuilder = new ArtifactorySqlServerQueryBuilder();
                query = mssqlBuilder.uniqueBuild(query, null, 0, batchSize);
                break;
            case POSTGRESQL:
                PostgresqlQueryBuilder psqlBuilder = new PostgresqlQueryBuilder();
                query = psqlBuilder.uniqueBuild(query, null, 0, batchSize);
                break;
            case MARIADB:
            case MYSQL:
                MysqlQueryBuilder mysqlBuilder = new MysqlQueryBuilder();
                query = mysqlBuilder.uniqueBuild(query, null, 0, batchSize);
                break;
        }
        return query;
    }

    public static RepoPath repoPathFromResultSet(ResultSet resultSet) throws SQLException {
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
        return repoPath;
    }

    public static GCCandidate gcCandidateFromResultSet(ResultSet resultSet) throws SQLException {
        RepoPath repoPath = repoPathFromResultSet(resultSet);
        String sha1 = resultSet.getString("sha1_actual");
        String sha2 = resultSet.getString("sha256");
        String md5 = resultSet.getString("md5_actual");
        String length = resultSet.getString("bin_length");

        return new GCCandidate(repoPath, sha1, sha2, md5, Long.valueOf(length));
    }

    public int deleteNodeProperties(long nodeId) throws SQLException {
        return jdbcHelper.executeUpdate("DELETE FROM node_props WHERE node_id = ?", nodeId);
    }

    public int delete(NodeProperty property) throws SQLException {
        return jdbcHelper.executeUpdate("DELETE FROM node_props WHERE prop_id = ? AND prop_key = ?",
                property.getPropId(), property.getPropKey());
    }

    public int updateValue(NodeProperty property) throws SQLException {
        String propValue = getPropertyValueEnforceLength(property);
        return jdbcHelper.executeUpdate("UPDATE node_props SET prop_value = ? WHERE prop_id = ? AND prop_key = ?",
                propValue, property.getPropId(), property.getPropKey());
    }

    public int create(NodeProperty property) throws SQLException {
        String propValue = getPropertyValueEnforceLength(property);
        return jdbcHelper.executeUpdate("INSERT INTO node_props " +
                        "(prop_id, node_id, prop_key, prop_value) " +
                        "VALUES(?, ?, ?, ?)",
                property.getPropId(), property.getNodeId(), property.getPropKey(), propValue);
    }

    private boolean isPostgresDb() {
        DbType dbType = dbService.getDatabaseType();
        return dbType == DbType.POSTGRESQL;
    }

    private String getPropertyValueEnforceLength(NodeProperty property) {
        String propValue = nullIfEmpty(property.getPropValue());
        int maxPropValue = getDbIndexedValueMaxSize(PROP_VALUE_MAX_SIZE);
        if (propValue != null && propValue.length() > maxPropValue) {
            log.info("Trimming property value to {} characters '{}'", maxPropValue,property.getPropKey());
            log.debug("Trimming property value to {} characters {}: {}", maxPropValue, property.getPropKey(), property.getPropValue());
            propValue = StringUtils.substring(propValue, 0, maxPropValue);
        }
        return propValue;
    }

    private NodeProperty propertyFromResultSet(ResultSet resultSet) throws SQLException {
        long propId = resultSet.getLong(1);
        long nodeId = resultSet.getLong(2);
        String propKey = resultSet.getString(3);
        String propValue = emptyIfNull(resultSet.getString(4));
        return new NodeProperty(propId, nodeId, propKey, propValue);
    }
}
