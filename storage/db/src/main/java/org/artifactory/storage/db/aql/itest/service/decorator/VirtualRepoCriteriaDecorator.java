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

package org.artifactory.storage.db.aql.itest.service.decorator;

import com.google.common.collect.Lists;
import org.artifactory.aql.model.*;
import org.artifactory.aql.result.rows.AqlRowResult;
import org.artifactory.storage.db.aql.sql.builder.query.aql.*;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Yinon Avraham
 */
public class VirtualRepoCriteriaDecorator implements DecorationStrategy {

    @Override
    public <T extends AqlRowResult> void decorate(AqlQuery<T> aqlQuery, AqlQueryDecoratorContext decoratorContext) {
        List<AqlQueryElement> aqlQueryElements = aqlQuery.getAqlElements();
        int index = 0;
        while ((index = findNextFieldCriteria(index, AqlPhysicalFieldEnum.itemRepo, aqlQueryElements)) >= 0) {
            index = handleRepoCriteria(index, aqlQueryElements, decoratorContext);
        }
    }

    private int handleRepoCriteria(int index, List<AqlQueryElement> aqlQueryElements, AqlQueryDecoratorContext decoratorContext) {
        Criterion criterion = (Criterion) aqlQueryElements.get(index);
        AqlComparatorEnum comparator = AqlComparatorEnum.value(criterion.getComparatorName());
        if (comparator != null) {
            switch (comparator) {
                case equals:
                case matches:
                case greater:
                case greaterEquals:
                case less:
                case lessEquals:
                    return handlePositive(index, aqlQueryElements, criterion, comparator, decoratorContext);
                case notEquals:
                case notMatches:
                    return handleNegative(index, aqlQueryElements, criterion, comparator, decoratorContext);
            }
        }
        throw new IllegalArgumentException("Unexpected repo comparator: " + criterion.getComparatorName());
    }

    private int handlePositive(int index, List<AqlQueryElement> aqlQueryElements, Criterion criterion,
            AqlComparatorEnum comparator, AqlQueryDecoratorContext decoratorContext) {
        String value = (String) ((AqlValue) criterion.getVariable2()).toObject();
        Predicate<String> repoKeyPredicate = createRepoKeyPredicate(comparator, value);
        boolean includeOriginalCriteria = comparator != AqlComparatorEnum.equals;
        List<String> repoKeys = findVirtualReposAndResolveRepoKeys(repoKeyPredicate, decoratorContext);
        return adjustQueryElements(index, aqlQueryElements, criterion, AqlComparatorEnum.equals, repoKeys,
                AqlAdapter.or, includeOriginalCriteria);
    }

    private int handleNegative(int index, List<AqlQueryElement> aqlQueryElements, Criterion criterion,
            AqlComparatorEnum comparator, AqlQueryDecoratorContext decoratorContext) {
        String value = (String) ((AqlValue) criterion.getVariable2()).toObject();
        Predicate<String> repoKeyPredicate = createRepoKeyPredicate(comparator, value);
        boolean includeOriginalCriteria = comparator != AqlComparatorEnum.notEquals;
        List<String> repoKeys = findVirtualReposAndResolveRepoKeys(repoKeyPredicate, decoratorContext);
        return adjustQueryElements(index, aqlQueryElements, criterion, AqlComparatorEnum.notEquals, repoKeys,
                AqlAdapter.and, includeOriginalCriteria);
    }

    private List<String> findVirtualReposAndResolveRepoKeys(Predicate<String> repoKeyPredicate,
            AqlQueryDecoratorContext decoratorContext) {
        AqlRepoProvider repoProvider = decoratorContext.getRepoProvider();
        return repoProvider.getVirtualRepoKeys().stream()
                .filter(repoKeyPredicate)
                .flatMap(virtualRepoKey -> repoProvider.getVirtualResolvedLocalAndCacheRepoKeys(virtualRepoKey).stream())
                .distinct()
                .collect(Collectors.toList());
    }

    private Predicate<String> createRepoKeyPredicate(AqlComparatorEnum comparator, String value) {
        switch (comparator) {
            case equals:
            case notEquals:
                return key -> key.equals(value);
            case matches:
            case notMatches:
                AqlMatchesPattern matchesPattern = AqlMatchesPattern.compile(value);
                return matchesPattern::matches;
            case greater:
                return key -> key.compareTo(value) > 0;
            case greaterEquals:
                return key -> key.compareTo(value) >= 0;
            case less:
                return key -> key.compareTo(value) < 0;
            case lessEquals:
                return key -> key.compareTo(value) <= 0;
        }
        throw new IllegalArgumentException("Unexpected comparator: " + comparator);
    }

    private int adjustQueryElements(int index, List<AqlQueryElement> aqlQueryElements, Criterion criterion,
            AqlComparatorEnum comparator, List<String> repoKeys, OperatorQueryElement operator, boolean includeOriginal) {
        if (repoKeys.size() > 0) {
            List<AqlQueryElement> newCompoundCriteria = newCompoundCriteria(criterion, comparator, repoKeys, operator, includeOriginal);
            aqlQueryElements.remove(index);
            aqlQueryElements.addAll(index, newCompoundCriteria);
            return index + newCompoundCriteria.size();
        }
        return index + 1;
    }

    private List<AqlQueryElement> newCompoundCriteria(Criterion criterion, AqlComparatorEnum comparator,
            List<String> repoKeys, OperatorQueryElement operator, boolean includeOriginal) {
        List<AqlQueryElement> newCompoundCriteria = Lists.newArrayList();
        boolean first = true;
        if (includeOriginal) {
            newCompoundCriteria.add(criterion);
            first = false;
        }
        for (String repoKey : repoKeys) {
            if (!first) {
                newCompoundCriteria.add(operator);
            } else {
                first = false;
            }
            newCompoundCriteria.add(newCriteriaFrom(criterion, comparator, repoKey));
        }
        if (newCompoundCriteria.size() > 1) {
            newCompoundCriteria.add(0, AqlAdapter.open);
            newCompoundCriteria.add(AqlAdapter.close);
        }
        return newCompoundCriteria;
    }

    private Criterion newCriteriaFrom(Criterion criterion, AqlComparatorEnum comparatorEnum, String repoKey) {
        AqlValue newValue = new AqlValue(AqlVariableTypeEnum.string, repoKey);
        return new SimpleCriterion(criterion.getSubDomains(), criterion.getVariable1(), criterion.getTable1(),
                comparatorEnum.signature, newValue, criterion.getTable2(), false);
    }
}
