package org.artifactory.storage.db.aql.itest.service.decorator;

import com.google.common.collect.Lists;
import org.artifactory.aql.AqlFieldResolver;
import org.artifactory.aql.model.*;
import org.artifactory.aql.result.rows.AqlRowResult;
import org.artifactory.storage.db.aql.sql.builder.links.TableLink;
import org.artifactory.storage.db.aql.sql.builder.query.aql.*;
import org.artifactory.storage.db.aql.sql.builder.query.sql.type.AqlTableGraph;
import org.artifactory.storage.db.aql.sql.model.SqlTableEnum;

import java.util.List;

import static org.artifactory.aql.model.AqlPhysicalFieldEnum.itemRepo;

/**
 * @author Tamir Hadad
 */
abstract class SpecialReposDecorator implements DecorationStrategy {
    @Override
    public <T extends AqlRowResult> void decorate(AqlQuery<T> aqlQuery, AqlQueryDecoratorContext decoratorContext) {
        filterIfNeeded(aqlQuery);
    }

    private <T extends AqlRowResult> void filterIfNeeded(AqlQuery<T> aqlQuery) {
        if (aqlQuery.getAqlElements().isEmpty()) {
            return;
        }
        if (AqlDomainEnum.items.equals(aqlQuery.getDomain())) {
            decorateItemsSearch(aqlQuery);
        } else if (AqlDomainEnum.properties.equals(aqlQuery.getDomain())) {
            decoratePropertiesSearch(aqlQuery);
        }
    }

    <T extends AqlRowResult> void decorateItemsSearch(AqlQuery<T> aqlQuery) {
        if (!repoEqualsFilterFound(aqlQuery)) {
            AqlField itemRepo = AqlFieldResolver.resolve(AqlPhysicalFieldEnum.itemRepo);
            AqlVariable specialRepo = AqlFieldResolver.resolve(getRepoName(), AqlVariableTypeEnum.string);
            TableLink nodesTable = AqlTableGraph.tablesLinksMap.get(SqlTableEnum.nodes);
            SimpleCriterion criteria = new SimpleCriterion(Lists.newArrayList(AqlDomainEnum.properties), itemRepo,
                    nodesTable.getTable(), AqlComparatorEnum.notEquals.signature, specialRepo, nodesTable.getTable(),
                    false);

            List<AqlQueryElement> aqlElements = aqlQuery.getAqlElements();
            aqlElements.add(0, AqlAdapter.open);
            aqlElements.add(AqlAdapter.close);
            aqlElements.add(AqlAdapter.and);
            aqlElements.add(criteria);
        }
    }

    <T extends AqlRowResult> void decoratePropertiesSearch(AqlQuery<T> aqlQuery) {
        // (query)AND(trash.time not exists)
        AqlField key = AqlFieldResolver.resolve(AqlPhysicalFieldEnum.propertyKey);
        AqlVariable trashTime = AqlFieldResolver.resolve(getPropertyName(), AqlVariableTypeEnum.string);
        TableLink propsTable = AqlTableGraph.tablesLinksMap.get(SqlTableEnum.node_props);
        SimplePropertyCriterion criteria = new SimplePropertyCriterion(Lists.newArrayList(AqlDomainEnum.properties),
                key, propsTable.getTable(), AqlComparatorEnum.notEquals.signature, trashTime, propsTable.getTable(),
                false);

        List<AqlQueryElement> aqlElements = aqlQuery.getAqlElements();
        aqlElements.add(0, AqlAdapter.open);
        aqlElements.add(AqlAdapter.close);
        aqlElements.add(AqlAdapter.and);
        aqlElements.add(criteria);
    }

    private <T extends AqlRowResult> boolean repoEqualsFilterFound(AqlQuery<T> aqlQuery) {
        List<AqlQueryElement> aqlElements = aqlQuery.getAqlElements();
        for (AqlQueryElement aqlQueryElement : aqlElements) {
            if (aqlQueryElement instanceof SimpleCriterion) {
                SimpleCriterion criteria = (SimpleCriterion) aqlQueryElement;
                if (criteriaIsRepoEquals(criteria)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean criteriaIsRepoEquals(SimpleCriterion criteria) {
        AqlField field = (AqlField) criteria.getVariable1();
        boolean fieldIsRepo = itemRepo == field.getFieldEnum();
        boolean comparatorIsEquals = AqlComparatorEnum.equals
                .equals(AqlComparatorEnum.value(criteria.getComparatorName()));
        return fieldIsRepo && comparatorIsEquals;
    }

    abstract String getRepoName();

    abstract String getPropertyName();
}
