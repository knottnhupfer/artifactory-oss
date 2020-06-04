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

import com.google.common.base.Charsets;
import org.apache.commons.io.IOUtils;
import org.artifactory.storage.db.util.BaseDao;
import org.artifactory.storage.db.util.JdbcHelper;
import org.jfrog.storage.util.DbUtils;
import org.jfrog.storage.wrapper.BlobWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A data access object for configs table access.
 *
 * @author Yossi Shaul
 */
@Repository
public class ConfigsDao extends BaseDao {

    @Autowired
    public ConfigsDao(JdbcHelper jdbcHelper) {
        super(jdbcHelper);
    }

    public int createConfig(String name, String data, long lastModification) throws SQLException, UnsupportedEncodingException {
        BlobWrapper blobWrapper = new BlobWrapper(data);
        return createConfig(name, blobWrapper, lastModification);
    }

    public int createConfig(String name, BlobWrapper blobWrapper, long lastModification) throws SQLException {
        return jdbcHelper.executeUpdate("INSERT INTO configs (config_name, last_modified, data) VALUES(?, ?, ?)",
                name, lastModification, blobWrapper);
    }

    public boolean hasConfig(String name) throws SQLException {
        ResultSet resultSet = null;
        try {
            resultSet = jdbcHelper.executeSelect("SELECT COUNT(1) FROM configs WHERE config_name = ?", name);
            if (resultSet.next()) {
                int propsCount = resultSet.getInt(1);
                return propsCount > 0;
            }
            return false;
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public InputStream loadStreamConfig(String name) throws SQLException {
        ResultSet resultSet = null;
        try {
            resultSet = jdbcHelper.executeSelect("SELECT data FROM configs WHERE config_name = ?", name);
            if (resultSet.next()) {
                InputStream binaryStream = null;
                try {
                    binaryStream = resultSet.getBinaryStream(1);
                    return IOUtils.toBufferedInputStream(binaryStream);
                } finally {
                    IOUtils.closeQuietly(binaryStream);
                }
            }
            return null;
        } catch (IOException e) {
            throw new SQLException("Failed to read config '" + name + "' due to: " + e.getMessage(), e);
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public String loadConfig(String name) throws SQLException {
        ResultSet resultSet = null;
        try {
            resultSet = jdbcHelper.executeSelect("SELECT data FROM configs WHERE config_name = ?", name);
            if (resultSet.next()) {
                InputStream binaryStream = resultSet.getBinaryStream(1);
                return IOUtils.toString(binaryStream, Charsets.UTF_8.name());
            }
            return null;
        } catch (IOException e) {
            throw new SQLException("Failed to read config '" + name + "' due to: " + e.getMessage(), e);
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public int updateConfig(String name, String data, long lastModification) throws UnsupportedEncodingException, SQLException {
        BlobWrapper blobWrapper = new BlobWrapper(data);
        return updateConfig(name, blobWrapper, lastModification);
    }

    public int updateConfig(String name, BlobWrapper blobWrapper, Long lastModification) throws SQLException {
        if (lastModification == null) {
            return jdbcHelper.executeUpdate("UPDATE configs SET data = ? WHERE config_name = ? ",
                    blobWrapper, name);
        } else {
            return jdbcHelper.executeUpdate("UPDATE configs SET data = ?, last_modified = ? WHERE config_name = ? ",
                    blobWrapper, lastModification, name);
        }
    }

    public int updateConfigWithExpectedLastModification(String name, long expectedLastModified, long lastModified, BlobWrapper blobWrapper) throws SQLException {
        return jdbcHelper.executeUpdate("UPDATE configs SET data = ?, last_modified = ? WHERE config_name = ? AND last_modified = ?",
                blobWrapper, lastModified, name, expectedLastModified);
    }

    public int deleteConfig(String name) throws SQLException {
        return jdbcHelper.executeUpdate("DELETE FROM configs WHERE config_name = ?", name);
    }
}
