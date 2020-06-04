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

package org.artifactory.storage.db.conversion.version.v213;

import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.storage.db.version.converter.DbDetails;
import org.artifactory.storage.fs.service.ConfigsService;
import org.jfrog.storage.JdbcHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.function.BiConsumer;

import static org.jfrog.storage.DbType.POSTGRESQL;

/**
 * @author Shay Bagants
 */
public class V213ConversionFailFunction implements BiConsumer<DbDetails, RuntimeException> {
    private static final Logger log = LoggerFactory.getLogger(V213ConversionFailFunction.class);

    public static final String PSQL_NODE_PROPS_INDEX_MISSING_MARKER = "db.conversion.postgresql.v213.missing";

    @Override
    public void accept(DbDetails dbDetails, RuntimeException e) {
        String error = "Failed to perform conversion. " + e.getMessage();
        if (dbDetails.getDbType() != POSTGRESQL) {
            log.error(error);
            throw e;
        }
        log.info("Attempting to restore original indexes if needed....");
        try {
            restoreOriginalIndexIfNeeded(dbDetails.getJdbcHelper());
        } catch (SQLException exp) {
            log.error("An error occurred while trying to restore original index", exp);
        }
        log.error(error + ". Skipping conversion.", e);
        ArtifactoryContext context = ContextHelper.get();
        ConfigsService configsService = context.beanForType(ConfigsService.class);
        if (!configsService.hasConfig(PSQL_NODE_PROPS_INDEX_MISSING_MARKER)) {
            log.warn("Conversion failed, adding marker file to hint that conversion is still required.");
            configsService.addConfig(PSQL_NODE_PROPS_INDEX_MISSING_MARKER, ".", System.currentTimeMillis());
        }
    }

    private void restoreOriginalIndexIfNeeded(JdbcHelper jdbcHelper) throws SQLException {
        jdbcHelper.executeUpdate(
                "CREATE INDEX IF NOT EXISTS node_props_node_prop_value_idx ON node_props(node_id, prop_key, substr(prop_value, 1, 2400));\n" +
                        "CREATE INDEX IF NOT EXISTS node_props_prop_key_value_idx ON node_props (prop_key, substr(prop_value, 1, 2400) varchar_pattern_ops);");
    }
}
