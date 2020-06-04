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

package org.artifactory.ui.rest.service.artifacts.search.packagesearch.strategy;

import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.model.AqlComparatorEnum;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlPhysicalFieldEnum;
import org.jfrog.client.util.PathUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Dan Feldman
 */
public class AqlUIPropertySearchStrategy implements AqlUISearchStrategy {

    protected String key;
    protected List<String> values;
    List<AqlDomainEnum> subdomainPath;
    protected AqlComparatorEnum comparator;


    public AqlUIPropertySearchStrategy(String key, AqlDomainEnum[] subdomainPath) {
        this.key = key;
        this.subdomainPath = Stream.of(subdomainPath).collect(Collectors.toList());
    }

    @Override
    public AqlUIPropertySearchStrategy values(List<String> values) {
        this.values = values;
        return this;
    }

    @Override
    public AqlUIPropertySearchStrategy values(String... values) {
        this.values = Stream.of(values).collect(Collectors.toList());
        return this;
    }

    @Override
    public AqlUIPropertySearchStrategy comparator(AqlComparatorEnum comparator) {
        this.comparator = comparator;
        return this;
    }

    @Override
    public AqlPhysicalFieldEnum getSearchField() {
        return AqlPhysicalFieldEnum.propertyKey;
    }

    @Override
    public String getSearchKey() {
        return key;
    }

    @Override
    public AqlBase.OrClause toQuery() {
        AqlBase.OrClause query = AqlApiItem.or();
        values.forEach(value ->
                query.append(new AqlBase.PropertyCriteriaClause(key, AqlComparatorEnum.matches, value, subdomainPath)));
        return query;
    }

    @Override
    public boolean includePropsInResult() {
        return true;
    }

    @Override
    public String toString() {
        return "AqlUIPropertySearchStrategy{" +
                "property key: " + key +
                ", values: " + PathUtils.collectionToDelimitedString(values) +
                ", comparator: " + comparator +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AqlUIPropertySearchStrategy)) {
            return false;
        }

        AqlUIPropertySearchStrategy aqlUIPropertySearchStrategy = (AqlUIPropertySearchStrategy) o;

        if (key != null ? !key.equals(aqlUIPropertySearchStrategy.key) : aqlUIPropertySearchStrategy.key != null) {
            return false;
        }

        if (values != null ? !values.equals(aqlUIPropertySearchStrategy.values) : aqlUIPropertySearchStrategy.values != null) {
            return false;
        }

        if (subdomainPath != null ? !subdomainPath.equals(aqlUIPropertySearchStrategy.subdomainPath) :
                aqlUIPropertySearchStrategy.subdomainPath != null) {
            return false;
        }

        return comparator != null ? comparator.equals(aqlUIPropertySearchStrategy.comparator) :
                aqlUIPropertySearchStrategy.comparator == null;
    }
}