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

package org.artifactory.storage.db.aql.sql.builder.query.aql;

import org.artifactory.aql.AqlException;
import org.artifactory.aql.model.AqlComparatorEnum;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlField;
import org.artifactory.aql.model.AqlVariable;
import org.artifactory.storage.db.aql.sql.builder.query.sql.SqlTable;

import java.util.List;

/**
 * This class represent simple criteria which contains field comparator and value
 * For example "_artifact_repo" "$equals" "libs-release-local"
 *
 * @author Gidi Shabat
 */
public class SimplePropertyCriterion extends Criterion {

    public SimplePropertyCriterion(List<AqlDomainEnum> subDomains, AqlVariable variable1, SqlTable table1,
            String comparatorName, AqlVariable variable2, SqlTable table2, boolean mspOperator) {
        super(subDomains, variable1, table1, comparatorName, variable2, table2,mspOperator);
    }

    /**
     * Convert simpleCriteria to sql criteria
     *
     * @throws org.artifactory.aql.AqlException
     */
    @Override
    public String toSql(List<Object> params) throws AqlException {
        // Get both variable which are Field and Value (this is simple criteria)
        AqlVariable variable1 = getVariable1();
        AqlVariable variable2 = getVariable2();
        // Get both tables which are same and equals to the Field table
        SqlTable table1 = getTable1();
        // Add the variables to the input params if needed
        // Convert criteria into Sql
        AqlComparatorEnum comparatorEnum = AqlComparatorEnum.value(getComparatorName());
        return createSqlPropertyCriteria(comparatorEnum, variable1, table1, variable2, params);
    }

    public String createSqlPropertyCriteria(AqlComparatorEnum comparatorEnum, AqlVariable variable1, SqlTable table1,
                                            AqlVariable variable2, List<Object> params) {
        String index1 = table1 != null && variable1 instanceof AqlField ? table1.getAlias() : "";
        switch (comparatorEnum) {
            case equals: {
                return generateEqualsQuery(variable1, variable2, params, index1);
            }
            case matches: {
                return generateMatchQuery(variable1, variable2, params, index1);
            }
            case notMatches: {
                if(isMspOperator()) {
                    return generateNotMatchQuery(variable1, variable2, params, index1);
                }else {
                    return generatePropertyNotMatchQuery(variable1, variable2, params, index1);
                }
            }
            case less: {
                return generateLessThanQuery(variable1, variable2, params, index1);
            }
            case greater: {
                return generateGreaterThanQuery(variable1, variable2, params, index1);
            }
            case greaterEquals: {
                return generateGreaterEqualQuery(variable1, variable2, params, index1);
            }
            case lessEquals: {
                return generateLessEqualsQuery(variable1, variable2, params, index1);
            }
            case notEquals: {
                if(isMspOperator()) {
                    return generateNotEqualsQuery(variable1, variable2, params, index1);
                }else {
                    return generatePropertyNotEqualsQuery(variable1, variable2, params, index1);
                }
            }
            default:
                throw new IllegalStateException("Should not reach to the point of code");
        }
    }
}
