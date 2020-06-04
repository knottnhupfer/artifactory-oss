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

package org.artifactory.storage.db.keys.dao;

import com.google.common.collect.Lists;
import org.artifactory.storage.db.keys.model.DbTrustedKey;
import org.artifactory.storage.db.util.BaseDao;
import org.artifactory.storage.db.util.JdbcHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * @author Rotem Kfir
 */
@Repository
public class TrustedKeysDao extends BaseDao {

    private static final String TABLE_NAME = "trusted_keys";

    @Autowired
    public TrustedKeysDao(@Nonnull JdbcHelper jdbcHelper) {
        super(jdbcHelper);
    }

    public int create(@Nonnull DbTrustedKey key) throws SQLException {
        return jdbcHelper.executeUpdate(
                "INSERT INTO " + TABLE_NAME + " (kid, trusted_key, fingerprint, alias, issued, issued_by, expiry) VALUES (?, ?, ?, ?, ?, ?, ?)",
                key.getKid(), key.getTrustedKey(), key.getFingerprint(), key.getAlias(), key.getIssued(), key.getIssuedBy(), key.getExpiry());
    }

    public int delete(@Nonnull String kid) throws SQLException {
        return jdbcHelper.executeUpdate("DELETE FROM " + TABLE_NAME + " WHERE kid = ?", kid);
    }

    public int deleteAll() throws SQLException {
        return jdbcHelper.executeUpdate("DELETE FROM " + TABLE_NAME);
    }

    public Optional<DbTrustedKey> findById(@Nonnull String kid) throws SQLException {
        return findByProperty("kid", kid);
    }

    public Optional<DbTrustedKey> findByAlias(@Nonnull String alias) throws SQLException {
        return findByProperty("alias", alias);
    }

    private Optional<DbTrustedKey> findByProperty(@Nonnull String propertyName, @Nonnull String value) throws SQLException {
        DbTrustedKey key = null;
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + propertyName + " = ?";
        try (ResultSet resultSet = jdbcHelper.executeSelect(query, value)) {
            if (resultSet.next()) {
                key = readFrom(resultSet);
            }
        }
        return Optional.ofNullable(key);
    }

    @Nonnull
    public List<DbTrustedKey> findAll() throws SQLException {
        List<DbTrustedKey> results = Lists.newArrayList();
        try (ResultSet resultSet = jdbcHelper.executeSelect("SELECT * FROM " + TABLE_NAME)) {
            while (resultSet.next()) {
                results.add(readFrom(resultSet));
            }
            return results;
        }
    }

    private DbTrustedKey readFrom(ResultSet resultSet) throws SQLException {
        return DbTrustedKey.builder()
                .kid(resultSet.getString("kid"))
                .trustedKey(resultSet.getString("trusted_key"))
                .fingerprint(resultSet.getString(("fingerprint")))
                .alias(resultSet.getString("alias"))
                .issued(resultSet.getLong("issued"))
                .issuedBy(resultSet.getString("issued_by"))
                .expiry(resultSet.getLong("expiry"))
                .build();
    }
}
