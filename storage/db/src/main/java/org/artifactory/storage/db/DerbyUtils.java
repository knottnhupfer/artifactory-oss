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

import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.storage.db.util.JdbcHelper;
import org.artifactory.storage.spring.StorageContextHelper;
import org.jfrog.storage.DbType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

import static org.jfrog.storage.util.DbUtils.*;

/**
 * @author yoavl
 */
public abstract class DerbyUtils {
    private static final Logger log = LoggerFactory.getLogger(DerbyUtils.class);

    /**
     * http://db.apache.org/derby/docs/10.2/ref/rrefaltertablecompress.html
     */
    private static final String CALL_COMPRESS = "CALL SYSCS_UTIL.SYSCS_COMPRESS_TABLE(?, ?, ?)";

    /**
     * http://db.apache.org/derby/docs/10.2/ref/rrefproceduresinplacecompress.html
     */
    private static final String CALL_COMPRESS_INPLACE = "CALL SYSCS_UTIL.SYSCS_INPLACE_COMPRESS_TABLE(?, ?, ?, ?, ?)";

    private DerbyUtils() {
        // utility class
    }

    public static void compress(BasicStatusHolder holder, DbType dbType) {
        try {
            JdbcHelper jdbcHelper = StorageContextHelper.get().beanForType(JdbcHelper.class);
            doWithConnection(jdbcHelper, conn -> compressDataStore(conn, dbType));
        } catch (Exception e) {
            holder.error("Could not compress storage", e, log);
        }
    }

    /**
     * Compresses the datastore tables (holding the blobs).
     */
    private static void compressDataStore(Connection con, DbType dbType) throws SQLException {
        log.info("Compressing derby binaries table...");
        con.setAutoCommit(true);
        DatabaseMetaData dbMetadata = con.getMetaData();
        try (ResultSet rs = dbMetadata.getTables(getActiveCatalog(con, dbType), getActiveSchema(con, dbType), null, new String[]{"TABLE"})) {
            String binariesTableName = normalizedName("binaries", dbMetadata);
            while (rs.next()) {
                String currentSchemaName = rs.getString("TABLE_SCHEM").toUpperCase();
                String currentTableName = rs.getString("TABLE_NAME").toUpperCase();
                if (currentTableName.equals(binariesTableName)) {
                    executeCall(con, CALL_COMPRESS, currentSchemaName, currentTableName, 1);
                    executeCall(con, CALL_COMPRESS_INPLACE, currentSchemaName, currentTableName, 3);
                    log.debug("Datastore compressed successfully");
                }
            }
        }
    }

    /**
     * Executes the given call (does not commit)
     *
     * @param connection  Database connection
     * @param command     Command to execute
     * @param schemaName  Name of selected schema
     * @param tableName   Name of selected table
     * @param paramLength Length of short params needed for the command
     */
    private static void executeCall(Connection connection, String command, String schemaName, String tableName,
            int paramLength) throws SQLException {
        try (CallableStatement cs = connection.prepareCall(command)) {
            cs.setString(1, schemaName);
            cs.setString(2, tableName);
            paramLength += 3;
            for (int i = 3; i < paramLength; i++) {
                cs.setShort(i, (short) 1);
            }
            cs.execute();
        }
    }
}