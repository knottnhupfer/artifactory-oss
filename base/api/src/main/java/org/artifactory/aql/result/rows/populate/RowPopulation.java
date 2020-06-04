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

import com.google.common.collect.Iterators;
import org.artifactory.aql.model.AqlFieldEnumSwitch;
import org.artifactory.aql.model.AqlLogicalFieldEnum;
import org.artifactory.aql.model.AqlPhysicalFieldEnum;
import org.artifactory.aql.model.DomainSensitiveField;

import java.sql.SQLException;
import java.util.Iterator;

/**
 * @author Yinon Avraham
 */
public final class RowPopulation {

    private RowPopulation() {}

    public static void populatePhysicalFields(RowPopulationContext populationContext, FieldResultPopulator fieldPopulator) throws SQLException {
        Iterator<DomainSensitiveField> resultFields = getResultFields(populationContext, physicalFields);
        while (resultFields.hasNext()) {
            DomainSensitiveField field = resultFields.next();
            fieldPopulator.populate(populationContext, field);
        }
    }

    public static void populateLogicalFields(RowPopulationContext populationContext) throws SQLException {
        Iterator<DomainSensitiveField> resultFields = getResultFields(populationContext, logicalFields);
        while (resultFields.hasNext()) {
            DomainSensitiveField field = resultFields.next();
            FieldResultPopulator populator = LogicalFieldResultPopulators.getPopulator(field);
            populator.populate(populationContext, field);
        }
    }

    private static Iterator<DomainSensitiveField> getResultFields(RowPopulationContext populationContext,
            AqlFieldEnumSwitch<Boolean> fieldSwitch) {
        return Iterators.filter(populationContext.getResultFields().iterator(), field -> field.getField().doSwitch(fieldSwitch));
    }

    private static final AqlFieldEnumSwitch<Boolean> physicalFields = new AqlFieldEnumSwitch<Boolean>() {
        @Override
        public Boolean caseOf(AqlLogicalFieldEnum fieldEnum) {
            return false;
        }
        @Override
        public Boolean caseOf(AqlPhysicalFieldEnum fieldEnum) {
            return true;
        }
    };

    private static final AqlFieldEnumSwitch<Boolean> logicalFields = new AqlFieldEnumSwitch<Boolean>() {
        @Override
        public Boolean caseOf(AqlLogicalFieldEnum fieldEnum) {
            return true;
        }
        @Override
        public Boolean caseOf(AqlPhysicalFieldEnum fieldEnum) {
            return false;
        }
    };

}
