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

package org.artifactory.storage.db.conversion.version.v202;

import org.artifactory.storage.db.conversion.ConversionPredicate;
import org.artifactory.storage.db.util.JdbcHelper;
import org.jfrog.storage.DbType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiPredicate;

import static org.jfrog.storage.util.DbUtils.columnExists;

/**
 * @author Dan Feldman
 */
public class V202ConversionPredicate implements ConversionPredicate {
    private static final Logger log = LoggerFactory.getLogger(V202ConversionPredicate.class);

    @Override
    public BiPredicate<JdbcHelper, DbType> condition() {
            return (jdbcHelper, dbType) -> {
                try {
                    // run conversion if column credentials_expired is missing from schema
                    return !columnExists(jdbcHelper, dbType, "users", "credentials_expired");
                } catch (Exception e) {
                    log.error("Cannot run conversion 'v202' - Failed to retrieve schema metadata: ", e);
                }
                return false;
            };
    }
}
