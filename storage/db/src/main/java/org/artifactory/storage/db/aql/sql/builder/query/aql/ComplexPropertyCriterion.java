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
import org.artifactory.aql.AqlFieldResolver;
import org.artifactory.aql.model.AqlComparatorEnum;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlPhysicalFieldEnum;
import org.artifactory.aql.model.AqlVariable;
import org.artifactory.storage.db.aql.sql.builder.query.sql.SqlTable;

import java.util.List;

import static org.artifactory.storage.db.aql.sql.model.SqlTableEnum.build_props;
import static org.artifactory.storage.db.aql.sql.model.SqlTableEnum.node_props;

/**
 * @author Gidi Shabat
 */
public class ComplexPropertyCriterion extends Criterion {

    public ComplexPropertyCriterion(List<AqlDomainEnum> subDomains, AqlVariable variable1, SqlTable table1,
            String comparatorName, AqlVariable variable2, SqlTable table2, boolean mspOperator) {
        super(subDomains, variable1, table1, comparatorName, variable2, table2,mspOperator);
    }

    /**
     * Converts propertyCriteria to Sql criteria
     */
    @Override
    public String toSql(List<Object> params) throws AqlException {
        // Get both variable which are Values (this is property criteria)
        AqlVariable value1 = getVariable1();
        AqlVariable value2 = getVariable2();
        // Get both tables which are node_props tables (this is property criteria)
        SqlTable table1 = getTable1();
        // SqlTable table2 = getTable2();
        // update the Sql input param list
        // Get the ComparatorEnum
        AqlComparatorEnum comparatorEnum = AqlComparatorEnum.value(getComparatorName());
        return createSqlComplexPropertyCriteria(comparatorEnum, value1, table1, value2, params);
    }

    private String createSqlComplexPropertyCriteria(AqlComparatorEnum comparatorEnum, AqlVariable variable1,
                                                   SqlTable table1,
                                                   AqlVariable variable2, List<Object> params) {
        AqlVariable key = AqlFieldResolver.resolve(AqlPhysicalFieldEnum.propertyKey);
        AqlVariable value = AqlFieldResolver.resolve(AqlPhysicalFieldEnum.propertyValue);
        String index1 = table1 != null ? table1.getAlias() : "";
        switch (comparatorEnum) {
            case equals: {
                return "(" + generateEqualsQuery(key, variable1, params, index1) + " and " +
                        generateEqualsQuery(value, variable2, params, index1) + ")";
            }
            case matches: {
                return "(" + generateEqualsQuery(key, variable1, params, index1) + " and " +
                        generateMatchQuery(value, variable2, params, index1) + ")";
            }
            case notMatches: {
                // In case that the query is inside MSP then use simple query without exist(...)
                if(isMspOperator()){
                    return "(" + generateNotMatchQuery(key, variable1, params, index1) + " or " +
                            generateNotMatchQuery(value, variable2, params, index1) + ")";
                } else {
                    Object param1 = resolveParam(variable1);
                    String param2 = resolveParamForSqlLikeQuery(variable2);
                    params.add(param1);
                    params.add(param2);
                    String tableName = table1.getTable().name();
                    String fieldName = node_props == table1.getTable() ? "node_id" :
                            build_props == table1.getTable() ? "build_id" : "module_id";
                    return "(" + isNullSql(index1, fieldName) + " or not exists (select 1 from "
                            + tableName + " where " + fieldToSql(index1, fieldName) + " = " +
                            fieldToSql(null, fieldName) +
                            " and " + fieldToSql(null, "prop_key") + " = " + PARAMETER_PLACEHOLDER +
                            " and " + fieldToSql(null, "prop_value") + " like  " +
                            PARAMETER_PLACEHOLDER + generateEscapingCriteria(param2) + "))";
                }
            }
            case less: {
                return "(" + generateEqualsQuery(key, variable1, params, index1) + " and " +
                        generateLessThanQuery(value, variable2, params, index1) + ")";
            }
            case greater: {
                return "(" + generateEqualsQuery(key, variable1, params, index1) + " and " +
                        generateGreaterThanQuery(value, variable2, params, index1) + ")";
            }
            case greaterEquals: {
                return "(" + generateEqualsQuery(key, variable1, params, index1) + " and " +
                        generateGreaterEqualQuery(value, variable2, params, index1) + ")";
            }
            case lessEquals: {
                return "(" + generateEqualsQuery(key, variable1, params, index1) + " and " +
                        generateLessEqualsQuery(value, variable2, params, index1) + ")";
            }
            case notEquals: {
                // In case that the query is inside MSP then use simple query without exist(...)
                if(isMspOperator()){
                    return "(" + generateNotEqualsQuery(key, variable1, params, index1) + " or " +
                            generateNotEqualsQuery(value, variable2, params, index1) + ")";
                }else {
                    Object param1 = resolveParam(variable1);
                    Object param2 = resolveParam(variable2);
                    params.add(param1);
                    params.add(param2);
                    String tableName = table1.getTable().name();
                    String fieldName = node_props == table1.getTable() ? "node_id" :
                            build_props == table1.getTable() ? "build_id" : "module_id";
                    return "(" + isNullSql(index1, fieldName) + " or not exists (select 1 from " + tableName +
                            " where " +
                            fieldToSql(index1, fieldName) + " = " + fieldToSql(null, fieldName) + " and " +
                            fieldToSql(null, "prop_key") + " = " + PARAMETER_PLACEHOLDER + " and " +
                            fieldToSql(null, "prop_value") + " = " + PARAMETER_PLACEHOLDER + "))";
                }
            }
            default:
                throw new IllegalStateException("Should not reach to the point of code");
        }
    }
}
