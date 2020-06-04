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

package org.artifactory.aql.result.rows.populate;

import org.artifactory.aql.model.AqlRepoProvider;
import org.artifactory.aql.model.DomainSensitiveField;
import org.artifactory.aql.result.rows.RowResult;

import java.sql.ResultSet;
import java.util.List;

/**
 * @author Yinon Avraham
 */
public class RowPopulationContext {

    private final ResultPopulationContext resultContext;
    private final RowResult row;

    public RowPopulationContext(ResultPopulationContext resultContext, RowResult row) {
        this.resultContext = resultContext;
        this.row = row;
    }

    public ResultSet getResultSet() {
        return resultContext.getResultSet();
    }

    public List<DomainSensitiveField> getResultFields() {
        return resultContext.getResultFields();
    }

    public AqlRepoProvider getRepoProvider() {
        return resultContext.getRepoProvider();
    }

    public List<String> getVirtualRepoKeysContainingRepo(String repoKey) {
        return resultContext.getVirtualRepoKeysContainingRepo(repoKey);
    }

    public RowResult getRow() {
        return row;
    }
}
