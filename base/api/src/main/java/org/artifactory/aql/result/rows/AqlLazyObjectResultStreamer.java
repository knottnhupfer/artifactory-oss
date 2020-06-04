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

package org.artifactory.aql.result.rows;

import org.artifactory.aql.result.AqlLazyResult;
import org.artifactory.aql.result.rows.populate.PhysicalFieldResultPopulators;
import org.artifactory.aql.result.rows.populate.ResultPopulationContext;
import org.artifactory.aql.result.rows.populate.RowPopulation;
import org.artifactory.aql.result.rows.populate.RowPopulationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;

/**
 * @author gidis
 */
public class AqlLazyObjectResultStreamer<T extends RowResult> {
    private static final Logger log = LoggerFactory.getLogger(AqlLazyObjectResultStreamer.class);

    private final ResultSet resultSet;
    private final Class<T> rowClass;
    private final ResultPopulationContext resultContext;

    public AqlLazyObjectResultStreamer(AqlLazyResult<? extends AqlRowResult> aqlLazyResult, Class<T> rowClass) {
        this.rowClass = rowClass;
        this.resultSet = aqlLazyResult.getResultSet();
        this.resultContext = new ResultPopulationContext(resultSet, aqlLazyResult.getFields(), aqlLazyResult.getRepoProvider());
    }

    public T getRow() {
        try {
            T row = rowClass.newInstance();
            if (resultSet.next()) {
                RowPopulationContext populationContext = new RowPopulationContext(resultContext, row);
                RowPopulation.populatePhysicalFields(populationContext, PhysicalFieldResultPopulators.forObjects);
                RowPopulation.populateLogicalFields(populationContext);
                return row;
            }
        } catch (Exception e) {
            log.error("Fail to create row: ", e);
        }
        return null;
    }
}

