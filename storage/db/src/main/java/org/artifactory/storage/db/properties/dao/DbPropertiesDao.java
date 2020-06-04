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

package org.artifactory.storage.db.properties.dao;

import com.google.common.collect.Lists;
import org.artifactory.common.config.db.ArtifactoryDbProperties;
import org.artifactory.common.storage.db.properties.DbVersionInfo;
import org.artifactory.storage.db.util.BaseDao;
import org.artifactory.storage.db.util.JdbcHelper;
import org.jfrog.storage.util.DbUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Date: 7/10/13 3:08 PM
 *
 * @author freds
 */
@Repository
public class DbPropertiesDao extends BaseDao {

    public static final String TABLE_NAME = "db_properties";

    private ArtifactoryDbProperties dbProperties;

    @Autowired
    public DbPropertiesDao(JdbcHelper jdbcHelper, ArtifactoryDbProperties dbProperties) {
        super(jdbcHelper);
        this.dbProperties = dbProperties;
    }

    @Nonnull
    public List<DbVersionInfo> getProperties() throws SQLException {
        List<DbVersionInfo> result = Lists.newArrayList();
        ResultSet rs = null;
        try {
            rs = jdbcHelper.executeSelect("SELECT * FROM " + TABLE_NAME);
            while (rs.next()) {
                result.add(new DbVersionInfo(rs.getLong(1), rs.getString(2),
                        zeroIfNull(rs.getInt(3)), zeroIfNull(rs.getLong(4))));
            }
        } finally {
            DbUtils.close(rs);
        }
        return result;
    }

    public boolean createProperties(DbVersionInfo dbProperties) throws SQLException {
        int updateCount = jdbcHelper.executeUpdate("INSERT INTO " + TABLE_NAME +
                        " (installation_date, artifactory_version, artifactory_revision, artifactory_release)" +
                        " VALUES(?, ?, ?, ?)",
                dbProperties.getInstallationDate(), dbProperties.getArtifactoryVersion(),
                nullIfZeroOrNeg(dbProperties.getArtifactoryRevision()),
                nullIfZeroOrNeg(dbProperties.getArtifactoryRelease()));
        return updateCount == 1;
    }

    public boolean isDbPropertiesTableExists() throws SQLException {
        return DbUtils.tableExists(jdbcHelper, dbProperties.getDbType(), TABLE_NAME);
    }
}
