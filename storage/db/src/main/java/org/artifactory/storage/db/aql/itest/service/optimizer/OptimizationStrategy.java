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

package org.artifactory.storage.db.aql.itest.service.optimizer;

import org.artifactory.aql.model.AqlField;
import org.artifactory.aql.model.AqlOperatorEnum;
import org.artifactory.aql.model.AqlPhysicalFieldEnum;
import org.artifactory.aql.result.rows.AqlRowResult;
import org.artifactory.storage.db.aql.sql.builder.query.aql.*;
import org.artifactory.storage.db.aql.sql.builder.query.sql.SqlTable;

import java.util.List;

/**
 * @author Gidi Shabat
 */
public abstract class OptimizationStrategy {

    public <T extends AqlRowResult> void doJob(AqlQuery<T> aqlQuery) {
        String trasformation = transformToCharacterRepresentation(aqlQuery);
        optimize(aqlQuery,trasformation);
    }

    public abstract <T extends AqlRowResult> void optimize(AqlQuery<T> aqlQuery, String transformation);

    /**
     * AQL to string transformation function (method)
     * This method transforms the AqlQuery into string representation. The "function" is being used to
     * Detect patterns that can be optimize.
     * <p/>
     * Result
     * Each Aql query element is represented by one character:
     * SimpleCriteria on property key           =             k
     * SimpleCriteria on property value         =             v
     * SimpleCriteria on property key (result)  =             K
     * SimpleCriteria on property value (result)=             V
     * SimpleCriteria on any other field        =             c
     * PropertyCriteria                         =             p
     * OpenParenthesisAqlElement                =             (
     * CloseParenthesisAqlElement               =             )
     * OperatorQueryElement - and               =             a
     * OperatorQueryElement - or                =             o
     * MspAqlElement                            =             m
     * ResultFilterAqlElement                   =             r
     */
    private <T extends AqlRowResult> String transformToCharacterRepresentation(AqlQuery<T> aqlQuery) {
        List<AqlQueryElement> aqlElements = aqlQuery.getAqlElements();
        StringBuilder builder = new StringBuilder();
        for (AqlQueryElement aqlElement : aqlElements) {
            if (aqlElement instanceof ComplexPropertyCriterion) {
                builder.append("p");
            }
            if (aqlElement instanceof OpenParenthesisAqlElement) {
                builder.append("(");
            }
            if (aqlElement instanceof CloseParenthesisAqlElement) {
                builder.append(")");
            }
            if (aqlElement instanceof ResultFilterAqlElement) {
                builder.append("r");
            }
            if (aqlElement instanceof MspAqlElement) {
                builder.append("m");
            }
            if (aqlElement instanceof SimpleCriterion || aqlElement instanceof SimplePropertyCriterion) {
                AqlField field = (AqlField) ((Criterion) aqlElement).getVariable1();
                SqlTable table1 = ((Criterion) aqlElement).getTable1();
                if (table1.getId() >= 100) {
                    if (AqlPhysicalFieldEnum.propertyKey == field.getFieldEnum()) {
                        builder.append("k");
                    } else if (AqlPhysicalFieldEnum.propertyValue == field.getFieldEnum()) {
                        builder.append("v");
                    } else {
                        builder.append("c");
                    }
                } else {
                    if (AqlPhysicalFieldEnum.propertyKey == field.getFieldEnum()) {
                        builder.append("K");
                    } else if (AqlPhysicalFieldEnum.propertyValue == field.getFieldEnum()) {
                        builder.append("V");
                    } else {
                        builder.append("C");
                    }
                }
            }
            if (aqlElement instanceof OperatorQueryElement) {
                AqlOperatorEnum operator = ((OperatorQueryElement) aqlElement).getOperatorEnum();
                if (AqlOperatorEnum.and == operator) {
                    builder.append("a");
                } else {
                    builder.append("o");
                }
            }
        }
        return builder.toString();
    }
}
