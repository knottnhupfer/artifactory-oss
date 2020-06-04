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

package org.artifactory.storage.db.blob.infos.dao;

import com.google.common.base.Charsets;
import org.apache.commons.io.IOUtils;
import org.artifactory.storage.db.blob.infos.model.DbBlobInfo;
import org.artifactory.storage.db.util.BaseDao;
import org.artifactory.storage.db.util.JdbcHelper;
import org.jfrog.storage.wrapper.BlobWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Inbar Tal
 */
@Repository
public class BlobInfosDao extends BaseDao {

    private static final String TABLE_NAME = "blob_infos";
    public static final int BULK_SIZE = 100;

    @Autowired
    public BlobInfosDao(@Nonnull JdbcHelper jdbcHelper) {
        super(jdbcHelper);
    }

    public int create(@Nonnull DbBlobInfo blobInfo) throws SQLException {
        BlobWrapper blobWrapper = new BlobWrapper(blobInfo.getBlobInfo());
        return jdbcHelper.executeUpdate(
                "INSERT INTO " + TABLE_NAME + " (checksum, blob_info) VALUES (?, ?)",
                blobInfo.getChecksum(), blobWrapper);
    }

    public int delete(@Nonnull String checksum) throws SQLException {
        return jdbcHelper.executeUpdate("DELETE FROM " + TABLE_NAME + " WHERE checksum = ?", checksum);
    }

    public int deleteBulk(@Nonnull List<String> checksums) throws SQLException {
        List<String> fullList = new ArrayList<>(checksums);
        List<String> subList;
        int passed = 0;
        int numDeleted = 0;
        int toIndex = Math.min(BULK_SIZE + passed, fullList.size());
        while (passed <= toIndex &&
                (subList = fullList.subList(passed, toIndex)).size() > 0) {
            Object[] currentParams = subList.toArray();
            String query = getQueryMultipleValuesSql(subList.size());
            numDeleted += jdbcHelper.executeUpdate(query, currentParams);
            passed += subList.size();
            toIndex = Math.min(BULK_SIZE + passed, fullList.size());
        }
        return numDeleted;
    }

    public int deleteAll() throws SQLException {
        return jdbcHelper.executeUpdate("DELETE FROM " + TABLE_NAME);
    }

    public Optional<DbBlobInfo> find(@Nonnull String checksum) throws SQLException, IOException {
        DbBlobInfo blobInfo = null;
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE checksum = ?";
        try (ResultSet resultSet = jdbcHelper.executeSelect(query, checksum)) {
            if (resultSet.next()) {
                blobInfo = readFrom(resultSet);
            }
        }
        return Optional.ofNullable(blobInfo);
    }

    private String getQueryMultipleValuesSql(int numberOfValues) {
        return "DELETE FROM " + TABLE_NAME +  " WHERE checksum IN (" +
                IntStream.range(0, numberOfValues).mapToObj(i -> "?").collect(
                        Collectors.joining(", ")) + ")";
    }

    private DbBlobInfo readFrom(ResultSet resultSet) throws SQLException, IOException {
        return DbBlobInfo.builder()
                .checksum(resultSet.getString("checksum"))
                .blobInfo(toString(resultSet.getBinaryStream("blob_info"))).build();
    }


    private String toString(InputStream blobInfoStream) throws IOException {
        return IOUtils.toString(blobInfoStream, Charsets.UTF_8.name());
    }
}





