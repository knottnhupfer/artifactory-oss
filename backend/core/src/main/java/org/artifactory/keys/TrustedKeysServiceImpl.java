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

package org.artifactory.keys;

import org.artifactory.api.keys.TrustedKeysService;
import org.artifactory.model.xstream.keys.TrustedKeyImpl;
import org.artifactory.storage.db.keys.dao.TrustedKeysDao;
import org.artifactory.storage.db.keys.model.DbTrustedKey;
import org.jfrog.storage.StorageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Rotem Kfir
 */
@Service
public class TrustedKeysServiceImpl implements TrustedKeysService {

    @Autowired
    private TrustedKeysDao dao;

    @Override
    @Nonnull
    public List<TrustedKey> findAllTrustedKeys() throws StorageException {
        try {
            List<DbTrustedKey> dbTrustedKeys = dao.findAll();
            return dbKeysToKeys(dbTrustedKeys);
        } catch (SQLException e) {
            throw new StorageException("Could not load Trusted Keys", e);
        }
    }

    @Nonnull
    @Override
    public Optional<TrustedKey> findTrustedKeyById(@Nonnull String kid) throws StorageException {
        try {
            Optional<DbTrustedKey> dbTrustedKey = dao.findById(kid);
            return dbTrustedKey.map(this::dbKeyToKey);
        } catch (SQLException e) {
            throw new StorageException("Could not find Trusted Key by id '" + kid + "'", e);
        }
    }

    @Nonnull
    @Override
    public Optional<TrustedKey> findTrustedKeyByAlias(@Nonnull String alias) throws StorageException {
        try {
            Optional<DbTrustedKey> dbTrustedKey = dao.findByAlias(alias);
            return dbTrustedKey.map(this::dbKeyToKey);
        } catch (SQLException e) {
            throw new StorageException("Could not find Trusted Key by alias '" + alias + "'", e);
        }
    }

    @Nonnull
    @Override
    public TrustedKey createTrustedKey(TrustedKey key) throws StorageException {
        try {
            dao.create(keyToDbKey(key));
            return key;
        } catch (SQLException e) {
            throw new StorageException("Could not create new Trusted Key", e);
        }
    }

    @Override
    public boolean deleteTrustedKey(String kid) throws StorageException {
        try {
            int deleted = dao.delete(kid);
            return deleted > 0;
        } catch (SQLException e) {
            throw new StorageException("Could not delete Trusted Key", e);
        }
    }

    @Override
    public long deleteAllTrustedKeys() throws StorageException {
        try {
            return dao.deleteAll();
        } catch (SQLException e) {
            throw new StorageException("Could not delete all Trusted Keys", e);
        }
    }

    @Nonnull
    private DbTrustedKey keyToDbKey(@Nonnull TrustedKey key) {
        return DbTrustedKey.builder()
                .kid(key.getKid())
                .trustedKey(key.getTrustedKey())
                .fingerprint(key.getFingerprint())
                .alias(key.getAlias())
                .issued(key.getIssued())
                .issuedBy(key.getIssuedBy())
                .expiry(key.getExpiry())
                .build();
    }

    @Nonnull
    private List<TrustedKey> dbKeysToKeys(@Nonnull List<DbTrustedKey> dbTrustedKeys) {
        return dbTrustedKeys.stream()
                .map(this::dbKeyToKey)
                .collect(Collectors.toList());
    }

    @Nonnull
    private TrustedKey dbKeyToKey(@Nonnull DbTrustedKey dbTrustedKey) {
        return TrustedKeyImpl.builder()
                .kid(dbTrustedKey.getKid())
                .trustedKey(dbTrustedKey.getTrustedKey())
                .fingerprint(dbTrustedKey.getFingerprint())
                .alias(dbTrustedKey.getAlias())
                .issued(dbTrustedKey.getIssued())
                .issuedBy(dbTrustedKey.getIssuedBy())
                .expiry(dbTrustedKey.getExpiry())
                .build();
    }
}
