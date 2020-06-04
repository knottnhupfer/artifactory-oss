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

package org.artifactory.storage.db.aql.sql.result;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.artifactory.aql.action.AqlAction;
import org.artifactory.aql.model.AqlFieldEnum;
import org.artifactory.aql.model.AqlRepoProvider;
import org.artifactory.aql.model.DomainSensitiveField;
import org.artifactory.aql.result.AqlEagerResult;
import org.artifactory.aql.result.rows.AqlBaseFullRowImpl;
import org.artifactory.aql.result.rows.AqlRowResult;
import org.artifactory.aql.result.rows.populate.PhysicalFieldResultPopulators;
import org.artifactory.aql.result.rows.populate.ResultPopulationContext;
import org.artifactory.aql.result.rows.populate.RowPopulation;
import org.artifactory.aql.result.rows.populate.RowPopulationContext;
import org.artifactory.storage.db.aql.sql.builder.query.sql.SqlQuery;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * In-Memory query result
 *
 * @author Gidi Shabat
 */
public class AqlEagerResultImpl<T extends AqlRowResult> implements AqlEagerResult<T> {

    private List<EagerRowResult> rows = Lists.newArrayList();
    private final AqlAction action;

    public <T extends AqlRowResult> AqlEagerResultImpl(ResultSet resultSet, SqlQuery<T> sqlQuery, AqlRepoProvider repoProvider) throws SQLException {
        action = sqlQuery.getAction();
        long limit = sqlQuery.getLimit();
        ResultPopulationContext resultContext = new ResultPopulationContext(resultSet, sqlQuery.getResultFields(), repoProvider);
        while (resultSet.next() && rows.size() < limit) {
            EagerRowResult row = new EagerRowResult();
            RowPopulationContext populationContext = new RowPopulationContext(resultContext, row);
            RowPopulation.populatePhysicalFields(populationContext, PhysicalFieldResultPopulators.forObjects);
            RowPopulation.populateLogicalFields(populationContext);
            rows.add(row);
        }
    }

    @Override
    public int getSize() {
        return rows.size();
    }

    /**
     * @return True if the result set is empty
     */
    public boolean isEmpty() {
        return rows.isEmpty();
    }

    @Override
    public T getResult(int i) {
        EagerRowResult rowResult = rows.get(i);
        return toTyped(rowResult);
    }

    @Override
    public List<T> getResults() {
        ArrayList<T> result = Lists.newArrayList();
        for (EagerRowResult row : rows) {
            result.add(toTyped(row));
        }
        return result;
    }

    @Override
    public AqlAction getAction() {
        return action;
    }


    @SuppressWarnings("unchecked")
    private T toTyped(EagerRowResult rowResult) {
        return (T) new AqlBaseFullRowImpl(toMapByFieldEnum(rowResult.toMap()));
    }

    private static Map<AqlFieldEnum, Object> toMapByFieldEnum(Map<DomainSensitiveField, Object> map) {
        Map<AqlFieldEnum, Object> newMap = Maps.newHashMap();
        for (Map.Entry<DomainSensitiveField, Object> entry : map.entrySet()) {
            newMap.put(entry.getKey().getField(), entry.getValue());
        }
        return newMap;
    }


}