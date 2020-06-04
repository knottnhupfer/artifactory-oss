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

package org.artifactory.aql;

import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.model.AqlOperatorEnum;
import org.artifactory.aql.result.AqlComposedResult;
import org.artifactory.aql.result.AqlEagerResult;
import org.artifactory.aql.result.AqlLazyResult;
import org.artifactory.aql.result.rows.AqlRowResult;

/**
 * @author Gidi Shabat
 */
public interface AqlService {

    /**
     * Parse the AQL query,
     * convert the parser result into Aql query,
     * convert the Aql query to sql query
     * and finally execute the query lazy
     */
    AqlLazyResult<AqlRowResult> executeQueryLazy(String query);

    /**
     * Parse the input AQL query and merge it with the extensionQuery using the operatorEnum operator.
     *
     * @param originalMainQuery The original query provided by the user
     * @param operatorEnum      The operator to merge the queries with
     * @param extensionQuery    The extension query to merge with the original query
     * @return AqlComposedResult of the search
     */
    <T extends AqlRowResult> AqlComposedResult executeQueryLazy(String originalMainQuery, AqlOperatorEnum operatorEnum,
                                                                AqlBase<? extends AqlBase, T> extensionQuery);

    /**
     * Parse the AQL query,
     * convert the parser result into AqlApi query,
     * convert the AqlApi query to sql query
     * and finally execute the query eagerly
     */
    AqlEagerResult executeQueryEager(String query);

    /**
     * Converts the AQL API QUERY into aqlApi query,
     * then convert the aqlApi query into SQL query,
     * and finally execute the query eagerly
     */

    <T extends AqlRowResult> AqlEagerResult<T> executeQueryEager(AqlBase<? extends AqlBase, T> aqlBase);

    <T extends AqlRowResult> AqlLazyResult<T> executeQueryLazy(AqlBase<? extends AqlBase, T> query);

}
