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

package org.artifactory.aql.result;

import org.artifactory.aql.result.rows.AqlRowResult;

import java.sql.ResultSet;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Autoclose stream of rows.
 * asStream provides the rows.
 * based on whileNextStream which keep return the container and calls next till exhousted.
 * A method onFinish recive exception or nulll on success - can be used to log errors.
 *
 * @author Saffi Hartal
 */
public interface AqlResultSetProvider<T extends AqlRowResult> extends AutoCloseable {
    ResultSet getResultSet();

    /**
     * return stream of this, and calls next()
     *
     * @param onFinish Consumer<Exception>
     */
    default Stream<? extends AqlResultSetProvider<T>> whileNextStream(Consumer<Exception> onFinish) {
        return AqlResultHelper.whileNextStream(this, onFinish);
    }

    Stream<T> asStream(Consumer<Exception> onFinish);

    default void close() throws Exception {
        if (getResultSet() != null) {
            getResultSet().close();
        }
    }
}
