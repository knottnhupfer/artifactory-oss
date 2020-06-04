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

package org.artifactory.storage.db.binstore.dao;

import com.google.common.collect.Lists;
import org.artifactory.api.storage.BinariesInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.common.ConstantValues;
import org.artifactory.storage.GCCandidate;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.db.binstore.entity.BinaryEntity;
import org.artifactory.storage.db.util.BaseDao;
import org.artifactory.storage.db.util.JdbcHelper;
import org.artifactory.storage.db.util.querybuilder.ArtifactoryQueryWriter;
import org.jfrog.storage.util.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import static org.jfrog.storage.util.DbUtils.withMetadata;

/**
 * A data access object for binaries table access.
 *
 * @author Yossi Shaul
 */
@Repository
public class BinariesDao extends BaseDao {
    private static final Logger log = LoggerFactory.getLogger(BinariesDao.class);

    public static final String TEMP_SHA1_PREFIX = "##";

    //Used by the migration to prevent excessive db calls, remove when we get rid of the migration
    private final String sha1ColumnName;

    @Autowired
    public BinariesDao(JdbcHelper jdbcHelper) {
        super(jdbcHelper);
        sha1ColumnName = resolveSha1ColumnName(jdbcHelper);
    }

    public boolean exists(ChecksumType checksumType, String checksum) throws SQLException {
        //Can't both operands of '=' to be ? parameters, that's why
        String query = "SELECT count(1) FROM binaries WHERE " + checksumType.name() + " = ?";
        int count = jdbcHelper.executeSelectCount(query, checksum);
        if (count > 1) {
            log.warn("Unexpected binaries count for checksum: '{}' - {}", checksum, count);
        }
        return count > 0;
    }

    @Nullable
    public BinaryEntity load(ChecksumType checksumType, String checksum) throws SQLException {
        ResultSet resultSet = null;
        try {
            //Can't both operands of '=' to be ? parameters, that's why
            String query = "SELECT sha1, md5, bin_length, sha256 FROM binaries WHERE " + checksumType.name() + " = ?";
            resultSet = jdbcHelper.executeSelect(query, checksum);
            if (resultSet.next()) {
                return binaryFromResultSet(resultSet);
            }
        } finally {
            DbUtils.close(resultSet);
        }
        return null;
    }

    public Collection<BinaryEntity> findAll() throws SQLException {
        Collection<BinaryEntity> results = Lists.newArrayList();
        ResultSet resultSet = null;
        try {
            resultSet = jdbcHelper.executeSelect("SELECT sha1, md5, bin_length, sha256 " +
                                                        "FROM binaries WHERE sha1 NOT LIKE '" + TEMP_SHA1_PREFIX + "%'");
            while (resultSet.next()) {
                results.add(binaryFromResultSet(resultSet));
            }
        } finally {
            DbUtils.close(resultSet);
        }
        return results;
    }

    public Collection<BinaryEntity> search(ChecksumType checksumType, Collection<String> validChecksums) throws SQLException {
        Collection<BinaryEntity> results = Lists.newArrayList();
        if (validChecksums == null || validChecksums.isEmpty()) {
            return results;
        }

        // Oracle limits the max elements in the IN clause to 1000. Lists bigger than max chunk value are done in
        // multiple queries
        List<String> checksums = Lists.newArrayList(validChecksums);
        final int CHUNK = ConstantValues.binaryProviderPruneChunkSize.getInt();
        // split to chunks of no more than CHUNK
        for (int i = 0; i < validChecksums.size(); i += CHUNK) {
            int chunkMaxIndex = Math.min(i + CHUNK, validChecksums.size());
            List<String> chunk = checksums.subList(i, chunkMaxIndex);
            ResultSet resultSet = null;
            try {
                resultSet = jdbcHelper.executeSelect("SELECT sha1, md5, bin_length, sha256 FROM binaries WHERE "
                        + checksumType.name() + " IN (#)", chunk);
                while (resultSet.next()) {
                    results.add(binaryFromResultSet(resultSet));
                }
            } finally {
                DbUtils.close(resultSet);
            }
        }

        return results;
    }

    public Collection<BinaryEntity> findPotentialDeletion() throws SQLException {
        Collection<BinaryEntity> results = Lists.newArrayList();
        ResultSet resultSet = null;
        try {
            resultSet = jdbcHelper.executeSelect("SELECT b.sha1, b.md5, b.bin_length, b.sha256 FROM binaries b" +
                    " WHERE b.sha1 NOT LIKE '" + TEMP_SHA1_PREFIX + "%'" +
                    " AND NOT EXISTS (SELECT n.node_id FROM nodes n WHERE n.sha1_actual = b.sha1)" +
                    " ORDER BY b.bin_length DESC");
            while (resultSet.next()) {
                results.add(binaryFromResultSet(resultSet));
            }
        } finally {
            DbUtils.close(resultSet);
        }
        return results;
    }

    public int deleteEntry(String sha1ToDelete) throws SQLException {
        return jdbcHelper.executeUpdate("DELETE FROM binaries WHERE binaries.sha1 = ?" +
                " AND NOT EXISTS (SELECT n.node_id FROM nodes n WHERE n.sha1_actual = ?)",
                 sha1ToDelete, sha1ToDelete);
    }

    /**
     * @return A pair of long values where the first is the counts of the binaries table elements and the second is the
     * total binaries size.
     */
    public BinariesInfo getCountAndTotalSize() throws SQLException {
        ResultSet resultSet = null;
        try {
            resultSet = jdbcHelper.executeSelect("SELECT count(b.sha1), sum(b.bin_length) FROM binaries b" +
                    " WHERE b.sha1 NOT LIKE '" + TEMP_SHA1_PREFIX + "%'");
            resultSet.next();
            return new BinariesInfo(resultSet.getLong(1), resultSet.getLong(2));
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public boolean create(BinaryEntity binaryEntity) throws SQLException {
        int updateCount = jdbcHelper.executeUpdate("INSERT INTO binaries (sha1, md5, bin_length, sha256) VALUES(?, ?, ?, ?)",
                binaryEntity.getSha1(), binaryEntity.getMd5(), binaryEntity.getLength(), binaryEntity.getSha2());
        return updateCount == 1;
    }

    private BinaryEntity binaryFromResultSet(ResultSet rs) throws SQLException {
        //sha1 sha2 md5 length, order in table is sha1 md5 length sha2
        return new BinaryEntity(rs.getString(1), rs.getString(4), rs.getString(2), rs.getLong(3));
    }

    /**
     * USED BY SHA256 MIGRATION ONLY!
     * Updates the sha256 column with {@param newSha2} for a row that has sha1 = {@param targetSha1}
     */
    public boolean insertSha2(String targetSha1, String newSha2) throws SQLException {
        return jdbcHelper.executeUpdate("UPDATE binaries SET sha256 = ? WHERE sha1 = ?", newSha2, targetSha1) > 0;
    }

    /**
     * * USED BY SHA256 MIGRATION ONLY!
     * @return sha1 values of all rows that are missing sha256 elements
     */
    public List<String> getSha1ForMissingSha2(long limit, long offset) throws SQLException {
        List<String> sha1 = Lists.newArrayList();
        ResultSet resultSet = null;
        try {
            String query = new ArtifactoryQueryWriter().select(" sha1 ").from(" binaries ").where(" sha256 IS NULL ")
                    .limit(limit).offset(offset).build();
            resultSet = jdbcHelper.executeSelect(query);
            while (resultSet.next()) {
                // some offset queries may change the columns of the returned rs, must get column by name
                sha1.add(resultSet.getString(sha1ColumnName));
            }
        } finally {
            DbUtils.close(resultSet);
        }
        return sha1;
    }

    /**
     * * USED BY SHA256 MIGRATION ONLY!
     */
    public int getSha1ForMissingSha2Count() throws SQLException {
        return jdbcHelper.executeSelectCount("SELECT COUNT(*) FROM binaries WHERE sha256 IS NULL");
    }

    public GCCandidate gcCandidateFromResultSet(ResultSet resultSet) {
        try {
            String sha1 = resultSet.getString("sha1");
            String sha2 = resultSet.getString("sha256");
            String md5 = resultSet.getString("md5");
            String length = resultSet.getString("bin_length");
            return new GCCandidate(null, sha1, sha2, md5, Long.valueOf(length));
        } catch (SQLException e) {
            throw new StorageException("Unable to read GC Candidate from result set", e);
        }
    }

    //Returns the normalized name for this db's binaries table/sha256 column name --> used by the migration to prevent excessive db calls
    private String resolveSha1ColumnName(JdbcHelper jdbcHelper) {
        String sha2ColName = "sha1";
        try {
            sha2ColName = withMetadata(jdbcHelper, metadata -> DbUtils.normalizedName("sha1", metadata));
        } catch (SQLException e) {
            log.warn("Could not resolve table metadata: {}", e.getMessage());
            log.debug("", e);
        }
        return sha2ColName;
    }
}
