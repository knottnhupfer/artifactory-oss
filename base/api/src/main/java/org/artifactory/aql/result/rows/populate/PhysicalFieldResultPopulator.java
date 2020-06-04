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

import org.artifactory.aql.model.AqlItemTypeEnum;
import org.artifactory.aql.model.AqlPhysicalFieldEnum;
import org.artifactory.aql.model.DomainSensitiveField;
import org.joda.time.format.ISODateTimeFormat;

import java.sql.SQLException;
import java.util.Date;
import java.util.function.Function;

/**
 * @author Yinon Avraham
 */
public class PhysicalFieldResultPopulator implements FieldResultPopulator {

    public static final Function<Long, Object> toISODateString = value -> value == 0 ? null : ISODateTimeFormat.dateTime().print(value);
    public static final Function<Long, Object> toDateObject = value -> value == 0 ? null : new Date(value);

    private final Function<Long, Object> dateLongToValueFunction;

    public PhysicalFieldResultPopulator(Function<Long, Object> dateLongToValueFunction) {
        this.dateLongToValueFunction = dateLongToValueFunction;
    }

    @Override
    public void populate(RowPopulationContext populationContext, DomainSensitiveField field) throws SQLException {
        AqlPhysicalFieldEnum physicalFieldEnum = (AqlPhysicalFieldEnum) field.getField();
        String dbFieldName = physicalFieldEnum.name();
        switch (physicalFieldEnum.getType()) {
            case date:
                populateDate(populationContext, field, dbFieldName);
                break;
            case longInt: {
                populateLong(populationContext, field, dbFieldName);
                break;
            }
            case integer: {
                populateInt(populationContext, field, dbFieldName);
                break;
            }
            case string: {
                populateString(populationContext, field, dbFieldName);
                break;
            }
            case itemType: {
                populateItemType(populationContext, field, dbFieldName);
                break;
            }
        }
    }

    private void populateLong(RowPopulationContext populationContext, DomainSensitiveField field, String dbFieldName)
            throws SQLException {
        long value = populationContext.getResultSet().getLong(dbFieldName);
        populationContext.getRow().put(field, value);
    }

    private void populateString(RowPopulationContext populationContext, DomainSensitiveField field, String dbFieldName)
            throws SQLException {
        String value = populationContext.getResultSet().getString(dbFieldName);
        populationContext.getRow().put(field, value);
    }

    private void populateInt(RowPopulationContext populationContext, DomainSensitiveField field, String dbFieldName)
            throws SQLException {
        int value = populationContext.getResultSet().getInt(dbFieldName);
        populationContext.getRow().put(field, value);
    }

    private void populateItemType(RowPopulationContext populationContext, DomainSensitiveField field,
            String dbFieldName) throws SQLException {
        int type = populationContext.getResultSet().getInt(dbFieldName);
        AqlItemTypeEnum aqlItemTypeEnum = AqlItemTypeEnum.fromTypes(type);
        populationContext.getRow().put(field, aqlItemTypeEnum);
    }

    private void populateDate(RowPopulationContext populationContext, DomainSensitiveField field, String dbFieldName)
            throws SQLException {
        Long valueLong = populationContext.getResultSet().getLong(dbFieldName);
        Object value = dateLongToValueFunction.apply(valueLong);
        populationContext.getRow().put(field, value);
    }

}
