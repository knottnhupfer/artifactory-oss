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

package org.artifactory.storage.db.blob.infos.service;

import org.artifactory.api.blob.infos.BlobInfosDBService;
import org.artifactory.exception.SQLIntegrityException;
import org.artifactory.storage.db.blob.infos.dao.BlobInfosDao;
import org.artifactory.storage.db.blob.infos.model.DbBlobInfo;
import org.jfrog.storage.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Optional;

/**
 * @author Inbar Tal
 */
@Service
public class BlobInfosDBServiceImpl implements BlobInfosDBService {
    private static final Logger log = LoggerFactory.getLogger(BlobInfosDBServiceImpl.class);

    @Autowired
    private BlobInfosDao dao;

    public boolean putBlobInfo(String checksum, String blobInfoJson) throws StorageException {
        try {
            log.debug("Create new blob info with checksum: {}", checksum);
            DbBlobInfo dbBlobInfo = new DbBlobInfo(checksum, blobInfoJson);
            return dao.create(dbBlobInfo) > 0;
        }  catch (SQLIntegrityConstraintViolationException e) {
            throw new SQLIntegrityException("Failed to create blobInfo, blobInfo with checksum " + checksum + " already exists", e);
        }  catch (Exception e) {
            throw new StorageException("Error occurred while inserting blobInfo with checksum: " + checksum, e);
        }
    }

    public int deleteBlobInfo(String checksum) throws StorageException {
        try {
            log.debug("Delete blob info with checksum: {}", checksum);
            return dao.delete(checksum);
        } catch (SQLException e) {
            throw new StorageException("Error occurred while deleting blobInfo with checksum: " + checksum, e);
        }
    }

    public int deleteBlobInfos(List<String> checksums) throws StorageException {
        try {
            int numDeleted = dao.deleteBulk(checksums);
            log.debug("{} blob infos were deleted successfully", numDeleted);
            return numDeleted;
        } catch (SQLException e) {
            throw new StorageException("Error occurred while deleting blobInfos from cache", e);
        }
    }

    public String getBlobInfo(String checksum) throws StorageException {
        try {
            Optional<DbBlobInfo> dbBlobInfo = dao.find(checksum);
            return (dbBlobInfo.isPresent()) ? dbBlobInfo.get().getBlobInfo() : null;
        } catch (Exception e) {
            throw new StorageException("Error occurred while searching for blobInfo with checksum: " + checksum, e);
        }
    }


}
