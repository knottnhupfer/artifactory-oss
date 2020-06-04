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

package org.artifactory.storage.db.conversion.version.v203;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.artifactory.storage.db.conversion.ConversionPredicate;
import org.artifactory.storage.db.util.JdbcHelper;
import org.jfrog.storage.DbType;
import org.jfrog.storage.util.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static org.jfrog.storage.util.DbUtils.withMetadata;

/**
 * All predicates for v5.5.0 conversions (sha2 and no-hazelcast)
 *
 * @author Dan Feldman
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class V203ConversionPredicates {
    private static final Logger log = LoggerFactory.getLogger(V203ConversionPredicates.class);

    private static final String COLUMN_SHA_256 = "sha256";
    private static final String TABLE_BINARIES = "binaries";
    private static final String TABLE_NODES = "nodes";

    /**
     * binaries table sha256 conversion predicate
     */
    public static class V550ConversionPredicate implements ConversionPredicate {
        @Override
        public BiPredicate<JdbcHelper, DbType> condition() {
            return (jdbcHelper, dbType) ->
                    test(metadata -> {
                        try {
                            return !DbUtils.columnExists(metadata, dbType, TABLE_BINARIES, COLUMN_SHA_256);
                        } catch (SQLException e) {
                            log.error("Cannot run conversion 'v550' - Failed to resolve schema metadata: ", e);
                        }
                        return false;
                    }, jdbcHelper, "v550");
        }
    }

    /**
     * nodes table sha256 and no-hazelcast conversion predicate
     */
    public static class V550aConversionPredicate implements ConversionPredicate {
        @Override
        public BiPredicate<JdbcHelper, DbType> condition() {
            return (jdbcHelper, dbType) ->
                    test(metadata -> {
                        try {
                            return !DbUtils.columnExists(metadata, dbType, TABLE_NODES, COLUMN_SHA_256)
                                    && !DbUtils.columnExists(metadata, dbType, TABLE_NODES, "repo_path_checksum");
                        } catch (SQLException e) {
                            log.error("Cannot run conversion 'v550a' - Failed to resolve schema metadata: ", e);
                        }
                        return false;
                    }, jdbcHelper, "v550a");
        }
    }

    /**
     * nodes table sha256 index conversion predicate
     */
    public static class V550bConversionPredicate implements ConversionPredicate {
        @Override
        public BiPredicate<JdbcHelper, DbType> condition() {
            return (jdbcHelper, dbType) ->
                    test(metadata -> {
                        try {
                            return !DbUtils.indexExists(jdbcHelper, TABLE_NODES, COLUMN_SHA_256, "nodes_sha256_idx", dbType);
                        } catch (SQLException e) {
                            log.error("Cannot run conversion 'v550b' - Failed to resolve schema metadata: ", e);
                        }
                        return false;
                    }, jdbcHelper, "v550b");
        }
    }

    /**
     * distributed_locks table conversion predicate
     */
    public static class V550cConversionPredicate implements ConversionPredicate {
        @Override
        public BiPredicate<JdbcHelper, DbType> condition() {
            return (jdbcHelper, dbType) ->
                    test(metadata -> {
                        try {
                            return !DbUtils.tableExists(metadata, dbType, "distributed_locks");
                        } catch (SQLException e) {
                            log.error("Cannot run conversion 'v550c' - Failed to resolve schema metadata: ", e);
                        }
                        return false;
                    }, jdbcHelper, "v550c");
        }
    }

    private static boolean test(Predicate<DatabaseMetaData> conversionPredicate, JdbcHelper jdbcHelper, String conversionName) {
        try {
            return withMetadata(jdbcHelper, conversionPredicate::test);
        } catch (Exception e) {
            log.error("Cannot run conversion '" + conversionName + "' - Failed to retrieve schema metadata: ", e);
        }
        log.debug("sha256 column in nodes table already exists, skipping '{}' conversion.", conversionName);
        return false;
    }
}
