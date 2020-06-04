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

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import org.apache.commons.io.IOUtils;
import org.artifactory.bundle.BundleTransactionStatus;
import org.artifactory.bundle.BundleType;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.db.bundle.model.DBBundleBlobsResult;
import org.artifactory.storage.db.util.BaseDao;
import org.artifactory.storage.db.util.JdbcHelper;
import org.jfrog.security.util.Pair;
import org.jfrog.storage.util.DbUtils;
import org.jfrog.storage.wrapper.BlobWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * @author Tomer Mayost
 */
@Repository
public class BundleBlobsDao extends BaseDao {

    private DbService dbService;

    @Autowired
    public BundleBlobsDao(JdbcHelper jdbcHelper, DbService dbService) {
        super(jdbcHelper);
        this.dbService = dbService;
    }

    public int create(String data, long bundleId) throws SQLException {
        BlobWrapper blobWrapper = new BlobWrapper(data);
        long id = dbService.nextId();
        return jdbcHelper.executeUpdate("INSERT INTO bundle_blobs (id, data, bundle_id) VALUES (?, ?, ?)", id,
                blobWrapper, bundleId);
    }

    public int create(InputStream data, long bundleId) throws SQLException {
        BlobWrapper blobWrapper = new BlobWrapper(data);
        long id = dbService.nextId();
        return jdbcHelper.executeUpdate("INSERT INTO bundle_blobs (id, data, bundle_id) VALUES (?, ?, ?)", id,
                blobWrapper, bundleId);
    }

    public DBBundleBlobsResult getBundleBlobData(String bundleName, String bundleVersion, BundleType bundleType)
            throws SQLException {
        try (ResultSet resultSet = jdbcHelper.executeSelect(
                "SELECT bundle_blobs.data, artifact_bundles.storing_repo " +
                        "FROM bundle_blobs " +
                        "JOIN artifact_bundles on artifact_bundles.id = bundle_blobs.bundle_id " +
                        "WHERE name = ? AND version = ? AND type = ?", bundleName, bundleVersion, bundleType.name())) {
            if (!resultSet.next()) {
                return null;
            }
            InputStream binaryStream = resultSet.getBinaryStream("data");
            String data = IOUtils.toString(binaryStream, Charsets.UTF_8.name());
            String storingRepo = resultSet.getString("storing_repo");
            return new DBBundleBlobsResult(storingRepo, data);
        } catch (IOException e) {
            throw new SQLException(
                    "Failed to read bundle blob for bundle  '" + bundleName + "/" + bundleVersion + "' due to: " +
                            e.getMessage(), e);
        }
    }

    public String getBundleBlobData(long bundleId) throws SQLException {
        ResultSet resultSet = null;
        try {
            resultSet = jdbcHelper.executeSelect("SELECT data FROM bundle_blobs WHERE bundle_id = ?", bundleId);
            if (resultSet.next()) {
                InputStream binaryStream = resultSet.getBinaryStream(1);
                return IOUtils.toString(binaryStream, Charsets.UTF_8.name());
            }
            return null;
        } catch (IOException e) {
            throw new SQLException(
                    "Failed to read bundle blob for bundle with id '" + bundleId + "' due to: " + e.getMessage(), e);
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public String getBundleBlobDataById(long id) throws SQLException {
        ResultSet resultSet = null;
        try {
            resultSet = jdbcHelper.executeSelect("SELECT data FROM bundle_blobs WHERE id = ?", id);
            if (resultSet.next()) {
                try(InputStream binaryStream = resultSet.getBinaryStream(1)) {
                    return IOUtils.toString(binaryStream, Charsets.UTF_8.name());
                }
            }
            return null;
        } catch (IOException e) {
            throw new SQLException(
                    "Failed to read bundle blob with id '" + id + "' due to: " + e.getMessage(), e);
        } finally {
            DbUtils.close(resultSet);
        }
    }

    /**
     * return a map of BundleType (outer key) to map of blob id (inner map key) to pair of <bundle name>_<bundle version> (value)
     */
    public  Map<BundleType, Map<Long, Pair<String,String>>> getAllBlobIdsForExport() throws SQLException {
        ResultSet resultSet = null;
        Map<BundleType, Map<Long, Pair<String,String>>> blobIds = Maps.newHashMap();
        try {
            resultSet = jdbcHelper.executeSelect(
                    "SELECT bundle_blobs.id, artifact_bundles.name, artifact_bundles.version, artifact_bundles.type FROM bundle_blobs, artifact_bundles" +
                            " WHERE artifact_bundles.id = bundle_blobs.bundle_id AND artifact_bundles.status = ?",
                    BundleTransactionStatus.COMPLETE.name());
            while (resultSet.next()) {
                long blobId = resultSet.getLong(1);
                Pair<String, String> bundleNameVersion = new Pair<>(resultSet.getString(2), resultSet.getString(3));
                BundleType type = BundleType.valueOf(resultSet.getString(4));
                blobIds.putIfAbsent(type, Maps.newHashMap());
                blobIds.get(type).put(blobId, bundleNameVersion);
            }
        } finally {
            DbUtils.close(resultSet);
        }
        return blobIds;
    }

    public boolean deleteBlob(long bundleId) throws SQLException {
        int deleted = jdbcHelper.executeUpdate("DELETE FROM bundle_blobs WHERE bundle_id = ?", bundleId);
        return deleted > 0;
    }

    public boolean deleteAllBlobs() throws SQLException {
        return jdbcHelper.executeUpdate("DELETE FROM bundle_blobs") > 0;
    }
}
