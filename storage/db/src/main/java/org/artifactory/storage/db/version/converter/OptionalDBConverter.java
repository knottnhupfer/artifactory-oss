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

import org.artifactory.storage.db.conversion.ConversionPredicate;
import org.artifactory.storage.db.util.JdbcHelper;
import org.jfrog.storage.DbType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;

/**
 * Converter that allow failures without throwing an exception on conversion failure.
 * Be careful and fully aware when using this converter type, failures in conversion might lead to future conversion
 * failures as well.
 *
 * @author Shay Bagants
 */
public class OptionalDBConverter extends ConditionalDBSqlConverter {
    private static final Logger log = LoggerFactory.getLogger(OptionalDBConverter.class);
    private final BiConsumer<DbDetails, RuntimeException> runnable;

    public OptionalDBConverter(String fromVersion, ConversionPredicate shouldConvert, BiConsumer<DbDetails, RuntimeException> failFunction) {
        super(fromVersion, shouldConvert);
        this.runnable = failFunction;
    }

    @Override
    public void convert(JdbcHelper jdbcHelper, DbType dbType) {
        try {
            super.convert(jdbcHelper, dbType);
        } catch (RuntimeException e) {
            DbDetails dbDetails = new DbDetails(dbType, jdbcHelper);
            log.error("Caught exception in optional converter for '" + fromVersion + "'", e);
            // catch an exception without throwing it as this is optional converter.
            runnable.accept(dbDetails, e);
        }
    }
}
