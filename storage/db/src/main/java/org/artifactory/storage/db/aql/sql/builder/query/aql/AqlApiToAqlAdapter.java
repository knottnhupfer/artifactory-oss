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

import org.artifactory.aql.AqlFieldResolver;
import org.artifactory.aql.api.AqlApiElement;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.model.*;
import org.artifactory.aql.result.rows.AqlRowResult;
import org.artifactory.storage.db.aql.sql.builder.query.sql.SqlTable;
import org.jfrog.security.util.Pair;

import java.util.List;

/**
 * @author Gidi Shabat
 */
public class AqlApiToAqlAdapter extends AqlAdapter {

    /**
     * Converts Aql (Api) into SqlQuery
     */
    public <T extends AqlRowResult> AqlQuery<T> toAqlModel(AqlBase aqlBase) {
        // Initialize the context
        AdapterContext<T> context = new AdapterContext<>();
        // Set the default operator that is being used if no other operator has been declared.
        context.push(and);
        // Determine whether the end result SQL query should use 'distinct' or not
        context.setDistinct(aqlBase.isDistinct());
        // Recursively visit the aqlBase elements anf fill the AqlQuery
        visitElements(aqlBase, context);
        // Add default filters
        injectDefaultValues(context);
        context.setAction(AqlActionEnum.getAction("find", context.getDomain()));
        return context.getAqlQuery();
    }

    /**
     * Recursively visit the AqlBase parser elements and transform them into AqlQuery Object.
     */
    private void visitElements(AqlApiElement rootElement, AdapterContext context) {
        List<AqlApiElement> list = rootElement.get();
        for (AqlApiElement element : list) {
            if (element instanceof AqlBase.SortApiElement) {
                handleSort((AqlBase.SortApiElement) element, context);
            }
            if (element instanceof AqlBase.DomainApiElement) {
                handleDomain((AqlBase.DomainApiElement) element, context);
            }
            if (element instanceof AqlBase.AndClause) {
                handleAnd((AqlBase.AndClause) element, context);
            }
            if (element instanceof AqlBase.OrClause) {
                handleOr((AqlBase.OrClause) element, context);
            }
            if (element instanceof AqlBase.FreezeJoin) {
                handleMsp((AqlBase.FreezeJoin) element, context);
            }
            if (element instanceof AqlBase.PropertyResultFilterClause) {
                handleResultFilter((AqlBase.PropertyResultFilterClause) element, context);
            }
            if (element instanceof AqlBase.PropertyCriteriaClause) {
                handlePropertyCriteria((AqlBase.PropertyCriteriaClause) element, context);
            }
            if (element instanceof AqlBase.CriteriaClause) {
                handleCriteria((AqlBase.CriteriaClause) element, context);
            }
            if (element instanceof AqlBase.LimitApiElement) {
                handleLimit((AqlBase.LimitApiElement) element, context);
            }
            if (element instanceof AqlBase.OffsetApiElement) {
                handleOffset((AqlBase.OffsetApiElement) element, context);
            }
            if (element instanceof AqlBase.FilterApiElement) {
                visitElements(element, context);
            }
            if (element instanceof AqlBase.IncludeApiElement) {
                handleResultInclude((AqlBase.IncludeApiElement) element, context);
            }
        }
    }

    private void handleResultInclude(AqlBase.IncludeApiElement element, AdapterContext context) {
        // fill context with domain fields
        for (DomainSensitiveField field : element.getResultFields()) {
            context.addField(field);
        }
        // fill context with include fields
        for (DomainSensitiveField field : element.getIncludeFields()) {
            context.addField(field);
        }
    }

    private void handleLimit(AqlBase.LimitApiElement element, AdapterContext context) {
        //Read the limit value from the AqlBase LimitApiElement and put it in the context (AqlQuery)
        context.setLimit(element.getLimit());
    }

    private void handleOffset(AqlBase.OffsetApiElement element, AdapterContext context) {
        //Read the offset value from the AqlBase LimitApiElement and put it in the context (AqlQuery)
        context.setOffset(element.getOffset());
    }

    private void handlePropertyCriteria(AqlBase.PropertyCriteriaClause element, AdapterContext context) {
        // Converts AqlBase propertyCriteriaClause into real PropertyCriteria
        AqlVariable variable1 = AqlFieldResolver.resolve(element.getString1(), AqlVariableTypeEnum.string);
        AqlVariable variable2 = AqlFieldResolver.resolve(element.getString2(), AqlVariableTypeEnum.string);
        List<AqlDomainEnum> subDomains = element.getSubDomains();
        Pair<SqlTable, SqlTable> tables = resolveTableForPropertyCriteria(context, subDomains);
        boolean mspOperator = getMspOperator(context) != null;
        Criterion criterion = new ComplexPropertyCriterion(subDomains, variable1, tables.getFirst(),
                element.getComparator().signature, variable2, tables.getSecond(),mspOperator);
        addCriteria(context, criterion);
    }

    private void handleCriteria(AqlBase.CriteriaClause element, AdapterContext context) {
        // Converts AqlBase CriteriaClause into real criteria
        AqlField criteriaField = AqlFieldResolver.resolve(element.getFieldEnum());
        AqlVariable criteriaValue = AqlFieldResolver.resolve(element.getValue(), element.getFieldEnum().getType());
        Pair<AqlVariable, AqlVariable> variables = new Pair<>(criteriaField, criteriaValue);
        Pair<SqlTable, SqlTable> tables = resolveTableForSimpleCriteria(variables, context);
        boolean mspOperator = getMspOperator(context) != null;
        List<AqlDomainEnum> subDomains = element.getSubDomains();
        Criterion criterion;
        // Create equals criteria
        if (AqlPhysicalFieldEnum.propertyKey == element.getFieldEnum() || AqlPhysicalFieldEnum.propertyValue == element.getFieldEnum()) {
            criterion = new SimplePropertyCriterion(subDomains, criteriaField, tables.getFirst(),
                    element.getComparator().signature, criteriaValue, tables.getSecond(),mspOperator);
        } else {
            criterion = new SimpleCriterion(subDomains, criteriaField, tables.getFirst(),
                    element.getComparator().signature, criteriaValue, tables.getSecond(),mspOperator);
        }
        addCriteria(context, criterion);
    }

    private void handleMsp(AqlBase.FreezeJoin freezeJoin, AdapterContext context) {
        if (freezeJoin.isEmpty()) {
            return;
        }
        // Add operator if needed
        addOperatorToAqlQueryElements(context);
        // Push Join element that contains table index, this index will be used by in all the property tables that are
        // being used inside this function
        context.push(new MspAqlElement(context.provideIndex()));
        context.addAqlQueryElements(open);
        // Recursively visit the internal elements
        visitElements(freezeJoin, context);
        context.addAqlQueryElements(close);
        // Pop the JoinAqlElement, we are getting out from the function
        context.pop();
    }

    private void handleResultFilter(AqlBase.PropertyResultFilterClause flat, AdapterContext context) {
        if (flat.isEmpty()) {
            return;
        }
        // Add operator if needed
        addOperatorToAqlQueryElements(context);
        // Push Join element that contains table index, this index will be used by in all the property tables that are
        // being used inside this function
        context.push(new ResultFilterAqlElement());
        context.addAqlQueryElements(open);
        // Recursively visit the internal elements
        visitElements(flat, context);
        context.addAqlQueryElements(close);
        // Pop the JoinAqlElement, we are getting out from the function
        context.pop();
    }

    private void handleOr(AqlBase.OrClause or, AdapterContext context) {
        if (or.isEmpty()) {
            return;
        }
        // Add operator if needed
        addOperatorToAqlQueryElements(context);
        // Push the or operator
        context.push(AqlAdapter.or);
        context.addAqlQueryElements(open);
        // Recursively visit the internal elements
        visitElements(or, context);
        context.addAqlQueryElements(close);
        // Pop the OrlElement, we are getting out from the function
        context.pop();
    }

    private void handleAnd(AqlBase.AndClause and, AdapterContext context) {
        if (and.isEmpty()) {
            return;
        }
        // Add operator if needed
        addOperatorToAqlQueryElements(context);
        // Push the and operator
        context.push(AqlAdapter.and);
        context.addAqlQueryElements(open);
        // Recursively visit the internal elements
        visitElements(and, context);
        context.addAqlQueryElements(close);
        // Pop the AndlElement, we are getting out from the function
        context.pop();
    }

    private void handleSort(AqlBase.SortApiElement sort, AdapterContext context) {
        // Get the Sort info from the AqlBase SortApiElement and set the info inside the new SortDetails object
        SortDetails sortDetails = new SortDetails();
        if (sort != null && !sort.isEmpty()) {
            sortDetails.setSortType(sort.getSortType());
            List<DomainSensitiveField> fields = sort.getFields();
            for (DomainSensitiveField field : fields) {
                sortDetails.addField((AqlPhysicalFieldEnum) field.getField());
                if(! context.getResultFields().contains(field)){
                    context.getResultFields().add(field);
                }
            }
        }
        context.setSort(sortDetails);
    }

    private void handleDomain(AqlBase.DomainApiElement domain, AdapterContext context) {
        // Get the Main domain info from the AqlBase DomainApiElement and set it in the AqlQuery
        context.setDomain(domain.getDomain());
    }
}
