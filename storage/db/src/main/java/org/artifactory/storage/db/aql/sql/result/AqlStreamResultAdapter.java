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

import com.google.common.collect.Maps;
import org.artifactory.aql.action.AqlAction;
import org.artifactory.aql.model.AqlFieldEnum;
import org.artifactory.aql.model.DomainSensitiveField;
import org.artifactory.aql.result.AqlResultSetProvider;
import org.artifactory.aql.result.rows.AqlBaseFullRowImpl;
import org.artifactory.aql.result.rows.AqlRowResult;
import org.artifactory.aql.result.rows.populate.PhysicalFieldResultPopulators;
import org.artifactory.aql.result.rows.populate.ResultPopulationContext;
import org.artifactory.aql.result.rows.populate.RowPopulation;
import org.artifactory.aql.result.rows.populate.RowPopulationContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author Saffi Hartal
 */
public class AqlStreamResultAdapter<T extends AqlRowResult> implements AqlResultSetProvider<T> {

    private final AqlAction action;
    private final ResultPopulationContext resultContext;
    private final ResultSet resultSet;

    public AqlStreamResultAdapter(ResultSet resultSet, ResultPopulationContext resultContext, AqlAction action) {
        this.action = action;
        this.resultContext = resultContext;
        this.resultSet = resultSet;
    }

    private static Map<AqlFieldEnum, Object> toMapByFieldEnum(Map<DomainSensitiveField, Object> map) {
        Map<AqlFieldEnum, Object> newMap = Maps.newHashMap();
        for (Map.Entry<DomainSensitiveField, Object> entry : map.entrySet()) {
            newMap.put(entry.getKey().getField(), entry.getValue());
        }
        return newMap;
    }

    @SuppressWarnings("WeakerAccess")
    public AqlBaseFullRowImpl toRowFullRow(EagerRowResult row) {
        return new AqlBaseFullRowImpl(toMapByFieldEnum(row.toMap()));
    }

    @Override
    public ResultSet getResultSet() {
        return resultSet;
    }


    public Stream<T> asStream(Consumer<Exception> onFinish) {
        return whileNextStream(onFinish)
                .map(it -> this.getRow(onFinish))
                .map(this::toRowFullRow)
                .map(it -> (T) it);
    }

    public AqlAction getAction() {
        return action;
    }

    private EagerRowResult getRow(Consumer<Exception> onFinish) {
        try {
            return getRow();
        } catch (SQLException e) {
            onFinish.accept(e);
            return null;
        }
    }

    private EagerRowResult getRow() throws SQLException {
        EagerRowResult rowResult = new EagerRowResult();
        RowPopulationContext populationContext = new RowPopulationContext(resultContext, rowResult);

        RowPopulation.populatePhysicalFields(populationContext, PhysicalFieldResultPopulators.forObjects);
        RowPopulation.populateLogicalFields(populationContext);
        return rowResult;
    }
}