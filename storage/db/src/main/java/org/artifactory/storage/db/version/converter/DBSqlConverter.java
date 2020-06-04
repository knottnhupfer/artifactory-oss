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

package org.artifactory.storage.db.version.converter;

import org.artifactory.storage.db.util.JdbcHelper;
import org.jfrog.storage.DbType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

import static org.jfrog.storage.util.DbUtils.doWithConnection;

/**
 * Converts database by conversion sql script
 *
 * @author Gidi Shabat
 */
public class DBSqlConverter implements DBConverter {
    private static final Logger log = LoggerFactory.getLogger(DBSqlConverter.class);

    final String fromVersion;

    public DBSqlConverter(String fromVersion) {
        this.fromVersion = fromVersion;
    }

    @Override
    public void convert(JdbcHelper jdbcHelper, DbType dbType) {
        try {
            doWithConnection(jdbcHelper, conn -> DbSqlConverterUtil.convert(conn, dbType, fromVersion));
        } catch (SQLException sql) {
            //conversion error is runtime and logged in the util, no need to catch it here.
            String msg = "Could not convert DB using " + fromVersion + " converter";
            log.error(msg + " due to " + sql.getMessage(), sql);
            throw new RuntimeException(msg, sql);
        }
    }
}
