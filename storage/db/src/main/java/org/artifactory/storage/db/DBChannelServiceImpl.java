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

package org.artifactory.storage.db;

import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.config.db.ArtifactoryDbProperties;
import org.artifactory.storage.db.util.JdbcHelper;
import org.jfrog.storage.DbType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.jfrog.storage.util.DbUtils.tableExists;

/**
 * @author gidis
 */
@Service
public class DBChannelServiceImpl implements DBChannelService {

    @Autowired
    private JdbcHelper jdbcHelper;

    @Autowired
    private ArtifactoryDbProperties dbProperties;

    private ArtifactoryContext artifactoryContext;

    @PostConstruct
    private void init() throws Exception {
        artifactoryContext = ContextHelper.get();
    }

    @Override
    public ResultSet executeSelect(String query, Object... params) throws SQLException {
        return jdbcHelper.executeSelect(query, params);
    }

    @Override
    public int executeUpdate(String query, Object... params) throws SQLException {
        DBChannelService transactionalMe = artifactoryContext.beanForType(DBChannelService.class);
        ArtifactoryContextThreadBinder.bind(artifactoryContext);
        ArtifactoryHome.bind(artifactoryContext.getArtifactoryHome());
        return transactionalMe.executeUpdateInternal(query, params);
    }

    @Override
    public DbType getDbType() {
        return dbProperties.getDbType();
    }

    @Override
    public boolean isTableExists(String tableName) {
        if (jdbcHelper.isClosed()) {
            throw new RuntimeException("JDBC helper is closed.");
        }
        try {
            return tableExists(jdbcHelper, dbProperties.getDbType(), tableName);
        } catch (SQLException e) {
            throw new RuntimeException("Could not find database table " + tableName, e);
        }
    }

    @Override
    public void close() {
        // Nothing here the jdbc helper is closing itself
    }

    @Override
    public int executeUpdateInternal(String query, Object... params) throws SQLException {
        return jdbcHelper.executeUpdate(query, params);

    }
}
