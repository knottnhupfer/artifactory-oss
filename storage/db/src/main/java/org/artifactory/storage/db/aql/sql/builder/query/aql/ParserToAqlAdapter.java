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

import com.google.common.collect.Lists;
import org.artifactory.aql.AqlException;
import org.artifactory.aql.action.AqlAction;
import org.artifactory.aql.action.AqlPropertyAction;
import org.artifactory.aql.model.*;
import org.artifactory.aql.result.rows.AqlRowResult;
import org.artifactory.storage.db.aql.parser.ParserElementResultContainer;
import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.DomainProviderElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.DomainSubPathElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.EmptyIncludeDomainElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.IncludeDomainElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.action.ActionPropertyKeysElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.action.AqlDomainAction;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive.DomainElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive.FunctionElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive.criteria.*;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive.include.IncludeExtensionElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.language.*;
import org.artifactory.storage.db.aql.parser.elements.high.level.language.action.ActionNewValueElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.language.action.ActionPropertyKeysValueElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.language.action.DryRunValueElement;
import org.artifactory.storage.db.aql.sql.builder.query.sql.AqlToSqlQueryBuilderException;
import org.jfrog.security.util.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.artifactory.aql.model.AqlPhysicalFieldEnum.*;

/**
 * Converts the parser results into AqlQuery
 *
 * @author Gidi Shabat
 */
public class ParserToAqlAdapter extends AqlAdapter {

    private final ResultFilterAqlElement resultFilter = new ResultFilterAqlElement();

    /**
     * Converts the parser results into AqlQuery
     * @throws AqlException
     */
    public <T extends AqlRowResult> AqlQuery<T> toAqlModel(ParserElementResultContainer parserResult) throws AqlException {
        // Initialize the context
        ParserToAqlAdapterContext<T> context = new ParserToAqlAdapterContext<>(parserResult.getAll());
        // Set the default operator that is being used if no other operator has been declared.
        context.push(and);
        // Resolve domain inf.
        handleDomainFields(context);
        // Resolve include fields info.
        handleIncludeFields(context);
        // Resolve sort info.
        handleSort(context);
        // Resolve limit info.
        handleLimit(context);
        // Resolve offset info.
        handleOffset(context);
        // Resolve required action
        handleAction(context);
        // Resolve Filter info
        handleFilter(context);
        // Add default filters
        injectDefaultValues(context);
        // Finally the AqlQuery is ready;
        return context.getAqlQuery();
    }

    /**
     * Converts the Criterion from the parser results into Aql criterion
     */
    private void handleFilter(ParserToAqlAdapterContext<? extends AqlRowResult> context) {
        context.resetIndex();
        while (context.hasNext()) {
            Pair<ParserElement, String> element = context.getElement();
            if (element.getFirst() instanceof EqualsCriteriaElement) {
                handleCriteriaEquals(context);
            }
            if (element.getFirst() instanceof DefaultCriteriaElement) {
                handleDefaultCriteria(context);
            }
            if (element.getFirst() instanceof CriteriaEqualsPropertyElement) {
                handleEqualsCriteriaProperty(context);
            }
            if (element.getFirst() instanceof CriteriaEqualsKeyPropertyElement) {
                handleEqualsKeyCriteriaProperty(context);
            }
            if (element.getFirst() instanceof CriteriaEqualsValuePropertyElement) {
                handleEqualsValueCriteriaProperty(context);
            }
            if (element.getFirst() instanceof CriteriaKeyPropertyElement) {
                handleKeyCriteriaProperty(context);
            }
            if (element.getFirst() instanceof CriteriaValuePropertyElement) {
                handleValueCriteriaProperty(context);
            }
            if (element.getFirst() instanceof CriteriaDefaultPropertyElement) {
                handleDefaultCriteriaProperty(context);
            }
            if (element.getFirst() instanceof FunctionElement) {
                handleFunction(context);
            }
            if (element.getFirst() instanceof CloseParenthesisElement) {
                handleCloseParenthesis(context);
            }
            if (element.getFirst() instanceof OpenParenthesisElement) {
                handleOpenParenthesis(context);
            }
            if (element.getFirst() instanceof CriteriaRelativeDateElement) {
                handleRelativeDateCriteria(context);
            }
            if (element.getFirst() instanceof SectionEndElement || element.getFirst() instanceof IncludeTypeElement) {
                return;
            }
            // Promote element
            context.decrementIndex(1);
        }
    }

    private void handleOpenParenthesis(ParserToAqlAdapterContext context) {
        // Add parenthesis element to the AqlQuery
        context.addAqlQueryElements(open);
    }

    private void handleIncludeFields(ParserToAqlAdapterContext<? extends AqlRowResult> context) {
        // Initialize the context
        gotoElement(IncludeExtensionElement.class, context);
        if (!context.hasNext()) {
            return;
        }
        // Scan all the include domain anf fields
        context.decrementIndex(1);
        boolean first = false;
        // Prepare the context for include property filter do not worry about empty parenthesis because the AqlOptimizer will clean it
        context.push(or);
        context.push(resultFilter);
        context.addAqlQueryElements(open);
        while (!(context.getElement().getFirst() instanceof SectionEndElement)) {
            // Resolve the field sub domains
            List<AqlDomainEnum> subDomains = resolveSubDomains(context);
            if (context.getElement().getFirst() instanceof RealFieldElement) {
                // Extra field
                first = handleIncludeExtraField(context, first, subDomains);
            } else if (context.getElement().getFirst() instanceof IncludeDomainElement ||
                    context.getElement().getFirst() instanceof EmptyIncludeDomainElement) {
                // Extra domain
                handleIncludeDomain(context, subDomains);
            } else {
                //Extra property result filter
                handleIncludePropertyKeyFilter(context, subDomains);
            }
        }
        context.addAqlQueryElements(close);
        context.pop();
        context.pop();
    }

    /**
     * If the extra field belongs to the main query domain then remove all the default fields and add only the fields from
     * the include section.
     * If the extra field doesn't belongs to the main domain then just add the field to the result fields.
     */
    private boolean handleIncludeExtraField(ParserToAqlAdapterContext<? extends AqlRowResult> context, boolean overrideFields,
                                            List<AqlDomainEnum> subDomains) {
        // If the extra field belongs to the main domain then remove all the default fields ()
        if (!overrideFields && subDomains.size() == 1) {
            AqlDomainEnum mainDomain = subDomains.get(0);
            List<DomainSensitiveField> resultFields = context.getResultFields();
            Iterator<DomainSensitiveField> iterator = resultFields.iterator();
            while (iterator.hasNext()) {
                DomainSensitiveField next = iterator.next();
                AqlDomainEnum aqlDomainEnum = next.getField().getDomain();
                if (aqlDomainEnum.equals(mainDomain)) {
                    iterator.remove();
                }
            }
            overrideFields = true;
        }
        AqlFieldEnum aqlField = resolveField(context);
        DomainSensitiveField field = new DomainSensitiveField(aqlField, subDomains);
        context.addField(field);
        context.decrementIndex(1);
        return overrideFields;
    }

    /**
     * Special case for properties that allows to  add property key to return specific property
     */
    private void handleIncludePropertyKeyFilter(ParserToAqlAdapterContext<? extends AqlRowResult> context, List<AqlDomainEnum> subDomains) {
        AqlDomainEnum aqlDomainEnum = subDomains.get(subDomains.size()-1);
        AqlPhysicalFieldEnum propKeyField = null;
        AqlPhysicalFieldEnum propValueField = null;
        switch (aqlDomainEnum){
            case properties:
                propKeyField = propertyKey;
                propValueField = propertyValue;
                break;
            case moduleProperties:
                propKeyField = modulePropertyKey;
                propValueField = modulePropertyValue;
                break;
            case buildProperties:
                propKeyField = buildPropertyKey;
                propValueField = buildPropertyValue;
                break;
        }
        String value = context.getElement().getSecond();
        addIncludePropertyKeyFilter(context, subDomains, propKeyField, propValueField, value);
        context.decrementIndex(1);
    }

    private void addIncludePropertyKeyFilter(ParserToAqlAdapterContext context, List<AqlDomainEnum> subDomains,
            AqlPhysicalFieldEnum propKeyField, AqlPhysicalFieldEnum propValueField, String value) {
        context.addField(new DomainSensitiveField(propKeyField, subDomains));
        context.addField(new DomainSensitiveField(propValueField, subDomains));
        AqlComparatorEnum comparatorEnum = AqlComparatorEnum.equals;
        // Only if the user has specify property key to filter then add the filter else just add the fields.
        if (!"*".equals(value)) {
            Criterion criterion = createSimpleCriteria(subDomains, propKeyField, value, comparatorEnum, context);
            addCriteria(context, criterion);
        }
    }

    /**
     * Allows to add the domain fields to the result fields
     */
    private void handleIncludeDomain(ParserToAqlAdapterContext<? extends AqlRowResult> context, List<AqlDomainEnum> subDomains) {
        DomainProviderElement element = (DomainProviderElement) context.getElement().getFirst();
        AqlDomainEnum aqlDomainEnum = element.getDomain();
        AqlFieldEnum[] fieldByDomain = aqlDomainEnum.getAllFields();
        for (AqlFieldEnum aqlFieldEnum : fieldByDomain) {
            boolean alreadyExist = false;
            for (DomainSensitiveField domainSensitiveField : context.getResultFields()) {
                if (domainSensitiveField.getField() == aqlFieldEnum) {
                    alreadyExist = true;
                }
            }
            if (!alreadyExist) {
                context.addField(new DomainSensitiveField(aqlFieldEnum, subDomains));
            }
        }
        context.decrementIndex(1);
    }

    /**
     * Allows to add limit to the query in order to limit the number of rows returned
     */
    private void handleLimit(ParserToAqlAdapterContext<? extends AqlRowResult> context) {
        gotoElement(LimitValueElement.class, context);
        if (!context.hasNext()) {
            return;
        }
        // Get the limit value from the element and set it in the context (AqlQuery)
        Pair<ParserElement, String> element = context.getElement();
        int limit = Double.valueOf(element.getSecond()).intValue();
        context.setLimit(limit);
    }


    /**
     * Allows to add limit to the query in order to limit the number of rows returned
     */
    private void handleOffset(ParserToAqlAdapterContext<? extends AqlRowResult> context) {
        gotoElement(OffsetValueElement.class, context);
        if (!context.hasNext()) {
            return;
        }
        // Get the offset value from the element and set it in the context (AqlQuery)
        Pair<ParserElement, String> element = context.getElement();
        int offset = Double.valueOf(element.getSecond()).intValue();
        context.setOffset(offset);
    }

    private void handleAction(ParserToAqlAdapterContext<? extends AqlRowResult> context) {
        gotoAction(context);
        if (!context.hasNext()) {
            return;
        }
        Pair<ParserElement, String> element = context.getElement();
        String actionName = element.getSecond();
        AqlDomainEnum domain = context.getDomain();
        AqlAction action = AqlActionEnum.getAction(actionName, domain);
        if (action == null) {
            throw new AqlException("Unsupported action '" + actionName + "' for domain " + domain + ".");
        }
        assignActionValue(context, action);
        assignActionPropKeys(context, action);
        markDryRunAction(context, action);
        context.setAction(action);
    }

    // Sets value for Aql actions which require it.
    private void assignActionValue(ParserToAqlAdapterContext<? extends AqlRowResult> context, AqlAction action) {
        if (AqlPropertyAction.class.isAssignableFrom(action.getClass())) {
            gotoElement(ActionNewValueElement.class, context);
            if (context.hasNext()) {
                Pair<ParserElement, String> element = context.getElement();
                ((AqlPropertyAction) action).setValue(element.getSecond());
            } else {
                throw new AqlException("Action '" + action.getName() + "' for domain " + context.getDomain()
                        + " requires a new value to set, but none was given using the 'newValue' directive.");
            }
        }
    }

    // Sets affected property keys for actions which require them.
    private void assignActionPropKeys(ParserToAqlAdapterContext<? extends AqlRowResult> context, AqlAction action) {
        if (!AqlPropertyAction.class.isAssignableFrom(action.getClass())) {
            return;
        }
        gotoElement(ActionPropertyKeysElement.class, context);
        if (!context.hasNext()) {
            throw new AqlException("Action '" + action.getName() + "' for domain " + context.getDomain()
                    + " requires a property key to set, but none was given using the 'keys' directive.");
        }
        AqlPropertyAction propAction = (AqlPropertyAction) action;
        context.decrementIndex(1);
        while (!(context.getElement().getFirst() instanceof SectionEndElement) && context.hasNext()) {
            if (context.getElement().getFirst() instanceof ActionPropertyKeysValueElement) {
                context.decrementIndex(1);
                propAction.addKey(context.getElement().getSecond());
            }
            context.decrementIndex(1);
        }
        addForceInclusionsForPropertyAction(context, propAction);

    }

    private void addForceInclusionsForPropertyAction(ParserToAqlAdapterContext context, AqlPropertyAction propAction) {
        // Force inclusion of item repo/path/name fields for property actions.
        ArrayList<AqlDomainEnum> itemPropsDomain = Lists.newArrayList(AqlDomainEnum.properties, AqlDomainEnum.items);
        context.addField(new DomainSensitiveField(AqlPhysicalFieldEnum.itemRepo, itemPropsDomain));
        context.addField(new DomainSensitiveField(AqlPhysicalFieldEnum.itemPath, itemPropsDomain));
        context.addField(new DomainSensitiveField(AqlPhysicalFieldEnum.itemName, itemPropsDomain));
        // Include only required properties to get just them in the result
        ArrayList<AqlDomainEnum> propsDomain = Lists.newArrayList(AqlDomainEnum.properties);
        context.push(or);
        context.push(resultFilter);
        context.addAqlQueryElements(open);
        propAction.getKeys()
                .forEach(propKey -> addIncludePropertyKeyFilter(context, propsDomain, propertyKey, propertyValue, propKey));
        context.addAqlQueryElements(close);
        context.pop();
        context.pop();
    }

    // Mark as dry run if needed
    private void markDryRunAction(ParserToAqlAdapterContext<? extends AqlRowResult> context, AqlAction action) {
        gotoElement(DryRunValueElement.class, context);
        if (context.hasNext()) {
            Pair<ParserElement, String> element = context.getElement();
            action.setDryRun(Boolean.parseBoolean(element.getSecond()));
        }
    }

    private void handleCloseParenthesis(ParserToAqlAdapterContext context) {
        // Pop operator from operator queue
        context.pop();
        // Push close parenthesis element to context (AqlQuery)
        context.addAqlQueryElements(close);
    }

    /**
     * Handles operator "and"/"or" operators and the "freezeJoin"/"resultFields" functions
     */
    private void handleFunction(ParserToAqlAdapterContext<? extends AqlRowResult> context) {
        Pair<ParserElement, String> element = context.getElement();
        AqlOperatorEnum function = AqlOperatorEnum.value(element.getSecond());
        // Add leading operator if needed
        addOperatorToAqlQueryElements(context);
        if (AqlOperatorEnum.freezeJoin == function) {
            // In case of freeze join function generate new alias index for the properties tables
            // All the criterias that uses property table inside the function will use the same table.
            // Push freezeJoin to the operators queue
            context.push(new MspAqlElement(context.provideIndex()));
        } else if (AqlOperatorEnum.and == function) {
            // Push or and the operators queue
            context.push(and);
        } else if (AqlOperatorEnum.or == function) {
            // Push or to the operators queue
            context.push(or);
        } else if (AqlOperatorEnum.resultFilter == function) {
            // Push or to the operators queue
            context.push(resultFilter);
        }
    }


    private void handleCriteriaEquals(ParserToAqlAdapterContext<? extends AqlRowResult> context) throws AqlException {
        // Remove element from parser result elements
        context.decrementIndex(1);
        // Get the field sub domains
        List<AqlDomainEnum> subDomain = resolveSubDomains(context);
        // Get the criteria first variable
        AqlPhysicalFieldEnum aqlField = resolvePhysicalField(context);
        // Remove element from parser result elements
        context.decrementIndex(1);
        // Get the criteria second variable
        String variable = resolveVariable(context);
        // Create equals criteria
        if (AqlPhysicalFieldEnum.propertyKey == aqlField || AqlPhysicalFieldEnum.propertyValue == aqlField||
                AqlPhysicalFieldEnum.modulePropertyKey == aqlField || AqlPhysicalFieldEnum.modulePropertyValue == aqlField||
                AqlPhysicalFieldEnum.buildPropertyKey == aqlField || AqlPhysicalFieldEnum.buildPropertyValue == aqlField) {
            Criterion criterion = createSimplePropertyCriteria(subDomain, aqlField, variable, AqlComparatorEnum.equals,
                    context);
            addCriteria(context, criterion);
        } else {
            Criterion criterion = createSimpleCriteria(subDomain, aqlField, variable, AqlComparatorEnum.equals, context);
            addCriteria(context, criterion);
        }
    }

    private void handleDefaultCriteria(ParserToAqlAdapterContext<? extends AqlRowResult> context) throws AqlException {
        // Remove element from parser result elements
        context.decrementIndex(1);
        // Get the field sub domains
        List<AqlDomainEnum> subDomains = resolveSubDomains(context);
        // Get the criteria first field
        AqlPhysicalFieldEnum aqlField = resolvePhysicalField(context);
        // Remove element from parser result elements
        context.decrementIndex(1);
        // Get the criteria comparator
        AqlComparatorEnum comparatorEnum = AqlComparatorEnum.value(context.getElement().getSecond());
        // Remove element from parser result elements
        context.decrementIndex(1);
        // Get the criteria second variable
        String name2 = resolveVariable(context);
        // Create criteria
        if (AqlPhysicalFieldEnum.propertyKey == aqlField || AqlPhysicalFieldEnum.propertyValue == aqlField||
                AqlPhysicalFieldEnum.buildPropertyKey == aqlField || AqlPhysicalFieldEnum.buildPropertyValue == aqlField||
                AqlPhysicalFieldEnum.modulePropertyKey == aqlField || AqlPhysicalFieldEnum.modulePropertyValue == aqlField) {
            Criterion criterion = createSimplePropertyCriteria(subDomains, aqlField, name2, comparatorEnum, context);
            addCriteria(context, criterion);
        } else {
            Criterion criterion = createSimpleCriteria(subDomains, aqlField, name2, comparatorEnum, context);
            addCriteria(context, criterion);
        }
    }

    private void handleRelativeDateCriteria(ParserToAqlAdapterContext<? extends AqlRowResult> context) {
        // Remove element from parser result elements
        context.decrementIndex(1);
        // Get the field sub domains
        List<AqlDomainEnum> subDomains = resolveSubDomains(context);
        // Get the criteria first field
        AqlPhysicalFieldEnum aqlField = resolvePhysicalField(context);
        // Remove element from parser result elements
        context.decrementIndex(1);
        // Get the criteria comparator
        AqlRelativeDateComparatorEnum relativeDateComparatorEnum = AqlRelativeDateComparatorEnum.value(context.getElement().getSecond());
        AqlComparatorEnum aqlComparatorEnum = relativeDateComparatorEnum.aqlComparatorEnum;
        // Remove element from parser result elements
        context.decrementIndex(1);
        // Get the criteria second variable
        String name2 = resolveVariable(context);
        long dateLong = relativeDateComparatorEnum.toDate(name2);
        if(dateLong<0){
            throw new AqlException("Invalid relative date format for: "+name2);
        }
        String date = ""+ dateLong;
        // Create criteria
        Criterion criterion = createSimpleCriteria(subDomains, aqlField, date, aqlComparatorEnum, context);
        addCriteria(context, criterion);
    }

    private String resolveVariable(ParserToAqlAdapterContext<? extends AqlRowResult> context) {
        String second = context.getElement().getSecond();
        if ("null".equals(second.toLowerCase()) && context.getElement().getFirst() instanceof NullElement) {
            second = null;
        }
        return second;
    }

    private void handleDefaultCriteriaProperty(ParserToAqlAdapterContext<? extends AqlRowResult> context) throws AqlException {
        // Remove element from parser result elements
        context.decrementIndex(1);
        // Get the field sub domains
        List<AqlDomainEnum> subDomains = resolveSubDomains(context);
        // Get the criteria first variable
        String name1 = context.getElement().getSecond();
        // Remove element from parser result elements
        context.decrementIndex(1);
        // Get the criteria comparator
        AqlComparatorEnum comparatorEnum = AqlComparatorEnum.value(context.getElement().getSecond());
        // Remove element from parser result elements
        context.decrementIndex(1);
        // Get the criteria second variable
        String name2 = context.getElement().getSecond();
        // Create criteria
        boolean mspOperator = getMspOperator(context) != null;
        Criterion criterion = createComplexPropertyCriteria(subDomains, name1, name2, comparatorEnum, context,mspOperator);
        addCriteria(context, criterion);
    }

    private void handleKeyCriteriaProperty(ParserToAqlAdapterContext<? extends AqlRowResult> context) throws AqlException {
        // Remove element from parser result elements
        context.decrementIndex(1);
        // Get the field sub domains
        List<AqlDomainEnum> subDomains = resolveSubDomains(context);
        // Get the criteria first variable
        String name1 = context.getElement().getSecond();
        // Remove element from parser result elements
        context.decrementIndex(1);
        // Get the criteria comparator
        AqlComparatorEnum comparatorEnum = AqlComparatorEnum.value(context.getElement().getSecond());
        // Remove element from parser result elements
        context.decrementIndex(1);
        AqlDomainEnum aqlDomainEnum = subDomains.get(subDomains.size()-1);
        AqlPhysicalFieldEnum propKeyField=null;
        switch (aqlDomainEnum){
            case properties:propKeyField=propertyKey;break;
            case moduleProperties:propKeyField=modulePropertyKey;break;
            case buildProperties:propKeyField=buildPropertyKey;break;
        }
        // Create criteria
        Criterion criterion = createSimplePropertyCriteria(subDomains, propKeyField, name1, comparatorEnum, context);
        addCriteria(context, criterion);
    }

    private void handleValueCriteriaProperty(ParserToAqlAdapterContext<? extends AqlRowResult> context) throws AqlException {
        // Remove element from parser result elements
        context.decrementIndex(1);
        // Get the field sub domains
        List<AqlDomainEnum> subDomains = resolveSubDomains(context);
        // Get the criteria first variable
        context.decrementIndex(1);
        // Get the criteria comparator
        AqlComparatorEnum comparatorEnum = AqlComparatorEnum.value(context.getElement().getSecond());
        // Remove element from parser result elements
        context.decrementIndex(1);
        // Get the criteria second variable
        String name2 = context.getElement().getSecond();
        AqlDomainEnum aqlDomainEnum = subDomains.get(subDomains.size()-1);
        AqlPhysicalFieldEnum propValueField=null;
        switch (aqlDomainEnum){
            case properties:propValueField=propertyValue;break;
            case moduleProperties:propValueField=modulePropertyValue;break;
            case buildProperties:propValueField=buildPropertyValue;break;
        }
        // Create criteria
        Criterion criterion = createSimplePropertyCriteria(subDomains, propValueField, name2, comparatorEnum, context);
        addCriteria(context, criterion);
    }

    private void handleEqualsCriteriaProperty(ParserToAqlAdapterContext<? extends AqlRowResult> context) throws AqlException {
        // Remove element from parser result elements
        context.decrementIndex(1);
        // Get the field sub domains
        List<AqlDomainEnum> subDomains = resolveSubDomains(context);
        // Get the criteria first variable
        String name1 = context.getElement().getSecond();
        // Remove element from parser result elements
        context.decrementIndex(1);
        // Get the criteria second variable
        String name2 = context.getElement().getSecond();
        // Create equals criteria
        boolean mspOperator = getMspOperator(context) != null;
        Criterion criterion = createComplexPropertyCriteria(subDomains, name1, name2, AqlComparatorEnum.equals, context, mspOperator);
        addCriteria(context, criterion);
    }

    private void handleEqualsKeyCriteriaProperty(ParserToAqlAdapterContext<? extends AqlRowResult> context) throws AqlException {
        // Remove element from parser result elements
        context.decrementIndex(1);
        // Get the field sub domains
        List<AqlDomainEnum> subDomains = resolveSubDomains(context);
        // Get the criteria first variable
        String name1 = context.getElement().getSecond();
        // Remove element from parser result elements
        context.decrementIndex(1);
        AqlDomainEnum aqlDomainEnum = subDomains.get(subDomains.size()-1);
        AqlPhysicalFieldEnum propKeyField=null;
        switch (aqlDomainEnum){
            case properties:
                propKeyField = propertyKey;
                break;
            case moduleProperties:
                propKeyField = modulePropertyKey;
                break;
            case buildProperties:
                propKeyField = buildPropertyKey;
                break;
        }
        // Create equals criteria
        Criterion criterion = createSimplePropertyCriteria(subDomains, propKeyField, name1, AqlComparatorEnum.equals,
                context);
        addCriteria(context, criterion);
    }

    private void handleEqualsValueCriteriaProperty(ParserToAqlAdapterContext<? extends AqlRowResult> context) throws AqlException {
        // Remove element from parser result elements
        context.decrementIndex(1);
        // Get the field sub domains
        List<AqlDomainEnum> subDomains = resolveSubDomains(context);
        // Remove element from parser result elements
        context.decrementIndex(1);
        // Get the criteria second variable
        String name2 = context.getElement().getSecond();
        AqlDomainEnum aqlDomainEnum = subDomains.get(subDomains.size()-1);
        AqlPhysicalFieldEnum propValueField=null;
        switch (aqlDomainEnum){
            case properties:
                propValueField = propertyValue;
                break;
            case moduleProperties:
                propValueField = modulePropertyValue;
                break;
            case buildProperties:
                propValueField = buildPropertyValue;
                break;
        }
        // Create equals criteria
        Criterion criterion = createSimplePropertyCriteria(subDomains, propValueField, name2, AqlComparatorEnum.equals, context);
        addCriteria(context, criterion);
    }

    private void handleSort(ParserToAqlAdapterContext<? extends AqlRowResult> context) throws AqlToSqlQueryBuilderException {
        gotoElement(SortTypeElement.class, context);
        if (!context.hasNext()) {
            return;
        }
        // Resolve the sortType from the element
        Pair<ParserElement, String> element = context.getElement();
        AqlSortTypeEnum sortTypeEnum = AqlSortTypeEnum.fromAql(element.getSecond());
        SortDetails sortDetails = new SortDetails();
        // Remove two elements from parser result elements
        context.decrementIndex(2);
        Pair<ParserElement, String> currentElement = context.getElement();
        // Get all the sort elements from the following parser elements
        while (!(currentElement.getFirst() instanceof CloseParenthesisElement)) {
            // Resolve the sub domains just to increment the pointer to the field position
            resolveSubDomains(context);
            AqlPhysicalFieldEnum field = resolvePhysicalField(context);
            // Remove element from parser result elements
            context.decrementIndex(1);
            sortDetails.addField(field);
            // Get the current element;
            currentElement = context.getElement();
        }
        sortDetails.setSortType(sortTypeEnum);
        // Set the sort details in the context (AqlQuery)
        context.setSort(sortDetails);
    }

    private List<AqlDomainEnum> resolveSubDomains(ParserToAqlAdapterContext<? extends AqlRowResult> context) {
        Pair<ParserElement, String> element = context.getElement();
        List<AqlDomainEnum> list = Lists.newArrayList();
        while (!(element.getFirst() instanceof RealFieldElement || element.getFirst() instanceof ValueElement ||
                element.getFirst() instanceof StarElement ||
                element.getFirst() instanceof IncludeDomainElement
                || element.getFirst() instanceof EmptyIncludeDomainElement)) {
            list.add(((DomainProviderElement) element.getFirst()).getDomain());
            context.decrementIndex(1);
            element = context.getElement();
        }
        if (element.getFirst() instanceof EmptyIncludeDomainElement) {
            list.add(((DomainProviderElement) element.getFirst()).getDomain());
        }
        return list;
    }

    private AqlPhysicalFieldEnum resolvePhysicalField(ParserToAqlAdapterContext<? extends AqlRowResult> context) {
        Pair<ParserElement, String> element = context.getElement();
        String fieldName = element.getSecond();
        AqlDomainEnum domain = ((DomainProviderElement) context.getElement().getFirst()).getDomain();
        return domain.resolvePhysicalField(fieldName);
    }

    private AqlFieldEnum resolveField(ParserToAqlAdapterContext<? extends AqlRowResult> context) {
        Pair<ParserElement, String> element = context.getElement();
        String fieldName = element.getSecond();
        AqlDomainEnum domain = ((DomainProviderElement) context.getElement().getFirst()).getDomain();
        return domain.resolveField(fieldName);
    }


    private void handleDomainFields(ParserToAqlAdapterContext<? extends AqlRowResult> context) {
        // resolve the result fields from the domain and add the field to the context (AqlQuery)
        gotoElement(DomainElement.class, context);
        context.decrementIndex(1);
        Pair<ParserElement, String> element = context.getElement();
        ArrayList<String> subdomains = Lists.newArrayList();
        while (element.getFirst() instanceof DomainSubPathElement) {
            subdomains.add(element.getSecond());
            context.decrementIndex(1);
            element = context.getElement();
        }
        AqlDomainEnum domain = AqlDomainEnum.valueFromSubDomains(subdomains);
        context.setDomain(domain);
        for (AqlFieldEnum field : domain.getDefaultResultFields()) {
            context.addField(new DomainSensitiveField(field, Lists.newArrayList(domain)));
        }
    }

    private void gotoElement(Class<? extends ParserElement> domainElementClass, ParserToAqlAdapterContext context) {
        context.resetIndex();
        while (context.hasNext() && (!context.getElement().getFirst().getClass().equals(domainElementClass))) {
            context.decrementIndex(1);
        }
    }

    private void gotoAction(ParserToAqlAdapterContext<? extends AqlRowResult> context) {
        context.resetIndex();
        while (context.hasNext() && (!AqlDomainAction.class.isAssignableFrom(context.getElement().getFirst().getClass()))) {
            context.decrementIndex(1);
        }
    }
}