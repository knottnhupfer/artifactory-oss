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

package org.artifactory.storage.db.bundle.dao;

import com.google.common.collect.Lists;
import lombok.NonNull;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.release.bundle.ReleaseBundleSearchFilter;
import org.artifactory.api.rest.distribution.bundle.utils.BundleNameVersionResolver;
import org.artifactory.bundle.BundleTransactionStatus;
import org.artifactory.bundle.BundleType;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.db.bundle.model.BundleNode;
import org.artifactory.storage.db.bundle.model.DBArtifactsBundle;
import org.artifactory.storage.db.bundle.model.DBBundleResult;
import org.artifactory.storage.db.util.BaseDao;
import org.artifactory.storage.db.util.JdbcHelper;
import org.artifactory.storage.db.util.querybuilder.ArtifactoryQueryWriter;
import org.jfrog.common.ClockUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author Tomer Mayost
 */
@Repository
public class ArtifactBundlesDao extends BaseDao {
    private static final String ARTIFACT_BUNDLES_TABLE = "artifact_bundles";
    private static final String BUNDLE_FILES_TABLE = "bundle_files";
    private static final String EXPIRED_BUNDLES_QUERY = "SELECT id FROM " + ARTIFACT_BUNDLES_TABLE + " where status != '" +
            BundleTransactionStatus.COMPLETE.name() + "' and date_created < ?";

    private static final String UPDATE_BUNDLES_CREATION_DATE_QUERY = "UPDATE " + ARTIFACT_BUNDLES_TABLE + " SET date_created = ? where " +
            "id = ? and status = '" + BundleTransactionStatus.INPROGRESS.name() + "'";

    private DbService dbService;

    @Autowired
    public ArtifactBundlesDao(JdbcHelper jdbcHelper, DbService dbService) {
        super(jdbcHelper);
        this.dbService = dbService;
    }

    public int create(DBArtifactsBundle bundle) throws SQLException {
        bundle.setId(Optional.ofNullable(nullIfZero(bundle.getId()))
                .orElseGet(dbService::nextId));
        bundle.validate();
        String storingRepo = BundleType.TARGET.equals(bundle.getType()) ? "release-bundles" : bundle.getStoringRepo();
        return jdbcHelper.executeUpdate("INSERT INTO " + ARTIFACT_BUNDLES_TABLE +
                        " (id, name, version, status, date_created, signature, type, storing_repo) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                bundle.getId(), bundle.getName(), bundle.getVersion(), bundle.getStatus().name(),
                bundle.getDateCreated().getMillis(), bundle.getSignature(), bundle.getType().name(),
                storingRepo);
    }

    public void deleteBundleNodes(long bundleId) throws SQLException {
        jdbcHelper.executeUpdate("DELETE FROM " + BUNDLE_FILES_TABLE + " WHERE bundle_id = ?", bundleId);
    }

    public boolean deleteArtifactsBundle(long id) throws SQLException {
        int deleted = jdbcHelper.executeUpdate("DELETE FROM " + ARTIFACT_BUNDLES_TABLE + " WHERE id = ?", id);
        return deleted > 0;
    }

    public boolean deleteAllArtifactsBundles() throws SQLException {
        return jdbcHelper.executeUpdate("DELETE FROM " + ARTIFACT_BUNDLES_TABLE) > 0;
    }

    public boolean deleteAllBundleNodes() throws SQLException {
        return jdbcHelper.executeUpdate("DELETE FROM " + BUNDLE_FILES_TABLE) > 0;
    }

    public int create(BundleNode bundleNode) throws SQLException {
        bundleNode.setId(Optional.ofNullable(nullIfZero(bundleNode.getId())).orElseGet(dbService::nextId));
        bundleNode.validate();
        return jdbcHelper.executeUpdate(
                "INSERT INTO " + BUNDLE_FILES_TABLE + " (id, node_id, bundle_id, repo_path, original_component_details) VALUES (?, ?, ?, ?, ?)",
                bundleNode.getId(), bundleNode.getNodeId(),
                bundleNode.getBundleId(), bundleNode.getRepoPath(), bundleNode.getOriginalFileDetails());
    }

    public void closeTransaction(String transactionPath) throws SQLException {
        BundleNameVersionResolver bundleNameVersion = new BundleNameVersionResolver(transactionPath);
        completeBundle(bundleNameVersion.getBundleName(), bundleNameVersion.getBundleVersion(), BundleType.TARGET);
    }

    public int setBundleStatus(@NonNull String bundleName, @NonNull String bundleVersion, @NonNull BundleType type,
            @NonNull BundleTransactionStatus status) throws SQLException {

        return jdbcHelper.executeUpdate(
                "UPDATE " + ARTIFACT_BUNDLES_TABLE + " SET status = ? WHERE name = ? AND version = ? AND type = ?",
                status.name(), bundleName, bundleVersion, type.name());
    }

    public int failBundle( @NonNull String bundleName,  @NonNull String bundleVersion, @NonNull BundleType type) throws SQLException {
        return setBundleStatus(bundleName, bundleVersion, type, BundleTransactionStatus.FAILED);
    }


    public int completeBundle(@NonNull String bundleName, @NonNull String bundleVersion, @NonNull BundleType type) throws SQLException {
        return setBundleStatus(bundleName, bundleVersion, type, BundleTransactionStatus.COMPLETE);
    }

    public DBArtifactsBundle getArtifactsBundle(String bundleName, String bundleVersion, BundleType type)
            throws SQLException {
        DBArtifactsBundle artifactsBundle = null;
        String query = "SELECT * FROM " + ARTIFACT_BUNDLES_TABLE + " WHERE name = ? AND version = ? AND type = ?";
        try (ResultSet resultSet = jdbcHelper.executeSelect(query, bundleName, bundleVersion, type.name())) {
            if (resultSet.next()) {
                artifactsBundle = artifactsBundleFromResultSet(resultSet, "*");
            }
        }
        return artifactsBundle;
    }

    public DBArtifactsBundle getArtifactsBundle(long id) throws SQLException {
        try (ResultSet resultSet = jdbcHelper.executeSelect("SELECT * FROM " + ARTIFACT_BUNDLES_TABLE + " WHERE id = ?", id)) {
            if (resultSet.next()) {
                return artifactsBundleFromResultSet(resultSet, "*");
            }
        }
        return null;
    }

    public DBArtifactsBundle getArtifactsBundleStatus(String bundleName, String bundleVersion, BundleType bundleType) throws SQLException {
        String query = "SELECT status, storing_repo FROM " + ARTIFACT_BUNDLES_TABLE + " WHERE name = ? AND version = ? AND type = ?";
        try (ResultSet resultSet = jdbcHelper.executeSelect(query, bundleName, bundleVersion, bundleType.name())) {
            if (resultSet.next()) {
                return artifactsBundleFromResultSet(resultSet, "status", "storing_repo");
            }
        }
        return null;
    }

    public List<DBArtifactsBundle> getArtifactsBundles(String bundleName, BundleType bundleType) throws SQLException {
        List<DBArtifactsBundle> artifactsBundles = Lists.newLinkedList();
        String query = "SELECT * FROM " + ARTIFACT_BUNDLES_TABLE + " WHERE name = ? AND type = ?";
        try (ResultSet resultSet = jdbcHelper.executeSelect(query, bundleName, bundleType.name())) {
            while (resultSet.next()) {
                artifactsBundles.add(artifactsBundleFromResultSet(resultSet, "*"));
            }
        }
        return artifactsBundles;
    }

    public List<DBArtifactsBundle> getAllArtifactsBundles(BundleType bundleType) throws SQLException {
        List<DBArtifactsBundle> artifactsBundles = Lists.newLinkedList();
        String query = "SELECT name, version, status, date_created, storing_repo FROM " + ARTIFACT_BUNDLES_TABLE + " WHERE type = ?";
        try (ResultSet resultSet = jdbcHelper.executeSelect(query, bundleType.name())) {
            while (resultSet.next()) {
                artifactsBundles.add(artifactsBundleFromResultSet(resultSet, "name", "version", "status", "date_created", "storing_repo"));
            }
        }
        return artifactsBundles;
    }

    /**
     * @return The latest version (according to creation date) of every completed bundle. In case there is more than one
     * latest version, returns the one with highest alphabetic order
     */
    public List<DBArtifactsBundle> getCompletedBundlesLastVersion(BundleType bundleType) throws SQLException {
        List<DBArtifactsBundle> artifactsBundles = Lists.newLinkedList();
        String query = "SELECT a1.name, max(a1.version) as version, a1.date_created FROM " + ARTIFACT_BUNDLES_TABLE + " a1 join" +
                " ( SELECT name, max(date_created) as date_created FROM " + ARTIFACT_BUNDLES_TABLE +
                "   WHERE status = '" + BundleTransactionStatus.COMPLETE.name() + "' AND type = ? GROUP BY name" +
                " ) a2" +
                " on a1.name = a2.name AND a1.date_created = a2.date_created" +
                " WHERE status = '" + BundleTransactionStatus.COMPLETE.name() + "' AND type = ? GROUP BY a1.name, a1.date_created";
        try (ResultSet resultSet = jdbcHelper.executeSelect(query, bundleType.name(), bundleType.name())) {
            while (resultSet.next()) {
                artifactsBundles.add(artifactsBundleFromResultSet(resultSet, "name", "version", "date_created"));
            }
        }
        return artifactsBundles;
    }

    /**
     * @return All release-bundles with {@link BundleTransactionStatus#COMPLETE} status, regardless of their type (target/source)
     */
    public List<DBBundleResult> getAllCompletedBundles() throws SQLException {
        List<DBBundleResult> bundles = Lists.newArrayList();
        String query = "SELECT name, storing_repo FROM " + ARTIFACT_BUNDLES_TABLE + " " +
                "WHERE status = '" + BundleTransactionStatus.COMPLETE.name() + "' GROUP BY name, storing_repo ORDER BY name";
        try (ResultSet resultSet = jdbcHelper.executeSelect(query)) {
            while (resultSet.next()) {
                bundles.add(populateDbBundleResult(resultSet));
            }
        }
        return bundles;
    }

    private DBBundleResult populateDbBundleResult(ResultSet resultSet) throws SQLException {
        return new DBBundleResult(resultSet.getString("name"), resultSet.getString("storing_repo"));
    }

    /**
     * @return The latest version (according to creation date) of every completed bundle. In case there is more than one
     * latest version, returns the one with highest alphabetic order
     */
    public List<DBArtifactsBundle> getFilteredBundlesLastVersion(ReleaseBundleSearchFilter filter) throws SQLException {
        List<DBArtifactsBundle> artifactsBundles = Lists.newLinkedList();
        String select = " a1.name, max(a1.version) as version, a1.date_created , a1.storing_repo ";

        String from = ARTIFACT_BUNDLES_TABLE  +
                " a1 join " +
                " ( SELECT name, max(date_created) as date_created FROM " + ARTIFACT_BUNDLES_TABLE +
                "   WHERE status = '" + BundleTransactionStatus.COMPLETE.name() +"'  AND type = ? GROUP BY name " +
                " ) a2 " +
                " on a1.name = a2.name AND a1.date_created = a2.date_created ";

        String where = " status = '" + BundleTransactionStatus.COMPLETE.name() + "' " +
                " AND type = ? " + addToWhereStatement(filter);
        String groupBy = " a1.name, a1.date_created , a1.storing_repo ";
        String orderby = addOrderBy(filter);

        String query = new ArtifactoryQueryWriter()
                .select(select)
                .from(from)
                .where(where)
                .groupBy(groupBy)
                .orderBy(orderby)
                .limit(filter.getDaoLimit())
                .offset(filter.getOffset())
                .build();

        Object[] params = setQueryParams(filter).toArray();
        try (ResultSet resultSet = jdbcHelper.executeSelect(query, params)) {
            while (resultSet.next()) {
                artifactsBundles.add(artifactsBundleFromResultSet(resultSet, "name", "version", "date_created", "storing_repo"));
            }
        }
        return artifactsBundles;
    }

    private ArrayList<Object> setQueryParams(ReleaseBundleSearchFilter filter) {
        ArrayList<Object> params = Lists.newArrayList();
        params.add(filter.getBundleType().name());
        params.add(filter.getBundleType().name());
        String name = filter.getName();
        if (StringUtils.isNotBlank(name)) {
            if (name.contains("*")) {
                name = name.replace("*", "%");
            }
            if (name.contains("?")) {
                name = name.replace("?", "_");
            }
            params.add("%" + name + "%");
        }
        if (filter.getAfter() != 0) {
            params.add(filter.getAfter());
        }
        if (filter.getBefore() != 0) {
            params.add(filter.getBefore());
        }
        return params;
    }

    private String addToWhereStatement(ReleaseBundleSearchFilter filter) {
        String query = " ";
        if (StringUtils.isNotBlank(filter.getName())) {
            query += " AND a1.name like ? ";
        }
        if (filter.getAfter() != 0) {
            query += " AND a1.date_created > ? ";

        }
        if (filter.getBefore() != 0) {
            query += " AND a1.date_created < ? ";
        }
        return query;
    }

    private String addOrderBy(ReleaseBundleSearchFilter filter) {
        String query = " ";
        query += StringUtils.isNotBlank(filter.getOrderBy()) ? filter.getOrderBy() + " " : " date_created ";
        query += StringUtils.isNotBlank(filter.getDirection()) ? filter.getDirection() + " " : " desc ";
        return query;
    }

    public List<BundleNode> getAllBundleNodes() throws SQLException {
        List<BundleNode> bundleNodes = Lists.newLinkedList();
        String query = "SELECT * FROM " + BUNDLE_FILES_TABLE;
        try (ResultSet resultSet = jdbcHelper.executeSelect(query)) {
            while (resultSet.next()) {
                bundleNodes.add(bundleNodeFromResultSet(resultSet));
            }
        }
        return bundleNodes;
    }

    public List<BundleNode> getBundleNodes(long bundleId) throws SQLException {
        List<BundleNode> bundleNodes = Lists.newLinkedList();
        String query = "SELECT * FROM " + BUNDLE_FILES_TABLE + " WHERE bundle_id = ?";
        try (ResultSet resultSet = jdbcHelper.executeSelect(query, bundleId)) {
            while (resultSet.next()) {
                bundleNodes.add(bundleNodeFromResultSet(resultSet));
            }
        }
        return bundleNodes;
    }

    public boolean isRepoPathRelatedToBundle(String path) throws SQLException {
        String query = new ArtifactoryQueryWriter()
                .select("repo_path")
                .from(BUNDLE_FILES_TABLE)
                .where("repo_path = ?")
                .limit(1L)
                .build();
        try (ResultSet resultSet = jdbcHelper.executeSelect(query, path)) {
           if (resultSet.next()) {
               return true;
           }
        }
        return false;
    }


    public boolean isDirectoryRelatedToBundle(String path) throws SQLException {
        String query = new ArtifactoryQueryWriter()
                .select("repo_path")
                .from(BUNDLE_FILES_TABLE)
                .where("repo_path like ?")
                .limit(1L)
                .build();
        try (ResultSet resultSet = jdbcHelper.executeSelect(query, appendTrailingSlash(path) + "%")) {
            if (resultSet.next()) {
                return true;
            }
        }
        return false;
    }

    private String appendTrailingSlash(String in) {
        if (in.endsWith("/")) {
            return in;
        }
        return in + "/";
    }

    @Nonnull
    public List<Long> getExpiredBundlesIds(Long cleanupPeriod) throws SQLException {
        List<Long> ids = new LinkedList<>();
        long expiryDate = ClockUtils.epochMillis() - TimeUnit.HOURS.toMillis(cleanupPeriod);
        try (ResultSet resultSet = jdbcHelper.executeSelect(EXPIRED_BUNDLES_QUERY, expiryDate)) {
            while (resultSet.next()) {
                ids.add(resultSet.getLong("id"));
            }
        }
        return ids;
    }

    public String getStoringRepo(String name, String version, BundleType type) throws SQLException {
        String query =
                "SELECT storing_repo FROM " + ARTIFACT_BUNDLES_TABLE + " WHERE name = ? AND version = ? AND type = ?";
        try (ResultSet resultSet = jdbcHelper.executeSelect(query, name, version, type.name())) {
            if (resultSet.next()) {
                return resultSet.getString(1);
            }
        }
        return null;
    }

    public List<Long> getAllBundlesRelatedToNode(long nodeId) throws SQLException {
        String query = "SELECT bundle_id FROM " + BUNDLE_FILES_TABLE + " WHERE node_id = ? ";
        List<Long> bundleIds = Lists.newArrayList();
        try (ResultSet resultSet = jdbcHelper.executeSelect(query, nodeId)) {
            while (resultSet.next()) {
                bundleIds.add(resultSet.getLong("bundle_id"));
            }
        }
        return bundleIds;
    }

    public int updateArtifactsBundleCreationDate(Long id) throws SQLException {
        return jdbcHelper.executeUpdate(UPDATE_BUNDLES_CREATION_DATE_QUERY, ClockUtils.epochMillis(), id);
    }

    private DBArtifactsBundle artifactsBundleFromResultSet(ResultSet resultSet, String... columns) throws SQLException {
        if ("*".equals(columns[0])) {
            return artifactsBundleFromResultSet(resultSet, "id", "name", "version", "status", "date_created", "signature", "type", "storing_repo");
        }
        DBArtifactsBundle artifactsBundle = new DBArtifactsBundle();
        for (String column : columns) {
            switch (column) {
                case "id":
                    artifactsBundle.setId(resultSet.getLong(column));
                    break;
                case "name":
                    artifactsBundle.setName(resultSet.getString(column));
                    break;
                case "version":
                    artifactsBundle.setVersion(resultSet.getString(column));
                    break;
                case "status":
                    artifactsBundle.setStatus(BundleTransactionStatus.valueOf(resultSet.getString(column)));
                    break;
                case "date_created":
                    artifactsBundle.setDateCreated(new DateTime(resultSet.getLong(column)));
                    break;
                case "signature":
                    artifactsBundle.setSignature(resultSet.getString(column));
                    break;
                case "type":
                    String type = resultSet.getString(column);
                    artifactsBundle.setType(type != null ? BundleType.valueOf(type) : BundleType.TARGET);
                    break;
                case "storing_repo":
                    artifactsBundle.setStoringRepo(resultSet.getString(column));
                    break;
                default:
            }
        }
        return artifactsBundle;
    }

    private BundleNode bundleNodeFromResultSet(ResultSet resultSet) throws SQLException {
        BundleNode bundleNode = new BundleNode();
        bundleNode.setId(resultSet.getLong(1));
        bundleNode.setNodeId(resultSet.getLong(2));
        bundleNode.setBundleId(resultSet.getLong(3));
        bundleNode.setRepoPath(resultSet.getString(4));
        bundleNode.setOriginalFileDetails(resultSet.getString(5));
        return bundleNode;
    }

    /**
     * Get original bundle node component details json of the source artifact
     *
     * @param repoPathOfBundleNode path of the artifact in the release bundle repo
     * @return json string representing ComponentDetails model for original artifacts details
     */
    public String getComponentDetails(String repoPathOfBundleNode) throws SQLException {
        String query = "SELECT original_component_details FROM " + BUNDLE_FILES_TABLE + " WHERE repo_path = ?";
        try (ResultSet resultSet = jdbcHelper.executeSelect(query, repoPathOfBundleNode)) {
            if (resultSet.next()) {
                return resultSet.getString(1);
            }
            return null;
        }
    }

}
