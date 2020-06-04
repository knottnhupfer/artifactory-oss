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

package org.artifactory.common.config.db;

import com.google.common.collect.Lists;
import org.artifactory.common.storage.db.properties.DbVersionInfo;
import org.jfrog.config.DbChannel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

/**
 * @author Dan Feldman
 */
public class DbVersionDataAccessObject {

    private DbChannel dbChannel;

    public DbVersionDataAccessObject(DbChannel dbChannel) {
        this.dbChannel = dbChannel;
    }

    public DbVersionInfo getDbVersion() {
        List<DbVersionInfo> dbProperties = Lists.newArrayList();
        try (ResultSet resultSet = dbChannel.executeSelect("SELECT * FROM db_properties ")) {
            while (resultSet.next()) {
                dbProperties.add(getDbVersionFromResult(resultSet));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve version information from database:" + e.getMessage(), e);
        }
        dbProperties.sort(Comparator.comparingInt(DbVersionInfo::getArtifactoryRevision));
        return !dbProperties.isEmpty() ? dbProperties.get(dbProperties.size() - 1) : null;
    }

    public boolean isDbPropertiesTableExists() {
        //noinspection EmptyTryBlock, unused
        try (ResultSet resultSet = dbChannel.executeSelect("SELECT * FROM db_properties")) {
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private DbVersionInfo getDbVersionFromResult(ResultSet resultSet) throws SQLException {
        return new DbVersionInfo(resultSet.getLong(1), resultSet.getString(2),
                zeroIfNull(resultSet.getInt(3)), zeroIfNull(resultSet.getLong(4)));
    }

    public void destroy() {
        dbChannel.close();
    }

    private static int zeroIfNull(Integer id) {
        return (id == null) ? 0 : id;
    }

    private static long zeroIfNull(Long id) {
        return (id == null) ? 0L : id;
    }
}
